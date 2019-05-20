package trackVoteEvent

import root.RepoRoot
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{BooleanType, Field, IntType, ObjectType, StringType, fields}

import scala.concurrent.Future


object SchemaTrackVoteEvent {
  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val TrackVoteEventFetcherId: Fetcher[RepoRoot, DataTrackVoteEvent, DataTrackVoteEvent, Int] =
    Fetcher((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.trackVoteEvent.getById(id))
    }
    )(HasId(_.id))

  lazy val TrackVoteEventField: ObjectType[Unit, DataTrackVoteEvent] = ObjectType(
    "trackVoteEvent",
    "TrackVoteEvent description.",
    () ⇒ fields[Unit, DataTrackVoteEvent](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("public", BooleanType, resolve = _.value.public),
      Field("currentTrackId", IntType, resolve = _.value.currentTrackId),
      Field("horaire", StringType, resolve = _.value.horaire),
      Field("location", StringType, resolve = _.value.location),
    ))
}
