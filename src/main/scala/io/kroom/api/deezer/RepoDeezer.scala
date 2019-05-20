package io.kroom.api.deezer

import io.circe.generic.auto._
import io.circe.parser
import scalaj.http.{Http, HttpRequest, HttpResponse}

case class data[T](data: T)

case class Id(id: Int)

case class DataListId(data: List[Id])

case class DataDeezerGenre(
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

case class DataDeezerAlbum(
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

case class DataDeezerArtist(
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

case class DataDeezerTrack(
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

case class DataDeezerSearch(
                             id: Int,
                             readable: Boolean,
                             title: String,
                             title_short: String,
                             title_version: String,
                             // url,
                             link: String,
                             duration: Int,
                             rank: Int,
                             explicit_lyrics: Boolean,
                             // url,
                             preview: String,
                             artist: Id,
                             album: Id,
                           )

object Order extends Enumeration {
  val RANKING, TRACK_ASC, TRACK_DESC, ARTIST_ASC, ARTIST_DESC, ALBUM_ASC, ALBUM_DESC, RATING_ASC, RATING_DESC, DURATION_ASC, DURATION_DESC = Value
}

object Connections extends Enumeration {
  val album, artist, history, playlist, podcast, radio, track, user = Value
}

// TODO LOG
// cached error ?
class RepoDeezer(val dbh: DBDeezer) {

  def getTrackById(id: Int): Option[DataDeezerTrack] = {
    dbh.getDeezerTrack(id) match {
      case Some(d) =>
        println(s"RepoDeezer: Track $id get from DB")
        return Some(d)
      case _ => ()
    }

    val urlEntry = s"https://api.deezer.com/track/$id"
    val request: HttpRequest = Http(urlEntry)
    val res: HttpResponse[String] = request.asString
    val decodingResult = parser.decode[DataDeezerTrack](res.body)

    decodingResult match {
      case Right(track) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        dbh.addDeezerTrack(track)
        Some(track)
      case Left(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        None
    }
  }

  def getArtistById(id: Int): Option[DataDeezerArtist] = {
    dbh.getDeezerArtist(id) match {
      case Some(d) =>
        println(s"RepoDeezer: Artist $id get from DB")
        return Some(d)
      case _ => ()
    }

    val urlEntry = s"https://api.deezer.com/artist/$id"
    val request: HttpRequest = Http(urlEntry)
    val res: HttpResponse[String] = request.asString
    val decodingResult = parser.decode[DataDeezerArtist](res.body)

    decodingResult match {
      case Right(artist) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        dbh.addDeezerArtist(artist)
        Some(artist)
      case Left(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        None
    }
  }

  def getAlbumById(id: Int): Option[DataDeezerAlbum] = {
    dbh.getDeezerAlbum(id) match {
      case Some(d) =>
        println(s"RepoDeezer: Album $id get from DB")
        return Some(d)
      case _ => ()
    }

    val urlEntry = s"https://api.deezer.com/album/$id"
    val request: HttpRequest = Http(urlEntry)
    val res: HttpResponse[String] = request.asString
    val decodingResult = parser.decode[DataDeezerAlbum](res.body)

    decodingResult match {
      case Right(album) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        dbh.addDeezerAlbum(album)
        Some(album)
      case Left(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        None
    }
  }

  def getGenreById(id: Int): Option[DataDeezerGenre] = {
    dbh.getDeezerGenre(id) match {
      case Some(d) =>
        println(s"RepoDeezer: Genre $id get from DB")
        return Some(d)
      case _ => ()
    }

    val urlEntry = s"https://api.deezer.com/genre/$id"
    val request: HttpRequest = Http(urlEntry)
    val res: HttpResponse[String] = request.asString
    val decodingResult = parser.decode[DataDeezerGenre](res.body)

    decodingResult match {
      case Right(genre) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        dbh.addDeezerGenre(genre)
        Some(genre)
      case Left(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        None
    }
  }

  def getSearch(search: String,
                connections: Option[Connections.Value],
                strict: Boolean,
                order: Option[Order.Value]
               ): List[DataDeezerSearch] = {
    val urlEntry = s"https://api.deezer.com/search${
      connections.map(e => "/" + e).getOrElse("")
    }?q=$search${
      if (strict) {
        "?strict=on"
      } else {
        ""
      }
    }${
      order.map(e => "&order=" + e.toString).getOrElse("")
    }"

    val request: HttpRequest = Http(urlEntry)
    val res: HttpResponse[String] = request.asString
    val decodingResult = parser.decode[data[List[DataDeezerSearch]]](res.body)

    decodingResult match {
      case Right(resJson) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        resJson.data
      case Left(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        List[DataDeezerSearch]()
    }
  }

}
