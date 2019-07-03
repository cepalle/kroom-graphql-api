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

  lazy val PlayListEditorAddTrackPayload: ObjectType[SecureContext, DataPayload[DataPlaylistEditor]] = ObjectType(
    "PlayListEditorAddTrackPayload",
    "PlayListEditorAddTrackPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataPlaylistEditor]](
      Field("playListEditor", OptionType(PlayListEditorField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val PlayListEditorDelTrackPayload: ObjectType[SecureContext, DataPayload[DataPlaylistEditor]] = ObjectType(
    "PlayListEditorDelTrackPayload",
    "PlayListEditorDelTrackPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataPlaylistEditor]](
      Field("playListEditor", OptionType(PlayListEditorField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val PlayListEditorMoveTrackPayload: ObjectType[SecureContext, DataPayload[DataPlaylistEditor]] = ObjectType(
    "PlayListEditorMoveTrackPayload",
    "PlayListEditorMoveTrackPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataPlaylistEditor]](
      Field("playListEditor", OptionType(PlayListEditorField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val PlayListEditorNewPayload: ObjectType[SecureContext, DataPayload[DataPlaylistEditor]] = ObjectType(
    "PlayListEditorNewPayload",
    "PlayListEditorNewPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataPlaylistEditor]](
      Field("playListEditor", OptionType(PlayListEditorField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val PlayListEditorDelPayload: ObjectType[SecureContext, DataPayload[Unit]] = ObjectType(
    "PlayListEditorDelPayload",
    "PlayListEditorDelPayload description.",
    () ⇒ fields[SecureContext, DataPayload[Unit]](
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val PlayListEditorAddUserPayload: ObjectType[SecureContext, DataPayload[DataPlaylistEditor]] = ObjectType(
    "PlayListEditorAddUserPayload",
    "PlayListEditorAddUserPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataPlaylistEditor]](
      Field("playListEditor", OptionType(PlayListEditorField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val PlayListEditorDelUserPayload: ObjectType[SecureContext, DataPayload[DataPlaylistEditor]] = ObjectType(
    "PlayListEditorDelUserPayload",
    "PlayListEditorDelUserPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataPlaylistEditor]](
      Field("playListEditor", OptionType(PlayListEditorField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  /* FIELD */

  lazy val PlayListEditorField: ObjectType[SecureContext, DataPlaylistEditor] = ObjectType(
    "PlaylistEditor",
    "PlaylistEditor description.",
    () ⇒ fields[SecureContext, DataPlaylistEditor](
      Field("id", IntType, resolve = _.value.id),

      Field("userMaster", OptionType(SchemaUser.UserField), resolve = ctx => Future {
        ctx.ctx.checkPrivacyPlaylist(ctx.value.id, ctx.value.public) { () =>
          ctx.ctx.repo.user.getById(ctx.value.userMasterId).get
        }
      }),

      Field("name", StringType, resolve = _.value.name),
      Field("public", BooleanType, resolve = _.value.public),

      Field("invitedUsers", OptionType(ListType(SchemaUser.UserField)), resolve = ctx => Future {
        ctx.ctx.repo.playListEditor.getInvitedUsers(ctx.value.id).get
      }),

      Field("tracks", OptionType(ListType(SchemaDeezer.TrackField)), resolve = ctx =>
        SchemaDeezer.TrackFetcherId.deferSeqOpt(ctx.value.tracks)
      )),
  )

}
