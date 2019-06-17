package io.kroom.api

import sangria.ast.{Document, OperationType}
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.parser.{QueryParser, SyntaxError}
import sangria.parser.DeliveryScheme.Try
import sangria.marshalling.circe._
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
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
import io.kroom.api.util.FormatError
import root.{DBRoot, RepoRoot, SchemaRoot}
import sangria.slowlog.SlowLog
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

object Server extends App with CorsSupport {
  implicit val system: ActorSystem = ActorSystem("sangria-server")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  import system.dispatcher
  import GraphQLRequestUnmarshaller._

  private val db = Database.forConfig("h2mem1")
  private val emailActor: ActorRef = system.actorOf(Props(new EmailSenderActor()))
  private lazy val subActorHandler: ActorRef = system.actorOf(Props(new SubscriptionActor(ctxInit)))
  private lazy val ctxInit: SecureContext = new SecureContext(None, new RepoRoot(new DBRoot(db), subActorHandler, emailActor))
  private val wbSubHandler = new WebSocketSubscription(subActorHandler)

  def executeGraphQL(query: Document, operationName: Option[String], variables: Json, tracing: Boolean, token: Option[String]): StandardRoute = {
    query.operationType(operationName) match {
      case Some(OperationType.Subscription) ⇒
        complete(ToResponseMarshallable(BadRequest → Json.fromString("Subscriptions not supported via HTTP. Use WebSockets")))
      case _ =>
        complete(
          Executor.execute(
            schema = SchemaRoot.KroomSchema,
            queryAst = query,
            userContext = new SecureContext(token, ctxInit.repo),
            variables = if (variables.isNull) Json.obj() else variables,
            operationName = operationName,
            middleware = if (tracing) SlowLog.apolloTracing :: Nil else Nil,
            deferredResolver = DeferredResolver.fetchers(
              SchemaDeezer.TrackFetcherId,
              SchemaDeezer.ArtistFetcherId,
              SchemaDeezer.AlbumFetcherId,
              SchemaDeezer.GenreFetcherId
            ),
            exceptionHandler = ExceptionCustom.exceptionHandler
          )
            .map(OK → _)
            .recover {
              case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
              case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
            }
        )
    }
  }

  val route: Route =
    optionalHeaderValueByName("X-Apollo-Tracing") { tracing ⇒
      optionalHeaderValueByName("Kroom-token-id") { kroomTokenId ⇒
        path("graphql") {
          handleWebSocketMessages(
            wbSubHandler.newSocketFlow()
          ) ~ get {
            explicitlyAccepts(`text/html`) {
              getFromResource("assets/playground.html")
            } ~
              parameters('query, 'operationName.?, 'variables.?) { (query, operationName, variables) ⇒
                QueryParser.parse(query) match {
                  case Success(ast) ⇒
                    variables.map(parse) match {
                      case Some(Left(error)) ⇒ complete(BadRequest, FormatError.formatError(error))
                      case Some(Right(json)) ⇒ executeGraphQL(ast, operationName, json, tracing.isDefined, kroomTokenId)
                      case None ⇒ executeGraphQL(ast, operationName, Json.obj(), tracing.isDefined, kroomTokenId)
                    }
                  case Failure(error) ⇒ complete(BadRequest, FormatError.formatError(error))
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
                        case Some(Left(error)) ⇒ complete(BadRequest, FormatError.formatError(error))
                        case Some(Right(json)) ⇒ executeGraphQL(ast, operationName, json, tracing.isDefined, kroomTokenId)
                        case None ⇒ executeGraphQL(ast, operationName, r.variables.json.getOption(body) getOrElse Json.obj(), tracing.isDefined, kroomTokenId)
                      }
                    case Some(Failure(error)) ⇒ complete(BadRequest, FormatError.formatError(error))
                    case None ⇒ complete(BadRequest, FormatError.formatError("No query to execute"))
                  }
                } ~
                  entity(as[Document]) { document ⇒
                    variablesParam.map(parse) match {
                      case Some(Left(error)) ⇒ complete(BadRequest, FormatError.formatError(error))
                      case Some(Right(json)) ⇒ executeGraphQL(document, operationNameParam, json, tracing.isDefined, kroomTokenId)
                      case None ⇒ executeGraphQL(document, operationNameParam, Json.obj(), tracing.isDefined, kroomTokenId)
                    }
                  }
              }
            }
        }
      }
    } ~
      path("email-confirmation") {
        get {
          parameters('token) { token ⇒ {
            ctxInit.repo.user.emailConfirmation(token) match {
              case Failure(_) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Email confirmation Failed"))
              case Success(_) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Email confirmation Success"))
            }
          }
          }
        }
      } ~
      path("update-pass-confirmation") {
        get {
          parameters('token) { token ⇒ {
            ctxInit.repo.user.newPasswordEmailConfirmation(token) match {
              case Failure(_) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Update password Failed"))
              case Success(_) => complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Update password Success"))
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
