import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Defines a GraphQL schema for the current project
  */
object SchemaDefinition {

  // Fetcher

  val GenreFetcher: Fetcher[RootRepo, DeezerGenre, DeezerGenre, Int] =
    Fetcher((ctx: RootRepo, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getGenreById(id))
    }
    )(HasId(_.id))

  val ArtistFetcher: Fetcher[RootRepo, DeezerArtist, DeezerArtist, Int] =
    Fetcher((ctx: RootRepo, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getArtistById(id))
    }
    )(HasId(_.id))

  val AlbumFetcher: Fetcher[RootRepo, DeezerAlbum, DeezerAlbum, Int] =
    Fetcher((ctx: RootRepo, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getAlbumById(id))
    }
    )(HasId(_.id))

  val TrackFetcher: Fetcher[RootRepo, DeezerTrack, DeezerTrack, Int] =
    Fetcher((ctx: RootRepo, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.getTrackById(id))
    }
    )(HasId(_.id))

  // Field

  val GenreField = ObjectType(
    "Genre",
    "Genre description.",
    fields[RootRepo, DeezerGenre](
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
    fields[RootRepo, DeezerArtist](
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

  lazy val AlbumField: ObjectLikeType[RootRepo, DeezerAlbum] = ObjectType(
    "Album",
    "Album description.",
    fields[RootRepo, DeezerAlbum](
      Field("id", IntType, resolve = _.value.id),
      Field("title", StringType, resolve = _.value.title),
      Field("link", StringType, resolve = _.value.link),
      Field("cover", StringType, resolve = _.value.cover),
      Field("cover_small", StringType, resolve = _.value.cover_small),
      Field("cover_medium", StringType, resolve = _.value.cover_medium),
      Field("cover_big", StringType, resolve = _.value.cover_big),
      Field("cover_xl", StringType, resolve = _.value.cover_xl),
      Field("genre_id", IntType, resolve = _.value.genre_id),
      Field("genre", OptionType(GenreField), resolve = ctx => GenreFetcher.deferOpt(ctx.value.genre_id)),
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
      Field("contributors", ListType(ArtistField), resolve = ctx => ArtistFetcher.deferSeq(ctx.value.contributors.map(_.id))),
      Field("artist_id", IntType, resolve = _.value.artist.id),
      Field("artist", ArtistField, resolve = ctx => ArtistFetcher.defer(ctx.value.artist.id)),
      //Field("tracks", ListType(TrackField), resolve = ctx => TrackFetcher.deferSeq(ctx.value.tracks.data.map(_.id))),
    ))

  lazy val TrackField: ObjectLikeType[RootRepo, DeezerTrack] = ObjectType(
    "Track",
    "Track description.",
    fields[RootRepo, DeezerTrack](
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
        resolve = ctx => ArtistFetcher.deferSeqOpt(ctx.value.contributors.map(_.id))
      ),
      Field("artist_id", IntType, resolve = _.value.artist.id),
      Field("album_id", IntType, resolve = _.value.album.id),
      Field("artist", OptionType(ArtistField), resolve = ctx => ArtistFetcher.deferOpt(ctx.value.artist.id)),
      Field("album", OptionType(AlbumField), resolve = ctx => AlbumFetcher.deferOpt(ctx.value.album.id)),
    ))

  // arguments

  // root

  val Query = ObjectType(
    "Query", fields[RootRepo, Unit](
      Field("track", OptionType(TrackField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ TrackFetcher.deferOpt(ctx.arg[Int]("id"))),
      Field("artist", OptionType(ArtistField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ ArtistFetcher.deferOpt(ctx.arg[Int]("id"))),
      Field("album", OptionType(AlbumField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ AlbumFetcher.deferOpt(ctx.arg[Int]("id"))),
      Field("genre", OptionType(GenreField),
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ GenreFetcher.deferOpt(ctx.arg[Int]("id"))),
    ))

  val KroomSchema = Schema(Query)
}
