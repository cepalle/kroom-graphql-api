package io.kroom.api.trackvoteevent

import io.kroom.api.SecureContext
import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.SchemaRoot
import io.kroom.api.user.SchemaUser
import io.kroom.api.util.DataPayload
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{BooleanType, Field, IntType, ListType, ObjectType, OptionType, StringType, fields}

import scala.concurrent.Future


object SchemaTrackVoteEvent {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val TrackVoteEventFetcherId: Fetcher[SecureContext, DataTrackVoteEvent, DataTrackVoteEvent, Int] =
    Fetcher.caching((ctx: SecureContext, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.repo.trackVoteEvent.getById(id).toOption)
    }
    )(HasId(_.id))

  lazy val TrackVoteEventField: ObjectType[SecureContext, DataTrackVoteEvent] = ObjectType(
    "TrackVoteEvent",
    "TrackVoteEvent description.",
    () ⇒ fields[SecureContext, DataTrackVoteEvent](
      Field("id", IntType, resolve = _.value.id),

      Field("userMaster", SchemaUser.UserField, resolve = ctx =>
        SchemaUser.UserFetcherId.defer(ctx.value.userMasterId)
      ),

      Field("name", StringType, resolve = _.value.name),
      Field("public", BooleanType, resolve = _.value.public),

      Field("currentTrack", OptionType(SchemaDeezer.TrackField), resolve = ctx =>
        SchemaDeezer.TrackFetcherId.deferOpt(ctx.value.currentTrackId)
      ),
      Field("trackWithVote", ListType(TrackWithVoteField), resolve = ctx => Future {
        ctx.ctx.repo.trackVoteEvent.getTrackWithVote(ctx.value.id).get
      }),

      Field("schedule", OptionType(StringType), resolve = _.value.schedule),
      Field("location", OptionType(StringType), resolve = _.value.location),

      Field("userInvited", ListType(SchemaUser.UserField), resolve = ctx => Future {
        ctx.ctx.repo.trackVoteEvent.getUserInvited(ctx.value.id).get
      }),
    ))

  lazy val TrackWithVoteField: ObjectType[SecureContext, DataTrackWithVote] = ObjectType(
    "TrackWithVote",
    "TrackWithVote description.",
    () ⇒ fields[SecureContext, DataTrackWithVote](
      Field("track", SchemaDeezer.TrackField, resolve = ctx =>
        SchemaDeezer.TrackFetcherId.defer(ctx.value.trackId)
      ),

      Field("score", IntType, resolve = _.value.score),
      Field("nb_vote_up", IntType, resolve = _.value.nb_vote_up),
      Field("nb_vote_down", IntType, resolve = _.value.nb_vote_down),
    ))
}
