package io.kroom.api.playlisteditor

import akka.actor.ActorRef
import io.kroom.api.deezer.{DataDeezerTrack, RepoDeezer}
import io.kroom.api.user.{DataUser, RepoUser}

import scala.util.Try

case class DataPlaylistEditor(
                               id: Int,
                               userMasterId: Int,
                               name: String,
                               public: Boolean,
                               tracks: List[Int]
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

  def getInvitedUsers(id: Int): Try[List[DataUser]] = {
    dbh.getInvitedUsers(id)
  }

  /* MUTATION */

  def addTrack(playListId: Int, trackId: Int): Try[DataPlaylistEditor] = {
    dbh.addTrack(playListId, trackId)
  }

  def delTrack(playListId: Int, trackId: Int): Try[DataPlaylistEditor] = {
    dbh.delTrack(playListId, trackId)
  }

  def moveTrack(playListId: Int, trackId: Int, up: Boolean): Try[DataPlaylistEditor] = {
    dbh.moveTrack(playListId, trackId, up)
  }

}
