package io.kroom.api.user

import io.kroom.api.deezer.{DataDeezerGenre, RepoDeezer}
import com.github.t3hnar.bcrypt._
import io.kroom.api.Authorization.{PermissionGroup, Privacy}
import io.kroom.api.ExceptionCustom.{MultipleException, SimpleException, UserAuthenticationException, UserRegistrationException}
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
    dbh.getById(id) match {
      case Failure(_) => Failure(SimpleException("userId not found"))
    }
  }

  def getByName(name: String): Try[DataUser] = {
    dbh.getByName(name) match {
      case Failure(_) => Failure(SimpleException("userName not found"))
    }
  }

  def getFriends(userId: Int): Try[List[DataUser]] = {
    dbh.getFriends(userId) match {
      case Failure(_) => Failure(SimpleException("userId not found"))
    }
  }

  def getMsicalPreferences(userId: Int): Try[List[DataDeezerGenre]] = {
    dbh.getMusicalPreferences(userId) match {
      case Failure(_) => Failure(SimpleException("userId not found"))
    }
  }

  // Mutation

  def signUp(name: String, email: String, pass: String): Try[DataUser] = {
    // TODO verif send email
    // TODO verif userName, email, pass
    // TODO token cookie ?
    val userByName = dbh.getByName(name) match {
      case Success(_) => Failure(UserRegistrationException("userName already exist"))
      case Failure(_) => Success(Unit)
    }
    val userByemail = dbh.getByEmail(email) match {
      case Success(_) => Failure(UserRegistrationException("email already exist"))
      case Failure(_) => Success(Unit)
    }

    val lCheck = List(userByName, userByemail) collect { case Failure(e) => e }

    if (lCheck.nonEmpty) {
      return Failure(MultipleException(lCheck))
    }

    dbh.addUserWithPass(name, email, pass.bcrypt) match {
      case Failure(e) => return Failure(e)
    }

    val user = dbh.getByName(name) match {
      case Failure(e) => return Failure(e)
      case Success(u) => u
    }

    dbh.updateToken(user.id, Some(TokenGenerator.generateToken()), Some("")) match {
      case Failure(e) => return Failure(e)
    }

    getById(user.id)
  }

  def signIn(userName: String, pass: String): Try[DataUser] = {
    val user = dbh.getByName(userName) match {
      case Failure(_) => return Failure(UserAuthenticationException("userName or password invalid"))
      case Success(s) => s
    }

    val passUser = user.passHash.getOrElse(
      return Failure(UserAuthenticationException("userName or password invalid"))
    )
    if (!pass.isBcryptedSafe(passUser).getOrElse(false)) {
      return Failure(UserAuthenticationException("userName or password invalid"))
    }

    // TODO time token
    // TODO token cookie ?
    dbh.updateToken(user.id, Some(TokenGenerator.generateToken()), Some("")) match {
      case Failure(e) => return Failure(e)
    }

    getById(user.id)
  }

  def getTokenPermGroup(token: String): Try[(DataUser, Set[PermissionGroup.Value])] = {
    val user = dbh.getByToken(token) match {
      case Failure(_) => return Failure(SimpleException("token not found"))
      case Success(s) => s
    }
    val perms = dbh.getPermGroup(user.id) match {
      case Failure(e) => return Failure(e)
    }
    Success((user, perms))
  }

  def getUserPermGroup(userId: Int): Try[Set[PermissionGroup.Value]] = {
    dbh.getPermGroup(userId) match {
      case Failure(_) => Failure(SimpleException("userId not found"))
    }
  }

  def addFriend(userId: Int, friendId: Int): Try[DataUser] = {
    dbh.addFriend(userId, friendId) match {
      case Failure(_) => Failure(SimpleException("userId or friendId not found"))
    }
  }

  def delFriend(userId: Int, friendId: Int): Try[DataUser] = {
    dbh.delFriend(userId, friendId) match {
      case Failure(_) => Failure(SimpleException("Delete Friend failed"))
    }
  }

  def addMusicalPreference(userId: Int, genreId: Int): Try[DataUser] = {
    // get Genre in DB
    repoDeezer.getGenreById(genreId) match {
      case Failure(_) => Failure(SimpleException("genreId not found"))
    }
    dbh.addMusicalPreference(userId, genreId) match {
      case Failure(_) => Failure(SimpleException("userId not found"))
    }
  }

  def delMusicalPreference(userId: Int, genreId: Int): Try[DataUser] = {
    dbh.delMusicalPreference(userId, genreId) match {
      case Failure(_) => Failure(SimpleException("Delete MusicalPreference failed"))
    }
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
    )) match {
      case Failure(_) => Failure(SimpleException("userId not found"))
    }
  }

}
