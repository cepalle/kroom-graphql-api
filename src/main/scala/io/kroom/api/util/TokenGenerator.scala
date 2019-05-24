package io.kroom.api.util

import java.security.{SecureRandom}

object TokenGenerator {

  val TOKEN_LENGTH = 45
  val TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_"
  val TOKEN_CHARS_LEN = TOKEN_CHARS.length()
  val secureRandom = new SecureRandom()

  private def generateToken(tokenLength: Int): String = {
    def generateTokenAccumulator(accumulator: String, number: Int): String =
      if (number == 0) accumulator
      else generateTokenAccumulator(accumulator + TOKEN_CHARS(secureRandom.nextInt(TOKEN_CHARS_LEN)).toString, number - 1)

    generateTokenAccumulator("", tokenLength)
  }

}
