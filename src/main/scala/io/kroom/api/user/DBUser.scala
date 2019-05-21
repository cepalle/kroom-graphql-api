package io.kroom.api.user

import io.kroom.api.deezer.{DBDeezer, DataDeezerGenre}
import sangria.execution.UserFacingError
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

  def getFriends(userId: Int): List[DataUser] = {
    val query = for {
      ((u, jf), f) <- tabUser join joinFriend on
        (_.id === _.idUser1) join tabUser on (_._2.idUser2 === _.id)
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

  // Mutation

  def addUserWithPass(name: String, email: String, passHash: String): Option[DataUser] = {
    val query = tabUser.map(c => (c.name, c.email, c.passHash)) += (name, email, Some(passHash))
    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    val queryUser = tabUser.filter(e => e.email === email).result.head
    val f2 = db.run(queryUser)
    Await.ready(f2, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjUser)
  }

  def checkUserEmailPass(email: String, passHash: String): Option[DataUser] = {
    val queryUser = tabUser.filter(e => e.email === email).result.head
    val f2 = db.run(queryUser)
    Await.ready(f2, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjUser)
      .flatMap(e => if (e.passHash.contains(passHash)) {
        Some(e)
      } else {
        None
      })
  }

  def checkUserNamePass(name: String, passHash: String): Option[DataUser] = {
    val queryUser = tabUser.filter(e => e.name === name).result.head
    val f2 = db.run(queryUser)
    Await.ready(f2, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjUser)
      .flatMap(e => if (e.passHash.contains(passHash)) {
        Some(e)
      } else {
        None
      })
  }

  def addFriend(userId: Int, friendId: Int): Option[DataUser] = {

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

object DBUser {

  class TabUser(tag: Tag) extends Table[(Int, String, String, Boolean, Option[String], Option[String], Option[String], Option[String])](tag, "USER") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc, O.Default(0))

    def name = column[String]("NAME", O.Unique)

    def email = column[String]("EMAIL", O.Unique)

    def emailIsconfirmed = column[Boolean]("EMAIL_IS_CONFIRMED", O.Default(false))

    def passHash = column[Option[String]]("PASS_HASH")

    def location = column[Option[String]]("LOCATION")

    def tokenOAuth = column[Option[String]]("TOKEN_OAUTH")

    def tokenOAuthOutOfDate = column[Option[String]]("TOKEN_OUT_OF_DATE")

    def * = (id, name, email, emailIsconfirmed, passHash, location, tokenOAuth, tokenOAuthOutOfDate)
  }

  val tabUser = TableQuery[TabUser]

  val tabToObjUser: ((Int, String, String, Boolean, Option[String], Option[String], Option[String], Option[String])) => DataUser = {
    case (id, name, email, emailIsconfirmed, passHash, location, tokenOAuth, tokenOAuthOutOfDate) => DataUser(
      id, name, email, emailIsconfirmed, passHash, location, tokenOAuth, tokenOAuthOutOfDate
    )
  }

  class JoinFriend(tag: Tag) extends Table[(Int, Int, Int)](tag, "JOIN_FRIEND") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc, O.Default(0))

    def idUser1 = column[Int]("ID_USER_1")

    def idUser2 = column[Int]("ID_USER_2")

    def * = (id, idUser1, idUser2)

    def supplier =
      foreignKey("ID_USER_1", idUser1, tabUser)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def supplier2 =
      foreignKey("ID_USER_2", idUser2, tabUser)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  }

  val joinFriend = TableQuery[JoinFriend]

  class JoinMusicalPreferences(tag: Tag) extends Table[(Int, Int, Int)](tag, "JOIN_MUSICAL_PREFERENCES") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc, O.Default(0))

    def idUser = column[Int]("ID_USER")

    def idDeezerGenre = column[Int]("ID_DEEZER_GENRE")

    def * = (id, idUser, idDeezerGenre)
  }

  val joinMusicalPreferences = TableQuery[JoinMusicalPreferences]

}
