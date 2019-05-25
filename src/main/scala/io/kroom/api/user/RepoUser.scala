package io.kroom.api.user

import io.kroom.api.deezer.{DataDeezerGenre, RepoDeezer}
import com.github.t3hnar.bcrypt._
import io.kroom.api.Authorization.{PermissionGroup, Privacy}
import io.kroom.api.ExceptionCustom.{UserAuthenticationException, UserRegistrationException}
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

  def getFriends(userId: Int): Try[List[DataUser]] = {
    dbh.getFriends(userId)
  }

  def getMsicalPreferences(userId: Int): Try[List[DataDeezerGenre]] = {
    dbh.getMusicalPreferences(userId)
  }

  // Mutation

  def signUp(name: String, email: String, pass: String): Try[DataUser] = {
    // TODO verif send email
    // TODO verif userName, email, pass
    // TODO token cookie ?
    dbh.addUserWithPass(name, email, pass.bcrypt) match {
      case Failure(e) => return Failure(UserRegistrationException(e.getMessage))
    }

    val user = dbh.getByName(name) match {
      case Failure(_) => return Failure(new IllegalStateException("RepoUser.signUp getByName failed"))
      case Success(u) => u
    }

    dbh.updateToken(user.id, Some(TokenGenerator.generateToken()), Some("")) match {
      case Failure(_) => return Failure(new IllegalStateException("RepoUser.signUp updateToken failed"))
    }

    getById(user.id) match {
      case Failure(_) => Failure(new IllegalStateException("RepoUser.signUp getById failed"))
    }
  }

  def signIn(userName: String, pass: String): Try[DataUser] = {
    // TODO token cookie ?
    val token = TokenGenerator.generateToken()

    val user = dbh.getByName(userName).getOrElse(throw UserAuthenticationException("userName or password invalid"))
    val passUser = user.passHash.getOrElse(throw UserAuthenticationException("userName or password invalid"))
    if (!pass.isBcryptedSafe(passUser).getOrElse(false)) {
      throw UserAuthenticationException("userName or password invalid")
    }

    // TODO time token
    dbh.updateToken(user.id, Some(token), Some(""))

    getById(user.id).getOrElse(throw new IllegalStateException("signIn user.id not found"))
  }

  def getTokenPermGroup(token: String): Try[(DataUser, Set[PermissionGroup.Value])] = {
    val user = dbh.getByToken(token).getOrElse(return None)
    val perms = dbh.getPermGroup(user.id)
    Some((user, perms))
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
    repoDeezer.getGenreById(genreId) // get Genre in DB
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
