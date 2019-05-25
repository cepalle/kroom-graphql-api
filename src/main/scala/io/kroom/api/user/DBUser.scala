package io.kroom.api.user

import io.circe.syntax._
import io.circe.parser
import io.circe.generic.auto._
import io.kroom.api.Authorization
import io.kroom.api.Authorization.PermissionGroup
import io.kroom.api.deezer.{DBDeezer, DataDeezerGenre}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

class DBUser(private val db: H2Profile.backend.Database) {

  import DBUser._

  def getById(id: Int): Try[DataUser] = {
    val query = tabUser.filter(_.id === id).result.head

    Await.ready(db.run(query), Duration.Inf).value
      .map(_.map(tabToObjUser))
      .getOrElse(Failure(new IllegalStateException("DBUser.getById user.id not found")))
  }

  def getByToken(token: String): Try[DataUser] = {
    // TODO check time out of date
    val query = tabUser.filter(_.tokenOAuth === token).result.head

    Await.ready(db.run(query), Duration.Inf).value
      .map(_.map(tabToObjUser))
      .getOrElse(Failure(new IllegalStateException("DBUser.getByToken user.token not found")))
  }

  def getByName(name: String): Try[DataUser] = {
    val query = tabUser.filter(_.name === name).result.head

    Await.ready(db.run(query), Duration.Inf).value
      .map(_.map(tabToObjUser))
      .getOrElse(Failure(new IllegalStateException("DBUser.getByName user.name not found")))
  }

  def getByEmail(email: String): Try[DataUser] = {
    val query = tabUser.filter(_.email === email).result.head

    Await.ready(db.run(query), Duration.Inf).value
      .map(_.map(tabToObjUser))
      .getOrElse(Failure(new IllegalStateException("DBUser.getByEmail user.email not found")))
  }

  def getFriends(userId: Int): Try[List[DataUser]] = {
    val query = for {
      ((u, _), f) <- tabUser join joinFriend on
        (_.id === _.idUser) join tabUser on (_._2.idFriend === _.id)
      if u.id === userId
    } yield f

    Await.ready(db.run(query.result), Duration.Inf).value
      .map(_.map(_.map(tabToObjUser)))
      .getOrElse(Failure(new IllegalStateException("DBUser.getFriends user.id not found")))
      .map(_.toList)
  }

  def getMusicalPreferences(userId: Int): Try[List[DataDeezerGenre]] = {
    val query = for {
      ((u, _), mp) <- tabUser join joinMusicalPreferences on
        (_.id === _.idUser) join DBDeezer.tabDeezerGenre on (_._2.idDeezerGenre === _.id)
      if u.id === userId
    } yield mp

    Await.ready(db.run(query.result), Duration.Inf).value
      .map(_.map(_.map(DBDeezer.tabToObjDeezerGenre)))
      .getOrElse(Failure(new IllegalStateException("DBUser.getMusicalPreferences user.id not found")))
      .map(_.toList)
  }

  def getPermGroup(userId: Int): Try[Set[Authorization.PermissionGroup.Value]] = {
    val query = joinPermGroup.filter(_.idUser === userId).result
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .map(_.map(_.map(c => Authorization.StringToPermissionGroup(c._2))))
      .getOrElse(Failure(new IllegalStateException("DBUser.getPermGroup user.id not found")))
      .map(_.toSet)
  }

  // Mutation

  def addUserWithPass(name: String, email: String, passHash: String): Try[DataUser] = {
    val queryInsertUser = tabUser.map(c => (c.name, c.email, c.passHash)) += (name, email, Some(passHash))
    Await.ready(db.run(queryInsertUser), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    val user = getByEmail(email) match {
      case Failure(e) => return Failure(e)
      case Success(s) => s
    }

    val queryInsertPerm = joinPermGroup += (user.id, Authorization.PermissionGroupToString(Authorization.PermissionGroup.user))
    Await.ready(db.run(queryInsertPerm), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(user.id)
  }

  def addFriend(userId: Int, friendId: Int): Try[DataUser] = {
    val query = DBIO.seq(
      joinFriend.map(e => (e.idUser, e.idFriend)) += (userId, friendId),
      joinFriend.map(e => (e.idUser, e.idFriend)) += (friendId, userId)
    )
    Await.ready(db.run(query), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

  def delFriend(userId: Int, friendId: Int): Try[DataUser] = {
    val query = DBIO.seq(
      joinFriend.filter(e => e.idFriend === friendId && e.idUser === userId).delete,
      joinFriend.filter(e => e.idFriend === userId && e.idUser === friendId).delete,
    )
    Await.ready(db.run(query), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

  def addMusicalPreference(userId: Int, genreId: Int): Try[DataUser] = {
    val query = joinMusicalPreferences.map(e => (e.idUser, e.idDeezerGenre)) += (userId, genreId)

    Await.ready(db.run(query), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

  def delMusicalPreference(userId: Int, genreId: Int): Try[DataUser] = {
    val query = joinMusicalPreferences
      .filter(e => e.idDeezerGenre === genreId && e.idUser === userId)
      .delete

    Await.ready(db.run(query), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

  def confirmEmail(userId: Int): Try[DataUser] = {
    val query = tabUser.filter(e => e.id === userId)
      .map(e => e.emailIsconfirmed)
      .update(true)

    Await.ready(db.run(query), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

  def updateLocation(userId: Int, location: String): Try[DataUser] = {
    val query = tabUser.filter(e => e.id === userId)
      .map(e => e.location)
      .update(Some(location))

    Await.ready(db.run(query), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

  def updateToken(userId: Int, token: Option[String], tokenOutOfDate: Option[String]): Try[DataUser] = {
    val query = tabUser.filter(e => e.id === userId)
      .map(e => (e.tokenOAuth, e.tokenOAuthOutOfDate))
      .update((token, tokenOutOfDate))

    Await.ready(db.run(query), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

  def updatePrivacy(userId: Int, pr: DataUserPrivacy): Try[DataUser] = {
    val query = tabUser.filter(e => e.id === userId)
      .map(e => e.privacyJson)
      .update(DataUserPrivacy.asJson.toString())

    Await.ready(db.run(query), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

  def addPermGroupe(userId: Int, auth: PermissionGroup.Value): Try[DataUser] = {
    val query = joinPermGroup += (userId, Authorization.PermissionGroupToString(auth))

    Await.ready(db.run(query), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

  def delPermGroupe(userId: Int, auth: PermissionGroup.Value): Try[DataUser] = {
    val query = joinPermGroup.filter(e => e.idUser === userId && e.permGroup === Authorization.PermissionGroupToString(auth))

    Await.ready(db.run(query.delete), Duration.Inf).value match {
      case Some(Failure(e)) => return Failure(e)
    }

    getById(userId)
  }

}

object DBUser {

  class TabUser(tag: Tag) extends Table[(Int, String, String, Boolean, Option[String], Option[String], Option[String], Option[String], String)](tag, "USER") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc, O.Default(0))

    def name = column[String]("NAME", O.Unique)

    def email = column[String]("EMAIL", O.Unique)

    def emailIsconfirmed = column[Boolean]("EMAIL_IS_CONFIRMED", O.Default(false))

    def passHash = column[Option[String]]("PASS_HASH")

    def location = column[Option[String]]("LOCATION")

    def tokenOAuth = column[Option[String]]("TOKEN_OAUTH")

    def tokenOAuthOutOfDate = column[Option[String]]("TOKEN_OUT_OF_DATE")

    def privacyJson = column[String]("PRIVACY_JSON", O.Default(DataUserPrivacy("private", "private", "private", "private").asJson.toString()))

    def * = (id, name, email, emailIsconfirmed, passHash, location, tokenOAuth, tokenOAuthOutOfDate, privacyJson)
  }

  val tabUser = TableQuery[TabUser]

  val tabToObjUser: ((Int, String, String, Boolean, Option[String], Option[String], Option[String], Option[String], String)) => DataUser = {
    case (id, name, email, emailIsconfirmed, passHash, location, tokenOAuth, tokenOAuthOutOfDate, privacyJson) => DataUser(
      id, name, email, emailIsconfirmed, passHash, location, tokenOAuth, tokenOAuthOutOfDate,
      parser.decode[DataUserPrivacy](privacyJson).getOrElse(throw new IllegalArgumentException("TabUser: json in db is invalid"))
    )
  }

  class JoinFriend(tag: Tag) extends Table[(Int, Int)](tag, "JOIN_FRIEND") {

    def idUser = column[Int]("ID_USER")

    def idFriend = column[Int]("ID_FRIEND")

    def * = (idUser, idFriend)

    def pk = primaryKey("PK_JOIN_FRIEND", (idUser, idFriend))

    def user =
      foreignKey("FK_JOIN_FRIEND_USER", idUser, tabUser)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def friend =
      foreignKey("FK_JOIN_FRIEND_FRIEND", idFriend, tabUser)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  }

  val joinFriend = TableQuery[JoinFriend]

  class JoinPermGroup(tag: Tag) extends Table[(Int, String)](tag, "JOIN_PERM_GROUP") {

    def idUser = column[Int]("ID_USER")

    def permGroup = column[String]("ID_PERM_GROUP")

    def * = (idUser, permGroup)

    def pk = primaryKey("PK_JOIN_PERM_GROUP", (idUser, permGroup))

    def user =
      foreignKey("FK_JOIN_PERM_GROUP_USER", idUser, tabUser)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  }

  val joinPermGroup = TableQuery[JoinPermGroup]

  class JoinMusicalPreferences(tag: Tag) extends Table[(Int, Int)](tag, "JOIN_MUSICAL_PREFERENCES") {

    def idUser = column[Int]("ID_USER")

    def idDeezerGenre = column[Int]("ID_DEEZER_GENRE")

    def * = (idUser, idDeezerGenre)

    def pk = primaryKey("PK_JOIN_MUSICAL_PREFERENCES", (idUser, idDeezerGenre))

    def user =
      foreignKey("FK_JOIN_MUSICAL_PREFERENCES_USER", idUser, tabUser)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def deezerGenre =
      foreignKey("FK_JOIN_MUSICAL_PREFERENCES_DEEZER_GENRE", idDeezerGenre, DBDeezer.tabDeezerGenre)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  }

  val joinMusicalPreferences = TableQuery[JoinMusicalPreferences]

}
