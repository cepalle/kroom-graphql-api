import slick.jdbc.H2Profile
// --

class RepoRoot(val dbh: DBHandler) {

  val deezerRepo = new RepoDeezer(dbh)

  val getDeezerTrackById = deezerRepo.getTrackById _

  val getDeezerArtistById = deezerRepo.getArtistById _

  val getDeezerAlbumById = deezerRepo.getAlbumById _

  val getDeezerGenreById = deezerRepo.getGenreById _

}
