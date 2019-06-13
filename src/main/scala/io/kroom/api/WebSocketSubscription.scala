package io.kroom.api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import akka.http.scaladsl.model.StatusCodes._
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser
import io.kroom.api.Server.system
import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.{DBRoot, RepoRoot, SchemaRoot}
import io.kroom.api.util.{FormatError, TokenGenerator}
import sangria.ast.OperationType
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, PreparedQuery, QueryAnalysisError}
import sangria.marshalling.InputUnmarshaller
import sangria.parser.QueryParser
import sangria.slowlog.SlowLog
import slick.jdbc.H2Profile

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object ApolloProtocol {
  val GQL_CONNECTION_INIT = "connection_init" // Client -> Server
  val GQL_CONNECTION_ACK = "connection_ack" // Server -> Client
  val GQL_CONNECTION_ERROR = "connection_error" // Server -> Client

  // NOTE: The keep alive message type does not follow the standard due to connection optimizations
  val GQL_CONNECTION_KEEP_ALIVE = "ka" // Server -> Client

  val GQL_CONNECTION_TERMINATE = "connection_terminate" // Client -> Server
  val GQL_START = "start" // Client -> Server
  val GQL_DATA = "data" // Server -> Client
  val GQL_ERROR = "error" // Server -> Client
  val GQL_COMPLETE = "complete" // Server -> Client
  val GQL_STOP = "stop" // Client -> Server
}

sealed trait WSEventCS

case class WSEventCSUserJoined(actorId: String, actorRef: ActorRef) extends WSEventCS

case class WSEventCSUserQuit(actorId: String) extends WSEventCS

case class WSEventCSUpdateQuery(subQuery: String, subQueryParamsId: Int) extends WSEventCS

case class WSEventCSMessage(actorId: String, content: String) extends WSEventCS


sealed trait WSEventSC

case class WSEventSCOpMsgType(
                               `type`: String,
                             ) extends WSEventSC

case class WSEventSCOpMsgString(
                                 str: String
                               ) extends WSEventSC

// ---

case class OpMsgType(
                      `type`: String,
                    )

case class KroomTokenId(
                         `Kroom-token-id`: Option[String]
                       )

case class Var(id: Option[Int])

case class Query(
                  query: String,
                  variables: Var,
                  operationName: Option[String]
                )

case class OpMsgCSInit(
                        payload: KroomTokenId,
                        `type`: String,
                      )

case class OpMsgCSStart(
                         id: String,
                         payload: Query,
                         `type`: String,
                       )

case class OpMsgCSStop(
                        id: String,
                        `type`: String,
                      )

case class OpMsgCSTerminate(
                             `type`: String,
                           )

// ---

class SubscriptionActor(ctxInit: SecureContext) extends Actor {

  import system.dispatcher

  private var clientsState: mutable.Map[String, clientState] = collection.mutable.Map[String, clientState]()

  case class subQueryData(
                           apolloQueryId: String,
                           preparedQuery: PreparedQuery[SecureContext, Unit, Json],
                           subQuery: String,
                           subQueryParamsId: Int,
                         )

  case class clientState(
                          actorRef: ActorRef,
                          token: Option[String],
                          subs: List[subQueryData]
                        )

  override def receive: Receive = {
    case WSEventCSUserJoined(actorId, actor) =>
      println("SubscriptionActor WSEventUserJoined")
      clientsState += (actorId -> clientState(actor, None, List()))
    case WSEventCSUserQuit(actorId) =>
      println("SubscriptionActor WSEventUserQuit")
      clientsState -= actorId
    case WSEventCSUpdateQuery(subQuery, subQueryParamsId) =>
      println("SubscriptionActor WSEventUpdateQuery", subQuery, subQueryParamsId)
      clientsState.foreach(c => {
        c._2.subs.foreach(sbQu => {
          if (sbQu.subQuery == subQuery && sbQu.subQueryParamsId == subQueryParamsId) {
            sbQu.preparedQuery.execute().map(res => {


              //c._2.actorRef ! res
            })
          }
        })
      })
    case WSEventCSMessage(actorId, content) =>
      println("SubscriptionActor WSEventCSMessage")
      parser.decode[OpMsgType](content).toTry.map(tpe => {
        println(s" - ${tpe.`type`}")
        tpe.`type` match {
          case ApolloProtocol.GQL_CONNECTION_INIT =>
            parser.decode[OpMsgCSInit](content).toTry.map(init => {
              println(" -- ", init)
              clientsState(actorId) = clientsState(actorId).copy(token = init.payload.`Kroom-token-id`)
              clientsState(actorId).actorRef ! WSEventSCOpMsgType(ApolloProtocol.GQL_CONNECTION_ACK)
            })
          case ApolloProtocol.GQL_START =>
            val clState = clientsState(actorId)
            parser.decode[OpMsgCSStart](content).toTry.map(start => {
              println(" -- ", start)
              QueryParser.parse(start.payload.query) match {
                case Success(ast) =>
                  ast.operationType(start.payload.operationName) match {
                    case Some(OperationType.Subscription) =>

                      import sangria.marshalling.circe._

                      Executor.prepare(
                        schema = SchemaRoot.KroomSchema,
                        queryAst = ast,
                        userContext = new SecureContext(clState.token, ctxInit.repo),
                        variables = start.payload.variables.asJson,
                        operationName = start.payload.operationName,
                        deferredResolver = DeferredResolver.fetchers(
                          SchemaDeezer.TrackFetcherId,
                          SchemaDeezer.ArtistFetcherId,
                          SchemaDeezer.AlbumFetcherId,
                          SchemaDeezer.GenreFetcherId
                        ),
                        exceptionHandler = ExceptionCustom.exceptionHandler
                      ).map(query => {
                        println("query", query)
                        val sQuData = subQueryData(
                          start.id,
                          query,
                          "TODO", // TODO
                          start.payload.variables.id.getOrElse(-1)
                        )
                        clientsState(actorId) = clState.copy(subs = clState.subs :+ sQuData)

                        query.execute().map(res => {
                          val vrap = Json.obj(
                            ("id", Json.fromString(start.id)),
                            ("type", Json.fromString(ApolloProtocol.GQL_DATA)),
                            ("payload", res)
                          )
                          println("res", vrap)
                          clState.actorRef ! WSEventSCOpMsgString(vrap.toString())
                        })
                      }).recover {
                        case e =>
                          val vrap = Json.obj(
                            ("id", Json.fromString(start.id)),
                            ("type", Json.fromString(ApolloProtocol.GQL_ERROR)),
                            ("payload", FormatError.formatError(e))
                          )
                          clState.actorRef ! WSEventSCOpMsgString(vrap.toString())
                      }
                    case x =>
                      val vrap = Json.obj(
                        ("id", Json.fromString(start.id)),
                        ("type", Json.fromString(ApolloProtocol.GQL_ERROR)),
                        ("payload", FormatError.formatError(s"OperationType: $x not supported with WebSockets. Use HTTP POST"))
                      )
                      clState.actorRef ! WSEventSCOpMsgString(vrap.toString())
                  }

                case Failure(e) =>
                  val vrap = Json.obj(
                    ("id", Json.fromString(start.id)),
                    ("type", Json.fromString(ApolloProtocol.GQL_ERROR)),
                    ("payload", FormatError.formatError(e))
                  )
                  clState.actorRef ! WSEventSCOpMsgString(vrap.toString())
              }
            })
          case ApolloProtocol.GQL_STOP =>
            parser.decode[OpMsgCSStop](content).toTry.map(stop => {
              println(" -- ", stop)
              val state = clientsState(actorId)
              clientsState(actorId) = state.copy(subs = state.subs.filter(e => e.apolloQueryId != stop.id))
            })
          case ApolloProtocol.GQL_CONNECTION_TERMINATE =>
            clientsState -= actorId
        }
      }
      )
  }

}

class WebSocketSubscription(val subActorHandler: ActorRef)
                           (implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer)
  extends Directives {

  private val actorClientSource = Source.actorRef[WSEventSC](100, OverflowStrategy.fail)

  def newSocketFlow(): Flow[Message, Message, ActorRef] =
    Flow.fromGraph(GraphDSL.create(actorClientSource) { implicit builder =>
      actorClientSource =>
        import GraphDSL.Implicits._

        val actorClientId = TokenGenerator.generateToken()

        val materialization = builder.materializedValue.map(actorClientRef => WSEventCSUserJoined(actorClientId, actorClientRef))
        val merge = builder.add(Merge[WSEventCS](2))
        val subActorSink = Sink.actorRef[WSEventCS](subActorHandler, WSEventCSUserQuit(actorClientId))

        val msgToWs = builder.add(Flow[Message].collect {
          case TextMessage.Strict(str) =>
            println("Received: ", str)
            WSEventCSMessage(actorClientId, str)
        })

        val wsToMsg = builder.add(Flow[WSEventSC].map {
          case op: WSEventSCOpMsgType =>
            println("Send: ", op.asJson.toString())
            TextMessage(op.asJson.toString())
          case op: WSEventSCOpMsgString =>
            println("Send: ", op.str)
            TextMessage(op.str)
        })

        materialization ~> merge ~> subActorSink
        msgToWs ~> merge

        actorClientSource ~> wsToMsg

        FlowShape(msgToWs.in, wsToMsg.out)
    })

}
