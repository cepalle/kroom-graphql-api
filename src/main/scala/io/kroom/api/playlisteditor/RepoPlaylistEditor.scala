package io.kroom.api.playlisteditor

import akka.actor.ActorRef
import io.kroom.api.deezer.RepoDeezer
import io.kroom.api.user.RepoUser

import scala.util.Try

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
  def getPublic: Try[List[DataPlaylistEditor]] = {
    dbh.getPublic
  }


  def getById(id: Int): Try[DataPlaylistEditor] = {
    dbh.getById(id)
  }

  def getByUserId(userId: Int): Try[List[DataPlaylistEditor]] = {
    dbh.getByUserId(userId)
  }

}
