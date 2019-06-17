package io.kroom.api

import akka.actor.Actor
import courier.Mailer

case class Email(to: String, content: String)


class EmailSender extends Actor {
  private val mailer = Mailer("host", 587)
    .auth(true)
    .as("you@gmail.com", "p@$$w3rd")()

  override def receive: Receive = {
    case email: Email =>
      val to = email.to
      val content = email.content

      /*
      mailer(Envelope.from("you" `@` "gmail.com")
        .to("mom" `@` "gmail.com")
        .cc("dad" `@` "gmail.com")
        .subject("miss you")
        .content(Text("hi mom"))).onSuccess {
          case _ => println("message delivered")
        }
      */
  }
}
