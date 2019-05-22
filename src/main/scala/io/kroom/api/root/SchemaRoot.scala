package io.kroom.api.root

import io.kroom.api.user.SchemaUser
import io.kroom.api.deezer.{Connections, Order}
import io.kroom.api.util.Privacy
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
    "Query", fields[RepoRoot, Unit](

      /* DEEZER */

      Field("track", OptionType(TrackField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ TrackFetcherId.deferOpt(ctx.arg[Int]("id"))),
      Field("artist", OptionType(ArtistField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ ArtistFetcherId.deferOpt(ctx.arg[Int]("id"))),
      Field("album", OptionType(AlbumField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ AlbumFetcherId.deferOpt(ctx.arg[Int]("id"))),
      Field("genre", OptionType(GenreField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ GenreFetcherId.deferOpt(ctx.arg[Int]("id"))),

      // Option length, index ?
      Field("search", ListType(SearchField),
        arguments = Argument("search", StringType)
          :: Argument("connections", OptionInputType(ConnectionEnum))
          :: Argument("strict", BooleanType)
          :: Argument("order", OptionInputType(OrderEnum))
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.deezer.getSearch(
            ctx.arg[String]("search"),
            ctx.argOpt[Connections.Value]("connections"),
            ctx.arg[Boolean]("strict"),
            ctx.argOpt[Order.Value]("order"),
          )
        }),

      /* TRACK_VOTE_EVENT */

      Field("TrackVoteEventById", OptionType(TrackVoteEventField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ {
          TrackVoteEventFetcherId.deferOpt(ctx.arg[Int]("id"))
        }),

      Field("TrackVoteEventsPublic", ListType(TrackVoteEventField),
        arguments = Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.getPublic
        }),

      Field("TrackVoteEventByUserId", ListType(TrackVoteEventField),
        arguments = Argument("userId", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.getByUserId(ctx.arg[Int]("userId"))
        }),

      /* USER */

      Field("UserGetById", OptionType(UserField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.user.getById(ctx.arg[Int]("id"))
        }),

    ))

  val Mutation = ObjectType(
    "Mutation", fields[RepoRoot, Unit](

      /* TRACK_VOTE_EVENT */

      Field("TrackVoteEventNew", OptionType(TrackVoteEventField),
        arguments = Argument("userIdMaster", IntType)
          :: Argument("name", StringType)
          :: Argument("public", BooleanType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.`new`(
            ctx.arg[Int]("userIdMaster"),
            ctx.arg[String]("name"),
            ctx.arg[Boolean]("public"),
          )
        }
      ),

      Field("TrackVoteEventUpdate", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userIdMaster", IntType)
          :: Argument("name", StringType)
          :: Argument("public", BooleanType)
          :: Argument("horaire", OptionInputType(StringType))
          :: Argument("location", OptionInputType(StringType))
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.update(
            ctx.arg[Int]("eventId"),
            ctx.arg[Int]("userIdMaster"),
            ctx.arg[String]("name"),
            ctx.arg[Boolean]("public"),
            ctx.argOpt[String]("horaire"),
            ctx.argOpt[String]("location"),
          )
        }
      ),

      Field("TrackVoteEventAddUser", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.addUser(
            ctx.arg[Int]("eventId"),
            ctx.arg[Int]("userId"),
          )
        }
      ),

      Field("TrackVoteEventDelUser", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.delUser(
            ctx.arg[Int]("eventId"),
            ctx.arg[Int]("userId"),
          )
        }
      ),

      Field("TrackVoteEventAddVote", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Argument("musicId", IntType)
          :: Argument("up", BooleanType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.addVote(
            ctx.arg[Int]("eventId"),
            ctx.arg[Int]("userId"),
            ctx.arg[Int]("musicId"),
            ctx.arg[Boolean]("up"),
          )
        }
      ),

      Field("TrackVoteEventDelVote", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Argument("musicId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.delVote(
            ctx.arg[Int]("eventId"),
            ctx.arg[Int]("userId"),
            ctx.arg[Int]("musicId"),
          )
        }
      ),

      /* USER */

      Field("UserSignUp", OptionType(UserField),
        arguments = Argument("userName", StringType)
          :: Argument("email", StringType)
          :: Argument("pass", StringType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.user.signUp(
            ctx.arg[String]("userName"),
            ctx.arg[String]("email"),
            ctx.arg[String]("pass"),
          )
        }
      ),

      Field("UserSignIn", OptionType(UserField),
        arguments = Argument("userName", OptionInputType(StringType))
          :: Argument("email", OptionInputType(StringType))
          :: Argument("pass", StringType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.user.signIn(
            ctx.argOpt[String]("userName"),
            ctx.argOpt[String]("email"),
            ctx.arg[String]("pass"),
          )
        }
      ),

      Field("UserAddFriend", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("friendId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.user.addFriend(
            ctx.arg[Int]("userId"),
            ctx.arg[Int]("friendId"),
          )
        }
      ),

      Field("UserDelFriend", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("friendId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.user.delFriend(
            ctx.arg[Int]("userId"),
            ctx.arg[Int]("friendId"),
          )
        }
      ),

      Field("UserAddMusicalPreference", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("genreId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.user.addMusicalPreference(
            ctx.arg[Int]("userId"),
            ctx.arg[Int]("genreId"),
          )
        }
      ),

      Field("UserDelMusicalPreference", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("genreId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.user.delMusicalPreference(
            ctx.arg[Int]("userId"),
            ctx.arg[Int]("genreId"),
          )
        }
      ),


    )
  )

  val KroomSchema = Schema(Query, Some(Mutation))
}
