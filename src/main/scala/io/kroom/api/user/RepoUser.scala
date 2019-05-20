package io.kroom.api.user

import sangria.execution.UserFacingError

case class DataUser(
                     id: Int,
                     userName: String,
                     email: String,
                     passHash: String,
                     friendsId: List[Int],
                     musicalPreferencesGenreId: List[Int],
                     location: Option[String]
                   )

class RepoUser(val dbh: DBUser) {

  def getById(id: Int): Option[DataUser] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
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
