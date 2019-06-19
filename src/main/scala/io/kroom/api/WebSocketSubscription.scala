package io.kroom.api

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import com.typesafe.scalalogging.StrictLogging
import io.circe.Json
import io.circe.syntax._
import io.circe.generic.auto._
import io.circe.parser
import io.kroom.api.Server.system
import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.SchemaRoot
import io.kroom.api.util.{FormatError, TokenGenerator}
import sangria.ast.OperationType
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{Executor, PreparedQuery}
import sangria.parser.QueryParser

import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success}

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

object SubQueryEnum extends Enumeration {
  val TrackVoteEventById, PlayListEditorById = Value

  def queryFindType(q: String): SubQueryEnum.Value = {
    if (q.indexOf("TrackVoteEventById") > -1) {
      return SubQueryEnum.TrackVoteEventById
    }
    if (q.indexOf("PlayListEditorById") > -1) {
      return SubQueryEnum.PlayListEditorById
    }
    SubQueryEnum.TrackVoteEventById
  }
}

sealed trait WSEventCS

case class WSEventCSUserJoined(actorId: String, actorRef: ActorRef) extends WSEventCS

case class WSEventCSUserQuit(actorId: String) extends WSEventCS

case class WSEventCSUpdateQuery(subQuery: SubQueryEnum.Value, subQueryParamsId: Int) extends WSEventCS

case class WSEventCSMessage(actorId: String, content: String) extends WSEventCS


sealed trait subActorEvent

case class subActorEventPreparedQuery(actorId: String, subQ: SubscriptionActor.subQueryData) extends subActorEvent

case class subActorEventKeepAlive(actorId: String) extends subActorEvent

sealed trait WSEventSC

case class WSEventSCOpMsgType(`type`: String) extends WSEventSC

case class WSEventSCOpMsgString(str: String) extends WSEventSC

// ---

case class OpMsgType(`type`: String)

case class KroomTokenId(`Kroom-token-id`: Option[String])

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

case class OpMsgCSTerminate(`type`: String)

// ---

object SubscriptionActor {

  case class subQueryData(
                           apolloQueryId: String,
                           preparedQuery: PreparedQuery[SecureContext, Unit, Json],
                           subQuery: SubQueryEnum.Value,
                           subQueryParamsId: Int,
                         )

  case class clientState(
                          actorRef: ActorRef,
                          token: Option[String],
                          subs: List[subQueryData]
                        )

}

class SubscriptionActor(ctxInit: SecureContext) extends Actor with StrictLogging {

  import system.dispatcher
  import SubscriptionActor._

  private var clientsState: mutable.Map[String, clientState] = collection.mutable.Map[String, clientState]()

  override def receive: Receive = {
    case subActorEventKeepAlive(actorId) =>
      if (clientsState.contains(actorId)) {
        clientsState(actorId).actorRef ! WSEventSCOpMsgType(ApolloProtocol.GQL_CONNECTION_KEEP_ALIVE)
        Future {
          Thread.sleep(1000)
          self ! subActorEventKeepAlive(actorId)
        }
      }
    case subActorEventPreparedQuery(actorId, sQuData) =>
      logger.debug("subActorEventPreparedQuery")

      val clState = clientsState(actorId)
      clientsState(actorId) = clState.copy(subs = clState.subs :+ sQuData)
    case WSEventCSUserJoined(actorId, actor) =>
      logger.debug("WSEventUserJoined")

      clientsState += (actorId -> clientState(actor, None, List()))
    case WSEventCSUserQuit(actorId) =>
      logger.debug("WSEventUserQuit")

      clientsState -= actorId
    case WSEventCSUpdateQuery(subQuery, subQueryParamsId) =>
      logger.debug("WSEventUpdateQuery", subQuery, subQueryParamsId)

      clientsState.foreach(c => {
        c._2.subs.foreach(sbQu => {
          if (sbQu.subQuery == subQuery && sbQu.subQueryParamsId == subQueryParamsId) {
            import sangria.marshalling.circe._

            sbQu.preparedQuery.execute().map(res => {
              val vrap = Json.obj(
                ("id", Json.fromString(sbQu.apolloQueryId)),
                ("type", Json.fromString(ApolloProtocol.GQL_DATA)),
                ("payload", res)
              )
              c._2.actorRef ! WSEventSCOpMsgString(vrap.toString())
            })
          }
        })
      })
    case WSEventCSMessage(actorId, content) =>
      logger.debug("WSEventCSMessage", content)

      parser.decode[OpMsgType](content).toTry.map(tpe => {
        tpe.`type` match {
          case ApolloProtocol.GQL_CONNECTION_INIT =>
            parser.decode[OpMsgCSInit](content).toTry.map(init => {
              clientsState(actorId) = clientsState(actorId).copy(token = init.payload.`Kroom-token-id`)
              clientsState(actorId).actorRef ! WSEventSCOpMsgType(ApolloProtocol.GQL_CONNECTION_ACK)
              clientsState(actorId).actorRef ! WSEventSCOpMsgType(ApolloProtocol.GQL_CONNECTION_KEEP_ALIVE)
              self ! subActorEventKeepAlive(actorId)
            })
          case ApolloProtocol.GQL_START =>
            val clState = clientsState(actorId)
            parser.decode[OpMsgCSStart](content).toTry.map(start => {
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
                        self ! subActorEventPreparedQuery(actorId, subQueryData(
                          start.id,
                          query,
                          SubQueryEnum.queryFindType(start.payload.query),
                          start.payload.variables.id.getOrElse(-1)
                        ))

                        query.execute().map(res => {
                          val vrap = Json.obj(
                            ("id", Json.fromString(start.id)),
                            ("type", Json.fromString(ApolloProtocol.GQL_DATA)),
                            ("payload", res)
                          )
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
  extends Directives with StrictLogging {

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
            logger.debug("Received: ", str)
            WSEventCSMessage(actorClientId, str)
        })

        val wsToMsg = builder.add(Flow[WSEventSC].map {
          case op: WSEventSCOpMsgType =>
            logger.debug("Send: ", op.asJson.toString())
            TextMessage(op.asJson.toString())
          case op: WSEventSCOpMsgString =>
            logger.debug("Send: ", op.str)
            TextMessage(op.str)
        })

        materialization ~> merge ~> subActorSink
        msgToWs ~> merge

        actorClientSource ~> wsToMsg

        FlowShape(msgToWs.in, wsToMsg.out)
    })

}
