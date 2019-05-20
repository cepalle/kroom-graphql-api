package io.kroom.api.trackVoteEvent

import io.kroom.api.user.DBUser
import slick.jdbc.H2Profile
import slick.jdbc.H2Profile.api._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class DBTrackVoteEvent(private val db: H2Profile.backend.Database) {

  import DBTrackVoteEvent._
  import DBUser._

  def getTrackVoteEventById(id: Int): Option[DataTrackVoteEvent] = {
    val query = tabTrackVoteEvent.filter(_.id === id).result.head
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(tabToObjTrackVoteEvent)
  }

  def getTrackVoteEventPublic: List[DataTrackVoteEvent] = {
    val query = tabTrackVoteEvent.filter(_.public).result
    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(_.map(tabToObjTrackVoteEvent))
      .map(_.toList)
      .getOrElse(List[DataTrackVoteEvent]())
  }

  def getTrackVoteEventByUserId(userId: Int): List[DataTrackVoteEvent] = {
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
      .getOrElse(List[DataTrackVoteEvent]())
  }

}

object DBTrackVoteEvent {

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

  def tabToObjTrackVoteEvent(t: (Int, String, Boolean, Int, String, String)): DataTrackVoteEvent = {
    val (id, name, public, currentTrackId, horaire, location) = t
    DataTrackVoteEvent(
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

