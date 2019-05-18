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

  def getTrackVoteEventById(id: Int): Option[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.getTrackVoteEventById(id)
  }

  def getTrackVoteEventPublic: List[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.getTrackVoteEventPublic
  }

  def getTrackVoteEventByUserId(userId: Int): List[DataTrackVoteEvent] = {
    dbh.trackVoteEvent.getTrackVoteEventByUserId(userId)
  }

  def trackVoteEventVote(eventId: Int, musicId: Int, bool: Boolean): Option[DataTrackVoteEvent] = {
    println("ici")
    throw new Throwable with UserFacingError {
      override def getMessage: String = "Not yet implemented"
    }
  }

}

