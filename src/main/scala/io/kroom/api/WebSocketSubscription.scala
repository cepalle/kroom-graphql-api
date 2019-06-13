package io.kroom.api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import io.kroom.api.util.TokenGenerator
import slick.jdbc.H2Profile

import scala.util.{Success, Try}

// ---

object ApolloProtocol {

  case class OperationMessage(
                               payload: Option[String], // payload?: any;
                               id: Option[String], // id?: string;
                               `type`: String, // type: string;
                             )

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

// ---

sealed trait WSEvent

case class WSEventUserJoined(actorId: String, actorRef: ActorRef) extends WSEvent

case class WSEventUserQuit(actorId: String) extends WSEvent


case class WSERandom() extends WSEvent

case class Connected(outgoing: ActorRef) extends WSEvent

case class SubscriptionAccepted() extends WSEvent

case class Subscribe(query: String, operation: Option[String]) extends WSEvent

case class QueryResult(json: String) extends WSEvent

// ---

// Connection init connect with token or error
// TODO client state

class SubscriptionActor(val db: H2Profile.backend.Database) extends Actor {

  private val clients = collection.mutable.LinkedHashMap[String, Any]()
  private var tests: List[ActorRef] = List[ActorRef]()

  /*
    client sub
    send to client if sub update
  */
  override def receive: Receive = {
    // sub
    case WSEventUserJoined(actorId, actor) =>
      println("ici555")
      tests = tests :+ actor
      actor ! WSERandom()
    case a =>
      println("ici5", a)
      tests.foreach(a => a ! WSERandom())
  }

}

// ---

class WebSocketSubscription(val db: H2Profile.backend.Database)
                           (implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer)
  extends Directives {

  private val subActorHandler = actorSystem.actorOf(Props(new SubscriptionActor(db)))
  private val actorClientSource = Source.actorRef[WSEvent](100, OverflowStrategy.fail)

  def newSocketFlow(): Flow[Message, Message, ActorRef] =
    Flow.fromGraph(GraphDSL.create(actorClientSource) { implicit builder =>
      actorClientSource =>
        import GraphDSL.Implicits._
        import ApolloProtocol._

        val actorClientId = TokenGenerator.generateToken()

        val materialization = builder.materializedValue.map(actorClientRef => WSEventUserJoined(actorClientId, actorClientRef))
        val merge = builder.add(Merge[WSEvent](2))
        val subActorSink = Sink.actorRef[WSEvent](subActorHandler, WSEventUserQuit(actorClientId))

        val messagesToWSEvent = builder.add(Flow[Message].collect {
          // serialization
          case TextMessage.Strict(direction) =>
            println("ici3", direction)
            WSERandom()
          case _ =>
            println("ici4")
            WSERandom()
        })

        val WSEventsToMessages = builder.add(Flow[WSEvent].map {

          // serialization
          case a =>
            import io.circe.syntax._
            import io.circe.generic.auto._
            val res = OperationMessage(None, None, GQL_CONNECTION_ACK).asJson.toString()
            println("ici66", a, res)
            TextMessage(res)
        })

        materialization ~> merge ~> subActorSink
        messagesToWSEvent ~> merge

        actorClientSource ~> WSEventsToMessages

        FlowShape(messagesToWSEvent.in, WSEventsToMessages.out)
    })

}
