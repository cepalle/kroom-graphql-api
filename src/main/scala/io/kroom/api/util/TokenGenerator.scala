package io.kroom.api.util

import java.security.SecureRandom

object TokenGenerator {

  private val TOKEN_LENGTH = 45
  private val TOKEN_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_"
  private val TOKEN_CHARS_LEN = TOKEN_CHARS.length()
  private val secureRandom = new SecureRandom()

  def generateToken(): String = {
    def generateTokenAccumulator(accumulator: String, number: Int): String =
      if (number == 0) accumulator
      else generateTokenAccumulator(accumulator + TOKEN_CHARS(secureRandom.nextInt(TOKEN_CHARS_LEN)).toString, number - 1)

    generateTokenAccumulator("", TOKEN_LENGTH)
  }

}
