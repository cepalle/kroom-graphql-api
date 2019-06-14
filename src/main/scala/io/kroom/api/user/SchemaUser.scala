package io.kroom.api.user

import io.kroom.api.Authorization.Privacy
import io.kroom.api.Authorization.permissionGroupToString
import io.kroom.api.Server.system
import io.kroom.api.{Authorization, SecureContext}
import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.SchemaRoot
import io.kroom.api.util.DataPayload
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{Field, IntType, ListType, ObjectType, OptionType, StringType, fields, FloatType}

import scala.concurrent.Future

object SchemaUser {

  import system.dispatcher

  /* FETCHER */

  /* PAYLOAD */

  lazy val UserGetByIdPayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserGetByIdPayload",
    "UserGetByIdPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val UserSignInPayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserSignInPayload",
    "UserSignInPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val UserSignUpPayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserSignUpPayload",
    "UserSignUpPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val UserAddFriendPayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserAddFriendPayload",
    "UserAddFriendPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val UserDelFriendPayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserDelFriendPayload",
    "UserDelFriendPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val UserAddMusicalPreferencePayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserAddMusicalPreferencePayload",
    "UserAddMusicalPreferencePayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val UserDelMusicalPreferencePayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserDelMusicalPreferencePayload",
    "UserDelMusicalPreferencePayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val UserUpdatePrivacyPayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserUpdatePrivacyPayload",
    "UserUpdatePrivacyPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val UserSignWithGooglePayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserSignWithGooglePayload",
    "UserSignWithGooglePayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  /* FIELD */

  lazy val PrivacyField: ObjectType[SecureContext, DataUserPrivacy] = ObjectType(
    "Privacy",
    "Privacy description.",
    () ⇒ fields[SecureContext, DataUserPrivacy](
      Field("email", SchemaRoot.PrivacyEnum, resolve = ctx => Authorization.Privacy.stringToPrivacy(ctx.value.email)),
      Field("location", SchemaRoot.PrivacyEnum, resolve = ctx => Authorization.Privacy.stringToPrivacy(ctx.value.location)),
      Field("friends", SchemaRoot.PrivacyEnum, resolve = ctx => Authorization.Privacy.stringToPrivacy(ctx.value.friends)),
      Field("musicalPreferencesGenre", SchemaRoot.PrivacyEnum, resolve = ctx => Authorization.Privacy.stringToPrivacy(ctx.value.musicalPreferencesGenre)),
    ))

  lazy val UserField: ObjectType[SecureContext, DataUser] = ObjectType(
    "User",
    "User description.",
    () ⇒ fields[SecureContext, DataUser](
      Field("id", OptionType(IntType), resolve = ctx =>
        ctx.ctx.checkPrivacyUser(ctx.value.id, Privacy.public) { () =>
          ctx.value.id
        }),
      Field("userName", StringType, resolve = _.value.userName),
      Field("email", OptionType(StringType), resolve = ctx =>
        ctx.ctx.checkPrivacyUser(ctx.value.id, Privacy.stringToPrivacy(ctx.value.privacy.email)) { () =>
          ctx.value.email
        }),
      Field("latitude", OptionType(FloatType), resolve = ctx =>
        ctx.ctx.checkPrivacyUser(ctx.value.id, Privacy.stringToPrivacy(ctx.value.privacy.location)) { () =>
          ctx.value.latitude
        }.flatMap(e => e)),
      Field("longitude", OptionType(FloatType), resolve = ctx =>
        ctx.ctx.checkPrivacyUser(ctx.value.id, Privacy.stringToPrivacy(ctx.value.privacy.location)) { () =>
          ctx.value.longitude
        }.flatMap(e => e)),
      Field("token", OptionType(StringType), resolve = ctx =>
        ctx.ctx.checkPrivacyUser(ctx.value.id, Privacy.`private`) { () =>
          ctx.value.token
        }.flatMap(e => e)),
      Field("privacy", OptionType(PrivacyField), resolve = ctx =>
        ctx.ctx.checkPrivacyUser(ctx.value.id, Privacy.public) { () =>
          ctx.value.privacy
        }),
      Field("friends", OptionType(ListType(UserField)), resolve = ctx => Future {
        ctx.ctx.checkPrivacyUser(ctx.value.id, Privacy.stringToPrivacy(ctx.value.privacy.friends)) { () =>
          ctx.ctx.repo.user.getFriends(ctx.value.id)
        }.flatMap(e => e.toOption)
      }),
      Field("musicalPreferences", OptionType(ListType(SchemaDeezer.GenreField)), resolve = ctx => Future {
        ctx.ctx.checkPrivacyUser(ctx.value.id, Privacy.stringToPrivacy(ctx.value.privacy.musicalPreferencesGenre)) { () =>
          ctx.ctx.repo.user.getMsicalPreferences(ctx.value.id)
        }.flatMap(e => e.toOption)
      }),

      Field("permissionGroup", OptionType(ListType(StringType)), resolve = ctx =>
        ctx.ctx.checkPrivacyUser(ctx.value.id, Privacy.public) { () =>
          ctx.ctx.repo.user.getUserPermGroup(ctx.value.id).map(_.map(permissionGroupToString).toList)
        }.flatMap(e => e.toOption)),

    ))

}
