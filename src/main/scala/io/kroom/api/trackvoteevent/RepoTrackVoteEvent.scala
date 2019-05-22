package io.kroom.api.trackvoteevent

import io.kroom.api.root.DBRoot
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

class RepoTrackVoteEvent(private val dbh: DBRoot) {

  def getById(id: Int): Option[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.getTrackVoteEventById(id)
  }

  def getPublic: List[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.getTrackVoteEventPublic
  }

  def getByUserId(userId: Int): List[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.getTrackVoteEventByUserId(userId)
  }

  def getTrackWithVote(eventId: Int): List[DataTrackWithVote] = {
    dbh.trackVoteEvent.getTrackWithVote(eventId)
  }

  def getUserInvited(eventId: Int): List[DataUser] = {
    dbh.trackVoteEvent.getUserInvited(eventId)
  }

  // Mutation

  def `new`(userIdMaster: Int,
            name: String,
            public: Boolean,
              ): Option[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.`new`(userIdMaster, name, public)
  }

  def update(eventId: Int,
             userIdMaster: Int,
             name: String,
             public: Boolean,
             schedule: Option[String],
             location: Option[String]
            ): Option[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.update(
      eventId,
      userIdMaster,
      name,
      public,
      schedule,
      location
    )
  }

  def addUser(eventId: Int, userId: Int): Option[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.addUser(eventId, userId)
  }

  def delUser(eventId: Int, userId: Int): Option[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.delUser(eventId, userId)
  }

  def addVote(eventId: Int, userId: Int, musicId: Int, up: Boolean): Option[DataTrackVoteEvent] = {
    // TODO May need fetch
    dbh.trackVoteEvent.addVote(eventId, userId, musicId, up)
  }

  def delVote(eventId: Int, userId: Int, musicId: Int): Option[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.delVote(eventId, userId, musicId)
  }

}
