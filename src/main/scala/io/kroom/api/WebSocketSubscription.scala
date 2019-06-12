package io.kroom.api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape, OverflowStrategy}
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Sink, Source}

sealed trait WSEvent

case class WSEventUserJoined(token: Option[String], actorRef: ActorRef) extends WSEvent

case class WSEventUserQuit() extends WSEvent

case class WSERandom() extends WSEvent

// ---

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

class WebSocketSubscription(val subActor: ActorRef,val secureContext: SecureContext)(implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer)
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
