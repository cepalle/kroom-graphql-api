package io.kroom.api.deezer

import io.kroom.api.SecureContext
import io.kroom.api.root.SchemaRoot
import io.kroom.api.util.DataPayload
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{BooleanType, EnumType, EnumValue, Field, FloatType, IntType, ListType, ObjectType, OptionType, StringType, fields}

import scala.concurrent.Future

object SchemaDeezer {

  import scala.concurrent.ExecutionContext.Implicits.global

  /* FETCHER */

  lazy val GenreFetcherId: Fetcher[SecureContext, DataDeezerGenre, DataDeezerGenre, Int] =
    Fetcher.caching((ctx: SecureContext, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.repo.deezer.getGenreById(id).toOption)
    }
    )(HasId(_.id))

  lazy val ArtistFetcherId: Fetcher[SecureContext, DataDeezerArtist, DataDeezerArtist, Int] =
    Fetcher.caching((ctx: SecureContext, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.repo.deezer.getArtistById(id).toOption)
    }
    )(HasId(_.id))

  lazy val AlbumFetcherId: Fetcher[SecureContext, DataDeezerAlbum, DataDeezerAlbum, Int] =
    Fetcher.caching((ctx: SecureContext, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.repo.deezer.getAlbumById(id).toOption)
    }
    )(HasId(_.id))

  lazy val TrackFetcherId: Fetcher[SecureContext, DataDeezerTrack, DataDeezerTrack, Int] =
    Fetcher.caching((ctx: SecureContext, ids: Seq[Int]) ⇒ Future {
      ids.flatMap(id => ctx.repo.deezer.getTrackById(id).toOption)
    }
    )(HasId(_.id))

  /* PAYLOAD */

  lazy val DeezerGenrePayload: ObjectType[SecureContext, DataPayload[DataDeezerGenre]] = ObjectType(
    "GenreFieldPayload",
    "GenreFieldPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataDeezerGenre]](
      Field("data", OptionType(GenreField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val DeezerArtistPayload: ObjectType[SecureContext, DataPayload[DataDeezerArtist]] = ObjectType(
    "ArtistFieldPayload",
    "ArtistFieldPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataDeezerArtist]](
      Field("data", OptionType(ArtistField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val DeezerAlbumPayload: ObjectType[SecureContext, DataPayload[DataDeezerAlbum]] = ObjectType(
    "AlbumFieldPayload",
    "AlbumFieldPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataDeezerAlbum]](
      Field("data", OptionType(AlbumField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val DeezerTrackPayload: ObjectType[SecureContext, DataPayload[DataDeezerTrack]] = ObjectType(
    "TrackFieldPayload",
    "TrackFieldPayload description.",
    () ⇒ fields[SecureContext, DataPayload[DataDeezerTrack]](
      Field("data", OptionType(TrackField), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  lazy val DeezerSearchPayload: ObjectType[SecureContext, DataPayload[List[DataDeezerSearch]]] = ObjectType(
    "SearchFieldsPayload",
    "SearchFieldsPayload description.",
    () ⇒ fields[SecureContext, DataPayload[List[DataDeezerSearch]]](
      Field("data", OptionType(ListType(SearchField)), resolve = _.value.data),
      Field("errors", ListType(SchemaRoot.ErrorField), resolve = _.value.errors),
    ))

  /* FIELD */

  lazy val GenreField: ObjectType[SecureContext, DataDeezerGenre] = ObjectType(
    "Genre",
    "Genre description.",
    () ⇒ fields[SecureContext, DataDeezerGenre](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("picture", StringType, resolve = _.value.picture),
      Field("pictureSmall", StringType, resolve = _.value.picture_small),
      Field("pictureMedium", StringType, resolve = _.value.picture_medium),
      Field("pictureBig", StringType, resolve = _.value.picture_big),
      Field("pictureXl", StringType, resolve = _.value.picture_xl),
    ))

  lazy val ArtistField: ObjectType[SecureContext, DataDeezerArtist] = ObjectType(
    "Artist",
    "Artist description.",
    () ⇒ fields[SecureContext, DataDeezerArtist](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("link", StringType, resolve = _.value.link),
      Field("share", StringType, resolve = _.value.share),
      Field("picture", StringType, resolve = _.value.picture),
      Field("pictureSmall", StringType, resolve = _.value.picture_small),
      Field("pictureMedium", StringType, resolve = _.value.picture_medium),
      Field("pictureBig", StringType, resolve = _.value.picture_big),
      Field("pictureXl", StringType, resolve = _.value.picture_xl),
      Field("nbAlbum", IntType, resolve = _.value.nb_album),
      Field("nbFan", IntType, resolve = _.value.nb_fan),
      Field("tracklist", StringType, resolve = _.value.tracklist),
    ))

  lazy val AlbumField: ObjectType[SecureContext, DataDeezerAlbum] = ObjectType(
    "Album",
    "Album description.",
    () ⇒ fields[SecureContext, DataDeezerAlbum](
      Field("id", IntType, resolve = _.value.id),
      Field("title", StringType, resolve = _.value.title),
      Field("link", StringType, resolve = _.value.link),
      Field("cover", StringType, resolve = _.value.cover),
      Field("coverSmall", StringType, resolve = _.value.cover_small),
      Field("coverMedium", StringType, resolve = _.value.cover_medium),
      Field("coverBig", StringType, resolve = _.value.cover_big),
      Field("coverXl", StringType, resolve = _.value.cover_xl),
      Field("genreId", IntType, resolve = _.value.genre_id),
      Field("genre", OptionType(GenreField), resolve = ctx => GenreFetcherId.deferOpt(ctx.value.genre_id)),
      // Field("genre_ids", ListType(IntType), resolve = _.value.contributors.map(_.id)),
      // Field("genres", ListType(GenreField),
      //  resolve = ctx => GenreFetcher.deferSeqOpt(ctx.value.contributors.map(_.id))
      // ),
      Field("label", StringType, resolve = _.value.label),
      Field("nbTracks", IntType, resolve = _.value.nb_tracks),
      Field("duration", IntType, resolve = _.value.duration),
      Field("fans", IntType, resolve = _.value.fans),
      Field("rating", IntType, resolve = _.value.rating),
      Field("releaseDate", StringType, resolve = _.value.release_date),
      Field("recordType", StringType, resolve = _.value.record_type),
      Field("available", BooleanType, resolve = _.value.available),
      Field("tracklist", StringType, resolve = _.value.tracklist),
      Field("explicitLyrics", BooleanType, resolve = _.value.explicit_lyrics),
      Field("explicitContentLyrics", IntType, resolve = _.value.explicit_content_lyrics),
      Field("explicitContentCover", IntType, resolve = _.value.explicit_content_cover),
      Field("contributors", ListType(ArtistField), resolve = ctx => ArtistFetcherId.deferSeq(ctx.value.contributors.map(_.id))),
      Field("artistId", IntType, resolve = _.value.artist.id),
      Field("artist", ArtistField, resolve = ctx => ArtistFetcherId.defer(ctx.value.artist.id)),
      Field("tracks", ListType(TrackField), resolve = ctx => TrackFetcherId.deferSeqOpt(ctx.value.tracks.data.map(_.id))),
    ))

  lazy val TrackField: ObjectType[SecureContext, DataDeezerTrack] = ObjectType(
    "Track",
    "Track description.",
    () ⇒ fields[SecureContext, DataDeezerTrack](
      Field("id", IntType, resolve = _.value.id),
      Field("readable", BooleanType, resolve = _.value.readable),
      Field("title", StringType, resolve = _.value.title),
      Field("titleShort", StringType, resolve = _.value.title_short),
      Field("titleVersion", StringType, resolve = _.value.title_version),
      Field("isrc", StringType, resolve = _.value.isrc),
      Field("link", StringType, resolve = _.value.link),
      Field("share", StringType, resolve = _.value.share),
      Field("duration", IntType, resolve = _.value.duration),
      Field("trackPosition", IntType, resolve = _.value.track_position),
      Field("diskNumber", IntType, resolve = _.value.disk_number),
      Field("rank", IntType, resolve = _.value.rank),
      Field("releaseDate", StringType, resolve = _.value.release_date),
      Field("explicitLyrics", BooleanType, resolve = _.value.explicit_lyrics),
      Field("explicitContentLyrics", IntType, resolve = _.value.explicit_content_lyrics),
      Field("explicitContentCover", IntType, resolve = _.value.explicit_content_cover),
      Field("preview", StringType, resolve = _.value.preview),
      Field("bpm", FloatType, resolve = _.value.bpm),
      Field("gain", FloatType, resolve = _.value.gain),
      Field("availableCountries", ListType(StringType), resolve = _.value.available_countries),
      Field("contributors", ListType(ArtistField),
        resolve = ctx => ArtistFetcherId.deferSeqOpt(ctx.value.contributors.map(_.id))
      ),
      Field("artistId", IntType, resolve = _.value.artist.id),
      Field("albumId", IntType, resolve = _.value.album.id),
      Field("artist", OptionType(ArtistField), resolve = ctx => ArtistFetcherId.deferOpt(ctx.value.artist.id)),
      Field("album", OptionType(AlbumField), resolve = ctx => AlbumFetcherId.deferOpt(ctx.value.album.id)),
    ))

  lazy val ConnectionEnum = EnumType(
    "Connection",
    Some("exemple: search/album"),
    List(
      EnumValue("ALBUM",
        value = Connections.album),
      EnumValue("ARTIST",
        value = Connections.artist),
      EnumValue("HISTORY",
        value = Connections.history),
      EnumValue("PLAYLIST",
        value = Connections.playlist),
      EnumValue("PODCAST",
        value = Connections.podcast),
      EnumValue("RADIO",
        value = Connections.radio),
      EnumValue("TRACK",
        value = Connections.track),
      EnumValue("USER",
        value = Connections.user),
    )
  )

  lazy val OrderEnum = EnumType(
    "Order",
    Some("Sort Order"),
    List(
      EnumValue("RANKING",
        value = Order.ranking),
      EnumValue("ALBUM_ASC",
        value = Order.albumASC),
      EnumValue("ALBUM_DESC",
        value = Order.albumDESC),
      EnumValue("ARTIST_ASC",
        value = Order.artistASC),
      EnumValue("ARTIST_DESC",
        value = Order.artistDESC),
      EnumValue("DURATION_ASC",
        value = Order.durationASC),
      EnumValue("DURATION_DESC",
        value = Order.durationDESC),
      EnumValue("RATING_ASC",
        value = Order.ratingASC),
      EnumValue("RATING_DESC",
        value = Order.ratingDESC),
      EnumValue("TRACK_ASC",
        value = Order.trackASC),
      EnumValue("TRACK_DESC",
        value = Order.trackDESC),
    )
  )

  lazy val SearchField: ObjectType[SecureContext, DataDeezerSearch] = ObjectType(
    "Search",
    "Search description.",
    () ⇒ fields[SecureContext, DataDeezerSearch](
      Field("id", IntType, resolve = _.value.id),
      Field("readable", BooleanType, resolve = _.value.readable),
      Field("title", StringType, resolve = _.value.title),
      Field("titleShort", StringType, resolve = _.value.title_short),

      Field("link", StringType, resolve = _.value.link),
      Field("duration", IntType, resolve = _.value.duration),
      Field("rank", IntType, resolve = _.value.rank),
      Field("explicitLyrics", BooleanType, resolve = _.value.explicit_lyrics),
      Field("preview", StringType, resolve = _.value.preview),

      Field("artist", OptionType(ArtistField), resolve = ctx => ArtistFetcherId.deferOpt(ctx.value.artist.id)),
      Field("album", OptionType(AlbumField), resolve = ctx => AlbumFetcherId.deferOpt(ctx.value.album.id)),
    ))

}
