package io.kroom.api.playlisteditor

import akka.actor.ActorRef
import io.kroom.api.{SubQueryEnum, WSEventCSUpdateQuery}
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

  def getByName(name: String): Try[DataPlaylistEditor] = {
    dbh.getByName(name)
  }

  def getByUserId(userId: Int): Try[List[DataPlaylistEditor]] = {
    dbh.getByUserId(userId)
  }

  def getInvitedUsers(id: Int): Try[List[DataUser]] = {
    dbh.getInvitedUsers(id)
  }

  /* MUTATION */

  def `new`(userMasterId: Int, name: String, public: Boolean): Try[DataPlaylistEditor] = {
    dbh.`new`(userMasterId, name, public)
  }

  def delete(id: Int): Try[Unit] = {
    val res = dbh.delete(id)
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.PlayListEditorById, id)
    res
  }

  def addUser(playlistId: Int, userId: Int): Try[DataPlaylistEditor] = {
    val res = dbh.addUser(playlistId, userId)
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.TrackVoteEventById, playlistId)
    res
  }

  def delUser(playlistId: Int, userId: Int): Try[DataPlaylistEditor] = {
    val res = dbh.delUser(playlistId, userId)
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.TrackVoteEventById, playlistId)
    res
  }

  def addTrack(playListId: Int, trackId: Int): Try[DataPlaylistEditor] = {
    val res = dbh.addTrack(playListId, trackId)
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.PlayListEditorById, playListId)
    res
  }

  def delTrack(playListId: Int, trackId: Int): Try[DataPlaylistEditor] = {
    val res = dbh.delTrack(playListId, trackId)
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.PlayListEditorById, playListId)
    res
  }

  def moveTrack(playListId: Int, trackId: Int, up: Boolean): Try[DataPlaylistEditor] = {
    val res = dbh.moveTrack(playListId, trackId, up)
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.PlayListEditorById, playListId)
    res
  }

}
