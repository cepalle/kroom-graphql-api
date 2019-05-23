package io.kroom.api.root

import io.kroom.api.user.RepoUser
import io.kroom.api.deezer.RepoDeezer
import io.kroom.api.trackvoteevent.RepoTrackVoteEvent


class RepoRoot(private val dbh: DBRoot) {

  val deezer = new RepoDeezer(dbh.deezer)
  val trackVoteEvent = new RepoTrackVoteEvent(dbh.trackVoteEvent, deezer)
  val user = new RepoUser(dbh.user, deezer)

}
