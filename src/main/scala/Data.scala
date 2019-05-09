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

case class DeezerAlbum(
                        id: Int,
                      )

// --

case class DeezerArtist(
                         id: Int,
                       )

// --

case class DeezerTrackArtistId(id: Int)

case class DeezerTrackAlbumId(id: Int)

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
                        contributors: List[DeezerTrackArtistId],
                        artist: DeezerTrackArtistId,
                        album: DeezerTrackAlbumId,
                      )

// --
/*
{
  "id": "3135556",
  "readable": true,
  "title": "Harder Better Faster Stronger",
  "title_short": "Harder Better Faster Stronger",
  "title_version": "",
  "isrc": "GBDUW0000059",
  "link": "https://www.deezer.com/track/3135556",
  "share": "https://www.deezer.com/track/3135556?utm_source=deezer&utm_content=track-3135556&utm_term=0_1557388355&utm_medium=web",
  "duration": "224",
  "track_position": 4,
  "disk_number": 1,
  "rank": "728714",
  "release_date": "2001-03-07",
  "explicit_lyrics": false,
  "explicit_content_lyrics": 0,
  "explicit_content_cover": 0,
  "preview": "https://cdns-preview-d.dzcdn.net/stream/c-deda7fa9316d9e9e880d2c6207e92260-5.mp3",
  "bpm": 123.4,
  "gain": -12.4,
  "available_countries": [
    "AE",
    "WS",
  ],
  "contributors": [
    {
      "id": 27,
      "name": "Daft Punk",
      "link": "https://www.deezer.com/artist/27",
      "share": "https://www.deezer.com/artist/27?utm_source=deezer&utm_content=artist-27&utm_term=0_1557388355&utm_medium=web",
      "picture": "https://api.deezer.com/artist/27/image",
      "picture_small": "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
      "picture_medium": "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
      "picture_big": "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
      "picture_xl": "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
      "radio": true,
      "tracklist": "https://api.deezer.com/artist/27/top?limit=50",
      "type": "artist",
      "role": "Main"
    }
  ],
  "artist": {
    "id": "27",
    "name": "Daft Punk",
    "link": "https://www.deezer.com/artist/27",
    "share": "https://www.deezer.com/artist/27?utm_source=deezer&utm_content=artist-27&utm_term=0_1557388355&utm_medium=web",
    "picture": "https://api.deezer.com/artist/27/image",
    "picture_small": "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
    "picture_medium": "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
    "picture_big": "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
    "picture_xl": "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
    "radio": true,
    "tracklist": "https://api.deezer.com/artist/27/top?limit=50",
    "type": "artist"
  },
  "album": {
    "id": "302127",
    "title": "Discovery",
    "link": "https://www.deezer.com/album/302127",
    "cover": "https://api.deezer.com/album/302127/image",
    "cover_small": "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/56x56-000000-80-0-0.jpg",
    "cover_medium": "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/250x250-000000-80-0-0.jpg",
    "cover_big": "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/500x500-000000-80-0-0.jpg",
    "cover_xl": "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/1000x1000-000000-80-0-0.jpg",
    "release_date": "2001-03-07",
    "tracklist": "https://api.deezer.com/album/302127/tracks",
    "type": "album"
  },
  "type": "track"
}
*/

class RootRepo {

  import RootRepo._

  def getTrackById(id: Int): DeezerTrack = {
    val request: HttpRequest = Http(s"https://api.deezer.com/track/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerTrack](res.body)
    decodingResult match {
      case Right(track) => track
      case Left(error) => DeezerTrack(
        id = 3135556,
        readable = true,
        title = "Harder Better Faster Stronger",
        title_short = "Harder Better Faster Stronger",
        title_version = "",
        isrc = "GBDUW0000059",
        link = "https://www.deezer.com/track/3135556",
        share = "https://www.deezer.com/track/3135556?utm_source=deezer&utm_content=track-3135556&utm_term=0_1557388355&utm_medium=web",
        duration = 224,
        track_position = 4,
        disk_number = 1,
        rank = 728714,
        release_date = "2001-03-07",
        explicit_lyrics = false,
        explicit_content_lyrics = 0,
        explicit_content_cover = 0,
        preview = "https://cdns-preview-d.dzcdn.net/stream/c-deda7fa9316d9e9e880d2c6207e92260-5.mp3",
        bpm = 123.4,
        gain = -12.4,
        available_countries = List(
          "AE",
          "WS",
        ),
        contributors = List(
          DeezerTrackArtist(
            id = 27,
            name = "Daft Punk",
            link = "https://www.deezer.com/artist/27",
            share = "https://www.deezer.com/artist/27?utm_source=deezer&utm_content=artist-27&utm_term=0_1557388355&utm_medium=web",
            picture = "https://api.deezer.com/artist/27/image",
            picture_small = "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
            picture_medium = "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
            picture_big = "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
            picture_xl = "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
            radio = true,
            tracklist = "https://api.deezer.com/artist/27/top?limit=50",
          ),
        ),
        artist = DeezerTrackArtist(
          id = 27,
          name = "Daft Punk",
          link = "https://www.deezer.com/artist/27",
          share = "https://www.deezer.com/artist/27?utm_source=deezer&utm_content=artist-27&utm_term=0_1557388355&utm_medium=web",
          picture = "https://api.deezer.com/artist/27/image",
          picture_small = "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/56x56-000000-80-0-0.jpg",
          picture_medium = "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/250x250-000000-80-0-0.jpg",
          picture_big = "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/500x500-000000-80-0-0.jpg",
          picture_xl = "https://e-cdns-images.dzcdn.net/images/artist/f2bc007e9133c946ac3c3907ddc5d2ea/1000x1000-000000-80-0-0.jpg",
          radio = true,
          tracklist = "https://api.deezer.com/artist/27/top?limit=50",
        ),
        album = DeezerTrackAlbum(
          id = 302127,
          title = "Discovery",
          link = "https://www.deezer.com/album/302127",
          cover = "https://api.deezer.com/album/302127/image",
          cover_small = "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/56x56-000000-80-0-0.jpg",
          cover_medium = "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/250x250-000000-80-0-0.jpg",
          cover_big = "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/500x500-000000-80-0-0.jpg",
          cover_xl = "https://e-cdns-images.dzcdn.net/images/cover/2e018122cb56986277102d2041a592c8/1000x1000-000000-80-0-0.jpg",
          release_date = "2001-03-07",
        ),
      )
    }
  }
}

object RootRepo {
}
