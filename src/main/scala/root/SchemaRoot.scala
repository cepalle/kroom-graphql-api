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
        arguments =
          Argument("search", StringType) ::
            Argument("connections", OptionInputType(ConnectionEnum)) ::
            Argument("strict", BooleanType) ::
            Argument("order", OptionInputType(OrderEnum)) ::
            Nil,
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
          ctx.ctx.trackVoteEvent.getTrackVoteEventPublic
        }),

      Field("TrackVoteEventByUserId", ListType(TrackVoteEventField),
        arguments = Argument("userId", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.getTrackVoteEventByUserId(ctx.arg[Int]("userId"))
        }),
    ))

  val MutationType = ObjectType(
    "Mutation", fields[RepoRoot, Unit](

      /* DEEZER */

      Field("TrackVoteEventVoteVote", OptionType(TrackVoteEventField),
        arguments = Argument("eventId", IntType) :: Argument("musicId", IntType) :: Argument("bool", BooleanType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.trackVoteEvent.trackVoteEventVote(
            ctx.arg[Int]("eventId"),
            ctx.arg[Int]("musicId"),
            ctx.arg[Boolean]("bool"),
          )
        }
      ),
    )
  )

  val KroomSchema = Schema(Query, Some(MutationType))
}
