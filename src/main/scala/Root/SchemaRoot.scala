package Root

import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{Argument, BooleanType, Field, IntType, ListType, ObjectType, OptionType, Schema, StringType, fields}

import scala.concurrent.Future

/**
  * Defines a GraphQL schema for the current project
  */
object SchemaRoot {

  import Deezer.SchemaDeezer._

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val TrackVoteEventFetcherId: Fetcher[RepoRoot, DataTrackVoteEvent, DataTrackVoteEvent, Int] =
    Fetcher((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getTrackVoteEventById(id))
    }
    )(HasId(_.id))

  lazy val TrackVoteEventField: ObjectType[Unit, DataTrackVoteEvent] = ObjectType(
    "TrackVoteEvent",
    "TrackVoteEvent description.",
    () ⇒ fields[Unit, DataTrackVoteEvent](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("public", BooleanType, resolve = _.value.public),
      Field("currentTrackId", IntType, resolve = _.value.currentTrackId),
      Field("horaire", StringType, resolve = _.value.horaire),
      Field("location", StringType, resolve = _.value.location),
    ))

  // root

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
          ctx.ctx.getTrackVoteEventPublic()
        }),

      Field("TrackVoteEventByUserId", ListType(TrackVoteEventField),
        arguments = Argument("userId", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.getTrackVoteEventByUserId(ctx.arg[Int]("userId"))
        }),
    ))

  val KroomSchema = Schema(Query)
}
