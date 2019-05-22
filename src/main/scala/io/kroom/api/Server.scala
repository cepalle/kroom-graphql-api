package io.kroom.api

import sangria.ast.Document
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, HandledException, QueryAnalysisError}
import sangria.parser.{QueryParser, SyntaxError}
import sangria.parser.DeliveryScheme.Try
import sangria.marshalling.circe._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe._
import io.circe.parser._
import io.circe.optics.JsonPath.{root => r}

import scala.util.control.NonFatal
import scala.util.{Failure, Success}
import deezer.SchemaDeezer
import io.kroom.api.trackvoteevent.SchemaTrackVoteEvent
import root.{DBRoot, RepoRoot, SchemaRoot}
import sangria.slowlog.SlowLog
import slick.jdbc.H2Profile.api._

object Server extends App with CorsSupport {
  implicit val system: ActorSystem = ActorSystem("sangria-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher
  import GraphQLRequestUnmarshaller._

  private val db = Database.forConfig("h2mem1")

  def executeGraphQL(query: Document, operationName: Option[String], variables: Json, tracing: Boolean) =
    complete(Executor.execute(
      schema = SchemaRoot.KroomSchema,
      queryAst = query,
      userContext = new SecureContext(None, new RepoRoot(new DBRoot(db))),
      variables = if (variables.isNull) Json.obj() else variables,
      operationName = operationName,
      middleware = if (tracing) SlowLog.apolloTracing :: Nil else Nil,
      deferredResolver = DeferredResolver.fetchers(
        SchemaDeezer.TrackFetcherId,
        SchemaDeezer.ArtistFetcherId,
        SchemaDeezer.AlbumFetcherId,
        SchemaDeezer.GenreFetcherId,
        SchemaTrackVoteEvent.TrackVoteEventFetcherId
      )
    )
      .map(OK → _)
      .recover {
        case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
        case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
      })

  def formatError(error: Throwable): Json = error match {
    case syntaxError: SyntaxError ⇒
      Json.obj("errors" → Json.arr(
        Json.obj(
          "message" → Json.fromString(syntaxError.getMessage),
          "locations" → Json.arr(Json.obj(
            "line" → Json.fromBigInt(syntaxError.originalError.position.line),
            "column" → Json.fromBigInt(syntaxError.originalError.position.column))))))
    case NonFatal(e) ⇒
      formatError(e.getMessage)
    case e ⇒
      throw e
  }

  def formatError(message: String): Json =
    Json.obj("errors" → Json.arr(Json.obj("message" → Json.fromString(message))))

  val route: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing ⇒
      path("graphql") {
        get {
          explicitlyAccepts(`text/html`) {
            getFromResource("assets/playground.html")
          } ~
            parameters('query, 'operationName.?, 'variables.?) { (query, operationName, variables) ⇒
              QueryParser.parse(query) match {
                case Success(ast) ⇒
                  variables.map(parse) match {
                    case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                    case Some(Right(json)) ⇒ executeGraphQL(ast, operationName, json, tracing.isDefined)
                    case None ⇒ executeGraphQL(ast, operationName, Json.obj(), tracing.isDefined)
                  }
                case Failure(error) ⇒ complete(BadRequest, formatError(error))
              }
            }
        } ~
          post {
            parameters('query.?, 'operationName.?, 'variables.?) { (queryParam, operationNameParam, variablesParam) ⇒
              entity(as[Json]) { body ⇒
                val query = queryParam orElse r.query.string.getOption(body)
                val operationName = operationNameParam orElse r.operationName.string.getOption(body)
                val variablesStr = variablesParam orElse r.variables.string.getOption(body)

                query.map(QueryParser.parse(_)) match {
                  case Some(Success(ast)) ⇒
                    variablesStr.map(parse) match {
                      case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                      case Some(Right(json)) ⇒ executeGraphQL(ast, operationName, json, tracing.isDefined)
                      case None ⇒ executeGraphQL(ast, operationName, r.variables.json.getOption(body) getOrElse Json.obj(), tracing.isDefined)
                    }
                  case Some(Failure(error)) ⇒ complete(BadRequest, formatError(error))
                  case None ⇒ complete(BadRequest, formatError("No query to execute"))
                }
              } ~
                entity(as[Document]) { document ⇒
                  variablesParam.map(parse) match {
                    case Some(Left(error)) ⇒ complete(BadRequest, formatError(error))
                    case Some(Right(json)) ⇒ executeGraphQL(document, operationNameParam, json, tracing.isDefined)
                    case None ⇒ executeGraphQL(document, operationNameParam, Json.obj(), tracing.isDefined)
                  }
                }
            }
          }
      }
    } ~
      (get & pathEndOrSingleSlash) {
        redirect("/graphql", PermanentRedirect)
      }

  DBRoot.init(db)
  Http().bindAndHandle(corsHandler(route), "0.0.0.0", sys.props.get("http.port").fold(8080)(_.toInt))
}
