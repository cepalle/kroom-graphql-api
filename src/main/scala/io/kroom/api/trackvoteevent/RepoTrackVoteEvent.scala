package io.kroom.api.trackvoteevent

import io.kroom.api.deezer.RepoDeezer
import io.kroom.api.user.{DataUser, RepoUser}
import monix.reactive.subjects.ConcurrentSubject
import monix.execution.Scheduler.Implicits.global

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

  val source: ConcurrentSubject[DataTrackVoteEvent, DataTrackVoteEvent] = ConcurrentSubject.publish[DataTrackVoteEvent]

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
             schedule: Option[String],
             location: Option[String]
            ): Try[DataTrackVoteEvent] = {
    dbh.getById(eventId).flatMap(track => {
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
    })
  }

  def addUser(eventId: Int, userId: Int): Try[DataTrackVoteEvent] = {
    dbh.addUser(eventId, userId)
  }

  def delUser(eventId: Int, userId: Int): Try[DataTrackVoteEvent] = {
    dbh.delUser(eventId, userId)
  }

  def addOrUpdateVote(eventId: Int, userId: Int, musicId: Int, up: Boolean): Try[DataTrackVoteEvent] = {
    dbh.hasVote(eventId, userId, musicId).flatMap(b => if (b) {
      dbh.updateVote(eventId, userId, musicId, up)
    } else {
      dbh.addVote(eventId, userId, musicId, up)
    })
  }

  def hasVote(eventId: Int, userId: Int, musicId: Int): Try[Boolean] = {
    dbh.hasVote(eventId, userId, musicId)
  }

  def delVote(eventId: Int, userId: Int, musicId: Int): Try[DataTrackVoteEvent] = {
    dbh.delVote(eventId, userId, musicId)
  }

}
