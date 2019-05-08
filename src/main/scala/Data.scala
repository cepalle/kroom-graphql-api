
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
                         radio: Boolean,
                         // url,
                         tracklist: String,
                         role: Option[String],
                       )

case class DeezerTrack(
                        id: Int,
                        readable: Boolean,
                        title: String,
                        title_short: String,
                        title_version: String,
                        unseen: Boolean,
                        isrc: String,
                        // url,
                        link: String,
                        // url,
                        share: String,
                        duration: Int,
                        track_position: Int,
                        disck_number: Int,
                        rank: Int,
                        // Date,
                        release_date: String,
                        explicit_lyrics: Boolean,
                        explicit_content_lyrices: Int,
                        explicit_content_cover: Int,
                        // url,
                        preview: String,
                        bpm: Float,
                        gain: Float,
                        available_countries: Array[String],
                        alternative: DeezerTrack,
                        contributors: Array[DeezerArtist],
                        artist: DeezerArtist,
                        album: DeezerAlbum,
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
                        cover_smal: String,
                        // url,
                        cover_medium: String,
                        // url,
                        cover_big: String,
                        // url,
                        cover_xl: String,
                        genre_id: Int,
                        genres: Array[DeezerGenre],
                        label: String,
                        nb_tracks: Int,
                        duration: Int,
                        fans: Int,
                        rating: Int,
                        // date,
                        release_date: String,
                        record_type: String,
                        available: Boolean,
                        alternative: DeezerAlbum,
                        // url,
                        tracklist: String,
                        explicit_lyrics: Boolean,
                        // TODO enum,
                        explicit_content_lyrics: Int,
                        explicit_content_cover: Int,
                        contributors: Array[DeezerArtist],
                        artist: DeezerArtist,
                        tracks: Array[DeezerTrack],
                      )

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
                       explicit_content_levels_available: Array[String],
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

// --

object Episode extends Enumeration {
  val NEWHOPE, EMPIRE, JEDI = Value
}

trait Character {
  def id: String

  def name: Option[String]

  def friends: List[String]

  def appearsIn: List[Episode.Value]
}

case class Human(
                  id: String,
                  name: Option[String],
                  friends: List[String],
                  appearsIn: List[Episode.Value],
                  homePlanet: Option[String]) extends Character

case class Droid(
                  id: String,
                  name: Option[String],
                  friends: List[String],
                  appearsIn: List[Episode.Value],
                  primaryFunction: Option[String]) extends Character

class CharacterRepo {

  import CharacterRepo._

  def getHero(episode: Option[Episode.Value]) =
    episode flatMap (_ ⇒ getHuman("1000")) getOrElse droids.last

  def getHuman(id: String): Option[Human] = humans.find(c ⇒ c.id == id)

  def getDroid(id: String): Option[Droid] = droids.find(c ⇒ c.id == id)

  def getHumans(limit: Int, offset: Int): List[Human] = humans.drop(offset).take(limit)

  def getDroids(limit: Int, offset: Int): List[Droid] = droids.drop(offset).take(limit)
}

object CharacterRepo {
  val humans = List(
    Human(
      id = "1000",
      name = Some("Luke Skywalker"),
      friends = List("1002", "1003", "2000", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Tatooine")),
    Human(
      id = "1001",
      name = Some("Darth Vader"),
      friends = List("1004"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Tatooine")),
    Human(
      id = "1002",
      name = Some("Han Solo"),
      friends = List("1000", "1003", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = None),
    Human(
      id = "1003",
      name = Some("Leia Organa"),
      friends = List("1000", "1002", "2000", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = Some("Alderaan")),
    Human(
      id = "1004",
      name = Some("Wilhuff Tarkin"),
      friends = List("1001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      homePlanet = None)
  )

  val droids = List(
    Droid(
      id = "2000",
      name = Some("C-3PO"),
      friends = List("1000", "1002", "1003", "2001"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      primaryFunction = Some("Protocol")),
    Droid(
      id = "2001",
      name = Some("R2-D2"),
      friends = List("1000", "1002", "1003"),
      appearsIn = List(Episode.NEWHOPE, Episode.EMPIRE, Episode.JEDI),
      primaryFunction = Some("Astromech"))
  )
}
