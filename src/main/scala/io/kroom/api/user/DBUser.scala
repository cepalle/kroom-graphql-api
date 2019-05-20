package io.kroom.api.user

import io.kroom.api.deezer.DataDeezerGenre
import sangria.execution.UserFacingError
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

class DBUser(private val db: H2Profile.backend.Database) {

  import DBUser._

  def getById(id: Int): Option[DataUser] = {
    None
  }

  def getFriends(userId: Int): List[DataUser] = {
    List[DataUser]()
  }

  def getmMsicalPreferences(userId: Int): List[DataDeezerGenre] = {
    List[DataDeezerGenre]()
  }

}

object DBUser {

  class TabUser(tag: Tag) extends Table[(Int, String, String, String)](tag, "USER") {

    def id = column[Int]("ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def email = column[String]("EMAIL")

    def passHash = column[String]("PASS_HASH")

    def location = column[String]("LOCATION")

    def * = (id, name, email, location)
  }

  val tabUser = TableQuery[TabUser]

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

    def idDeezerTrack = column[Int]("ID_DEEZER_TRACK")

    def * = (id, idUser, idDeezerTrack)
  }

  val joinMusicalPreferences = TableQuery[JoinMusicalPreferences]


}
