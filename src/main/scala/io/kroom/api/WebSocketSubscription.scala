package io.kroom.api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}
import slick.jdbc.H2Profile


// ---

object ApolloProtocol {

  /*
  export interface OperationMessage {
    payload?: any;
    id?: string;
    type: string;
  }
  */

  val GQL_CONNECTION_INIT = "GQL_CONNECTION_INIT" // payload: Object
  val GQL_START = "GQL_START" // id + payload: {query, variables, operationName}
  val GQL_STOP = "GQL_STOP" // id
  val GQL_CONNECTION_TERMINATE = "GQL_CONNECTION_TERMINATE"
  // -->
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

// ---

// Connection init connect with token or error
// TODO client state

class SubscriptionActor extends Actor {

  private val clients = collection.mutable.LinkedHashMap[String, Any]()

  /*
    client sub
    send to client if sub update
  */
  override def receive: Receive = {
    // sub
    case _ => None

  }

}

// ---

class WebSocketSubscription(val subActor: ActorRef, val db: H2Profile.backend.Database)(implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer)
  extends Directives {

  private val subActorSource = Source.actorRef[WSEvent](100, OverflowStrategy.fail)

  def socketFlow(token: Option[String]): Flow[Message, Message, ActorRef] =
    Flow.fromGraph(GraphDSL.create(subActorSource) { implicit builder =>
      subActorSource =>
        import GraphDSL.Implicits._

        val materialization = builder.materializedValue.map(subActorRef => WSEventUserJoined(token, subActorRef))
        val merge = builder.add(Merge[WSEvent](2))
        val subActorSink = Sink.actorRef[WSEvent](subActor, WSEventUserQuit())

        val messagesToWSEvent = builder.add(Flow[Message].collect {
          // serialization
          case TextMessage.Strict(direction) => WSERandom()
        })

        val WSEventsToMessages = builder.add(Flow[WSEvent].map {
          // serialization
          case WSERandom() => {
            TextMessage("")
          }
        })

        materialization ~> merge ~> subActorSink
        messagesToWSEvent ~> merge

        subActorSource ~> WSEventsToMessages

        FlowShape(messagesToWSEvent.in, WSEventsToMessages.out)
    })

}
