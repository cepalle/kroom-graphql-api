
// --

class RootRepo {

  val deezerRepo = new DeezerRepo()

  val getDeezerTrackById = deezerRepo.getTrackById _

  val getDeezerArtistById = deezerRepo.getArtistById _

  val getDeezerAlbumById = deezerRepo.getAlbumById _

  val getDeezerGenreById = deezerRepo.getGenreById _

}
