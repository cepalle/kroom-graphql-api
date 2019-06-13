package io.kroom.api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import io.circe.Json
import io.kroom.api.Server.system
import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.{DBRoot, RepoRoot, SchemaRoot}
import io.kroom.api.util.TokenGenerator
import sangria.ast.OperationType
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{Executor, PreparedQuery}
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

case class OpMsgTODO(
                      `type`: String,
                    )

// ---

case class OpMsgType(
                      `type`: String,
                    )

case class KroomTokenId(
                         `Kroom-token-id`: String
                       )

case class Id(id: Int)

case class Query(
                  query: String,
                  variables: Option[Id],
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
  import io.circe.generic.auto._
  import io.circe.parser

  private var clientsState: mutable.Map[String, clientState] = collection.mutable.Map[String, clientState]()

  case class subQueryData(
                           apolloQueryId: String,
                           preparedQuery: Future[PreparedQuery[SecureContext, Any, Json]], // TODO
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
            sbQu.preparedQuery.map(p => c._2.actorRef ! p.execute())
          }
        })
      })
    case WSEventCSMessage(actorId, content) =>
      println("SubscriptionActor WSEventCSMessage")
      parser.decode[OpMsgType](content).toTry.map(tpe =>
        tpe.`type` match {
          case ApolloProtocol.GQL_CONNECTION_INIT =>
            parser.decode[OpMsgCSInit](content).toTry.map(init => {
              clientsState(actorId) = clientsState(actorId).copy(token = Some(init.payload.`Kroom-token-id`))
            })
          case ApolloProtocol.GQL_START =>
            parser.decode[OpMsgCSStart](content).toTry.map(start => {
              QueryParser.parse(start.payload.query) match {
                case Success(ast) =>
                  ast.operationType(start.payload.operationName) match {
                    case Some(OperationType.Subscription) =>
                      val state = clientsState(actorId)
                      // TODO
                      /*
                      val tmp = subQueryData(
                        start.id,
                        Executor.prepare(
                          schema = SchemaRoot.KroomSchema,
                          queryAst = ast,
                          userContext = new SecureContext(state.token, ctxInit.repo),
                          variables = if (start.payload.variables) Json.obj() else variables,
                          operationName = start.payload.operationName,
                          deferredResolver = DeferredResolver.fetchers(
                            SchemaDeezer.TrackFetcherId,
                            SchemaDeezer.ArtistFetcherId,
                            SchemaDeezer.AlbumFetcherId,
                            SchemaDeezer.GenreFetcherId
                          ),
                          exceptionHandler = ExceptionCustom.exceptionHandler
                        ),
                        "TODO",
                        42
                      )
                      */
                    case x =>
                      println(s"OperationType: $x not supported with WebSockets. Use HTTP POST")
                  }

                case Failure(e) =>
                  println(e.getMessage)
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
          case op: OpMsgTODO =>
            /*
            import io.circe.syntax._
            import io.circe.generic.auto._
            val opString = op.asJson.toString()
            */
            println("Send: ", op)
            TextMessage("TODO")
        })

        materialization ~> merge ~> subActorSink
        msgToWs ~> merge

        actorClientSource ~> wsToMsg

        FlowShape(msgToWs.in, wsToMsg.out)
    })

}
