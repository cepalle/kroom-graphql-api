package io.kroom.api.playlisteditor

import io.kroom.api.deezer.DBDeezer
import io.kroom.api.user.{DBUser, DataUser}
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Success, Try}

class DBPlaylistEditor(private val db: H2Profile.backend.Database) {

  import DBPlaylistEditor._
  import DBUser._

  def getPublic: Try[List[DataPlaylistEditor]] = {
    val query = tabPlayList.filter(_.public).result

    Await.ready(db.run(query), Duration.Inf).value.get
      .map(_.map(tabToObjPlayList))
      .map(_.toList)
  }

  def getById(id: Int): Try[DataPlaylistEditor] = {
    val query = tabPlayList.filter(_.id === id).result.head

    Await.ready(db.run(query), Duration.Inf).value.get
      .map(tabToObjPlayList)
  }

  def getByUserId(userId: Int): Try[List[DataPlaylistEditor]] = {
    val query = for {
      ((u, j), e) <- tabUser join joinPlayListUser on
        (_.id === _.idUser) join tabPlayList on (_._2.idPLaylist === _.id)
      if u.id === userId
    } yield e

    Await.ready(db.run(query.result), Duration.Inf).value.get
      .map(_.map(tabToObjPlayList))
      .map(_.toList)
  }

  def getTracksWithOrder(id: Int): Try[List[DataTrackWithOrder]] = {
    val query = for {
      ((e, ju), u) <- tabPlayList join joinPlayListTrack on (_.id === _.idPlayList) join DBDeezer.tabDeezerTrack on (_._2.idDeezerTrack === _.id)
      if e.id === id
    } yield (u, ju.pos)

    Await.ready(db.run(query.result), Duration.Inf).value.get
      .map(_
        .map(t => DBDeezer.tabToObjDeezerTrack(t._1).map(tr => DataTrackWithOrder(t._2, tr)))
        .collect { case Success(s) => s }
      )
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
    extends Table[(Int, Int, String, Boolean)](tag, "PLAY_LIST_EDITOR") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc, O.Default(0))

    def userMasterId = column[Int]("USER_MASTER_ID")

    def name = column[String]("NAME", O.Unique)

    def public = column[Boolean]("PUBLIC")

    def * = (id, userMasterId, name, public)
  }

  val tabPlayList = TableQuery[TabPlayList]

  val tabToObjPlayList: ((Int, Int, String, Boolean)) => DataPlaylistEditor = {
    case (id, userMasterId, name, public) =>
      DataPlaylistEditor(
        id, userMasterId, name, public
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

  class JoinPlayListTrack(tag: Tag)
    extends Table[(Int, Int, Int)](tag, "JOIN_PLAY_LIST_TRACK") {

    def idPlayList = column[Int]("ID_PLAY_LIST")

    def idDeezerTrack = column[Int]("ID_DEEZER_TRACK")

    def pos = column[Int]("POS")

    def * = (idPlayList, idDeezerTrack, pos)

    def pk = primaryKey("PK_JOIN_PLAY_LIST_TRACK", (idPlayList, idDeezerTrack))

    def deezerTrack =
      foreignKey("FK_JOIN_PLAY_LIST_TRACK_DEEZER_TRACK", idDeezerTrack, DBDeezer.tabDeezerTrack)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def playlist =
      foreignKey("FK_JOIN_PLAY_LIST_TRACK_PLAY_LIST", idPlayList, tabPlayList)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  }

  val joinPlayListTrack = TableQuery[JoinPlayListTrack]

}
