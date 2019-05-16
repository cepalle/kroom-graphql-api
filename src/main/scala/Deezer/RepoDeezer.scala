package Deezer

import Root.DBRoot
import io.circe.generic.auto._
import io.circe.parser
import scalaj.http.{Http, HttpRequest, HttpResponse}

case class Id(id: Int)

case class DataListId(data: List[Id])

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
                        // genres: Deezer.DataListId,
                        label: String,
                        nb_tracks: Int,
                        duration: Int,
                        fans: Int,
                        rating: Int,
                        // date
                        release_date: String,
                        record_type: String,
                        available: Boolean,
                        // alternative: Deezer.DeezerAlbum,
                        // url
                        tracklist: String,
                        explicit_lyrics: Boolean,
                        //0:Not Explicit; 1:Explicit; 2:Unknown; 3:Edited; 4:Partially Explicit (Album "lyrics" only); 5:Partially Unknown (Album "lyrics" only); 6:No Advice Available; 7:Partially No Advice Available (Album "lyrics" only)
                        explicit_content_lyrics: Int,
                        //0:Not Explicit; 1:Explicit; 2:Unknown; 3:Edited; 4:Partially Explicit (Album "lyrics" only); 5:Partially Unknown (Album "lyrics" only); 6:No Advice Available; 7:Partially No Advice Available (Album "lyrics" only)
                        explicit_content_cover: Int,
                        contributors: List[Id],
                        artist: Id,
                        tracks: DataListId,
                      )

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

// TODO LOG
// cached error ?
class RepoDeezer(val dbh: DBRoot) {

  def getTrackById(id: Int): Option[DeezerTrack] = {
    dbh.getDeezerTrack(id) match {
      case Some(d) => {
        println("Deezer.RepoDeezer: Track get from cached")
        return Some(d)
      }
      case _ => ()
    }

    val request: HttpRequest = Http(s"https://api.deezer.com/track/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerTrack](res.body)
    decodingResult match {
      case Right(track) => {
        println("Deezer.RepoDeezer: Track get from Deezer API")
        dbh.addDeezerTrack(track)
        Some(track)
      }
      case Left(error) =>
        print("Deezer.RepoDeezer get track: ")
        println(error)
        None
    }
  }

  def getArtistById(id: Int): Option[DeezerArtist] = {
    dbh.getDeezerArtist(id) match {
      case Some(d) => {
        println("Deezer.RepoDeezer: Artist get from cached")
        return Some(d)
      }
      case _ => ()
    }

    val request: HttpRequest = Http(s"https://api.deezer.com/artist/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerArtist](res.body)
    decodingResult match {
      case Right(artist) => {
        println("Deezer.RepoDeezer: Artist get from Deezer API")
        dbh.addDeezerArtist(artist)
        Some(artist)
      }
      case Left(error) =>
        print("Deezer.RepoDeezer get artist: ")
        println(error)
        None
    }
  }

  def getAlbumById(id: Int): Option[DeezerAlbum] = {
    dbh.getDeezerAlbum(id) match {
      case Some(d) => {
        println("Deezer.RepoDeezer: Album get from cached")
        return Some(d)
      }
      case _ => ()
    }

    val request: HttpRequest = Http(s"https://api.deezer.com/album/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerAlbum](res.body)
    decodingResult match {
      case Right(album) => {
        println("Deezer.RepoDeezer: Album get from Deezer API")
        dbh.addDeezerAlbum(album)
        Some(album)
      }
      case Left(error) =>
        print("Deezer.RepoDeezer get album : ")
        println(error)
        None
    }
  }

  def getGenreById(id: Int): Option[DeezerGenre] = {
    dbh.getDeezerGenre(id) match {
      case Some(d) => {
        println("Deezer.RepoDeezer: Genre get from cached")
        return Some(d)
      }
      case _ => ()
    }

    val request: HttpRequest = Http(s"https://api.deezer.com/genre/$id")
    val res: HttpResponse[String] = request.asString

    val decodingResult = parser.decode[DeezerGenre](res.body)
    decodingResult match {
      case Right(genre) => {
        println("Deezer.RepoDeezer: Genre get from Deezer API")
        dbh.addDeezerGenre(genre)
        Some(genre)
      }
      case Left(error) =>
        print("Deezer.RepoDeezer get genre : ")
        println(error)
        None
    }
  }

}
