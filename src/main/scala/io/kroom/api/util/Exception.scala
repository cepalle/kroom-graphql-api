package io.kroom.api.util

case class AuthenticationException(message: String) extends Exception(message)

case class AuthorisationException(message: String) extends Exception(message)
