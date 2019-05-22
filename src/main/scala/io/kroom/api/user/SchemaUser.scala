package io.kroom.api.user

import io.kroom.api.SecureContext
import io.kroom.api.util.Privacy
import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.SchemaRoot
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{Field, IntType, ListType, ObjectType, OptionType, StringType, fields}

import scala.concurrent.Future

object SchemaUser {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val UserFetcherId: Fetcher[SecureContext, DataUser, DataUser, Int] =
    Fetcher.caching((ctx: SecureContext, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.repo.user.getById(id))
    }
    )(HasId(_.id))

  lazy val PrivacyField: ObjectType[SecureContext, DataUserPrivacy] = ObjectType(
    "Privacy",
    "Privacy description.",
    () ⇒ fields[SecureContext, DataUserPrivacy](
      Field("email", SchemaRoot.PrivacyEnum, resolve = ctx => Privacy.IntToPrivacy(ctx.value.email)),
      Field("location", SchemaRoot.PrivacyEnum, resolve = ctx => Privacy.IntToPrivacy(ctx.value.location)),
      Field("friends", SchemaRoot.PrivacyEnum, resolve = ctx => Privacy.IntToPrivacy(ctx.value.friends)),
      Field("musicalPreferencesGenre", SchemaRoot.PrivacyEnum, resolve = ctx => Privacy.IntToPrivacy(ctx.value.musicalPreferencesGenre)),
    ))

  lazy val UserField: ObjectType[SecureContext, DataUser] = ObjectType(
    "User",
    "User description.",
    () ⇒ fields[SecureContext, DataUser](
      Field("id", IntType, resolve = _.value.id),
      Field("userName", StringType, resolve = _.value.userName),
      Field("email", StringType, resolve = _.value.email),
      Field("location", OptionType(StringType), resolve = _.value.location),

      Field("friends", ListType(UserField), resolve = ctx => Future {
        ctx.ctx.repo.user.getFriends(ctx.value.id)
      }),
      Field("musicalPreferences", ListType(SchemaDeezer.GenreField), resolve = ctx => Future {
        ctx.ctx.repo.user.getmMsicalPreferences(ctx.value.id)
      }),
      Field("privacy", PrivacyField, resolve = _.value.privacy),
    ))

}
