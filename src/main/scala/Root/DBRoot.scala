package Root

import Deezer.DBDeezer
import TrackVoteEvent.DBTrackVoteEvent
import User.DBUser
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class DBRoot(private val db: H2Profile.backend.Database) {
  val deezer = DBDeezer(db)
  val trackVoteEvent = DBTrackVoteEvent(db)
  val user = DBUser(db)

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

