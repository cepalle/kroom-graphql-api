package io.kroom.api.trackvoteevent

import io.kroom.api.deezer.RepoDeezer
import io.kroom.api.user.DataUser

case class DataTrackWithVote(
                              trackId: Int,
                              score: Int,
                              nb_vote_up: Int,
                              nb_vote_down: Int,
                            )

case class DataTrackVoteEvent(
                               id: Int,
                               userMasterId: Int,
                               name: String,
                               public: Boolean,
                               currentTrackId: Option[Int],
                               schedule: Option[String],
                               location: Option[String]
                             )

class RepoTrackVoteEvent(private val dbh: DBTrackVoteEvent, private val repoDeezer: RepoDeezer) {

  def getById(id: Int): Option[DataTrackVoteEvent] = {
    dbh.getTrackVoteEventById(id)
  }

  def getPublic: List[DataTrackVoteEvent] = {
    dbh.getTrackVoteEventPublic
  }

  def getByUserId(userId: Int): List[DataTrackVoteEvent] = {
    dbh.getTrackVoteEventByUserId(userId)
  }

  def getTrackWithVote(eventId: Int): List[DataTrackWithVote] = {
    dbh.getTrackWithVote(eventId)
  }

  def getUserInvited(eventId: Int): List[DataUser] = {
    dbh.getUserInvited(eventId)
  }

  // Mutation

  def `new`(userIdMaster: Int,
            name: String,
            public: Boolean,
           ): Option[DataTrackVoteEvent] = {
    dbh.`new`(userIdMaster, name, public)
  }

  def update(eventId: Int,
             userIdMaster: Int,
             name: String,
             public: Boolean,
             schedule: Option[String],
             location: Option[String]
            ): Option[DataTrackVoteEvent] = {
    dbh.update(
      eventId,
      userIdMaster,
      name,
      public,
      schedule,
      location
    )
  }

  def addUser(eventId: Int, userId: Int): Option[DataTrackVoteEvent] = {
    dbh.addUser(eventId, userId)
  }

  def delUser(eventId: Int, userId: Int): Option[DataTrackVoteEvent] = {
    dbh.delUser(eventId, userId)
  }

  def addVote(eventId: Int, userId: Int, musicId: Int, up: Boolean): Option[DataTrackVoteEvent] = {
    repoDeezer.getTrackById(musicId) // get Track in DB
    dbh.addVote(eventId, userId, musicId, up)
  }

  def delVote(eventId: Int, userId: Int, musicId: Int): Option[DataTrackVoteEvent] = {
    dbh.delVote(eventId, userId, musicId)
  }

}
