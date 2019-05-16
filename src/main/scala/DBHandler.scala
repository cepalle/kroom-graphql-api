import io.circe.parser
import io.circe.generic.auto._
import io.circe.syntax._
import slick.jdbc.H2Profile.api._
import slick.lifted.QueryBase

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}


class DBHandler {

  import DBHandler._

  // -- DEEZER

  def getDeezerGenre(id: Int): Option[DeezerGenre] = {
    val query = tabDeezerGenre.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerGenre)
  }

  def addDeezerGenre(dg: DeezerGenre): Boolean = {
    val f = db.run(tabDeezerGenre += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  def getDeezerAlbum(id: Int): Option[DeezerAlbum] = {
    val query = tabDeezerAlbum.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerAlbum)
  }

  def addDeezerAlbum(dg: DeezerAlbum): Boolean = {
    val f = db.run(tabDeezerAlbum += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  def getDeezerArtist(id: Int): Option[DeezerArtist] = {
    val query = tabDeezerArtist.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerArtist)
  }

  def addDeezerArtist(dg: DeezerArtist): Boolean = {
    val f = db.run(tabDeezerArtist += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  def getDeezerTrack(id: Int): Option[DeezerTrack] = {
    val query = tabDeezerTrack.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjDeezerTrack)
  }

  def addDeezerTrack(dg: DeezerTrack): Boolean = {
    val f = db.run(tabDeezerTrack += ((dg.id, dg.asJson.toString())))
    Await.ready(f, Duration.Inf)
    true
  }

  // -- USER

  // -- TRACK VOTE EVENT

  def getTrackVoteEventById(id: Int): Option[TrackVoteEvent] = {
    val query = tabTrackVoteEvent.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjTrackVoteEvent)
  }

  def getTrackVoteEventPublic(): List[TrackVoteEvent] = {
    val query = tabTrackVoteEvent.filter(_.public).result
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(_.map(tabToObjTrackVoteEvent))
      .map(_.toList)
      .getOrElse(List[TrackVoteEvent]())
  }

  def getTrackVoteEventByUserId(userId: Int): List[TrackVoteEvent] = {
    val query = for {
      ((u, j), e) <- tabUser join joinTrackVoteEventUserInvited on
        (_.id === _.idUser) join tabTrackVoteEvent on (_._2.idTrackVoteEvent === _.id)
      if u.id === userId
    } yield e
    val f = db.run(query.result)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(_.map(tabToObjTrackVoteEvent))
      .map(_.toList)
      .getOrElse(List[TrackVoteEvent]())
  }


}

// foreign key
object DBHandler {
  private val db = Database.forConfig("h2mem1")

  // -- DEEZER

  class TabDeezerGenre(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_GENRE") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerGenre = TableQuery[TabDeezerGenre]

  def tabToObjDeezerGenre(t: (Int, String)): DeezerGenre = {
    parser.decode[DeezerGenre](t._2).toOption match {
      case Some(res) => res
      case _ => throw new IllegalArgumentException("TabDeezerGenre: json in db is invalid")
    }
  }

  class TabDeezerAlbum(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ALBUM") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerAlbum = TableQuery[TabDeezerAlbum]

  def tabToObjDeezerAlbum(t: (Int, String)): DeezerAlbum = {
    parser.decode[DeezerAlbum](t._2).toOption match {
      case Some(res) => res
      case _ => throw new IllegalArgumentException("TabDeezerAlbum: json in db is invalid")
    }
  }

  class TabDeezerArtist(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_ARTIST") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerArtist = TableQuery[TabDeezerArtist]

  def tabToObjDeezerArtist(t: (Int, String)): DeezerArtist = {
    parser.decode[DeezerArtist](t._2).toOption match {
      case Some(res) => res
      case _ => throw new IllegalArgumentException("TabDeezerArtist: json in db is invalid")
    }
  }

  class TabDeezerTrack(tag: Tag) extends Table[(Int, String)](tag, "DEEZER_TRACK") {

    def id = column[Int]("ID", O.PrimaryKey)

    def json = column[String]("JSON")

    def * = (id, json)
  }

  val tabDeezerTrack = TableQuery[TabDeezerTrack]

  def tabToObjDeezerTrack(t: (Int, String)): DeezerTrack = {
    parser.decode[DeezerTrack](t._2).toOption match {
      case Some(res) => res
      case _ => throw new IllegalArgumentException("TabDeezerTrack: json in db is invalid")
    }
  }

  // -- USER

  class TabUser(tag: Tag) extends Table[(Int, String, String, String)](tag, "USER") {

    def id = column[Int]("ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def email = column[String]("EMAIL")

    def location = column[String]("LOCATION")

    def * = (id, name, email, location)
  }

  val tabUser = TableQuery[TabUser]

  class JoinFriend(tag: Tag) extends Table[(Int, Int, Int)](tag, "JOIN_FRIEND") {

    def id = column[Int]("ID", O.PrimaryKey)

    def idUser1 = column[Int]("ID_USER_1")

    def idUser2 = column[Int]("ID_USER_2")

    def * = (id, idUser1, idUser2)
  }

  val joinFriend = TableQuery[JoinFriend]

  class JoinMusicalPreferences(tag: Tag) extends Table[(Int, Int, Int)](tag, "JOIN_MUSICAL_PREFERENCES") {

    def id = column[Int]("ID", O.PrimaryKey)

    def idUser = column[Int]("ID_USER")

    def idDeezerTrack = column[Int]("ID_DEEZER_TRACK")

    def * = (id, idUser, idDeezerTrack)
  }

  val joinMusicalPreferences = TableQuery[JoinMusicalPreferences]

  // -- TRACK VOTE EVENT

  class TabTrackVoteEvent(tag: Tag)
    extends Table[(Int, String, Boolean, Int, String, String)](tag, "TRACK_VOTE_EVENT") {

    def id = column[Int]("ID", O.PrimaryKey)

    def name = column[String]("NAME")

    def public = column[Boolean]("PUBLIC")

    def currentTrackId = column[Int]("CURRENT_TRACK_ID")

    def horaire = column[String]("HORAIRE")

    def location = column[String]("LOCATION")

    def * = (id, name, public, currentTrackId, horaire, location)
  }

  val tabTrackVoteEvent = TableQuery[TabTrackVoteEvent]

  def tabToObjTrackVoteEvent(t: (Int, String, Boolean, Int, String, String)): TrackVoteEvent = {
    val (id, name, public, currentTrackId, horaire, location) = t
    TrackVoteEvent(
      id, name, public, currentTrackId, horaire, location
    )
  }

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

        tabUser.schema ++
        joinFriend.schema ++
        joinMusicalPreferences.schema ++

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
