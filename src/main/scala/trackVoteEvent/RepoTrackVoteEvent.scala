package trackVoteEvent

import root.DBRoot
import sangria.execution.UserFacingError

case class DataTrackVoteEvent(
                               id: Int,
                               name: String,
                               public: Boolean,
                               currentTrackId: Int,
                               horaire: String,
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
