package DB

import Repo._
import io.circe.generic.auto._
import io.circe.parser
import io.circe.syntax._
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class DBRoot(val db: H2Profile.backend.Database) {

  import DBRoot._

  val getDeezerGenre = DBDeezer.getDeezerGenre(db, _)
  val addDeezerGenre = DBDeezer.addDeezerGenre(db, _)
  val getDeezerAlbum = DBDeezer.getDeezerAlbum(db, _)
  val addDeezerAlbum = DBDeezer.addDeezerAlbum(db, _)
  val getDeezerArtist = DBDeezer.getDeezerArtist(db, _)
  val addDeezerArtist = DBDeezer.addDeezerArtist(db, _)
  val getDeezerTrack = DBDeezer.getDeezerTrack(db, _)
  val addDeezerTrack = DBDeezer.addDeezerTrack(db, _)

  val getTrackVoteEventById = DBTrackVoteEvent.getTrackVoteEventById(db, _)
  val getTrackVoteEventPublic = () => DBTrackVoteEvent.getTrackVoteEventPublic(db)
  val getTrackVoteEventByUserId = DBTrackVoteEvent.getTrackVoteEventByUserId(db, _)

}

// foreign key
// ids ?
object DBRoot {

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

  object DBTrackVoteEvent {
    def getTrackVoteEventById(db: H2Profile.backend.Database, id: Int): Option[TrackVoteEvent] = {
      val query = tabTrackVoteEvent.filter(_.id === id).result.head
      val f = db.run(query)

      Await.ready(f, Duration.Inf).value
        .flatMap(_.toOption)
        .map(tabToObjTrackVoteEvent)
    }

    def getTrackVoteEventPublic(db: H2Profile.backend.Database): List[TrackVoteEvent] = {
      val query = tabTrackVoteEvent.filter(_.public).result
      val f = db.run(query)

      Await.ready(f, Duration.Inf).value
        .flatMap(_.toOption)
        .map(_.map(tabToObjTrackVoteEvent))
        .map(_.toList)
        .getOrElse(List[TrackVoteEvent]())
    }

    def getTrackVoteEventByUserId(db: H2Profile.backend.Database, userId: Int): List[TrackVoteEvent] = {
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

  }

  def init(db: H2Profile.backend.Database): Unit = {
    val setup = DBIO.seq(
      (DBDeezer.tabDeezerGenre.schema ++
        DBDeezer.tabDeezerAlbum.schema ++
        DBDeezer.tabDeezerArtist.schema ++
        DBDeezer.tabDeezerTrack.schema ++

        tabUser.schema ++
        joinFriend.schema ++
        joinMusicalPreferences.schema ++

        DBTrackVoteEvent.tabTrackVoteEvent.schema ++
        DBTrackVoteEvent.joinTrackVoteEventUserInvited.schema ++
        DBTrackVoteEvent.joinTrackVoteEventUserVoteTrack.schema
        ).create,
    )

    val f = db.run(setup)

    val result = Await.ready(f, Duration.Inf).value.get

    println(result)
  }

}

