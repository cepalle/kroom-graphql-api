package io.kroom.api.root

import akka.actor.ActorRef
import io.kroom.api.user.RepoUser
import io.kroom.api.deezer.RepoDeezer
import io.kroom.api.trackvoteevent.RepoTrackVoteEvent

class RepoRoot(private val dbh: DBRoot, private val subActor: ActorRef) {

  val deezer = new RepoDeezer(dbh.deezer)
  val user = new RepoUser(dbh.user, deezer)
  val trackVoteEvent = new RepoTrackVoteEvent(dbh.trackVoteEvent, deezer, user, subActor)

}
