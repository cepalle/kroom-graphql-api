package root

import deezer.RepoDeezer
import trackVoteEvent.RepoTrackVoteEvent
import user.RepoUser


class RepoRoot(private val dbh: DBRoot) {

  val deezer = new RepoDeezer(dbh.deezer)
  val trackVoteEvent = new RepoTrackVoteEvent(dbh)
  val user = new RepoUser(dbh.user)

}
