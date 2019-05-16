package Root

import Deezer.RepoDeezer
import TrackVoteEvent.RepoTrackVoteEvent


class RepoRoot(private val dbh: DBRoot) {

  val deezer = new RepoDeezer(dbh.deezer)
  val trackVoteEvent = new RepoTrackVoteEvent(dbh)

}
