package io.kroom.api.trackvoteevent

import sangria.execution.UserFacingError
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
                               currentTrackId: Int,
                               schedule: String,
                               location: String
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

  def newEvent(userIdMaster: Int,
               name: String,
               public: Boolean,
               horaire: String,
               location: String
              ): Option[DataTrackVoteEvent] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def update(userIdMaster: Option[Int],
             name: Option[String],
             public: Option[Boolean],
             horaire: Option[String],
             location: Option[String]
            ): Option[DataTrackVoteEvent] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def addUser(eventId: Int, userId: Int): Option[DataTrackVoteEvent] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def delUser(eventId: Int, userId: Int): Option[DataTrackVoteEvent] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def addVote(eventId: Int, userId: Int, musicId: Int, up: Boolean): Option[DataTrackVoteEvent] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

  def delVote(eventId: Int, userId: Int, musicId: Int): Option[DataTrackVoteEvent] = {
    throw new Throwable with UserFacingError {
      override def getMessage: String = "TODO"
    }
  }

}
