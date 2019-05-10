import slick.jdbc.H2Profile
// --

class RepoRoot(val db: H2Profile.backend.Database) {

  val deezerRepo = new RepoDeezer(db)

  val getDeezerTrackById = deezerRepo.getTrackById _

  val getDeezerArtistById = deezerRepo.getArtistById _

  val getDeezerAlbumById = deezerRepo.getAlbumById _

  val getDeezerGenreById = deezerRepo.getGenreById _

}
