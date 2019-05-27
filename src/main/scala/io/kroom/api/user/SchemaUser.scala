package io.kroom.api.user

import io.kroom.api.Authorization.Privacy
import io.kroom.api.Authorization.PermissionGroupToString
import io.kroom.api.SecureContext
import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.SchemaRoot
import io.kroom.api.util.DataPayload
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{Field, IntType, ListType, ObjectType, OptionType, StringType, fields}

import scala.concurrent.Future

object SchemaUser {

  import scala.concurrent.ExecutionContext.Implicits.global

  /* FETCHER */

  lazy val UserFetcherId: Fetcher[SecureContext, DataUser, DataUser, Int] =
    Fetcher.caching((ctx: SecureContext, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.repo.user.getById(id).toOption)
    }
    )(HasId(_.id))

  /* PAYLOAD */

  lazy val UserGetByIdPayload: ObjectType[SecureContext, DataPayload[DataUser]] = ObjectType(
    "UserGetByIdPayload",
    "UserGetByIdPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataUser]](
      Field("user", OptionType(UserField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  /* FIELD */

  lazy val PrivacyField: ObjectType[SecureContext, DataUserPrivacy] = ObjectType(
    "Privacy",
    "Privacy description.",
    () ⇒ fields[SecureContext, DataUserPrivacy](
      Field("email", StringType, resolve = _.value.email),
      Field("location", StringType, resolve = _.value.location),
      Field("friends", StringType, resolve = _.value.friends),
      Field("musicalPreferencesGenre", StringType, resolve = _.value.musicalPreferencesGenre),
    ))

  lazy val UserField: ObjectType[SecureContext, DataUser] = ObjectType(
    "User",
    "User description.",
    () ⇒ fields[SecureContext, DataUser](
      Field("id", OptionType(IntType), resolve = ctx =>
        ctx.ctx.checkPrivacy(ctx.value.id, Privacy.public) { () =>
          ctx.value.id
        }),
      Field("userName", StringType, resolve = _.value.userName),
      Field("email", OptionType(StringType), resolve = ctx =>
        ctx.ctx.checkPrivacy(ctx.value.id, Privacy.stringToPrivacy(ctx.value.privacy.email)) { () =>
          ctx.value.email
        }),
      Field("location", OptionType(StringType), resolve = ctx =>
        ctx.ctx.checkPrivacy[Option[String]](ctx.value.id, Privacy.stringToPrivacy(ctx.value.privacy.location)) { () =>
          ctx.value.location
        }.flatMap(e => e)),
      Field("token", OptionType(StringType), resolve = ctx =>
        ctx.ctx.checkPrivacy[Option[String]](ctx.value.id, Privacy.`private`) { () =>
          ctx.value.token
        }.flatMap(e => e)),
      Field("privacy", OptionType(PrivacyField), resolve = ctx =>
        ctx.ctx.checkPrivacy(ctx.value.id, Privacy.public) { () =>
          ctx.value.privacy
        }),
      Field("friends", OptionType(ListType(UserField)), resolve = ctx => Future {
        ctx.ctx.checkPrivacy(ctx.value.id, Privacy.stringToPrivacy(ctx.value.privacy.friends)) { () =>
          ctx.ctx.repo.user.getFriends(ctx.value.id)
        }.flatMap(e => e.toOption)
      }),
      Field("musicalPreferences", OptionType(ListType(SchemaDeezer.GenreField)), resolve = ctx => Future {
        ctx.ctx.checkPrivacy(ctx.value.id, Privacy.stringToPrivacy(ctx.value.privacy.musicalPreferencesGenre)) { () =>
          ctx.ctx.repo.user.getMsicalPreferences(ctx.value.id)
        }.flatMap(e => e.toOption)
      }),

      Field("permissionGroup", OptionType(ListType(StringType)), resolve = ctx =>
        ctx.ctx.checkPrivacy(ctx.value.id, Privacy.public) { () =>
          ctx.ctx.repo.user.getUserPermGroup(ctx.value.id).map(_.map(PermissionGroupToString).toList)
        }.flatMap(e => e.toOption)),

    ))

}
