package io.kroom.api.user

import io.kroom.api.deezer.DataDeezerGenre
import sangria.execution.UserFacingError

case class DataUser(
                     id: Int,
                     userName: String,
                     email: String,
                     passHash: String,
                     location: Option[String],
                   )

class RepoUser(val dbh: DBUser) {

  def getById(id: Int): Option[DataUser] = {
    dbh.getById(id)
  }

  def getFriends(userId: Int): List[DataUser] = {
    dbh.getFriends(userId)
  }

  def getmMsicalPreferences(userId: Int): List[DataDeezerGenre] = {
    dbh.getmMsicalPreferences(userId)
  }

  // Mutation

  def signUp(userName: String, email: String, pass: String): Option[DataUser] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def signIn(userName: Option[String], email: Option[String], pass: String): Option[DataUser] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def addFriend(userId: Int, friendId: Int): Option[DataUser] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def delFriend(userId: Int, friendId: Int): Option[DataUser] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def addMusicalPreference(userId: Int, genreId: Int): Option[DataUser] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def delMusicalPreference(userId: Int, genreId: Int): Option[DataUser] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

}
