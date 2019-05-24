package io.kroom.api.root

import io.kroom.api.deezer.DBDeezer
import io.kroom.api.trackvoteevent.DBTrackVoteEvent
import io.kroom.api.user.DBUser
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class DBRoot(private val db: H2Profile.backend.Database) {
  val deezer: DBDeezer = new DBDeezer(db)
  val trackVoteEvent: DBTrackVoteEvent = new DBTrackVoteEvent(db)
  val user: DBUser = new DBUser(db)
}

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
        DBUser.joinPermGroup.schema ++
        DBUser.joinMusicalPreferences.schema ++

        DBTrackVoteEvent.tabTrackVoteEvent.schema ++
        DBTrackVoteEvent.joinTrackVoteEventUser.schema ++
        DBTrackVoteEvent.joinTrackVoteEventUserVoteTrack.schema
        ).create,
    )

    val f = db.run(setup)

    val result = Await.ready(f, Duration.Inf).value.get

    println(result)
  }

}
