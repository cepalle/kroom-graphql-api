package deezer

import root.RepoRoot
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{BooleanType, EnumType, EnumValue, Field, FloatType, IntType, ListType, ObjectType, OptionType, StringType, fields}

import scala.concurrent.Future

object SchemaDeezer {

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val GenreFetcherId: Fetcher[RepoRoot, DataDeezerGenre, DataDeezerGenre, Int] =
    Fetcher.caching((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.deezer.getGenreById(id))
    }
    )(HasId(_.id))

  lazy val ArtistFetcherId: Fetcher[RepoRoot, DataDeezerArtist, DataDeezerArtist, Int] =
    Fetcher.caching((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.deezer.getArtistById(id))
    }
    )(HasId(_.id))

  lazy val AlbumFetcherId: Fetcher[RepoRoot, DataDeezerAlbum, DataDeezerAlbum, Int] =
    Fetcher.caching((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.deezer.getAlbumById(id))
    }
    )(HasId(_.id))

  lazy val TrackFetcherId: Fetcher[RepoRoot, DataDeezerTrack, DataDeezerTrack, Int] =
    Fetcher.caching((ctx: RepoRoot, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.deezer.getTrackById(id))
    }
    )(HasId(_.id))

  lazy val GenreField: ObjectType[Unit, DataDeezerGenre] = ObjectType(
    "Genre",
    "Genre description.",
    () ⇒ fields[Unit, DataDeezerGenre](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("picture", StringType, resolve = _.value.picture),
      Field("picture_small", StringType, resolve = _.value.picture_small),
      Field("picture_medium", StringType, resolve = _.value.picture_medium),
      Field("picture_big", StringType, resolve = _.value.picture_big),
      Field("picture_xl", StringType, resolve = _.value.picture_xl),
    ))

  lazy val ArtistField: ObjectType[Unit, DataDeezerArtist] = ObjectType(
    "Artist",
    "Artist description.",
    () ⇒ fields[Unit, DataDeezerArtist](
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

  lazy val AlbumField: ObjectType[Unit, DataDeezerAlbum] = ObjectType(
    "Album",
    "Album description.",
    () ⇒ fields[Unit, DataDeezerAlbum](
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
      Field("tracks", ListType(TrackField), resolve = ctx => TrackFetcherId.deferSeqOpt(ctx.value.tracks.data.map(_.id))),
    ))

  lazy val TrackField: ObjectType[Unit, DataDeezerTrack] = ObjectType(
    "Track",
    "Track description.",
    () ⇒ fields[Unit, DataDeezerTrack](
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

  val ConnectionEnum = EnumType(
    "Connection",
    Some("exemple: search/album"),
    List(
      EnumValue("album",
        value = Connections.album),
      EnumValue("artist",
        value = Connections.artist),
      EnumValue("history",
        value = Connections.history),
      EnumValue("playlist",
        value = Connections.playlist),
      EnumValue("podcast",
        value = Connections.podcast),
      EnumValue("radio",
        value = Connections.radio),
      EnumValue("track",
        value = Connections.track),
      EnumValue("user",
        value = Connections.user),
    )
  )

  val OrderEnum = EnumType(
    "Order",
    Some("Sort Order"),
    List(
      EnumValue("RANKING",
        value = Order.RANKING),
      EnumValue("ALBUM_ASC",
        value = Order.ALBUM_ASC),
      EnumValue("ALBUM_DESC",
        value = Order.ALBUM_DESC),
      EnumValue("ARTIST_ASC",
        value = Order.ARTIST_ASC),
      EnumValue("ARTIST_DESC",
        value = Order.ARTIST_DESC),
      EnumValue("DURATION_ASC",
        value = Order.DURATION_ASC),
      EnumValue("DURATION_DESC",
        value = Order.DURATION_DESC),
      EnumValue("RATING_ASC",
        value = Order.RATING_ASC),
      EnumValue("RATING_DESC",
        value = Order.RATING_DESC),
      EnumValue("TRACK_ASC",
        value = Order.TRACK_ASC),
      EnumValue("TRACK_DESC",
        value = Order.TRACK_DESC),
    )
  )

  lazy val SearchField: ObjectType[Unit, DataDeezerSearch] = ObjectType(
    "Serach",
    "Serach description.",
    () ⇒ fields[Unit, DataDeezerSearch](
      Field("id", IntType, resolve = _.value.id),
      Field("readable", BooleanType, resolve = _.value.readable),
      Field("title", StringType, resolve = _.value.title),
      Field("title_short", StringType, resolve = _.value.title_short),
      Field("title_version", StringType, resolve = _.value.title_version),

      Field("link", StringType, resolve = _.value.link),
      Field("duration", IntType, resolve = _.value.duration),
      Field("rank", IntType, resolve = _.value.rank),
      Field("explicit_lyrics", BooleanType, resolve = _.value.explicit_lyrics),
      Field("preview", StringType, resolve = _.value.preview),

      Field("artist", OptionType(ArtistField), resolve = ctx => ArtistFetcherId.deferOpt(ctx.value.artist.id)),
      Field("album", OptionType(AlbumField), resolve = ctx => AlbumFetcherId.deferOpt(ctx.value.album.id)),
    ))

}
