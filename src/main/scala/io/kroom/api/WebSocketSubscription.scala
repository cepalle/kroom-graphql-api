package io.kroom.api

import akka.NotUsed
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.Message
import akka.http.scaladsl.server.Directives
import akka.stream.{ActorMaterializer, FlowShape}
import akka.stream.scaladsl.{Flow, GraphDSL}

trait WSEvent

case class WSEventUserJoined()


class SubscriptionActor extends Actor {

  override def receive: Receive = {
    case _ => None

  }

}


class WebSocketSubscription(implicit val actorSystem: ActorSystem, implicit val actorMaterializer: ActorMaterializer)
  extends Directives {

  private val subActor = actorSystem.actorOf(Props(new SubscriptionActor()))

  def socketFlow(token: Option[String]): Flow[Message, Message, NotUsed] =
    Flow.fromGraph(GraphDSL.create() { implicit builder =>
      import GraphDSL.Implicits._


      FlowShape(messagesToGameEventsFlow.in, gameEventsToMessagesFlow.out)
    })

}
