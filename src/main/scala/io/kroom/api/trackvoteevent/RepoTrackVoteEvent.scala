package io.kroom.api.trackvoteevent

import akka.actor.ActorRef
import io.kroom.api.{SubQueryEnum, WSEventCSUpdateQuery}
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
                               locAndSchRestriction: Boolean,
                               currentTrackId: Option[Int],
                               scheduleBegin: Option[Long],
                               scheduleEnd: Option[Long],
                               latitude: Option[Double],
                               longitude: Option[Double]
                             )

class RepoTrackVoteEvent(
                          private val dbh: DBTrackVoteEvent,
                          private val repoDeezer: RepoDeezer,
                          private val repoUser: RepoUser,
                          private val subActor: ActorRef
                        ) {

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
    dbh.add(userIdMaster, name, public)
      .flatMap(trackEvent => dbh.addUser(trackEvent.id, userIdMaster))
  }

  def update(eventId: Int,
             userIdMaster: Int,
             name: String,
             public: Boolean,
             locAndSchRestriction: Boolean,
             scheduleBegin: Option[Long],
             scheduleEnd: Option[Long],
             latitude: Option[Double],
             longitude: Option[Double]
            ): Try[DataTrackVoteEvent] = {
    dbh.getById(eventId).flatMap(track => {
      if (track.userMasterId != userIdMaster) {
        dbh.addUser(eventId, userIdMaster)
      }
      val res = dbh.update(
        eventId,
        userIdMaster,
        name,
        public,
        locAndSchRestriction,
        scheduleBegin,
        scheduleEnd,
        latitude,
        longitude
      )
      subActor ! WSEventCSUpdateQuery(SubQueryEnum.TrackVoteEvent, eventId)
      res
    })
  }

  def addUser(eventId: Int, userId: Int): Try[DataTrackVoteEvent] = {
    val res = dbh.addUser(eventId, userId)
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.TrackVoteEvent, eventId)
    res
  }

  def delUser(eventId: Int, userId: Int): Try[DataTrackVoteEvent] = {
    val res = dbh.delUser(eventId, userId)
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.TrackVoteEvent, eventId)
    res
  }

  def addOrUpdateVote(eventId: Int, userId: Int, musicId: Int, up: Boolean): Try[DataTrackVoteEvent] = {
    val res = dbh.hasVote(eventId, userId, musicId).flatMap(b => if (b) {
      dbh.updateVote(eventId, userId, musicId, up)
    } else {
      dbh.addVote(eventId, userId, musicId, up)
    })
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.TrackVoteEvent, eventId)
    res
  }

  def delVote(eventId: Int, userId: Int, musicId: Int): Try[DataTrackVoteEvent] = {
    val res = dbh.delVote(eventId, userId, musicId)
    subActor ! WSEventCSUpdateQuery(SubQueryEnum.TrackVoteEvent, eventId)
    res
  }

  def hasVote(eventId: Int, userId: Int, musicId: Int): Try[Boolean] = {
    dbh.hasVote(eventId, userId, musicId)
  }

}
