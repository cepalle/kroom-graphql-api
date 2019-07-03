package io.kroom.api.playlisteditor

import io.kroom.api.user.{DBUser, DataUser}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._
import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Success, Try}

class DBPlaylistEditor(private val db: H2Profile.backend.Database) {

  import DBPlaylistEditor._
  import DBUser._

  def getPublic: Try[List[DataPlaylistEditor]] = {
    val query = tabPlayList.filter(_.public).result

    Await.ready(db.run(query), Duration.Inf).value.get
      .map(_.map(tabToObjPlayList).collect({ case Success(v) => v }))
      .map(_.toList)
  }

  def getById(id: Int): Try[DataPlaylistEditor] = {
    val query = tabPlayList.filter(_.id === id).result.head

    Await.ready(db.run(query), Duration.Inf).value.get
      .map(tabToObjPlayList).collect({ case Success(v) => v })
  }

  def getByName(name: String): Try[DataPlaylistEditor] = {
    val query = tabPlayList.filter(_.name === name).result.head

    Await.ready(db.run(query), Duration.Inf).value.get
      .map(tabToObjPlayList).collect({ case Success(v) => v })
  }

  def getByUserId(userId: Int): Try[List[DataPlaylistEditor]] = {
    val query = for {
      (j, e) <- joinPlayListUser join tabPlayList on (_.idPLaylist === _.id)
      if j.idUser === userId
    } yield e

    Await.ready(db.run(query.result), Duration.Inf).value.get
      .map(_.map(tabToObjPlayList).collect({ case Success(v) => v }))
      .map(_.toList)
  }

  def getInvitedUsers(id: Int): Try[List[DataUser]] = {
    val query = for {
      ((e, ju), u) <- tabPlayList join joinPlayListUser on (_.id === _.idPLaylist) join DBUser.tabUser on (_._2.idUser === _.id)
      if e.id === id
    } yield u

    Await.ready(db.run(query.result), Duration.Inf).value.get
      .map(_.map(DBUser.tabToObjUser) collect { case Success(s) => s })
      .map(_.toList)
  }

  /* MUTATION */

  def delete(id: Int): Try[Unit] = {
    val query = tabPlayList.filter(_.id === id).delete

    Await.ready(db.run(query), Duration.Inf).value.get
      .map(_ => Unit)
  }

  def `new`(userMasterId: Int, name: String, public: Boolean): Try[DataPlaylistEditor] = {
    val query = (tabPlayList
      .map(e => (e.userMasterId, e.name, e.public, e.tracks))
      returning tabPlayList.map(_.id)
      ) += (userMasterId, name, public, List[Int]().asJson.toString())

    Await.ready(db.run(query), Duration.Inf).value.get
      .flatMap(id => addUser(id, userMasterId))
  }

  def addUser(playlistId: Int, userId: Int): Try[DataPlaylistEditor] = {
    val query = joinPlayListUser
      .map(e => (e.idPLaylist, e.idUser)) += (playlistId, userId)

    Await.ready(db.run(query), Duration.Inf).value.get
      .flatMap(_ => getById(playlistId))
  }

  def delUser(playlistId: Int, userId: Int): Try[DataPlaylistEditor] = {
    val query = joinPlayListUser
      .filter(e => e.idPLaylist === playlistId && e.idUser === userId).delete

    Await.ready(db.run(query), Duration.Inf).value.get
      .flatMap(_ => getById(playlistId))
  }

  def addTrack(playListId: Int, trackId: Int): Try[DataPlaylistEditor] = {
    getById(playListId).flatMap(pl => {
      val newList = pl.tracks :+ trackId

      val query = tabPlayList.filter(_.id === playListId)
        .map(_.tracks)
        .update(newList.asJson.toString())

      Await.ready(db.run(query), Duration.Inf).value.get
        .flatMap(_ => getById(playListId))
    })
  }

  def delTrack(playListId: Int, trackId: Int): Try[DataPlaylistEditor] = {
    getById(playListId).flatMap(pl => {
      val newList = pl.tracks.filter(_ != trackId)

      val query = tabPlayList.filter(_.id === playListId)
        .map(_.tracks)
        .update(newList.asJson.toString())

      Await.ready(db.run(query), Duration.Inf).value.get
        .flatMap(_ => getById(playListId))
    })
  }

  def moveTrack(playListId: Int, trackId: Int, up: Boolean): Try[DataPlaylistEditor] = {
    getById(playListId).flatMap(pl => {

      val index = pl.tracks.indexOf(trackId)

      val newList: List[Int] = if (up) {
        if (index <= 0) {
          pl.tracks
        } else {
          val tmp = pl.tracks(index)
          val up1 = pl.tracks.updated(index, pl.tracks(index - 1))
          val up2 = up1.updated(index - 1, tmp)
          up2
        }
      } else {
        if (index + 1 >= pl.tracks.length) {
          pl.tracks
        } else {
          val tmp = pl.tracks(index)
          val up1 = pl.tracks.updated(index, pl.tracks(index + 1))
          val up2 = up1.updated(index + 1, tmp)
          up2
        }
      }

      val query = tabPlayList.filter(_.id === playListId)
        .map(_.tracks)
        .update(newList.asJson.toString())

      Await.ready(db.run(query), Duration.Inf).value.get
        .flatMap(_ => getById(playListId))
    })
  }

}

/*

playlist {
  id: Int,
  userMaster,
  name,
  private: Boolean,

  invitedUser: [User],
  tracks: [{pos: Int, Track}] // order
}

*/

object DBPlaylistEditor {

  class TabPlayList(tag: Tag)
    extends Table[(Int, Int, String, Boolean, String)](tag, "PLAY_LIST_EDITOR") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc, O.Default(0))

    def userMasterId = column[Int]("USER_MASTER_ID")

    def name = column[String]("NAME", O.Unique)

    def public = column[Boolean]("PUBLIC")

    def tracks = column[String]("TRACKS_JSON")

    def * = (id, userMasterId, name, public, tracks)
  }

  val tabPlayList = TableQuery[TabPlayList]

  val tabToObjPlayList: ((Int, Int, String, Boolean, String)) => Try[DataPlaylistEditor] = {
    case (id, userMasterId, name, public, tracksJson) =>
      parser.decode[List[Int]](tracksJson).toTry.map(tracks =>
        DataPlaylistEditor(
          id, userMasterId, name, public, tracks
        )
      )
  }

  class JoinPlayListUser(tag: Tag)
    extends Table[(Int, Int)](tag, "JOIN_PLAY_LIST_USER_INVITED") {

    def idPLaylist = column[Int]("ID_PLAY_LIST")

    def idUser = column[Int]("ID_USER")

    def * = (idPLaylist, idUser)

    def pk = primaryKey("PK_JOIN_PLAY_LIST_USER_INVITED", (idPLaylist, idUser))

    def playList =
      foreignKey("FK_JOIN_PLAY_LIST_USER_PLAY_LIST", idPLaylist, tabPlayList)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def user =
      foreignKey("FK_JOIN_PLAY_LIST_USER_USER", idUser, DBUser.tabUser)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  val joinPlayListUser = TableQuery[JoinPlayListUser]

}
