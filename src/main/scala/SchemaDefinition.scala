import sangria.schema._


/**
  * Defines a GraphQL schema for the current project
  */
object SchemaDefinition {
  /**
    * Resolves the lists of characters. These resolutions are batched and
    * cached for the duration of a query.
    */

  val ArtistField = ObjectType(
    "Artist",
    "Artist description.",
    fields[RootRepo, DeezerTrackArtist](
      Field("id", IntType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("link", StringType, resolve = _.value.link),
      Field("share", StringType, resolve = _.value.share),
      Field("picture", StringType, resolve = _.value.picture),
      Field("picture_small", StringType, resolve = _.value.picture_small),
      Field("picture_medium", StringType, resolve = _.value.picture_medium),
      Field("picture_big", StringType, resolve = _.value.picture_big),
      Field("picture_xl", StringType, resolve = _.value.picture_xl),
      Field("radio", BooleanType, resolve = _.value.radio),
      Field("tracklist", StringType, resolve = _.value.tracklist),
    ))

  val AlbumField = ObjectType(
    "Album",
    "Album description.",
    fields[RootRepo, DeezerTrackAlbum](
      Field("id", IntType, resolve = _.value.id),
      Field("title", StringType, resolve = _.value.title),
      Field("link", StringType, resolve = _.value.link),
      Field("cover", StringType, resolve = _.value.cover),
      Field("cover_small", StringType, resolve = _.value.cover_small),
      Field("cover_medium", StringType, resolve = _.value.cover_medium),
      Field("cover_big", StringType, resolve = _.value.cover_big),
      Field("cover_xl", StringType, resolve = _.value.cover_xl),
      Field("release_date", StringType, resolve = _.value.release_date),
    ))

  val TrackField = ObjectType(
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
      Field("contributors", ListType(ArtistField), resolve = _.value.contributors),
      Field("artist", ArtistField, resolve = _.value.artist),
      Field("album", AlbumField, resolve = _.value.album),
    ))

  val Query = ObjectType(
    "Query", fields[RootRepo, Unit](
      Field("tracks", ListType(TrackField),
        resolve = ctx ⇒ ctx.ctx.getTracks),
    ))

  val StarWarsSchema = Schema(Query)
}
