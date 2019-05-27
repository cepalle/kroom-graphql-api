package io.kroom.api

import sangria.execution.{ExceptionHandler, HandledException}
import sangria.marshalling.ResultMarshaller


object ExceptionCustom {

  case class AuthorisationException(message: String) extends Exception(message)

  val exceptionHandler = ExceptionHandler {
    case (_: ResultMarshaller, e: AuthorisationException) ⇒ HandledException(e.message)
  }
}
