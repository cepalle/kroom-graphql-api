package Repo

import DB.DBRoot

case class TrackVoteEvent(
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

  def getTrackVoteEventById(id: Int): Option[TrackVoteEvent] = {
    dbh.getTrackVoteEventById(id)
  }

  def getTrackVoteEventPublic(): List[TrackVoteEvent] = {
    dbh.getTrackVoteEventPublic()
  }

  def getTrackVoteEventByUserId(userId: Int): List[TrackVoteEvent] = {
    dbh.getTrackVoteEventByUserId(userId)
  }

}
