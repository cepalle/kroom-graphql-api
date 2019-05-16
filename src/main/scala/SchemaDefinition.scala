import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Defines a GraphQL schema for the current project
  */
object SchemaDefinition {

  // Fetcher

  val GenreFetcherId: Fetcher[RepoRoot, DeezerGenre, DeezerGenre, Int] =
    Fetcher((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getDeezerGenreById(id))
    }
    )(HasId(_.id))

  val ArtistFetcherId: Fetcher[RepoRoot, DeezerArtist, DeezerArtist, Int] =
    Fetcher((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getDeezerArtistById(id))
    }
    )(HasId(_.id))

  val AlbumFetcherId: Fetcher[RepoRoot, DeezerAlbum, DeezerAlbum, Int] =
    Fetcher((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getDeezerAlbumById(id))
    }
    )(HasId(_.id))

  val TrackFetcherId: Fetcher[RepoRoot, DeezerTrack, DeezerTrack, Int] =
    Fetcher((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getDeezerTrackById(id))
    }
    )(HasId(_.id))

  val TrackVoteEventFetcherId: Fetcher[RepoRoot, TrackVoteEvent, TrackVoteEvent, Int] =
    Fetcher((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getTrackVoteEventById(id))
    }
    )(HasId(_.id))

  // Field

  val GenreField = ObjectType(
    "Genre",
    "Genre description.",
    fields[RepoRoot, DeezerGenre](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("picture", StringType, resolve = _.value.picture),
      Field("picture_small", StringType, resolve = _.value.picture_small),
      Field("picture_medium", StringType, resolve = _.value.picture_medium),
      Field("picture_big", StringType, resolve = _.value.picture_big),
      Field("picture_xl", StringType, resolve = _.value.picture_xl),
    ))

  val ArtistField = ObjectType(
    "Artist",
    "Artist description.",
    fields[RepoRoot, DeezerArtist](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("link", StringType, resolve = _.value.link),
      Field("share", StringType, resolve = _.value.share),
      Field("picture", StringType, resolve = _.value.picture),
      Field("picture_small", StringType, resolve = _.value.picture_small),
      Field("picture_medium", StringType, resolve = _.value.picture_medium),
      Field("picture_big", StringType, resolve = _.value.picture_big),
      Field("picture_xl", StringType, resolve = _.value.picture_xl),
      Field("nb_album", IntType, resolve = _.value.nb_album),
      Field("nb_fan", IntType, resolve = _.value.nb_fan),
      Field("tracklist", StringType, resolve = _.value.tracklist),
    ))

  lazy val AlbumField: ObjectLikeType[RepoRoot, DeezerAlbum] = ObjectType(
    "Album",
    "Album description.",
    fields[RepoRoot, DeezerAlbum](
      Field("id", IntType, resolve = _.value.id),
      Field("title", StringType, resolve = _.value.title),
      Field("link", StringType, resolve = _.value.link),
      Field("cover", StringType, resolve = _.value.cover),
      Field("cover_small", StringType, resolve = _.value.cover_small),
      Field("cover_medium", StringType, resolve = _.value.cover_medium),
      Field("cover_big", StringType, resolve = _.value.cover_big),
      Field("cover_xl", StringType, resolve = _.value.cover_xl),
      Field("genre_id", IntType, resolve = _.value.genre_id),
      Field("genre", OptionType(GenreField), resolve = ctx => GenreFetcherId.deferOpt(ctx.value.genre_id)),
      // Field("genre_ids", ListType(IntType), resolve = _.value.contributors.map(_.id)),
      // Field("genres", ListType(GenreField),
      //  resolve = ctx => GenreFetcher.deferSeqOpt(ctx.value.contributors.map(_.id))
      // ),
      Field("label", StringType, resolve = _.value.label),
      Field("nb_tracks", IntType, resolve = _.value.nb_tracks),
      Field("duration", IntType, resolve = _.value.duration),
      Field("fans", IntType, resolve = _.value.fans),
      Field("rating", IntType, resolve = _.value.rating),
      Field("release_date", StringType, resolve = _.value.release_date),
      Field("record_type", StringType, resolve = _.value.record_type),
      Field("available", BooleanType, resolve = _.value.available),
      Field("tracklist", StringType, resolve = _.value.tracklist),
      Field("explicit_lyrics", BooleanType, resolve = _.value.explicit_lyrics),
      Field("explicit_content_lyrics", IntType, resolve = _.value.explicit_content_lyrics),
      Field("explicit_content_cover", IntType, resolve = _.value.explicit_content_cover),
      Field("contributors", ListType(ArtistField), resolve = ctx => ArtistFetcherId.deferSeq(ctx.value.contributors.map(_.id))),
      Field("artist_id", IntType, resolve = _.value.artist.id),
      Field("artist", ArtistField, resolve = ctx => ArtistFetcherId.defer(ctx.value.artist.id)),
      //Field("tracks", ListType(TrackField), resolve = ctx => TrackFetcher.deferSeq(ctx.value.tracks.data.map(_.id))),
    ))

  lazy val TrackField: ObjectLikeType[RepoRoot, DeezerTrack] = ObjectType(
    "Track",
    "Track description.",
    fields[RepoRoot, DeezerTrack](
      Field("id", IntType, resolve = _.value.id),
      Field("readable", BooleanType, resolve = _.value.readable),
      Field("title", StringType, resolve = _.value.title),
      Field("title_short", StringType, resolve = _.value.title_short),
      Field("title_version", StringType, resolve = _.value.title_version),
      Field("isrc", StringType, resolve = _.value.isrc),
      Field("link", StringType, resolve = _.value.link),
      Field("share", StringType, resolve = _.value.share),
      Field("duration", IntType, resolve = _.value.duration),
      Field("track_position", IntType, resolve = _.value.track_position),
      Field("disk_number", IntType, resolve = _.value.disk_number),
      Field("rank", IntType, resolve = _.value.rank),
      Field("release_date", StringType, resolve = _.value.release_date),
      Field("explicit_lyrics", BooleanType, resolve = _.value.explicit_lyrics),
      Field("explicit_content_lyrics", IntType, resolve = _.value.explicit_content_lyrics),
      Field("explicit_content_cover", IntType, resolve = _.value.explicit_content_cover),
      Field("preview", StringType, resolve = _.value.preview),
      Field("bpm", FloatType, resolve = _.value.bpm),
      Field("gain", FloatType, resolve = _.value.gain),
      Field("available_countries", ListType(StringType), resolve = _.value.available_countries),
      Field("contributors", ListType(ArtistField),
        resolve = ctx => ArtistFetcherId.deferSeqOpt(ctx.value.contributors.map(_.id))
      ),
      Field("artist_id", IntType, resolve = _.value.artist.id),
      Field("album_id", IntType, resolve = _.value.album.id),
      Field("artist", OptionType(ArtistField), resolve = ctx => ArtistFetcherId.deferOpt(ctx.value.artist.id)),
      Field("album", OptionType(AlbumField), resolve = ctx => AlbumFetcherId.deferOpt(ctx.value.album.id)),
    ))

  lazy val TrackVoteEventField: ObjectLikeType[RepoRoot, TrackVoteEvent] = ObjectType(
    "TrackVoteEvent",
    "TrackVoteEvent description.",
    fields[RepoRoot, TrackVoteEvent](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("public", BooleanType, resolve = _.value.public),
      Field("currentTrackId", IntType, resolve = _.value.currentTrackId),
      Field("horaire", StringType, resolve = _.value.horaire),
      Field("location", StringType, resolve = _.value.location),
    ))

  // arguments

  // root

  val Query = ObjectType(
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
