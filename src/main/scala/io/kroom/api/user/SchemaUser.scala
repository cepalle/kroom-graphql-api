package io.kroom.api.user

import io.kroom.api.deezer.SchemaDeezer
import io.kroom.api.root.{RepoRoot, SchemaRoot}
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{Field, IntType, ListType, ObjectType, OptionType, StringType, fields}

import scala.concurrent.Future

object SchemaUser {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val UserFetcherId: Fetcher[RepoRoot, DataUser, DataUser, Int] =
    Fetcher.caching((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.user.getById(id))
    }
    )(HasId(_.id))

  lazy val PrivacyField: ObjectType[RepoRoot, DataUserPrivacy] = ObjectType(
    "Privacy",
    "Privacy description.",
    () ⇒ fields[RepoRoot, DataUserPrivacy](
      Field("email", SchemaRoot.PrivacyEnum, resolve = _.value.email),
      Field("location", SchemaRoot.PrivacyEnum, resolve = _.value.location),
      Field("friends", SchemaRoot.PrivacyEnum, resolve = _.value.friends),
      Field("musicalPreferencesGenre", SchemaRoot.PrivacyEnum, resolve = _.value.musicalPreferencesGenre),
    ))

  lazy val UserField: ObjectType[RepoRoot, DataUser] = ObjectType(
    "User",
    "User description.",
    () ⇒ fields[RepoRoot, DataUser](
      Field("id", IntType, resolve = _.value.id),
      Field("userName", StringType, resolve = _.value.userName),
      Field("email", StringType, resolve = _.value.email),
      Field("location", OptionType(StringType), resolve = _.value.location),

      Field("friends", ListType(UserField), resolve = ctx => Future {
        ctx.ctx.user.getFriends(ctx.value.id)
      }),
      Field("musicalPreferences", ListType(SchemaDeezer.GenreField), resolve = ctx => Future {
        ctx.ctx.user.getmMsicalPreferences(ctx.value.id)
      }),
      Field("privacy", PrivacyField, resolve = _.value.privacy),
    ))

}
