package io.kroom.api.trackvoteevent

import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.RepoRoot
import io.kroom.api.user.SchemaUser
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{BooleanType, Field, IntType, ObjectType, StringType, fields, ListType}

import scala.concurrent.Future


object SchemaTrackVoteEvent {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val TrackVoteEventFetcherId: Fetcher[RepoRoot, DataTrackVoteEvent, DataTrackVoteEvent, Int] =
    Fetcher.caching((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.trackVoteEvent.getById(id))
    }
    )(HasId(_.id))

  lazy val TrackVoteEventField: ObjectType[RepoRoot, DataTrackVoteEvent] = ObjectType(
    "trackVoteEvent",
    "TrackVoteEvent description.",
    () ⇒ fields[RepoRoot, DataTrackVoteEvent](
      Field("id", IntType, resolve = _.value.id),

      Field("userMaster", SchemaUser.UserField, resolve = ctx =>
        SchemaUser.UserFetcherId.defer(ctx.value.userMasterId)
      ),

      Field("name", StringType, resolve = _.value.name),
      Field("public", BooleanType, resolve = _.value.public),

      Field("currentTrack", SchemaDeezer.TrackField, resolve = ctx =>
        SchemaDeezer.TrackFetcherId.defer(ctx.value.currentTrackId)
      ),
      Field("trackWithVote", ListType(TrackWithVoteField), resolve = ctx => Future {
        ctx.ctx.trackVoteEvent.getTrackWithVote(ctx.value.id)
      }),

      Field("horaire", StringType, resolve = _.value.horaire),
      Field("location", StringType, resolve = _.value.location),

      Field("userInvited", ListType(SchemaUser.UserField), resolve = ctx => Future {
        ctx.ctx.trackVoteEvent.getUserInvited(ctx.value.id)
      }),
    ))

  lazy val TrackWithVoteField: ObjectType[RepoRoot, DataTrackWithVote] = ObjectType(
    "trackWithVote",
    "trackWithVote description.",
    () ⇒ fields[RepoRoot, DataTrackWithVote](
      Field("track", SchemaDeezer.TrackField, resolve = ctx =>
        SchemaDeezer.TrackFetcherId.defer(ctx.value.trackId)
      ),

      Field("score", IntType, resolve = _.value.score),
      Field("nb_vote_up", IntType, resolve = _.value.nb_vote_up),
      Field("nb_vote_down", IntType, resolve = _.value.nb_vote_down),
    ))
}
