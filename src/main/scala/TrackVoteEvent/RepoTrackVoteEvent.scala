package TrackVoteEvent

import Root.DBRoot

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

}
