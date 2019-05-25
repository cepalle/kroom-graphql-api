package io.kroom.api

import sangria.execution.{ExceptionHandler, HandledException}
import sangria.marshalling.ResultMarshaller


object ExceptionCustom {

  case class MultipleException(e: Throwable*) extends Exception("MultipleException")

  case class UserRegistrationException(message: String) extends Exception(message)

  case class UserAuthenticationException(message: String) extends Exception(message)

  case class AuthorisationException(message: String) extends Exception(message)

  case class SimpleException(message: String) extends Exception(message)

  val exceptionHandler = ExceptionHandler {
    case (_: ResultMarshaller, e: MultipleException) ⇒ HandledException(e.getMessage) // TODO
    case (_: ResultMarshaller, e: UserRegistrationException) ⇒ HandledException(e.message)
    case (_: ResultMarshaller, e: UserAuthenticationException) ⇒ HandledException(e.message)
    case (_: ResultMarshaller, e: AuthorisationException) ⇒ HandledException(e.message)
    case (_: ResultMarshaller, e: SimpleException) ⇒ HandledException(e.message)
  }
}