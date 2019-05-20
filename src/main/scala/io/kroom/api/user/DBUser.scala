package io.kroom.api.user

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

}

// Nullable type ?
object DBUser {

  class TabUser(tag: Tag) extends Table[(Int, String, String, String, String)](tag, "USER") {

    def id = column[Int]("ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def email = column[String]("EMAIL")

    def passHash = column[String]("PASS_HASH")

    def location = column[String]("LOCATION")

    def * = (id, name, email, passHash, location)
  }

  val tabUser = TableQuery[TabUser]

  val tabToObjUser: ((Int, String, String, String, String)) => DataUser = {
    case (id, name, email, passHash, location) => DataUser(
      id, name, email, passHash, Some(location)
    )
  }

  class JoinFriend(tag: Tag) extends Table[(Int, Int, Int)](tag, "JOIN_FRIEND") {

    def id = column[Int]("ID", O.PrimaryKey)

    def idUser1 = column[Int]("ID_USER_1")

    def idUser2 = column[Int]("ID_USER_2")

    def * = (id, idUser1, idUser2)
  }

  val joinFriend = TableQuery[JoinFriend]

  class JoinMusicalPreferences(tag: Tag) extends Table[(Int, Int, Int)](tag, "JOIN_MUSICAL_PREFERENCES") {

    def id = column[Int]("ID", O.PrimaryKey)

    def idUser = column[Int]("ID_USER")

    def idDeezerGenre = column[Int]("ID_DEEZER_GENRE")

    def * = (id, idUser, idDeezerGenre)
  }

  val joinMusicalPreferences = TableQuery[JoinMusicalPreferences]

}
