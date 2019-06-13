package io.kroom.api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import io.kroom.api.root.{DBRoot, RepoRoot}
import io.kroom.api.util.TokenGenerator
import sangria.execution.{Executor, PreparedQuery}
import slick.jdbc.H2Profile

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
                         `Kroom-token-id`: Int
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

  import io.circe.generic.auto._
  import io.circe.parser

  private var clientsState: List[clientState] = List[clientState]()

  case class subQueryData(
                           apolloQueryId: String,
                           preparedQuery: PreparedQuery[SecureContext, Nothing, Nothing], // TODO
                           subQuery: String,
                           subQueryParamsId: Int,
                         )

  case class clientState(
                          actorId: String,
                          actorRef: ActorRef,
                          token: Option[String],
                          subs: List[subQueryData]
                        )

  override def receive: Receive = {
    case WSEventCSUserJoined(actorId, actor) =>
      println("SubscriptionActor WSEventUserJoined")
      clientsState = clientsState :+ clientState(actorId, actor, None, List())
    case WSEventCSUserQuit(actorId) =>
      println("SubscriptionActor WSEventUserQuit")
      clientsState = clientsState.filter(_.actorId != actorId)
    case WSEventCSUpdateQuery(subQuery, subQueryParamsId) =>
      println("SubscriptionActor WSEventUpdateQuery", subQuery, subQueryParamsId)
      clientsState.foreach(c => {
        c.subs.foreach(sbQu => {
          if (sbQu.subQuery == subQuery && sbQu.subQueryParamsId == subQueryParamsId) {
            c.actorRef ! sbQu.preparedQuery.execute()
          }
        })
      })
    case WSEventCSMessage(actorId, content) =>
      println("SubscriptionActor WSEventCSMessage")
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
