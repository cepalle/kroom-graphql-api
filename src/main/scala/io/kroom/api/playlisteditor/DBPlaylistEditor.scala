package io.kroom.api.playlisteditor

import io.kroom.api.deezer.DBDeezer
import io.kroom.api.user.DBUser
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

class DBPlaylistEditor(private val db: H2Profile.backend.Database) {

  import DBPlaylistEditor._


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
