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

      Field("invitedUsers", OptionType(ListType(SchemaUser.UserField)), resolve = ctx => Future {
        ctx.ctx.repo.playListEditor.getInvitedUsers(ctx.value.id).get
      }),

      // wrap in order
      Field("tracks", OptionType(ListType(TrackWithVoteField)), resolve = ctx => Future {
        ctx.ctx.repo.playListEditor.getTracksWithOrder(ctx.value.id).get
      }),
    ))

  lazy val TrackWithVoteField: ObjectType[SecureContext, DataTrackWithOrder] = ObjectType(
    "TrackWitOrder",
    "TrackWitOrder description.",
    () ⇒ fields[SecureContext, DataTrackWithOrder](
      Field("track", SchemaDeezer.TrackField, resolve = ctx =>
        SchemaDeezer.TrackFetcherId.defer(ctx.value.trackId)
      ),

      Field("pos", IntType, resolve = _.value.pos),
    ))

}
