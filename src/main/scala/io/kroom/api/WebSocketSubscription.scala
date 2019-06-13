package io.kroom.api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import io.kroom.api.util.TokenGenerator
import sangria.execution.{Executor, PreparedQuery}
import slick.jdbc.H2Profile

import scala.concurrent.Future
import scala.util.{Success, Try}

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

sealed trait WSEvent

case class WSEventUserJoined(actorId: String, actorRef: ActorRef) extends WSEvent

case class WSEventUserQuit(actorId: String) extends WSEvent

case class WSEventUpdateQuery(subQuery: String, subQueryParamsId: Int) extends WSEvent

case class OperationMessage(
                             payload: Option[String], // payload?: any;
                             id: Option[String], // id?: string;
                             `type`: String, // type: string;
                           ) extends WSEvent

class SubscriptionActor(val db: H2Profile.backend.Database) extends Actor {

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
    case WSEventUserJoined(actorId, actor) =>
      println("SubscriptionActor WSEventUserJoined")
      clientsState = clientsState :+ clientState(actorId, actor, None, List())
    case WSEventUserQuit(actorId) =>
      println("SubscriptionActor WSEventUserQuit")
      clientsState = clientsState.filter(_.actorId != actorId)
    case WSEventUpdateQuery(subQuery, subQueryParamsId) =>
      // Send update
    case a =>
      println("SubscriptionActor op")
      clientsState.foreach(c => c.actorRef ! OperationMessage(None, None, "TODO"))
  }

}

class WebSocketSubscription(val db: H2Profile.backend.Database)
                           (implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer)
  extends Directives {

  private val subActorHandler = actorSystem.actorOf(Props(new SubscriptionActor(db)))
  private val actorClientSource = Source.actorRef[WSEvent](100, OverflowStrategy.fail)

  def newSocketFlow(): Flow[Message, Message, ActorRef] =
    Flow.fromGraph(GraphDSL.create(actorClientSource) { implicit builder =>
      actorClientSource =>
        import GraphDSL.Implicits._

        val actorClientId = TokenGenerator.generateToken()

        val materialization = builder.materializedValue.map(actorClientRef => WSEventUserJoined(actorClientId, actorClientRef))
        val merge = builder.add(Merge[WSEvent](2))
        val subActorSink = Sink.actorRef[WSEvent](subActorHandler, WSEventUserQuit(actorClientId))

        val msgToWs = builder.add(Flow[Message].collect {
          case TextMessage.Strict(str) =>
            println("Received: ", str)
            OperationMessage(None, None, "TODO")
        })

        val wsToMsg = builder.add(Flow[WSEvent].map {
          case op: OperationMessage =>
            import io.circe.syntax._
            import io.circe.generic.auto._
            val opString = op.asJson.toString()
            println("Send: ", op)
            TextMessage(opString)
        })

        materialization ~> merge ~> subActorSink
        msgToWs ~> merge

        actorClientSource ~> wsToMsg

        FlowShape(msgToWs.in, wsToMsg.out)
    })

}
