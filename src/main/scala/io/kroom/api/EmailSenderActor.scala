package io.kroom.api

import akka.actor.Actor
import courier.{Envelope, Mailer, Session, Text}
import javax.mail.internet.InternetAddress
import io.kroom.api.Server.system

case class Email(to: String, subject: String, content: String)


class EmailSenderActor extends Actor {

  import system.dispatcher

  private val mailer = Mailer("localhost", 8025)()

  override def receive: Receive = {
    case Email(to, subject, content) =>
      mailer(
        Envelope.from(new InternetAddress("admin@kroom.io"))
          .to(new InternetAddress(to))
          .subject(subject)
          .content(Text(content))
      ).onComplete {
        case _ => println("EmailSender: message delivered")
      }
  }
}
