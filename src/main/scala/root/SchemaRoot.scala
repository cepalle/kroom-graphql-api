package root

import sangria.schema.{Argument, Field, IntType, ListType, ObjectType, OptionType, Schema, fields}

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

  val KroomSchema = Schema(Query)
}
