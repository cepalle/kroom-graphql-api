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

class DBUser(private val db: H2Profile.backend.Database) {

  import DBUser._

  def getById(id: Int): Option[DataUser] = {
    val query = tabUser.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjUser)
  }

  def getByToken(token: String): Option[DataUser] = {
    // TODO check time out of date
    val query = tabUser.filter(_.tokenOAuth === token).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjUser)
  }

  def getByName(name: String): Option[DataUser] = {
    val query = tabUser.filter(_.name === name).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjUser)
  }

  def getByEmail(email: String): Option[DataUser] = {
    val query = tabUser.filter(_.email === email).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjUser)
  }

  def getFriends(userId: Int): List[DataUser] = {
    val query = for {
      ((u, jf), f) <- tabUser join joinFriend on
        (_.id === _.idUser) join tabUser on (_._2.idFriend === _.id)
      if u.id === userId
    } yield f
    val f = db.run(query.result)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(_.map(tabToObjUser))
      .map(_.toList)
      .getOrElse(List[DataUser]())
  }

  def getmMsicalPreferences(userId: Int): List[DataDeezerGenre] = {
    val query = for {
      ((u, jmp), mp) <- tabUser join joinMusicalPreferences on
        (_.id === _.idUser) join DBDeezer.tabDeezerGenre on (_._2.idDeezerGenre === _.id)
      if u.id === userId
    } yield mp
    val f = db.run(query.result)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(_.map(DBDeezer.tabToObjDeezerGenre))
      .map(_.toList)
      .getOrElse(List[DataDeezerGenre]())
  }

  def getPermGroup(userId: Int): Set[Authorization.PermissionGroup.Value] = {
    val query = joinPermGroup.filter(_.idUser === userId).result
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(_.map(c => Authorization.StringToPermissionGroup(c._2)))
      .map(_.toSet)
      .getOrElse(Set[Authorization.PermissionGroup.Value]())
  }

  // Mutation

  def addUserWithPass(name: String, email: String, passHash: String): Option[DataUser] = {
    val query1 = tabUser.map(c => (c.name, c.email, c.passHash)) += (name, email, Some(passHash))
    Await.ready(db.run(query1), Duration.Inf)

    val user = getByEmail(email).getOrElse(return None)

    val query2 = joinPermGroup += (user.id, Authorization.PermissionGroupToString(Authorization.PermissionGroup.user))
    Await.ready(db.run(query2), Duration.Inf)

    getById(user.id)
  }

  def addFriend(userId: Int, friendId: Int): Option[DataUser] = {
    val query = DBIO.seq(
      joinFriend.map(e => (e.idUser, e.idFriend)) += (userId, friendId),
      joinFriend.map(e => (e.idUser, e.idFriend)) += (friendId, userId)
    )
    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getById(userId)
  }

  def delFriend(userId: Int, friendId: Int): Option[DataUser] = {
    val query = DBIO.seq(
      joinFriend.filter(e => e.idFriend === friendId && e.idUser === userId).delete,
      joinFriend.filter(e => e.idFriend === userId && e.idUser === friendId).delete,
    )
    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getById(userId)
  }

  def addMusicalPreference(userId: Int, genreId: Int): Option[DataUser] = {
    val query = joinMusicalPreferences.map(e => (e.idUser, e.idDeezerGenre)) += (userId, genreId)

    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getById(userId)
  }

  def delMusicalPreference(userId: Int, genreId: Int): Option[DataUser] = {
    val query = joinMusicalPreferences
      .filter(e => e.idDeezerGenre === genreId && e.idUser === userId)
      .delete

    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getById(userId)
  }

  def confirmEmail(userId: Int): Option[DataUser] = {
    val query = tabUser.filter(e => e.id === userId)
      .map(e => e.emailIsconfirmed)
      .update(true)

    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getById(userId)
  }

  def updateLocation(userId: Int, location: String): Option[DataUser] = {
    val query = tabUser.filter(e => e.id === userId)
      .map(e => e.location)
      .update(Some(location))

    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getById(userId)
  }

  def updateToken(userId: Int, token: Option[String], tokenOutOfDate: Option[String]): Option[DataUser] = {
    val query = tabUser.filter(e => e.id === userId)
      .map(e => (e.tokenOAuth, e.tokenOAuthOutOfDate))
      .update((token, tokenOutOfDate))

    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getById(userId)
  }

  def updatePrivacy(userId: Int, pr: DataUserPrivacy): Option[DataUser] = {
    val query = tabUser.filter(e => e.id === userId)
      .map(e => e.privacyJson)
      .update(DataUserPrivacy.asJson.toString())

    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getById(userId)
  }

  def addPermGroupe(userId: Int, auth: PermissionGroup.Value): Option[DataUser] = {
    val query = joinPermGroup += (userId, Authorization.PermissionGroupToString(auth))
    Await.ready(db.run(query), Duration.Inf)

    getById(userId)
  }

  def delPermGroupe(userId: Int, auth: PermissionGroup.Value): Option[DataUser] = {
    val query = joinPermGroup.filter(e => e.idUser === userId && e.permGroup === Authorization.PermissionGroupToString(auth))
    Await.ready(db.run(query.delete), Duration.Inf)

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

    def privacyJson = column[String]("PRIVACY_JSON", O.Default(DataUserPrivacy(3, 3, 1, 1).asJson.toString()))

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
