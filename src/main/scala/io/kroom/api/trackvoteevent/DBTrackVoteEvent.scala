package io.kroom.api.trackvoteevent

import io.kroom.api.deezer.DBDeezer
import io.kroom.api.user.{DBUser, DataUser}
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
      ((u, j), e) <- tabUser join joinTrackVoteEventUser on
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

  def getTrackWithVote(eventId: Int): List[DataTrackWithVote] = {
    // One query ?
    val qVoteTrue = (for {
      (e, jv) <- tabTrackVoteEvent join joinTrackVoteEventUserVoteTrack on (_.id === _.idTrackVoteEvent)
      if e.id === eventId
    } yield (e, jv))
      .filter(_._2.voteUp === true)
      .groupBy(_._2.idDeezerTrack)
      .map({
        case (idDeezerTrack, css) => (idDeezerTrack, css.length)
      })

    val qVoteAll = (for {
      (e, jv) <- tabTrackVoteEvent join joinTrackVoteEventUserVoteTrack on (_.id === _.idTrackVoteEvent)
      if e.id === eventId
    } yield (e, jv))
      .groupBy(_._2.idDeezerTrack)
      .map({
        case (idDeezerTrack, css) => (idDeezerTrack, css.length)
      })

    val f = db.run(qVoteTrue.result zip qVoteAll.result)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(d => {
        val (voteTrue, allVote) = d

        allVote.map(v => {
          val (id, nbTot) = v
          val nbUp = voteTrue.find(_._1 == id).map(_._1).getOrElse(0)
          DataTrackWithVote(id, 2 * nbUp - nbTot, nbUp, nbTot - nbUp)
        })
      })
      .map(_.toList)
      .getOrElse(List[DataTrackWithVote]())
  }

  def getUserInvited(eventId: Int): List[DataUser] = {
    val query = for {
      ((e, ju), u) <- tabTrackVoteEvent join joinTrackVoteEventUser on (_.id === _.idTrackVoteEvent) join DBUser.tabUser on (_._2.idUser === _.id)
      if e.id === eventId
    } yield u

    val f = db.run(query.result)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .map(_.map(DBUser.tabToObjUser))
      .map(_.toList)
      .getOrElse(List[DataUser]())
  }

  // Mutation

  def `new`(userIdMaster: Int,
            name: String,
            public: Boolean,
              ): Option[DataTrackVoteEvent] = {
    val query = (tabTrackVoteEvent
      .map(e => (e.userMasterId, e.name, public))
      returning tabTrackVoteEvent.map(_.id)
      ) += (userIdMaster, name, public)

    val f = db.run(query)

    Await.ready(f, Duration.Inf).value
      .flatMap(_.toOption)
      .flatMap(id => getTrackVoteEventById(id))
  }

  def update(eventId: Int,
             userIdMaster: Int,
             name: String,
             public: Boolean,
             schedule: Option[String],
             location: Option[String]
            ): Option[DataTrackVoteEvent] = {
    Await.ready(db.run(
      tabTrackVoteEvent
        .filter(_.id === eventId)
        .map(e => (e.userMasterId, e.name, e.public, e.schedule, e.location))
        .update((userIdMaster, name, public, schedule, location))
    ), Duration.Inf)

    getTrackVoteEventById(eventId)
  }

  def addUser(eventId: Int, userId: Int): Option[DataTrackVoteEvent] = {
    val query = joinTrackVoteEventUser
      .map(e => (e.idTrackVoteEvent, e.idUser)) += (eventId, userId)
    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getTrackVoteEventById(eventId)
  }

  def delUser(eventId: Int, userId: Int): Option[DataTrackVoteEvent] = {
    val query = joinTrackVoteEventUser
      .filter(e => e.idTrackVoteEvent === eventId && e.idUser === userId).delete
    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getTrackVoteEventById(eventId)
  }

  def addVote(eventId: Int, userId: Int, musicId: Int, up: Boolean): Option[DataTrackVoteEvent] = {
    // TODO Check is music fetch
    val query = joinTrackVoteEventUserVoteTrack
      .map(e => (e.idTrackVoteEvent, e.idUser, e.idDeezerTrack)) += (eventId, userId, musicId)
    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getTrackVoteEventById(eventId)
  }

  def delVote(eventId: Int, userId: Int, musicId: Int): Option[DataTrackVoteEvent] = {
    val query = joinTrackVoteEventUserVoteTrack
      .filter(e => e.idTrackVoteEvent === eventId && e.idUser === userId && e.idDeezerTrack === musicId)
      .delete
    val f = db.run(query)
    Await.ready(f, Duration.Inf)

    getTrackVoteEventById(eventId)
  }

}

object DBTrackVoteEvent {

  class TabTrackVoteEvent(tag: Tag)
    extends Table[(Int, Int, String, Boolean, Option[Int], Option[String], Option[String])](tag, "TRACK_VOTE_EVENT") {

    def id = column[Int]("ID", O.PrimaryKey, O.AutoInc, O.Default(0))

    def userMasterId = column[Int]("USER_MASTER_ID")

    def name = column[String]("NAME")

    def public = column[Boolean]("PUBLIC")

    def currentTrackId = column[Option[Int]]("CURRENT_TRACK_ID")

    def schedule = column[Option[String]]("SCHEDULE")

    def location = column[Option[String]]("LOCATION")

    def * = (id, userMasterId, name, public, currentTrackId, schedule, location)
  }

  val tabTrackVoteEvent = TableQuery[TabTrackVoteEvent]

  val tabToObjTrackVoteEvent: ((Int, Int, String, Boolean, Option[Int], Option[String], Option[String])) => DataTrackVoteEvent = {
    case (id, userMasterId, name, public, currentTrackId, schedule, location) =>
      DataTrackVoteEvent(
        id, userMasterId, name, public, currentTrackId, schedule, location
      )
  }

  class JoinTrackVoteEventUser(tag: Tag)
    extends Table[(Int, Int)](tag, "JOIN_TRACK_VOTE_EVENT_USER_INVITED") {

    def idTrackVoteEvent = column[Int]("ID_TRACK_VOTE_EVENT")

    def idUser = column[Int]("ID_USER")

    def * = (idTrackVoteEvent, idUser)

    def pk = primaryKey("PK_JOIN_TRACK_VOTE_EVENT_USER_INVITED", (idTrackVoteEvent, idUser))

    def trackVoteEvent =
      foreignKey("FK_JOIN_TRACK_VOTE_EVENT_EVENT_USER_TRACK_VOTE_EVENT", idTrackVoteEvent, tabTrackVoteEvent)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def user =
      foreignKey("FK_JOIN_TRACK_VOTE_EVENT_EVENT_USER_USER", idUser, DBUser.tabUser)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  val joinTrackVoteEventUser = TableQuery[JoinTrackVoteEventUser]

  class JoinTrackVoteEventUserVoteTrack(tag: Tag)
    extends Table[(Int, Int, Int, Boolean)](tag, "JOIN_TRACK_VOTE_EVENT_USER_VOTE_TRACK") {

    def idTrackVoteEvent = column[Int]("ID_TRACK_VOTE_EVENT")

    def idUser = column[Int]("ID_USER")

    def idDeezerTrack = column[Int]("ID_DEEZER_TRACK")

    def voteUp = column[Boolean]("VOTE_UP")

    def * = (idTrackVoteEvent, idUser, idDeezerTrack, voteUp)

    def pk = primaryKey("PK_JOIN_TRACK_VOTE_EVENT_USER_VOTE_TRACK", (idTrackVoteEvent, idUser, idDeezerTrack))

    def deezerTrack =
      foreignKey("FK_JOIN_TRACK_VOTE_EVENT_USER_VOTE_TRACK_DEEZER_TRACK", idDeezerTrack, DBDeezer.tabDeezerTrack)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def trackVoteEvent =
      foreignKey("FK_JOIN_TRACK_VOTE_EVENT_USER_VOTE_TRACK_TRACK_VOTE_EVENT", idTrackVoteEvent, tabTrackVoteEvent)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

    def user =
      foreignKey("FK_JOIN_TRACK_VOTE_EVENT_USER_VOTE_TRACK_USER", idUser, DBUser.tabUser)(_.id, onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)

  }

  val joinTrackVoteEventUserVoteTrack = TableQuery[JoinTrackVoteEventUserVoteTrack]

}
