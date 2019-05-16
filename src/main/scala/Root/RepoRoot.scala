package Root

import Deezer.RepoDeezer

case class DataTrackVoteEvent(
                           id: Int,
                           name: String,
                           public: Boolean,
                           currentTrackId: Int,
                           horaire: String,
                           location: String
                         )


class RepoRoot(val dbh: DBRoot) {

  val deezerRepo = new RepoDeezer(dbh)

  val getDeezerTrackById = deezerRepo.getTrackById _

  val getDeezerArtistById = deezerRepo.getArtistById _

  val getDeezerAlbumById = deezerRepo.getAlbumById _

  val getDeezerGenreById = deezerRepo.getGenreById _

  def getTrackVoteEventById(id: Int): Option[DataTrackVoteEvent] = {
    dbh.getTrackVoteEventById(id)
  }

  def getTrackVoteEventPublic(): List[DataTrackVoteEvent] = {
    dbh.getTrackVoteEventPublic()
  }

  def getTrackVoteEventByUserId(userId: Int): List[DataTrackVoteEvent] = {
    dbh.getTrackVoteEventByUserId(userId)
  }

}
