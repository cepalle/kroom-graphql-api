import scalaj.http.{Http, HttpRequest, HttpResponse}
import io.circe.generic.auto._
import io.circe.parser

/*
case class DeezerGenre(
                        id: Int,
                        name: String,
                        // url,
                        picture: String,
                        // url,
                        picture_small: String,
                        // url,
                        picture_medium: String,
                        // url,
                        picture_big: String,
                        // url,
                        picture_xl: String,
                      )

case class DeezerUser(
                       id: Int,
                       name: String,
                       lastname: String,
                       firstname: String,
                       email: String,
                       status: Int,
                       // date
                       birthday: String,
                       inscription_date: String,
                       // F or M
                       gender: String,
                       // url
                       link: String,
                       // url,
                       picture: String,
                       // url,
                       picture_small: String,
                       // url,
                       picture_medium: String,
                       // url,
                       picture_big: String,
                       // url,
                       picture_xl: String,
                       country: String,
                       lang: String,
                       is_kid: Boolean,
                       explicit_content_level: String,
                       // explicit_display, explicit_no_recommendation and explicit_hide
                       explicit_content_levels_available: List[String],
                       // url
                       tracklist: String,
                     )

case class DeezerSearchTrackResult(
                                    id: Int,
                                    readable: Boolean,
                                    title: String,
                                    title_short: String,
                                    title_version: String,
                                    // url
                                    link: String,
                                    duration: Int,
                                    rank: Int,
                                    explicit_lyrics: Boolean,
                                    // url
                                    preview: String,
                                    // no complet
                                    artist: DeezerArtist,
                                    // no complet
                                    album: DeezerAlbum,
                                  )
*/

case class Id(id: Int)

case class DeezerAlbum(
                        id: Int,
                        title: String,
                        upc: String,
                        // url,
                        link: String,
                        // url,
                        share: String,
                        // url,
                        cover: String,
                        // url,
                        cover_small: String,
                        // url,
                        cover_medium: String,
                        // url,
                        cover_big: String,
                        // url,
                        cover_xl: String,
                        // -1 not found
                        genre_id: Int,
                        // genres: List[Id],
                        label: String,
                        nb_tracks: Int,
                        duration: Int,
                        fans: Int,
                        rating: Int,
                        // date
                        release_date: String,
                        record_type: String,
                        available: Boolean,
                        // alternative: DeezerAlbum,
                        // url
                        tracklist: String,
                        explicit_lyrics: Boolean,
                        //0:Not Explicit; 1:Explicit; 2:Unknown; 3:Edited; 4:Partially Explicit (Album "lyrics" only); 5:Partially Unknown (Album "lyrics" only); 6:No Advice Available; 7:Partially No Advice Available (Album "lyrics" only)
                        explicit_content_lyrics: Int,
                        //0:Not Explicit; 1:Explicit; 2:Unknown; 3:Edited; 4:Partially Explicit (Album "lyrics" only); 5:Partially Unknown (Album "lyrics" only); 6:No Advice Available; 7:Partially No Advice Available (Album "lyrics" only)
                        explicit_content_cover: Int,
                        contributors: List[Id],
                        artist: Id,
                        // tracks: List[Id],
                      )

// --

case class DeezerArtist(
                         id: Int,
                         name: String,
                         // url,
                         link: String,
                         // url,
                         share: String,
                         // url,
                         picture: String,
                         // url,
                         picture_small: String,
                         // url,
                         picture_medium: String,
                         // url,
                         picture_big: String,
                         // url,
                         picture_xl: String,
                         nb_album: Int,
                         nb_fan: Int,
                         // url
                         tracklist: String
                       )

// --

case class DeezerTrack(
                        id: Int,
                        readable: Boolean,
                        title: String,
                        title_short: String,
                        title_version: String,
                        isrc: String,
                        // url,
                        link: String,
                        // url,
                        share: String,
                        duration: Int,
                        track_position: Int,
                        disk_number: Int,
                        rank: Int,
                        // Date,
                        release_date: String,
                        explicit_lyrics: Boolean,
                        explicit_content_lyrics: Int,
                        explicit_content_cover: Int,
                        // url,
                        preview: String,
                        bpm: Double,
                        gain: Double,
                        available_countries: List[String],
                        contributors: List[Id],
                        artist: Id,
                        album: Id,
                      )

// --

class RootRepo {

  def getTrackById(id: Int): DeezerTrack = {
    val request: HttpRequest = Http(s"https://api.deezer.com/track/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerTrack](res.body)
    decodingResult match {
      case Right(track) => track
      case Left(error) => throw error
    }
  }

  def getArtistById(id: Int): DeezerArtist = {
    val request: HttpRequest = Http(s"https://api.deezer.com/artist/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerArtist](res.body)
    decodingResult match {
      case Right(track) => track
      case Left(error) => throw error
    }
  }

  def getAlbumById(id: Int): DeezerAlbum = {
    val request: HttpRequest = Http(s"https://api.deezer.com/album/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerAlbum](res.body)
    decodingResult match {
      case Right(track) => track
      case Left(error) => throw error
    }
  }


}

