package root

import deezer.{Order, Connections}
import sangria.schema._
import sangria.macros.derive._
import scala.concurrent.Future

/**
  * Defines a GraphQL schema for the current project
  */
object SchemaRoot {

  import deezer.SchemaDeezer._
  import trackVoteEvent.SchemaTrackVoteEvent._

  import scala.concurrent.ExecutionContext.Implicits.global

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
    ))

  val MutationType = ObjectType(
    "Mutation", fields[RepoRoot, Unit](

      /* DEEZER */

      Field("TrackVoteEventNew", OptionType(TrackVoteEventField),
        arguments = Argument("userIdMaster", IntType)
          :: Argument("name", StringType)
          :: Argument("public", BooleanType)
          :: Argument("horaire", StringType)
          :: Argument("location", StringType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.newEvent(
            ctx.arg[Int]("userIdMaster"),
            ctx.arg[String]("name"),
            ctx.arg[Boolean]("public"),
            ctx.arg[String]("horaire"),
            ctx.arg[String]("location"),
          )
        }
      ),

      Field("TrackVoteEventUpdate", OptionType(TrackVoteEventField),
        arguments = Argument("userIdMaster", OptionInputType(IntType))
          :: Argument("name", OptionInputType(StringType))
          :: Argument("public", OptionInputType(BooleanType))
          :: Argument("horaire", OptionInputType(StringType))
          :: Argument("location", OptionInputType(StringType))
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.update(
            ctx.argOpt[Int]("userIdMaster"),
            ctx.argOpt[String]("name"),
            ctx.argOpt[Boolean]("public"),
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

    )
  )

  val KroomSchema = Schema(Query, Some(MutationType))
}
