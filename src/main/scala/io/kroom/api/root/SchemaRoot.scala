package io.kroom.api.root

import io.kroom.api.Authorization.Privacy
import io.kroom.api.Authorization.Permissions
import io.kroom.api.SecureContext
import io.kroom.api.user.{DataUser, SchemaUser}
import io.kroom.api.deezer.{Connections, Order}
import sangria.schema._

import scala.concurrent.Future

/**
  * Defines a GraphQL schema for the current project
  */
object SchemaRoot {

  import SchemaUser._
  import io.kroom.api.deezer.SchemaDeezer._
  import io.kroom.api.trackvoteevent.SchemaTrackVoteEvent._

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val PrivacyEnum = EnumType(
    "PrivacyEnum",
    Some("PrivacyEnum"),
    List(
      EnumValue("PUBLIC",
        value = Privacy.public),
      EnumValue("AMIS",
        value = Privacy.amis),
      EnumValue("PRIVATE",
        value = Privacy.`private`),
    )
  )

  lazy val Query = ObjectType(
    "Query", fields[SecureContext, Unit](

      /* DEEZER */

      Field("DeezerTrack", OptionType(TrackField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.DeezerTrack) { () =>
          TrackFetcherId.deferOpt(ctx.arg[Int]("id"))
        }.get),
      Field("DeezerArtist", OptionType(ArtistField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.DeezerArtist) { () =>
          ArtistFetcherId.deferOpt(ctx.arg[Int]("id"))
        }.get),
      Field("DeezerAlbum", OptionType(AlbumField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.DeezerAlbum) { () =>
          AlbumFetcherId.deferOpt(ctx.arg[Int]("id"))
        }.get),
      Field("DeezerGenre", OptionType(GenreField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.DeezerGenre) { () =>
          GenreFetcherId.deferOpt(ctx.arg[Int]("id"))
        }.get),

      // Option length, index ?
      Field("DeezerSearch", ListType(SearchField),
        arguments = Argument("search", StringType)
          :: Argument("connections", OptionInputType(ConnectionEnum))
          :: Argument("strict", BooleanType)
          :: Argument("order", OptionInputType(OrderEnum))
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.DeezerSearch) { () =>
          Future {
            ctx.ctx.repo.deezer.getSearch(
              ctx.arg[String]("search"),
              ctx.argOpt[Connections.Value]("connections"),
              ctx.arg[Boolean]("strict"),
              ctx.argOpt[Order.Value]("order"),
            ).get
          }
        }.get),

      /* TRACK_VOTE_EVENT */

      Field("TrackVoteEventsPublic", ListType(TrackVoteEventField),
        arguments = Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.TrackVoteEventsPublic) { () =>
          Future {
            ctx.ctx.repo.trackVoteEvent.getPublic.get
          }
        }.get),

      Field("TrackVoteEventById", OptionType(TrackVoteEventField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.TrackVoteEventById) { () =>
          TrackVoteEventFetcherId.deferOpt(ctx.arg[Int]("id"))
        }.get),

      Field("TrackVoteEventByUserId", ListType(TrackVoteEventField),
        arguments = Argument("userId", IntType) :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.TrackVoteEventByUserId) { () =>
          Future {
            ctx.ctx.repo.trackVoteEvent.getByUserId(ctx.arg[Int]("userId")).get
          }
        }.get),

      /* USER */

      Field("UserGetById", UserField,
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserGetById) { () =>
          Future {
            ctx.ctx.repo.user.getById(ctx.arg[Int]("id")).get
          }
        }.get),

    ))

  val Mutation = ObjectType(
    "Mutation", fields[SecureContext, Unit](

      /* TRACK_VOTE_EVENT */

      Field("TrackVoteEventNew", OptionType(TrackVoteEventField),
        arguments = Argument("userIdMaster", IntType)
          :: Argument("name", StringType)
          :: Argument("public", BooleanType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.TrackVoteEventNew) { () =>
          Future {
            // TODO userMaster is invited and can't be del
            ctx.ctx.repo.trackVoteEvent.`new`(
              ctx.arg[Int]("userIdMaster"),
              ctx.arg[String]("name"),
              ctx.arg[Boolean]("public"),
            ).get
          }
        }.get
      ),

      Field("TrackVoteEventUpdate", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userIdMaster", IntType)
          :: Argument("name", StringType)
          :: Argument("public", BooleanType)
          :: Argument("horaire", OptionInputType(StringType))
          :: Argument("location", OptionInputType(StringType))
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.TrackVoteEventUpdate) { () =>
          Future {
            // TODO check if is userMaster
            ctx.ctx.repo.trackVoteEvent.update(
              ctx.arg[Int]("eventId"),
              ctx.arg[Int]("userIdMaster"),
              ctx.arg[String]("name"),
              ctx.arg[Boolean]("public"),
              ctx.argOpt[String]("horaire"),
              ctx.argOpt[String]("location"),
            ).get
          }
        }.get
      ),

      Field("TrackVoteEventAddUser", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.TrackVoteEventAddUser) { () =>
          Future {
            // TODO check if is userMaster
            ctx.ctx.repo.trackVoteEvent.addUser(
              ctx.arg[Int]("eventId"),
              ctx.arg[Int]("userId"),
            ).get
          }
        }.get
      ),

      Field("TrackVoteEventDelUser", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.TrackVoteEventDelUser) { () =>
          Future {
            // TODO check if is userMaster
            ctx.ctx.repo.trackVoteEvent.delUser(
              ctx.arg[Int]("eventId"),
              ctx.arg[Int]("userId"),
            ).get
          }
        }.get
      ),

      Field("TrackVoteEventAddVote", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Argument("musicId", IntType)
          :: Argument("up", BooleanType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.TrackVoteEventAddVote) { () =>
          Future {
            // TODO check if is invited
            ctx.ctx.repo.trackVoteEvent.addVote(
              ctx.arg[Int]("eventId"),
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("musicId"),
              ctx.arg[Boolean]("up"),
            ).get
          }
        }.get
      ),

      Field("TrackVoteEventDelVote", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Argument("musicId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.TrackVoteEventDelVote) { () =>
          Future {
            // TODO check if is invited
            ctx.ctx.repo.trackVoteEvent.delVote(
              ctx.arg[Int]("eventId"),
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("musicId"),
            ).get
          }
        }.get
      ),

      /* USER */

      Field("UserSignUp", UserField,
        arguments = Argument("userName", StringType)
          :: Argument("email", StringType)
          :: Argument("pass", StringType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserSignUp) { () =>
          UpdateCtx(ctx.ctx.repo.user.signUp(
            ctx.arg[String]("userName"),
            ctx.arg[String]("email"),
            ctx.arg[String]("pass"),
          )) { user ⇒
            new SecureContext(user.token, ctx.ctx.repo)
          }
        }.get
      ),

      Field("UserSignIn", UserField,
        arguments = Argument("userName", StringType)
          :: Argument("pass", StringType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserSignIn) { () =>
          UpdateCtx(ctx.ctx.repo.user.signIn(
            ctx.arg[String]("userName"),
            ctx.arg[String]("pass"),
          )) { user ⇒
            new SecureContext(user.token, ctx.ctx.repo)
          }
        }.get
      ),

      Field("UserAddFriend", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("friendId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserAddFriend) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.addFriend(
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("friendId"),
            ).get
          }
        }.get
      ),

      Field("UserDelFriend", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("friendId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserDelFriend) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.delFriend(
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("friendId"),
            ).get
          }
        }.get
      ),

      Field("UserAddMusicalPreference", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("genreId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserAddMusicalPreference) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.addMusicalPreference(
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("genreId"),
            ).get
          }
        }.get
      ),

      Field("UserDelMusicalPreference", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("genreId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserDelMusicalPreference) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.delMusicalPreference(
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("genreId"),
            ).get
          }
        }.get
      ),

      Field("UserUpdatePrivacy", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("email", PrivacyEnum)
          :: Argument("location", PrivacyEnum)
          :: Argument("friends", PrivacyEnum)
          :: Argument("musicalPreferencesGenre", PrivacyEnum)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserUpdatePrivacy) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.updatePrivacy(
              ctx.arg[Int]("userId"),
              ctx.arg[Privacy.Value]("email"),
              ctx.arg[Privacy.Value]("location"),
              ctx.arg[Privacy.Value]("friends"),
              ctx.arg[Privacy.Value]("musicalPreferencesGenre"),
            ).get
          }
        }.get
      ),

    )
  )

  val KroomSchema = Schema(Query, Some(Mutation))
}
