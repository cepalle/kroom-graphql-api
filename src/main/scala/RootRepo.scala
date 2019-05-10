
// --

class RootRepo {

  val deezerRepo = new DeezerRepo()

  def getDeezerTrackById(id: Int): Option[DeezerTrack] = deezerRepo.getTrackById(id)

  def getDeezerArtistById(id: Int): Option[DeezerArtist] = deezerRepo.getArtistById(id)

  def getDeezerAlbumById(id: Int): Option[DeezerAlbum] = deezerRepo.getAlbumById(id)

  def getDeezerGenreById(id: Int): Option[DeezerGenre] = deezerRepo.getGenreById(id)

}
