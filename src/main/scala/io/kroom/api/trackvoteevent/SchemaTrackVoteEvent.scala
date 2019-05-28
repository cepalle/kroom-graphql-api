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

  /* FETCHER */

  /* PAYLOAD */

  lazy val TrackVoteEventByIdPayload: ObjectType[SecureContext, DataPayload[DataTrackVoteEvent]] = ObjectType(
    "TrackVoteEventByIdPayload",
    "TrackVoteEventByIdPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataTrackVoteEvent]](
      Field("trackVoteEvent", OptionType(TrackVoteEventField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val TrackVoteEventByUserIdPayload: ObjectType[SecureContext, DataPayload[List[DataTrackVoteEvent]]] = ObjectType(
    "TrackVoteEventByUserIdPayload",
    "TrackVoteEventByUserIdPayload description.",
    () ⇒ fields[SecureContext, DataPayload[List[DataTrackVoteEvent]]](
      Field("trackVoteEvents", OptionType(ListType(TrackVoteEventField)), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val TrackVoteEventNewPayload: ObjectType[SecureContext, DataPayload[DataTrackVoteEvent]] = ObjectType(
    "TrackVoteEventNewPayload",
    "TrackVoteEventNewPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataTrackVoteEvent]](
      Field("trackVoteEvent", OptionType(TrackVoteEventField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val TrackVoteEventUpdatePayload: ObjectType[SecureContext, DataPayload[DataTrackVoteEvent]] = ObjectType(
    "TrackVoteEventUpdatePayload",
    "TrackVoteEventUpdatePayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataTrackVoteEvent]](
      Field("trackVoteEvent", OptionType(TrackVoteEventField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  /* FIELD */

  lazy val TrackVoteEventField: ObjectType[SecureContext, DataTrackVoteEvent] = ObjectType(
    "TrackVoteEvent",
    "TrackVoteEvent description.",
    () ⇒ fields[SecureContext, DataTrackVoteEvent](
      Field("id", IntType, resolve = _.value.id),

      Field("userMaster", OptionType(SchemaUser.UserField), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.ctx.repo.user.getById(ctx.value.userMasterId).get
        }
      }),

      Field("name", StringType, resolve = _.value.name),
      Field("public", BooleanType, resolve = _.value.public),

      Field("currentTrack", OptionType(SchemaDeezer.TrackField), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.value.currentTrackId.map(id => ctx.ctx.repo.deezer.getTrackById(id).get)
        }.flatten
      }),

      Field("trackWithVote", OptionType(ListType(TrackWithVoteField)), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.ctx.repo.trackVoteEvent.getTrackWithVote(ctx.value.id).get
        }
      }),

      Field("schedule", OptionType(StringType), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.value.schedule
        }.flatten
      }),

      Field("location", OptionType(StringType), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.value.location
        }.flatten
      }),

      Field("userInvited", OptionType(ListType(SchemaUser.UserField)), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.ctx.repo.trackVoteEvent.getUserInvited(ctx.value.id).get
        }
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
