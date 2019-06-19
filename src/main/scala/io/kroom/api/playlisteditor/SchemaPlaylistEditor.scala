package io.kroom.api.playlisteditor

import io.kroom.api.SecureContext
import io.kroom.api.Server.system
import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.SchemaRoot
import io.kroom.api.user.SchemaUser
import io.kroom.api.util.DataPayload
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{BooleanType, Field, FloatType, IntType, ListType, LongType, ObjectType, OptionType, StringType, fields}

import scala.concurrent.Future


object SchemaPlaylistEditor {

  import system.dispatcher


  /* PAYLOAD */

  lazy val PlayListEditorByIdPayload: ObjectType[SecureContext, DataPayload[DataPlaylistEditor]] = ObjectType(
    "PlayListEditorByIdPayload",
    "PlayListEditorByIdPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataPlaylistEditor]](
      Field("playListEditor", OptionType(PlayListEditorField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val PlayListEditorByUserIdPayload: ObjectType[SecureContext, DataPayload[List[DataPlaylistEditor]]] = ObjectType(
    "PlayListEditorByUserIdPayload",
    "PlayListEditorByUserIdPayload description.",
    () ⇒ fields[SecureContext, DataPayload[List[DataPlaylistEditor]]](
      Field("playListEditor", OptionType(ListType(PlayListEditorField)), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  /* FIELD */

  lazy val PlayListEditorField: ObjectType[SecureContext, DataPlaylistEditor] = ObjectType(
    "PlaylistEditor",
    "PlaylistEditor description.",
    () ⇒ fields[SecureContext, DataPlaylistEditor](
      Field("id", IntType, resolve = _.value.id),

      Field("userMaster", OptionType(SchemaUser.UserField), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.ctx.repo.user.getById(ctx.value.userMasterId).get
        }
      }),

      Field("name", StringType, resolve = _.value.name),
      Field("public", BooleanType, resolve = _.value.public),
      Field("locAndSchRestriction", BooleanType, resolve = _.value.locAndSchRestriction),

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

      Field("scheduleBegin", OptionType(LongType), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.value.scheduleBegin
        }.flatten
      }),

      Field("scheduleEnd", OptionType(LongType), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.value.scheduleEnd
        }.flatten
      }),

      Field("latitude", OptionType(FloatType), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.value.latitude
        }.flatten
      }),

      Field("longitude", OptionType(FloatType), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.value.longitude
        }.flatten
      }),

      Field("userInvited", OptionType(ListType(SchemaUser.UserField)), resolve = ctx => Future {
        ctx.ctx.checkPrivacyTrackEvent(ctx.value.id, ctx.value.public) { () =>
          ctx.ctx.repo.trackVoteEvent.getUserInvited(ctx.value.id).get
        }
      }),
    ))
}
