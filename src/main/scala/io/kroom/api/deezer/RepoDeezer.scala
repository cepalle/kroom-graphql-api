package io.kroom.api.deezer

import io.circe.generic.auto._
import io.circe.parser
import scalaj.http.{Http, HttpRequest, HttpResponse}

import scala.util.{Failure, Success, Try}

case class Data[T](data: T)

case class Id(id: Int)

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
                            tracks: Data[List[Id]],
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
  val ranking, trackASC, trackDESC, artistASC, artistDESC, albumASC, albumDESC, ratingASC, ratingDESC, durationASC, durationDESC = Value
}

object Connections extends Enumeration {
  val album, artist, history, playlist, podcast, radio, track, user = Value
}

class RepoDeezer(val dbh: DBDeezer) {

  def getTrackById(id: Int): Try[DataDeezerTrack] = {
    dbh.getDeezerTrack(id).map(d => {
      println(s"RepoDeezer: Track $id get from DB")
      return Success(d)
    })

    val urlEntry = s"https://api.deezer.com/track/$id"
    val request: HttpRequest = Http(urlEntry)
    val res: HttpResponse[String] = request.asString
    val decodingResult = parser.decode[DataDeezerTrack](res.body).toTry

    decodingResult match {
      case Success(track) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        dbh.addDeezerTrack(track)
        Success(track)
      case Failure(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        Failure(error)
    }
  }

  def getArtistById(id: Int): Try[DataDeezerArtist] = {
    dbh.getDeezerArtist(id).map(d => {
      println(s"RepoDeezer: Artist $id get from DB")
      return Success(d)
    })

    val urlEntry = s"https://api.deezer.com/artist/$id"
    val request: HttpRequest = Http(urlEntry)
    val res: HttpResponse[String] = request.asString
    val decodingResult = parser.decode[DataDeezerArtist](res.body).toTry

    decodingResult match {
      case Success(artist) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        dbh.addDeezerArtist(artist)
        Success(artist)
      case Failure(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        Failure(error)
    }
  }

  def getAlbumById(id: Int): Try[DataDeezerAlbum] = {
    dbh.getDeezerAlbum(id).map(d => {
      println(s"RepoDeezer: Album $id get from DB")
      return Success(d)
    })

    val urlEntry = s"https://api.deezer.com/album/$id"
    val request: HttpRequest = Http(urlEntry)
    val res: HttpResponse[String] = request.asString
    val decodingResult = parser.decode[DataDeezerAlbum](res.body).toTry

    decodingResult match {
      case Success(album) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        dbh.addDeezerAlbum(album)
        Success(album)
      case Failure(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        Failure(error)
    }
  }

  def getGenreById(id: Int): Try[DataDeezerGenre] = {
    dbh.getDeezerGenre(id).map(d => {
      println(s"RepoDeezer: Genre $id get from DB")
      return Success(d)
    })

    val urlEntry = s"https://api.deezer.com/genre/$id"
    val request: HttpRequest = Http(urlEntry)
    val res: HttpResponse[String] = request.asString
    val decodingResult = parser.decode[DataDeezerGenre](res.body).toTry

    decodingResult match {
      case Success(genre) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        dbh.addDeezerGenre(genre)
        Success(genre)
      case Failure(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        Failure(error)
    }
  }

  def getSearch(search: String,
                connections: Option[Connections.Value],
                strict: Boolean,
                order: Option[Order.Value]
               ): Try[List[DataDeezerSearch]] = {
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
    val decodingResult = parser.decode[Data[List[DataDeezerSearch]]](res.body).toTry

    decodingResult match {
      case Success(resJson) =>
        println(s"RepoDeezer: Deezer API $urlEntry")
        Success(resJson.data)
      case Failure(error) =>
        println(s"RepoDeezer: Deezer API $urlEntry $error")
        println(res.body)
        Failure(error)
    }
  }

}
