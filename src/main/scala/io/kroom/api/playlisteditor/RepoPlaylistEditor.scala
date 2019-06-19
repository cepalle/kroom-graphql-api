package io.kroom.api.playlisteditor

import akka.actor.ActorRef
import io.kroom.api.deezer.RepoDeezer
import io.kroom.api.user.RepoUser

case class DataPlaylistEditor(
                               id: Int,
                               userMasterId: Int,
                               name: String,
                               public: Boolean,
                             )

class RepoPlaylistEditor(
                          private val dbh: DBPlaylistEditor,
                          private val repoDeezer: RepoDeezer,
                          private val repoUser: RepoUser,
                          private val subActor: ActorRef
                        ) {

}
