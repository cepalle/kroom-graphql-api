package Root

import Deezer.DBDeezer
import TrackVoteEvent.DBTrackVoteEvent
import User.DBUser
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class DBRoot(val db: H2Profile.backend.Database) {

  val getDeezerGenre = DBDeezer.getDeezerGenre(db, _)
  val addDeezerGenre = DBDeezer.addDeezerGenre(db, _)
  val getDeezerAlbum = DBDeezer.getDeezerAlbum(db, _)
  val addDeezerAlbum = DBDeezer.addDeezerAlbum(db, _)
  val getDeezerArtist = DBDeezer.getDeezerArtist(db, _)
  val addDeezerArtist = DBDeezer.addDeezerArtist(db, _)
  val getDeezerTrack = DBDeezer.getDeezerTrack(db, _)
  val addDeezerTrack = DBDeezer.addDeezerTrack(db, _)

  val getTrackVoteEventById = DBTrackVoteEvent.getTrackVoteEventById(db, _)
  val getTrackVoteEventPublic = () => DBTrackVoteEvent.getTrackVoteEventPublic(db)
  val getTrackVoteEventByUserId = DBTrackVoteEvent.getTrackVoteEventByUserId(db, _)

}

// foreign key
// ids ?
object DBRoot {

  def init(db: H2Profile.backend.Database): Unit = {
    println("Database starting ...")
    val setup = DBIO.seq(
      (DBDeezer.tabDeezerGenre.schema ++
        DBDeezer.tabDeezerAlbum.schema ++
        DBDeezer.tabDeezerArtist.schema ++
        DBDeezer.tabDeezerTrack.schema ++

        DBUser.tabUser.schema ++
        DBUser.joinFriend.schema ++
        DBUser.joinMusicalPreferences.schema ++

        DBTrackVoteEvent.tabTrackVoteEvent.schema ++
        DBTrackVoteEvent.joinTrackVoteEventUserInvited.schema ++
        DBTrackVoteEvent.joinTrackVoteEventUserVoteTrack.schema
        ).create,
    )

    val f = db.run(setup)

    val result = Await.ready(f, Duration.Inf).value.get

    println(result)
  }

}

