import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class TrackVoteEvent(
                           id: Int,
                         )


class RepoRoot(val dbh: DBHandler) {

  val deezerRepo = new RepoDeezer(dbh)

  val getDeezerTrackById = deezerRepo.getTrackById _

  val getDeezerArtistById = deezerRepo.getArtistById _

  val getDeezerAlbumById = deezerRepo.getAlbumById _

  val getDeezerGenreById = deezerRepo.getGenreById _

  def getTrackVoteEventById(id: Int): Option[TrackVoteEvent] = {
    None
  }

  def getTrackVoteEventPublic(): Future[List[TrackVoteEvent]] = {
    Future {
      val l = List[TrackVoteEvent]()
      l
    }
  }

}
