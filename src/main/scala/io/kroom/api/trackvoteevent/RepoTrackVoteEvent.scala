package io.kroom.api.trackvoteevent

import io.kroom.api.deezer.RepoDeezer
import io.kroom.api.user.{DataUser, RepoUser}

import scala.util.{Failure, Success, Try}

case class DataTrackWithVote(
                              trackId: Int,
                              score: Int,
                              nb_vote_up: Int,
                              nb_vote_down: Int,
                            )

case class DataTrackVoteEvent(
                               id: Int,
                               userMasterId: Int,
                               name: String,
                               public: Boolean,
                               currentTrackId: Option[Int],
                               schedule: Option[String],
                               location: Option[String]
                             )

class RepoTrackVoteEvent(private val dbh: DBTrackVoteEvent, private val repoDeezer: RepoDeezer, private val repoUser: RepoUser) {

  def getById(id: Int): Try[DataTrackVoteEvent] = {
    dbh.getById(id)
  }

  def getByName(name: String): Try[DataTrackVoteEvent] = {
    dbh.getByName(name)
  }

  def getPublic: Try[List[DataTrackVoteEvent]] = {
    dbh.getPublic
  }

  def getByUserId(userId: Int): Try[List[DataTrackVoteEvent]] = {
    dbh.getByUserId(userId)
  }

  def getTrackWithVote(eventId: Int): Try[List[DataTrackWithVote]] = {
    dbh.getTrackWithVote(eventId)
  }

  def getUserInvited(eventId: Int): Try[List[DataUser]] = {
    dbh.getUserInvited(eventId)
  }

  // Mutation

  def `new`(userIdMaster: Int,
            name: String,
            public: Boolean,
           ): Try[DataTrackVoteEvent] = {
    val trackEvent = dbh.add(userIdMaster, name, public).get
    dbh.addUser(trackEvent.id, userIdMaster)
  }

  def update(eventId: Int,
             userIdMaster: Int,
             name: String,
             public: Boolean,
             schedule: Option[String],
             location: Option[String]
            ): Try[DataTrackVoteEvent] = {
    val track = dbh.getById(eventId).get

    if (track.userMasterId != userIdMaster) {
      dbh.addUser(eventId, userIdMaster)
    }
    dbh.update(
      eventId,
      userIdMaster,
      name,
      public,
      schedule,
      location
    )
  }

  def addUser(eventId: Int, userId: Int): Try[DataTrackVoteEvent] = {
    dbh.addUser(eventId, userId)
  }

  def delUser(eventId: Int, userId: Int): Try[DataTrackVoteEvent] = {
    /*
    val userTry = repoUser.getById(userId) match {
      case Success(_) => Success(Unit)
      case Failure(_) => Failure(SimpleException("userIdMaster not found"))
    }
    val eventIdTry = dbh.getById(eventId) match {
      case Success(_) => Success(Unit)
      case Failure(_) => Failure(SimpleException("eventId not found"))
    }

    val lCheck = List(userTry, eventIdTry) collect { case Failure(e) => e }

    if (lCheck.nonEmpty) {
      return Failure(MultipleException(lCheck))
    }
    */

    dbh.delUser(eventId, userId)
  }

  def addVote(eventId: Int, userId: Int, musicId: Int, up: Boolean): Try[DataTrackVoteEvent] = {
    /*
    val musicTry = repoDeezer.getTrackById(musicId) match {
      case Success(_) => Success(Unit)
      case Failure(_) => Failure(SimpleException("musicId not found"))
    }
    val userTry = repoUser.getById(userId) match {
      case Success(_) => Success(Unit)
      case Failure(_) => Failure(SimpleException("userIdMaster not found"))
    }
    val eventIdTry = dbh.getById(eventId) match {
      case Success(_) => Success(Unit)
      case Failure(_) => Failure(SimpleException("eventId not found"))
    }

    val lCheck = List(userTry, eventIdTry, musicTry) collect { case Failure(e) => e }

    if (lCheck.nonEmpty) {
      return Failure(MultipleException(lCheck))
    }
    */

    dbh.addVote(eventId, userId, musicId, up)
  }

  def delVote(eventId: Int, userId: Int, musicId: Int): Try[DataTrackVoteEvent] = {
    /*
    val musicTry = repoDeezer.getTrackById(musicId) match {
      case Success(_) => Success(Unit)
      case Failure(_) => Failure(SimpleException("musicId not found"))
    }
    val userTry = repoUser.getById(userId) match {
      case Success(_) => Success(Unit)
      case Failure(_) => Failure(SimpleException("userIdMaster not found"))
    }
    val eventIdTry = dbh.getById(eventId) match {
      case Success(_) => Success(Unit)
      case Failure(_) => Failure(SimpleException("eventId not found"))
    }

    val lCheck = List(userTry, eventIdTry, musicTry) collect { case Failure(e) => e }

    if (lCheck.nonEmpty) {
      return Failure(MultipleException(lCheck))
    }
    */

    dbh.delVote(eventId, userId, musicId)
  }

}
