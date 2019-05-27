package io.kroom.api.user

import io.kroom.api.deezer.{DataDeezerGenre, RepoDeezer}
import com.github.t3hnar.bcrypt._
import io.kroom.api.Authorization.{PermissionGroup, Privacy}
import io.kroom.api.util.TokenGenerator

import scala.util.{Failure, Success, Try}

case class DataUserPrivacy(
                            email: String,
                            location: String,
                            friends: String,
                            musicalPreferencesGenre: String,
                          )

case class DataUser(
                     id: Int,
                     userName: String,
                     email: String,
                     emailIsconfirmed: Boolean,
                     passHash: Option[String],
                     location: Option[String],
                     token: Option[String],
                     tokenOutOfDate: Option[String],
                     privacy: DataUserPrivacy
                   )

class RepoUser(val dbh: DBUser, private val repoDeezer: RepoDeezer) {

  def getById(id: Int): Try[DataUser] = {
    dbh.getById(id)
  }

  def getByName(name: String): Try[DataUser] = {
    dbh.getByName(name)
  }

  def getByEmail(email: String): Try[DataUser] = {
    dbh.getByEmail(email)
  }

  def getFriends(userId: Int): Try[List[DataUser]] = {
    dbh.getFriends(userId)
  }

  def getMsicalPreferences(userId: Int): Try[List[DataDeezerGenre]] = {
    dbh.getMusicalPreferences(userId)
  }

  // Mutation

  def signUp(name: String, email: String, pass: String): Try[DataUser] = {
    dbh.addUserWithPass(name, email, pass.bcrypt) recover { case e => return Failure(e) }

    val user = dbh.getByName(name) match {
      case Failure(e) => return Failure(e)
      case Success(s) => s
    }

    // TODO token cookie ?
    dbh.updateToken(user.id, Some(TokenGenerator.generateToken()), Some("")) recover { case e => return Failure(e) }

    getById(user.id)
  }

  def signIn(userName: String, pass: String): Try[DataUser] = {
    // TODO time token
    // TODO token cookie ?
    val user = dbh.getByName(userName) match {
      case Failure(e) => return Failure(e)
      case Success(s) => s
    }

    val passUser = user.passHash.getOrElse(
      return Failure(new IllegalStateException("user has empty password"))
    )
    if (!pass.isBcryptedSafe(passUser).getOrElse(false)) {
      return Failure(new IllegalStateException("password invalid"))
    }

    dbh.updateToken(user.id, Some(TokenGenerator.generateToken()), Some("")) recover { case e => return Failure(e) }

    getById(user.id)
  }

  def getTokenPermGroup(token: String): Try[(DataUser, Set[PermissionGroup.Value])] = {
    val user = dbh.getByToken(token) match {
      case Failure(e) => return Failure(e)
      case Success(s) => s
    }
    val perms = dbh.getPermGroup(user.id) match {
      case Failure(e) => return Failure(e)
      case Success(s) => s
    }
    Success((user, perms))
  }

  def getUserPermGroup(userId: Int): Try[Set[PermissionGroup.Value]] = {
    dbh.getPermGroup(userId)
  }

  def addFriend(userId: Int, friendId: Int): Try[DataUser] = {
    dbh.addFriend(userId, friendId)
  }

  def delFriend(userId: Int, friendId: Int): Try[DataUser] = {
    dbh.delFriend(userId, friendId)
  }

  def addMusicalPreference(userId: Int, genreId: Int): Try[DataUser] = {
    // may need fetch Genre in DB
    repoDeezer.getGenreById(genreId)
    dbh.addMusicalPreference(userId, genreId)
  }

  def delMusicalPreference(userId: Int, genreId: Int): Try[DataUser] = {
    dbh.delMusicalPreference(userId, genreId)
  }

  def updatePrivacy(
                     userId: Int,
                     email: Privacy.Value,
                     location: Privacy.Value,
                     friends: Privacy.Value,
                     musicalPreferencesGenre: Privacy.Value,
                   ): Try[DataUser] = {
    dbh.updatePrivacy(userId, DataUserPrivacy(
      Privacy.privacyToString(email),
      Privacy.privacyToString(location),
      Privacy.privacyToString(friends),
      Privacy.privacyToString(musicalPreferencesGenre),
    ))
  }

}
