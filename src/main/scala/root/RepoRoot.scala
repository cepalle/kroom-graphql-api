package root

import deezer.RepoDeezer
import trackVoteEvent.RepoTrackVoteEvent


class RepoRoot(private val dbh: DBRoot) {

  val deezer = new RepoDeezer(dbh.deezer)
  val trackVoteEvent = new RepoTrackVoteEvent(dbh)

}
