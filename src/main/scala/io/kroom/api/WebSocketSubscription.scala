package io.kroom.api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, PoisonPill, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import slick.jdbc.H2Profile
import io.circe.generic.auto._
import io.circe.{Json, parser}
import io.circe.syntax._

import scala.util.{Success, Try}


// ---

/*
export default class MessageTypes {
  public static GQL_CONNECTION_INIT = 'connection_init'; // Client -> Server
  public static GQL_CONNECTION_ACK = 'connection_ack'; // Server -> Client
  public static GQL_CONNECTION_ERROR = 'connection_error'; // Server -> Client

  // NOTE: The keep alive message type does not follow the standard due to connection optimizations
  public static GQL_CONNECTION_KEEP_ALIVE = 'ka'; // Server -> Client

  public static GQL_CONNECTION_TERMINATE = 'connection_terminate'; // Client -> Server
  public static GQL_START = 'start'; // Client -> Server
  public static GQL_DATA = 'data'; // Server -> Client
  public static GQL_ERROR = 'error'; // Server -> Client
  public static GQL_COMPLETE = 'complete'; // Server -> Client
  public static GQL_STOP = 'stop'; // Client -> Server
}*/

object ApolloProtocol {

  /*
  export interface OperationMessage {
    payload?: any;
    id?: string;
    type: string;
  }
  */

  // Client -> Server
  val GQL_CONNECTION_INIT = "GQL_CONNECTION_INIT" // payload: Object
  val GQL_START = "GQL_START" // id + payload: {query, variables, operationName}
  val GQL_STOP = "GQL_STOP" // id
  val GQL_CONNECTION_TERMINATE = "GQL_CONNECTION_TERMINATE"
  // Server -> Client
  val GQL_CONNECTION_ACK = "GQL_CONNECTION_ACK"
  val GQL_CONNECTION_ERROR = "GQL_CONNECTION_ERROR"
  val GQL_DATA = "GQL_DATA" // id + payload: {data, errors}
  val GQL_ERROR = "GQL_ERROR" // id + payload: Error
  val GQL_COMPLETE = "GQL_COMPLETE" // id
  val GQL_CONNECTION_KEEP_ALIVE = "GQL_CONNECTION_KEEP_ALIVE"

}

// ---

sealed trait WSEvent

case class WSEventUserJoined(token: Option[String], actorRef: ActorRef) extends WSEvent

case class WSEventUserQuit() extends WSEvent

case class WSERandom() extends WSEvent

case class Connected(outgoing: ActorRef) extends WSEvent

case class SubscriptionAccepted() extends WSEvent

case class Subscribe(query: String, operation: Option[String]) extends WSEvent

case class QueryResult(json: Json) extends WSEvent

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
    case WSEventUserJoined(token, actor) =>
      println("ici555")
      tests = tests :+ actor
      actor ! WSERandom()
    case a =>
      println("ici5", a)
      tests.foreach(a => a ! WSERandom())
  }

}

// ---

/*
  Apollo close connexion because bad request ?
*/
class WebSocketSubscription(val db: H2Profile.backend.Database)
                           (implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer)
  extends Directives {

  private val subActorHandler = actorSystem.actorOf(Props(new SubscriptionActor(db)))
  private val subActorSource = Source.actorRef[WSEvent](100, OverflowStrategy.fail)

  def socketFlow(token: Option[String]): Flow[Message, Message, ActorRef] =
    Flow.fromGraph(GraphDSL.create(subActorSource) { implicit builder =>
      subActorSourceCp =>
        import GraphDSL.Implicits._

        println("ici2")
        val materialization = builder.materializedValue.map(subActorRef => WSEventUserJoined(token, subActorRef))
        val merge = builder.add(Merge[WSEvent](2))
        val subActorSink = Sink.actorRef[WSEvent](subActorHandler, WSEventUserQuit())

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
            println("ici66", a)
            TextMessage("{\"type\":\"start\"}")

        })

        materialization ~> merge ~> subActorSink
        messagesToWSEvent ~> merge

        subActorSourceCp ~> WSEventsToMessages

        FlowShape(messagesToWSEvent.in, WSEventsToMessages.out)
    })

}
