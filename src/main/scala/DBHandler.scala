import io.circe.parser
import io.circe.generic.auto._
import io.circe.syntax._
import slick.jdbc.H2Profile.api._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}


class DBHandler {

  import DBHandler._

  def getDeezerGenre(id: Int): Option[DeezerGenre] = {
    val query = tabDeezerGenre.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value.flatMap(_.toOption).flatMap(e => {
      val (id, json) = e
      parser.decode[DeezerGenre](json).toOption
    })
  }

  def addDeezerGenre(dg: DeezerGenre): Boolean = {
    val f = db.run(tabDeezerGenre += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    false
  }

  def getDeezerAlbum(id: Int): Option[DeezerAlbum] = {
    val query = tabDeezerAlbum.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value.flatMap(_.toOption).flatMap(e => {
      val (id, json) = e
      parser.decode[DeezerAlbum](json).toOption
    })
  }

  def addDeezerAlbum(dg: DeezerAlbum): Boolean = {
    val f = db.run(tabDeezerAlbum += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    false
  }

  def getDeezerArtist(id: Int): Option[DeezerArtist] = {
    val query = tabDeezerArtist.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value.flatMap(_.toOption).flatMap(e => {
      val (id, json) = e
      parser.decode[DeezerArtist](json).toOption
    })
  }

  def addDeezerArtist(dg: DeezerArtist): Boolean = {
    val f = db.run(tabDeezerArtist += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    false
  }

  def getDeezerTrack(id: Int): Option[DeezerTrack] = {
    val query = tabDeezerTrack.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value.flatMap(_.toOption).flatMap(e => {
      val (id, json) = e
      parser.decode[DeezerTrack](json).toOption
    })
  }

  def addDeezerTrack(dg: DeezerTrack): Boolean = {
    val f = db.run(tabDeezerTrack += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    false
  }

}

// foreign key
object DBHandler {
  private val db = Database.forConfig("h2mem1")

  // DEEZER

  class TabDeezerGenre(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_GENRE") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerGenre = TableQuery[TabDeezerGenre]

  class TabDeezerAlbum(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ALBUM") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerAlbum = TableQuery[TabDeezerAlbum]

  class TabDeezerArtist(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ARTIST") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerArtist = TableQuery[TabDeezerArtist]

  class TabDeezerTrack(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_TRACK") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerTrack = TableQuery[TabDeezerTrack]

  // -- TRACK VOTE EVENT

  class TabTrackVoteEvent(tag: Tag)
    extends Table[(Int, String, Boolean, Int, Int, String, String, String)](tag, "TRACK_VOTE_EVENT") {

    def id = column[Int]("ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def public = column[Boolean]("PUBLIC")

    def currentTrackId = column[Int]("CURRENT_TRACK_ID")

    def currentVotesId = column[Int]("CURRENT_VOTES_ID")

    def horaire = column[String]("HORAIRE")

    def location = column[String]("LOCATION")

    def usersInvitedId = column[String]("USERS_INVITED_ID")

    def * = (id, name, public, currentTrackId, currentVotesId, horaire, location, usersInvitedId)
  }

  val tabTrackVoteEvent = TableQuery[TabTrackVoteEvent]

  class JoinTrackVoteEventUserInvited(tag: Tag)
    extends Table[(Int, Int, Int)](tag, "JOIN_TRACK_VOTE_EVENT_USER_INVITED") {

    def id = column[Int]("ID", O.PrimaryKey)

    def idTrackVoteEvent = column[Int]("ID_TRACK_VOTE_EVENT")

    def idUser = column[Int]("ID_USER")

    def * = (id, idTrackVoteEvent, idUser)
  }

  val joinTrackVoteEventUserInvited = TableQuery[JoinTrackVoteEventUserInvited]

  class JoinTrackVoteEventUserVoteTrack(tag: Tag)
    extends Table[(Int, Int, Int, Int, Boolean)](tag, "JOIN_TRACK_VOTE_EVENT_USER_VOTE_TRACK") {

    def id = column[Int]("ID", O.PrimaryKey)

    def idTrackVoteEvent = column[Int]("ID_TRACK_VOTE_EVENT")

    def idUser = column[Int]("ID_USER")

    def idDeezerTrack = column[Int]("ID_DEEZER_TRACK")

    def voteUp = column[Boolean]("VOTE_UP")

    def * = (id, idTrackVoteEvent, idUser, idDeezerTrack, voteUp)
  }

  val joinTrackVoteEventUserVoteTrack = TableQuery[JoinTrackVoteEventUserVoteTrack]


  def init(): Unit = {
    val setup = DBIO.seq(
      (tabDeezerGenre.schema ++
        tabDeezerAlbum.schema ++
        tabDeezerArtist.schema ++
        tabDeezerTrack.schema ++

        tabTrackVoteEvent.schema ++
        joinTrackVoteEventUserInvited.schema ++
        joinTrackVoteEventUserVoteTrack.schema
        ).create,
    )

    val f = db.run(setup)

    val result = Await.ready(f, Duration.Inf).value.get

    println(result)
  }

}
