package io.kroom.api.util


object Privacy extends Enumeration {
  val public, amis, `private` = Value

  def IntToPrivacy(nb: Int): Privacy.Value = {
    nb match {
      case 1 => public
      case 2 => amis
      case _ => `private`
    }
  }

  def PrivacyToInt(e: Privacy.Value): Int = {
    e match {
      case Privacy.public => 1
      case Privacy.amis => 2
      case _ => 3
    }
  }
}
