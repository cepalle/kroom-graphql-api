package io.kroom.api.root

import io.kroom.api.Authorization.Privacy
import io.kroom.api.Authorization.Permissions
import io.kroom.api.SecureContext
import io.kroom.api.Server.system
import io.kroom.api.user.{DataUser, SchemaUser}
import io.kroom.api.deezer._
import io.kroom.api.playlisteditor.DataPlaylistEditor
import io.kroom.api.trackvoteevent.{DataTrackVoteEvent, RepoTrackVoteEvent}
import io.kroom.api.util.{DataError, DataPayload, DistanceGeo}
import sangria.schema._
import javax.mail.internet.{AddressException, InternetAddress}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Defines a GraphQL schema for the current project
  */
object SchemaRoot {

  import SchemaUser._
  import io.kroom.api.deezer.SchemaDeezer._
  import io.kroom.api.trackvoteevent.SchemaTrackVoteEvent._
  import io.kroom.api.playlisteditor.SchemaPlaylistEditor._
  import system.dispatcher

  lazy val PrivacyEnum = EnumType(
    "PrivacyEnum",
    Some("PrivacyEnum"),
    List(
      EnumValue("PUBLIC",
        value = Privacy.public),
      EnumValue("FRIENDS",
        value = Privacy.friends),
      EnumValue("PRIVATE",
        value = Privacy.`private`),
    )
  )

  lazy val ErrorField: ObjectType[SecureContext, DataError] = ObjectType(
    "Error",
    "Error description.",
    () ⇒ fields[SecureContext, DataError](
      Field("field", StringType, resolve = _.value.field),
      Field("messages", ListType(StringType), resolve = _.value.errors),
    ))

  lazy val Query = ObjectType(
    "Query", fields[SecureContext, Unit](

      /* DEEZER */

      Field("DeezerTrack", DeezerTrackPayload,
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.DeezerTrack) { () =>
            ctx.ctx.repo.deezer.getTrackById(ctx.arg[Int]("id")) match {
              case Success(value) => DataPayload[DataDeezerTrack](Some(value), List())
              case Failure(_) => DataPayload[DataDeezerTrack](None, List(
                DataError("id", List("Track Id not found")))
              )
            }
          }.get
        }),

      Field("DeezerArtist", DeezerArtistPayload,
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.DeezerArtist) { () =>
            ctx.ctx.repo.deezer.getArtistById(ctx.arg[Int]("id")) match {
              case Success(value) => DataPayload[DataDeezerArtist](Some(value), List())
              case Failure(_) => DataPayload[DataDeezerArtist](None, List(
                DataError("id", List("Artist Id not found")))
              )
            }
          }.get
        }),

      Field("DeezerAlbum", DeezerAlbumPayload,
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.DeezerAlbum) { () =>
            ctx.ctx.repo.deezer.getAlbumById(ctx.arg[Int]("id")) match {
              case Success(value) => DataPayload[DataDeezerAlbum](Some(value), List())
              case Failure(_) => DataPayload[DataDeezerAlbum](None, List(
                DataError("id", List("Album Id not found")))
              )
            }
          }.get
        }),

      Field("DeezerGenre", DeezerGenrePayload,
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.DeezerGenre) { () =>
            ctx.ctx.repo.deezer.getGenreById(ctx.arg[Int]("id")) match {
              case Success(value) => DataPayload[DataDeezerGenre](Some(value), List())
              case Failure(_) => DataPayload[DataDeezerGenre](None, List(
                DataError("id", List("Genre Id not found")))
              )
            }
          }.get
        }),

      Field("DeezerSearch", DeezerSearchPayload,
        arguments = Argument("search", StringType)
          :: Argument("connections", OptionInputType(ConnectionEnum))
          :: Argument("strict", BooleanType)
          :: Argument("order", OptionInputType(OrderEnum))
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.DeezerSearch) { () =>
            ctx.ctx.repo.deezer.getSearch(
              ctx.arg[String]("search"),
              ctx.argOpt[Connections.Value]("connections"),
              ctx.arg[Boolean]("strict"),
              ctx.argOpt[Order.Value]("order"),
            ) match {
              case Success(value) => DataPayload[List[DataDeezerSearch]](Some(value), List())
              case Failure(_) => DataPayload[List[DataDeezerSearch]](Some(List()), List())
            }
          }.get
        }),

      /* TRACK_VOTE_EVENT */

      Field("TrackVoteEventsPublic", ListType(TrackVoteEventField), Some("Return all TrackVoteEvent public."),
        arguments = Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventsPublic) { () =>
            ctx.ctx.repo.trackVoteEvent.getPublic.get
          }.get
        }),

      Field("TrackVoteEventById", TrackVoteEventByIdPayload,
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventById) { () => {
            ctx.ctx.repo.trackVoteEvent.getById(ctx.arg[Int]("id")) match {
              case Success(value) => DataPayload[DataTrackVoteEvent](Some(value), List())
              case Failure(_) => DataPayload[DataTrackVoteEvent](None, List(
                DataError("id", List("TrackVoteEvent Id not found"))
              ))
            }
          }
          }.get
        }),

      Field("TrackVoteEventByUserId", TrackVoteEventByUserIdPayload, Some("Return all TrackVoteEvent where User are invited."),
        arguments = Argument("userId", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventByUserId) { () =>
            ctx.ctx.repo.trackVoteEvent.getByUserId(ctx.arg[Int]("userId")) match {
              case Success(value) => DataPayload[List[DataTrackVoteEvent]](Some(value), List())
              case Failure(_) => DataPayload[List[DataTrackVoteEvent]](None, List(
                DataError("userId", List("User Id not found")))
              )
            }
          }.get
        }),

      /* USER */

      Field("UserGetById", UserGetByIdPayload,
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserGetById) { () =>
          Future {
            ctx.ctx.repo.user.getById(ctx.arg[Int]("id")) match {
              case Success(value) => DataPayload[DataUser](Some(value), List())
              case Failure(_) => DataPayload[DataUser](None, List(
                DataError("id", List("User Id not found")))
              )
            }
          }
        }.get),

      Field("UserNameAutocompletion", ListType(UserField),
        arguments = Argument("prefix", StringType) :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserNameAutocompletion) { () =>
          Future {
            ctx.ctx.repo.user.getCompletion(ctx.arg[String]("prefix")) match {
              case Success(value) => value
              case Failure(_) => List()
            }
          }
        }.get),

      /* PLAY_LIST_EDITOR */

      Field("PlayListEditorsPublic", ListType(PlayListEditorField), Some("Return all PlayListEditor public."),
        arguments = Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorsPublic) { () =>
            ctx.ctx.repo.playListEditor.getPublic.get
          }.get
        }),

      Field("PlayListEditorById", PlayListEditorByIdPayload,
        arguments = Argument("id", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorById) { () => {
            ctx.ctx.repo.playListEditor.getById(ctx.arg[Int]("id")) match {
              case Success(value) => DataPayload[DataPlaylistEditor](Some(value), List())
              case Failure(_) => DataPayload[DataPlaylistEditor](None, List(
                DataError("id", List("PlayListEditor Id not found"))
              ))
            }
          }
          }.get
        }),

      Field("PlayListEditorByUserId", PlayListEditorByUserIdPayload, Some("Return all PlayListEditor where User are invited."),
        arguments = Argument("userId", IntType) :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorByUserId) { () =>
            ctx.ctx.repo.playListEditor.getByUserId(ctx.arg[Int]("userId")) match {
              case Success(value) => DataPayload[List[DataPlaylistEditor]](Some(value), List())
              case Failure(_) => DataPayload[List[DataPlaylistEditor]](None, List(
                DataError("userId", List("User Id not found")))
              )
            }
          }.get
        }),

    ))

  val Mutation = ObjectType(
    "Mutation", fields[SecureContext, Unit](

      /* TRACK_VOTE_EVENT */

      Field("TrackVoteEventNew", TrackVoteEventNewPayload,
        arguments = Argument("userIdMaster", IntType)
          :: Argument("name", StringType)
          :: Argument("public", BooleanType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventNew) { () => {
            val userIdMaster = ctx.arg[Int]("userIdMaster")
            val name = ctx.arg[String]("name")
            val public = ctx.arg[Boolean]("public")

            val errors = {
              val userIdMasterErrors = {
                DataError("userIdMaster", List[Option[String]](
                  ctx.ctx.repo.user.getById(userIdMaster) match {
                    case Success(_) => None
                    case Failure(_) => Some("userIdMaster not found")
                  },
                  userIdMaster == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userIdMaster isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              val nameErrors = {
                val lower1 = """(?=.*[a-z])""".r
                val charValid = """^([a-zA-Z0-9_-]*)$""".r
                val length4 = """(?=.{4,})""".r

                DataError("name", List[Option[String]](
                  length4.findFirstMatchIn(name) match {
                    case Some(_) => None
                    case None => Some("name need 4 character")
                  },
                  lower1.findFirstMatchIn(name) match {
                    case Some(_) => None
                    case None => Some("name need 1 lowercase")
                  },
                  charValid.findFirstMatchIn(name) match {
                    case Some(_) => None
                    case None => Some("name can only contain lowercase, uppercase, underscore and hyphen")
                  },
                  ctx.ctx.repo.trackVoteEvent.getByName(name) match {
                    case Success(_) => Some("name already exist")
                    case Failure(_) => None
                  }
                ) collect { case Some(s) => s })
              }

              List(userIdMasterErrors, nameErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val trackEvent = ctx.ctx.repo.trackVoteEvent.`new`(userIdMaster, name, public).get
              DataPayload[DataTrackVoteEvent](Some(trackEvent), List())
            } else {
              DataPayload[DataTrackVoteEvent](None, errors)
            }
          }
          }.get
        }
      ),

      Field("TrackVoteEventDel", TrackVoteEventDelPayload,
        arguments = Argument("id", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventDel) { () => {
            val id = ctx.arg[Int]("id")

            val errors = {

              val idErrors = {

                DataError("id", List[Option[String]](
                  ctx.ctx.repo.trackVoteEvent.getById(id) match {
                    case Success(s) => if (s.userMasterId == ctx.ctx.user.id) {
                      None
                    } else {
                      Some("You aren't the master")
                    }
                    case Failure(_) => Some("eventId not found")
                  }
                ) collect { case Some(s) => s })
              }

              List(idErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              ctx.ctx.repo.trackVoteEvent.delete(id).get
              DataPayload[Unit](Some(Unit), List())
            } else {
              DataPayload[Unit](None, errors)
            }
          }
          }.get
        }
      ),

      Field("TrackVoteEventUpdate", TrackVoteEventUpdatePayload,
        arguments = Argument("eventId", IntType)
          :: Argument("userIdMaster", IntType)
          :: Argument("name", StringType)
          :: Argument("public", BooleanType)
          :: Argument("locAndSchRestriction", BooleanType)
          :: Argument("scheduleBegin", OptionInputType(LongType))
          :: Argument("scheduleEnd", OptionInputType(LongType))
          :: Argument("latitude", OptionInputType(FloatType))
          :: Argument("longitude", OptionInputType(FloatType))
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventUpdate) { () =>

            val eventId = ctx.arg[Int]("eventId")
            val userIdMaster = ctx.arg[Int]("userIdMaster")
            val name = ctx.arg[String]("name")
            val public = ctx.arg[Boolean]("public")
            val locAndSchRestriction = ctx.arg[Boolean]("locAndSchRestriction")
            val scheduleBegin = ctx.argOpt[Long]("scheduleBegin")
            val scheduleEnd = ctx.argOpt[Long]("scheduleEnd")
            val latitude = ctx.argOpt[Double]("latitude")
            val longitude = ctx.argOpt[Double]("longitude")

            val errors = {
              val eventIdErrors = {
                DataError("eventId", List[Option[String]](
                  ctx.ctx.repo.trackVoteEvent.getById(eventId) match {
                    case Success(s) => if (s.userMasterId == ctx.ctx.user.id) {
                      None
                    } else {
                      Some("You aren't the master")
                    }
                    case Failure(_) => Some("eventId not found")
                  }
                ) collect { case Some(s) => s })
              }

              val userIdMasterErrors = {
                DataError("userIdMaster", List[Option[String]](
                  ctx.ctx.repo.user.getById(userIdMaster) match {
                    case Success(_) => None
                    case Failure(_) => Some("userIdMaster not found")
                  }
                ) collect { case Some(s) => s })
              }

              val nameErrors = {
                val lower1 = """(?=.*[a-z])""".r
                val charValid = """^([a-zA-Z0-9_-]*)$""".r
                val length4 = """(?=.{4,})""".r

                DataError("name", List[Option[String]](
                  length4.findFirstMatchIn(name) match {
                    case Some(_) => None
                    case None => Some("name need 4 character")
                  },
                  lower1.findFirstMatchIn(name) match {
                    case Some(_) => None
                    case None => Some("name need 1 lowercase")
                  },
                  charValid.findFirstMatchIn(name) match {
                    case Some(_) => None
                    case None => Some("name can only contain lowercase, uppercase, underscore and hyphen")
                  },
                  ctx.ctx.repo.trackVoteEvent.getByName(name) match {
                    case Success(s) => if (s.id == eventId) {
                      None
                    } else {
                      Some("name already exist")
                    }
                    case Failure(_) => None
                  }
                ) collect { case Some(s) => s })
              }

              val latitudeError = {
                DataError("latitude", List[Option[String]](
                  if (latitude.exists(_ > 90.0)) {
                    Some("latitude is =< 90")
                  } else {
                    None
                  },
                  if (latitude.exists(_ < -90.0)) {
                    Some("latitude is => -90")
                  } else {
                    None
                  },
                ) collect { case Some(s) => s })
              }

              val longitudeError = {
                DataError("longitude", List[Option[String]](
                  if (longitude.exists(_ > 180.0)) {
                    Some("longitude is =< 180")
                  } else {
                    None
                  },
                  if (longitude.exists(_ < -180.0)) {
                    Some("longitude is => -180")
                  } else {
                    None
                  },
                ) collect { case Some(s) => s })
              }

              val scheduleBeginError = {
                DataError("scheduleBegin", List[Option[String]](
                  if (scheduleBegin.exists(b => scheduleEnd.exists(_ < b))) {
                    Some("scheduleBegin is before scheduleEnd")
                  } else {
                    None
                  },
                ) collect { case Some(s) => s })
              }

              List(
                eventIdErrors,
                userIdMasterErrors,
                nameErrors,
                latitudeError,
                longitudeError,
                scheduleBeginError
              ).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val trackEvent = ctx.ctx.repo.trackVoteEvent.update(eventId, userIdMaster, name, public, locAndSchRestriction, scheduleBegin, scheduleEnd, latitude, longitude).get
              DataPayload[DataTrackVoteEvent](Some(trackEvent), List())
            } else {
              DataPayload[DataTrackVoteEvent](None, errors)
            }
          }.get
        }
      ),

      Field("TrackVoteEventAddUser", TrackVoteEventAddUserPayload,
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventAddUser) { () => {
            val eventId = ctx.arg[Int]("eventId")
            val userId = ctx.arg[Int]("userId")

            val errors = {
              val eventIdErrors = {
                DataError("eventId", List[Option[String]](
                  ctx.ctx.repo.trackVoteEvent.getById(eventId) match {
                    case Success(s) => if (s.userMasterId == ctx.ctx.user.id) {
                      None
                    } else {
                      Some("You aren't the master")
                    }
                    case Failure(_) => Some("eventId not found")
                  }
                ) collect { case Some(s) => s })
              }

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => ctx.ctx.repo.trackVoteEvent.getUserInvited(eventId)
                      .toOption
                      .map(_.map(_.id))
                      .map(_.contains(userId))
                      .flatMap(e => if (e) {
                        Some("user already invited")
                      } else {
                        None
                      })
                    case Failure(_) => Some("userId not found")
                  }
                ) collect { case Some(s) => s })
              }

              List(eventIdErrors, userIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val trackEvent = ctx.ctx.repo.trackVoteEvent.addUser(eventId, userId).get
              DataPayload[DataTrackVoteEvent](Some(trackEvent), List())
            } else {
              DataPayload[DataTrackVoteEvent](None, errors)
            }
          }
          }.get
        }
      ),

      Field("TrackVoteEventDelUser", TrackVoteEventDelUserPayload,
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventDelUser) { () =>
            val eventId = ctx.arg[Int]("eventId")
            val userId = ctx.arg[Int]("userId")

            val errors = {
              val eventIdErrors = {
                DataError("eventId", List[Option[String]](
                  ctx.ctx.repo.trackVoteEvent.getById(eventId) match {
                    case Success(s) => if (s.userMasterId == ctx.ctx.user.id) {
                      None
                    } else {
                      Some("You aren't the master")
                    }
                    case Failure(_) => Some("eventId not found")
                  }
                ) collect { case Some(s) => s })
              }

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => ctx.ctx.repo.trackVoteEvent.getUserInvited(eventId)
                      .toOption
                      .map(_.map(_.id))
                      .map(_.contains(userId))
                      .flatMap(e => if (!e) {
                        Some("user isn't invited")
                      } else {
                        None
                      })
                    case Failure(_) => Some("userId not found")
                  },
                  ctx.ctx.repo.trackVoteEvent.getById(eventId)
                    .toOption
                    .map(_.userMasterId)
                    .map(_ == userId)
                    .flatMap(e => if (e) {
                      Some("you can't delete the master")
                    } else {
                      None
                    })
                ) collect { case Some(s) => s })
              }

              List(eventIdErrors, userIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val trackEvent = ctx.ctx.repo.trackVoteEvent.delUser(eventId, userId).get
              DataPayload[DataTrackVoteEvent](Some(trackEvent), List())
            } else {
              DataPayload[DataTrackVoteEvent](None, errors)
            }
          }.get
        }
      ),

      Field("TrackVoteEventAddOrUpdateVote", TrackVoteEventAddOrUpdateVotePayload,
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Argument("musicId", IntType)
          :: Argument("up", BooleanType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventAddVote) { () => {
            val eventId = ctx.arg[Int]("eventId")
            val userId = ctx.arg[Int]("userId")
            val musicId = ctx.arg[Int]("musicId")
            val up = ctx.arg[Boolean]("up")

            val errors = {
              val eventIdErrors = {
                DataError("eventId", List[Option[String]](
                  ctx.ctx.repo.trackVoteEvent.getById(eventId) match {
                    case Success(s) => if (s.public) {
                      None
                    } else {
                      ctx.ctx.repo.trackVoteEvent.getUserInvited(eventId)
                        .toOption
                        .map(_.map(_.id))
                        .map(_.contains(userId))
                        .flatMap(e => if (!e) {
                          Some("user isn't invited in a private event")
                        } else {
                          None
                        })
                    }
                    case Failure(_) => Some("eventId not found")
                  },
                  ctx.ctx.repo.trackVoteEvent.getById(eventId).toOption.flatMap(s => {
                    val aujd = System.currentTimeMillis
                    if (s.locAndSchRestriction && (
                      s.scheduleEnd.exists(p => p < aujd) || s.scheduleBegin.exists(p => p > aujd) &&
                        !ctx.ctx.user.longitude.exists(longU =>
                          ctx.ctx.user.latitude.exists(latU =>
                            s.latitude.exists(latE =>
                              s.longitude.exists(longE =>
                                DistanceGeo.distanceGeo(latU, longU, latE, longE) < RepoTrackVoteEvent.distanceMax
                              )
                            )
                          )
                        )
                      )) {
                      Some("the event is restricted for a location and a schedule")
                    } else {
                      None
                    }
                  })
                ) collect { case Some(s) => s })
              }

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => None
                    case Failure(_) => Some("userId not found")
                  },
                  userId == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userId isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              val musicIdErrors = {
                DataError("musicId", List[Option[String]](
                  ctx.ctx.repo.deezer.getTrackById(musicId) match {
                    case Success(_) => None
                    case Failure(_) => Some("musicId not found")
                  },
                ) collect { case Some(s) => s })
              }

              List(eventIdErrors, userIdErrors, musicIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val trackEvent = ctx.ctx.repo.trackVoteEvent.addOrUpdateVote(eventId, userId, musicId, up).get
              DataPayload[DataTrackVoteEvent](Some(trackEvent), List())
            } else {
              DataPayload[DataTrackVoteEvent](None, errors)
            }
          }
          }.get
        }
      ),

      Field("TrackVoteEventDelVote", TrackVoteEventDelVotePayload,
        arguments = Argument("eventId", IntType)
          :: Argument("userId", IntType)
          :: Argument("musicId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventDelVote) { () =>

            val eventId = ctx.arg[Int]("eventId")
            val userId = ctx.arg[Int]("userId")
            val musicId = ctx.arg[Int]("musicId")

            val errors = {
              val eventIdErrors = {
                DataError("eventId", List[Option[String]](
                  ctx.ctx.repo.trackVoteEvent.getById(eventId) match {
                    case Success(_) => None
                    case Failure(_) => Some("eventId not found")
                  },
                  ctx.ctx.repo.trackVoteEvent.hasVote(eventId, userId, musicId)
                    .toOption
                    .flatMap(e => if (e) {
                      None
                    } else {
                      Some("Vote not found")
                    })
                ) collect { case Some(s) => s })
              }

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => None
                    case Failure(_) => Some("userId not found")
                  },
                  userId == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userId isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              val musicIdErrors = {
                DataError("musicId", List[Option[String]](
                  ctx.ctx.repo.deezer.getTrackById(musicId) match {
                    case Success(_) => None
                    case Failure(_) => Some("musicId not found")
                  }
                ) collect { case Some(s) => s })
              }

              List(eventIdErrors, userIdErrors, musicIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val trackEvent = ctx.ctx.repo.trackVoteEvent.delVote(eventId, userId, musicId).get
              DataPayload[DataTrackVoteEvent](Some(trackEvent), List())
            } else {
              DataPayload[DataTrackVoteEvent](None, errors)
            }
          }.get
        }
      ),

      /* USER */

      Field("UserSignUp", UserSignUpPayload,
        arguments = Argument("userName", StringType)
          :: Argument("email", StringType)
          :: Argument("pass", StringType)
          :: Nil,
        resolve = ctx ⇒ {
          ctx.ctx.authorised(Permissions.UserSignUp) { () => {
            val userName = ctx.arg[String]("userName")
            val email = ctx.arg[String]("email")
            val pass = ctx.arg[String]("pass")

            val errors = {
              val emailErrors = {
                val emailIsValid = try {
                  new InternetAddress(email).validate()
                  true
                } catch {
                  case _: AddressException => false
                }

                DataError("email", List[Option[String]](
                  emailIsValid match {
                    case true => None
                    case false => Some("email bad format")
                  },
                  ctx.ctx.repo.user.getByEmail(email) match {
                    case Success(_) => Some("email already exist")
                    case Failure(_) => None
                  }
                ) collect { case Some(s) => s })
              }

              val passErrors = {
                val lower1 = """(?=.*[a-z])""".r
                val upper1 = """(?=.*[A-Z])""".r
                val numeric1 = """(?=.*[0-9])""".r
                val length8 = """(?=.{8,})""".r

                DataError("pass", List[Option[String]](
                  lower1.findFirstMatchIn(pass) match {
                    case Some(_) => None
                    case None => Some("Password need a lowercase")
                  },
                  upper1.findFirstMatchIn(pass) match {
                    case Some(_) => None
                    case None => Some("Password need a uppercase")
                  },
                  numeric1.findFirstMatchIn(pass) match {
                    case Some(_) => None
                    case None => Some("Password need a number")
                  },
                  length8.findFirstMatchIn(pass) match {
                    case Some(_) => None
                    case None => Some("Password need 8 character")
                  },
                ) collect { case Some(s) => s })
              }

              val userNameErrors = {
                val lower1 = """(?=.*[a-z])""".r
                val charValid = """^([a-zA-Z0-9_-]*)$""".r
                val length4 = """(?=.{4,})""".r

                DataError("userName", List[Option[String]](
                  length4.findFirstMatchIn(userName) match {
                    case Some(_) => None
                    case None => Some("username need 4 character")
                  },
                  lower1.findFirstMatchIn(userName) match {
                    case Some(_) => None
                    case None => Some("username need 1 lowercase")
                  },
                  charValid.findFirstMatchIn(userName) match {
                    case Some(_) => None
                    case None => Some("username can only contain lowercase, uppercase, underscore and hyphen")
                  },
                  ctx.ctx.repo.user.getByName(userName) match {
                    case Success(_) => Some("userName already exist")
                    case Failure(_) => None
                  }
                ) collect { case Some(s) => s })
              }

              List(emailErrors, passErrors, userNameErrors).filter(e => e.errors.nonEmpty)
            }

            val payload = if (errors.isEmpty) {
              val user = ctx.ctx.repo.user.signUp(userName, email, pass).get
              DataPayload[DataUser](Some(user), List())
            } else {
              DataPayload[DataUser](None, errors)
            }

            UpdateCtx(payload) { p ⇒
              new SecureContext(p.data.flatMap(_.token), ctx.ctx.repo)
            }
          }
          }.get
        }
      ),

      Field("UserSignIn", UserSignInPayload,
        arguments = Argument("userName", StringType)
          :: Argument("pass", StringType)
          :: Nil,
        resolve = ctx ⇒ {
          ctx.ctx.authorised(Permissions.UserSignIn) { () => {
            val res = ctx.ctx.repo.user.signIn(
              ctx.arg[String]("userName"),
              ctx.arg[String]("pass"),
            ) match {
              case Success(value) =>
                DataPayload[DataUser](Some(value), List())
              case Failure(_) => DataPayload[DataUser](None, List(
                DataError("pass", List("username or password invalid")))
              )
            }
            UpdateCtx(res) { userPayload ⇒
              new SecureContext(userPayload.data.flatMap(_.token), ctx.ctx.repo)
            }
          }
          }.get
        }
      ),

      Field("UserSignWithGoogle", UserSignInPayload,
        arguments = Argument("token", StringType)
          :: Nil,
        resolve = ctx ⇒ {
          ctx.ctx.authorised(Permissions.UserSignIn) { () => {
            val res = ctx.ctx.repo.user.signWithGoogle(
              ctx.arg[String]("token"),
            ) match {
              case Success(value) =>
                DataPayload[DataUser](Some(value), List())
              case Failure(_) => DataPayload[DataUser](None, List(
                DataError("token", List("token invalid")))
              )
            }
            UpdateCtx(res) { userPayload ⇒
              new SecureContext(userPayload.data.flatMap(_.token), ctx.ctx.repo)
            }
          }
          }.get
        }
      ),

      Field("UserAddFriend", UserAddFriendPayload,
        arguments = Argument("userId", IntType)
          :: Argument("friendId", IntType)
          :: Nil,
        resolve = ctx ⇒
          Future {
            ctx.ctx.authorised(Permissions.UserAddFriend) { () =>
              val userId = ctx.arg[Int]("userId")
              val friendId = ctx.arg[Int]("friendId")

              val errors = {

                val userIdErrors = {
                  DataError("userId", List[Option[String]](
                    ctx.ctx.repo.user.getById(userId) match {
                      case Success(_) => None
                      case Failure(_) => Some("userId not found")
                    },
                    ctx.ctx.repo.user.getFriends(userId)
                      .toOption
                      .map(_.map(_.id))
                      .map(_.contains(friendId))
                      .flatMap(b => if (b) {
                        Some("you are already friend")
                      } else {
                        None
                      }),
                    userId == ctx.ctx.user.id match {
                      case true => None
                      case false => Some("userId isn't you")
                    }
                  ) collect { case Some(s) => s })
                }

                val friendIdErrors = {
                  DataError("friendId", List[Option[String]](
                    ctx.ctx.repo.user.getById(friendId) match {
                      case Success(_) => None
                      case Failure(_) => Some("friendId not found")
                    },
                    userId == friendId match {
                      case true => Some("you can't be friend with you :'( it's sad")
                      case false => None
                    }
                  ) collect { case Some(s) => s })
                }

                List(userIdErrors, friendIdErrors).filter(e => e.errors.nonEmpty)
              }

              if (errors.isEmpty) {
                val user = ctx.ctx.repo.user.addFriend(userId, friendId).get
                DataPayload[DataUser](Some(user), List())
              } else {
                DataPayload[DataUser](None, errors)
              }
            }.get
          }
      ),

      Field("UserDelFriend", UserDelFriendPayload,
        arguments = Argument("userId", IntType)
          :: Argument("friendId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.UserDelFriend) { () =>
            val userId = ctx.arg[Int]("userId")
            val friendId = ctx.arg[Int]("friendId")

            val errors = {

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => None
                    case Failure(_) => Some("userId not found")
                  },
                  ctx.ctx.repo.user.getFriends(userId)
                    .toOption
                    .map(_.map(_.id))
                    .map(_.contains(friendId))
                    .flatMap(b => if (!b) {
                      Some("you aren't friend")
                    } else {
                      None
                    }),
                  userId == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userId isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              val friendIdErrors = {
                DataError("friendId", List[Option[String]](
                  ctx.ctx.repo.user.getById(friendId) match {
                    case Success(_) => None
                    case Failure(_) => Some("friendId not found")
                  },
                ) collect { case Some(s) => s })
              }

              List(userIdErrors, friendIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.user.delFriend(userId, friendId).get
              DataPayload[DataUser](Some(user), List())
            } else {
              DataPayload[DataUser](None, errors)
            }
          }.get
        }
      ),

      Field("UserAddMusicalPreference", UserAddMusicalPreferencePayload,
        arguments = Argument("userId", IntType)
          :: Argument("genreId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.UserAddMusicalPreference) { () =>
            val userId = ctx.arg[Int]("userId")
            val genreId = ctx.arg[Int]("genreId")

            val errors = {

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => None
                    case Failure(_) => Some("userId not found")
                  },
                  userId == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userId isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              val genreIdErrors = {
                DataError("genreId", List[Option[String]](
                  ctx.ctx.repo.deezer.getGenreById(genreId) match {
                    case Success(_) => None
                    case Failure(_) => Some("genreId not found")
                  },
                  ctx.ctx.repo.user.getMsicalPreferences(userId)
                    .toOption
                    .map(_.map(_.id))
                    .map(_.contains(genreId))
                    .flatMap(b => if (b) {
                      Some("you already have this genre in your preferences")
                    } else {
                      None
                    })
                ) collect { case Some(s) => s })
              }

              List(userIdErrors, genreIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.user.addMusicalPreference(userId, genreId).get
              DataPayload[DataUser](Some(user), List())
            } else {
              DataPayload[DataUser](None, errors)
            }
          }.get
        }
      ),

      Field("UserDelMusicalPreference", UserDelMusicalPreferencePayload,
        arguments = Argument("userId", IntType)
          :: Argument("genreId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.UserDelMusicalPreference) { () =>
            val userId = ctx.arg[Int]("userId")
            val genreId = ctx.arg[Int]("genreId")

            val errors = {

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => None
                    case Failure(_) => Some("userId not found")
                  },
                  userId == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userId isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              val genreIdErrors = {
                DataError("genreId", List[Option[String]](
                  ctx.ctx.repo.deezer.getGenreById(genreId) match {
                    case Success(_) => None
                    case Failure(_) => Some("genreId not found")
                  },
                  ctx.ctx.repo.user.getMsicalPreferences(userId)
                    .toOption
                    .map(_.map(_.id))
                    .map(_.contains(genreId))
                    .flatMap(b => if (!b) {
                      Some("you don't have this genre in your preferences")
                    } else {
                      None
                    })
                ) collect { case Some(s) => s })
              }

              List(userIdErrors, genreIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.user.delMusicalPreference(userId, genreId).get
              DataPayload[DataUser](Some(user), List())
            } else {
              DataPayload[DataUser](None, errors)
            }
          }.get
        }
      ),

      Field("UserUpdatePrivacy", UserUpdatePrivacyPayload,
        arguments = Argument("userId", IntType)
          :: Argument("email", PrivacyEnum)
          :: Argument("location", PrivacyEnum)
          :: Argument("friends", PrivacyEnum)
          :: Argument("musicalPreferencesGenre", PrivacyEnum)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.UserUpdatePrivacy) { () =>
            val userId = ctx.arg[Int]("userId")
            val email = ctx.arg[Privacy.Value]("email")
            val location = ctx.arg[Privacy.Value]("location")
            val friends = ctx.arg[Privacy.Value]("friends")
            val musicalPreferencesGenre = ctx.arg[Privacy.Value]("musicalPreferencesGenre")

            val errors = {

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => None
                    case Failure(_) => Some("userId not found")
                  },
                  userId == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userId isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              List(userIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.user.updatePrivacy(userId, email, location, friends, musicalPreferencesGenre).get
              DataPayload[DataUser](Some(user), List())
            } else {
              DataPayload[DataUser](None, errors)
            }
          }.get
        }
      ),

      Field("UserUpdateLocation", UserUpdateLocationPayload,
        arguments = Argument("userId", IntType)
          :: Argument("latitude", FloatType)
          :: Argument("longitude", FloatType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.UserUpdatePrivacy) { () =>
            val userId = ctx.arg[Int]("userId")
            val latitude = ctx.arg[Double]("latitude")
            val longitude = ctx.arg[Double]("longitude")

            val errors = {

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => None
                    case Failure(_) => Some("userId not found")
                  },
                  userId == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userId isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              val latitudeError = {
                DataError("latitude", List[Option[String]](
                  if (latitude > 90.0) {
                    Some("latitude is =< 90")
                  } else {
                    None
                  },
                  if (latitude < -90.0) {
                    Some("latitude is => -90")
                  } else {
                    None
                  },
                ) collect { case Some(s) => s })
              }

              val longitudeError = {
                DataError("longitude", List[Option[String]](
                  if (longitude > 180.0) {
                    Some("longitude is =< 180")
                  } else {
                    None
                  },
                  if (longitude < -180.0) {
                    Some("longitude is => -180")
                  } else {
                    None
                  },
                ) collect { case Some(s) => s })
              }

              List(userIdErrors, latitudeError, longitudeError).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.user.updateLocation(userId, latitude, longitude).get
              DataPayload[DataUser](Some(user), List())
            } else {
              DataPayload[DataUser](None, errors)
            }
          }.get
        }
      ),

      Field("UserUpdatePassword", UserUpdatePasswordPayload,
        arguments = Argument("userId", IntType)
          :: Argument("newPassword", StringType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.UserUpdatePrivacy) { () =>
            val userId = ctx.arg[Int]("userId")
            val newPassword = ctx.arg[String]("newPassword")

            val errors = {

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => None
                    case Failure(_) => Some("userId not found")
                  },
                  userId == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userId isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              val passErrors = {
                val lower1 = """(?=.*[a-z])""".r
                val upper1 = """(?=.*[A-Z])""".r
                val numeric1 = """(?=.*[0-9])""".r
                val length8 = """(?=.{8,})""".r

                DataError("pass", List[Option[String]](
                  lower1.findFirstMatchIn(newPassword) match {
                    case Some(_) => None
                    case None => Some("Password need a lowercase")
                  },
                  upper1.findFirstMatchIn(newPassword) match {
                    case Some(_) => None
                    case None => Some("Password need a uppercase")
                  },
                  numeric1.findFirstMatchIn(newPassword) match {
                    case Some(_) => None
                    case None => Some("Password need a number")
                  },
                  length8.findFirstMatchIn(newPassword) match {
                    case Some(_) => None
                    case None => Some("Password need 8 character")
                  },
                ) collect { case Some(s) => s })
              }

              List(userIdErrors, passErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.user.updateNewPassword(userId, newPassword).get
              DataPayload[DataUser](Some(user), List())
            } else {
              DataPayload[DataUser](None, errors)
            }
          }.get
        }
      ),

      /* PLAY_LIST_EDITOR */

      Field("PlayListEditorNew", PlayListEditorNewPayload,
        arguments = Argument("userMasterId", IntType)
          :: Argument("name", StringType)
          :: Argument("public", BooleanType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorNew) { () =>
            val userMasterId = ctx.arg[Int]("userMasterId")
            val name = ctx.arg[String]("name")
            val public = ctx.arg[Boolean]("public")

            val errors = {

              val userMasterIdErrors = {
                DataError("userMasterId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userMasterId) match {
                    case Success(_) => None
                    case Failure(_) => Some("userIdMaster not found")
                  },
                  userMasterId == ctx.ctx.user.id match {
                    case true => None
                    case false => Some("userIdMaster isn't you")
                  }
                ) collect { case Some(s) => s })
              }

              val nameErrors = {
                DataError("name", List[Option[String]](
                  ctx.ctx.repo.playListEditor.getByName(name) match {
                    case Success(_) => Some("name already exist")
                    case Failure(_) => None
                  }
                ) collect { case Some(s) => s })
              }

              List(userMasterIdErrors, nameErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.playListEditor.`new`(userMasterId, name, public).get
              DataPayload[DataPlaylistEditor](Some(user), List())
            } else {
              DataPayload[DataPlaylistEditor](None, errors)
            }
          }.get
        }
      ),

      Field("PlayListEditorDel", PlayListEditorDelPayload,
        arguments = Argument("id", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorDel) { () =>
            val id = ctx.arg[Int]("id")

            val errors = {

              val idErrors = {

                DataError("id", List[Option[String]](
                  ctx.ctx.repo.trackVoteEvent.getById(id) match {
                    case Success(s) => if (s.userMasterId == ctx.ctx.user.id) {
                      None
                    } else {
                      Some("You aren't the master")
                    }
                    case Failure(_) => Some("PlayListEditorId not found")
                  }
                ) collect { case Some(s) => s })
              }

              List(idErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              ctx.ctx.repo.playListEditor.delete(id).get
              DataPayload[Unit](Some(Unit), List())
            } else {
              DataPayload[Unit](None, errors)
            }
          }.get
        }
      ),

      Field("PlayListEditorAddUser", PlayListEditorAddUserPayload,
        arguments = Argument("playlistId", IntType)
          :: Argument("userId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorAddUser) { () => {
            val playlistId = ctx.arg[Int]("playlistId")
            val userId = ctx.arg[Int]("userId")

            val errors = {
              val playlistIdErrors = {
                DataError("playlistId", List[Option[String]](
                  ctx.ctx.repo.playListEditor.getById(playlistId) match {
                    case Success(s) => if (s.userMasterId == ctx.ctx.user.id) {
                      None
                    } else {
                      Some("You aren't the master")
                    }
                    case Failure(_) => Some("playlistId not found")
                  }
                ) collect { case Some(s) => s })
              }

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => ctx.ctx.repo.playListEditor.getInvitedUsers(playlistId)
                      .toOption
                      .map(_.map(_.id))
                      .map(_.contains(userId))
                      .flatMap(e => if (e) {
                        Some("user already invited")
                      } else {
                        None
                      })
                    case Failure(_) => Some("userId not found")
                  }
                ) collect { case Some(s) => s })
              }

              List(playlistIdErrors, userIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val playlist = ctx.ctx.repo.playListEditor.addUser(playlistId, userId).get
              DataPayload[DataPlaylistEditor](Some(playlist), List())
            } else {
              DataPayload[DataPlaylistEditor](None, errors)
            }
          }
          }.get
        }
      ),

      Field("PlayListEditorDelUser", PlayListEditorDelUserPayload,
        arguments = Argument("playlistId", IntType)
          :: Argument("userId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorDelUser) { () =>
            val playlistId = ctx.arg[Int]("playlistId")
            val userId = ctx.arg[Int]("userId")

            val errors = {
              val playlistIdErrors = {
                DataError("playlistId", List[Option[String]](
                  ctx.ctx.repo.playListEditor.getById(playlistId) match {
                    case Success(s) => if (s.userMasterId == ctx.ctx.user.id) {
                      None
                    } else {
                      Some("You aren't the master")
                    }
                    case Failure(_) => Some("playlistId not found")
                  }
                ) collect { case Some(s) => s })
              }

              val userIdErrors = {
                DataError("userId", List[Option[String]](
                  ctx.ctx.repo.user.getById(userId) match {
                    case Success(_) => ctx.ctx.repo.playListEditor.getInvitedUsers(playlistId)
                      .toOption
                      .map(_.map(_.id))
                      .map(_.contains(userId))
                      .flatMap(e => if (!e) {
                        Some("user isn't invited")
                      } else {
                        None
                      })
                    case Failure(_) => Some("userId not found")
                  },
                  ctx.ctx.repo.playListEditor.getById(playlistId)
                    .toOption
                    .map(_.userMasterId)
                    .map(_ == userId)
                    .flatMap(e => if (e) {
                      Some("you can't delete the master")
                    } else {
                      None
                    })
                ) collect { case Some(s) => s })
              }

              List(playlistIdErrors, userIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val playlist = ctx.ctx.repo.playListEditor.delUser(playlistId, userId).get
              DataPayload[DataPlaylistEditor](Some(playlist), List())
            } else {
              DataPayload[DataPlaylistEditor](None, errors)
            }
          }.get
        }
      ),

      Field("PlayListEditorAddTrack", PlayListEditorAddTrackPayload,
        arguments = Argument("playListId", IntType)
          :: Argument("trackId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorAddTrack) { () =>
            val playListId = ctx.arg[Int]("playListId")
            val trackId = ctx.arg[Int]("trackId")

            val errors = {

              val playListIdErrors = {
                DataError("playListId", List[Option[String]](
                  ctx.ctx.repo.playListEditor.getById(playListId) match {
                    case Success(_) => None
                    case Failure(_) => Some("playListId not found")
                  },
                  // if not public need be invited TODO
                ) collect { case Some(s) => s })
              }

              val trackIdErrors = {

                DataError("trackId", List[Option[String]](
                  ctx.ctx.repo.deezer.getTrackById(trackId) match {
                    case Success(_) => None
                    case Failure(_) => Some("trackId not found")
                  },
                  // check if is alredy in playlist TODO
                ) collect { case Some(s) => s })
              }

              List(playListIdErrors, trackIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.playListEditor.addTrack(playListId, trackId).get
              DataPayload[DataPlaylistEditor](Some(user), List())
            } else {
              DataPayload[DataPlaylistEditor](None, errors)
            }
          }.get
        }
      ),

      Field("PlayListEditorDelTrack", PlayListEditorDelTrackPayload,
        arguments = Argument("playListId", IntType)
          :: Argument("trackId", IntType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorDelTrack) { () =>
            val playListId = ctx.arg[Int]("playListId")
            val trackId = ctx.arg[Int]("trackId")

            val errors = {

              val playListIdErrors = {
                DataError("playListId", List[Option[String]](
                  ctx.ctx.repo.playListEditor.getById(playListId) match {
                    case Success(_) => None
                    case Failure(_) => Some("playListId not found")
                  },
                  // if not public need be invited TODO
                ) collect { case Some(s) => s })
              }

              val trackIdErrors = {

                DataError("trackId", List[Option[String]](
                  ctx.ctx.repo.deezer.getTrackById(trackId) match {
                    case Success(_) => None
                    case Failure(_) => Some("trackId not found")
                  },
                  // check if is in playlist TODO
                ) collect { case Some(s) => s })
              }

              List(playListIdErrors, trackIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.playListEditor.delTrack(playListId, trackId).get
              DataPayload[DataPlaylistEditor](Some(user), List())
            } else {
              DataPayload[DataPlaylistEditor](None, errors)
            }
          }.get
        }
      ),

      Field("PlayListEditorMoveTrack", PlayListEditorMoveTrackPayload,
        arguments = Argument("playListId", IntType)
          :: Argument("trackId", IntType)
          :: Argument("up", BooleanType)
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.PlayListEditorMoveTrack) { () =>
            val playListId = ctx.arg[Int]("playListId")
            val trackId = ctx.arg[Int]("trackId")
            val up = ctx.arg[Boolean]("up") // check if is possible ? TODO

            val errors = {

              val playListIdErrors = {
                DataError("playListId", List[Option[String]](
                  ctx.ctx.repo.playListEditor.getById(playListId) match {
                    case Success(_) => None
                    case Failure(_) => Some("playListId not found")
                  },
                  // if not public need be invited TODO
                ) collect { case Some(s) => s })
              }

              val trackIdErrors = {

                DataError("trackId", List[Option[String]](
                  ctx.ctx.repo.deezer.getTrackById(trackId) match {
                    case Success(_) => None
                    case Failure(_) => Some("trackId not found")
                  },
                  // check if is in playlist TODO
                ) collect { case Some(s) => s })
              }

              List(playListIdErrors, trackIdErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val user = ctx.ctx.repo.playListEditor.moveTrack(playListId, trackId, up).get
              DataPayload[DataPlaylistEditor](Some(user), List())
            } else {
              DataPayload[DataPlaylistEditor](None, errors)
            }
          }.get
        }
      ),

    )
  )

  val Subscription: ObjectType[SecureContext, Unit] = {
    ObjectType(
      "Subscription", fields[SecureContext, Unit](
        Field("TrackVoteEventById", TrackVoteEventByIdPayload,
          arguments = Argument("id", IntType) :: Nil,
          resolve = ctx ⇒ Future {
            ctx.ctx.authorised(Permissions.TrackVoteEventById) { () => {
              ctx.ctx.repo.trackVoteEvent.getById(ctx.arg[Int]("id")) match {
                case Success(value) => DataPayload[DataTrackVoteEvent](Some(value), List())
                case Failure(_) => DataPayload[DataTrackVoteEvent](None, List(
                  DataError("id", List("TrackVoteEvent Id not found"))
                ))
              }
            }
            }.get
          }
        ),
        Field("PlayListEditorById", PlayListEditorByIdPayload,
          arguments = Argument("id", IntType) :: Nil,
          resolve = ctx ⇒ Future {
            ctx.ctx.authorised(Permissions.PlayListEditorById) { () => {
              ctx.ctx.repo.playListEditor.getById(ctx.arg[Int]("id")) match {
                case Success(value) => DataPayload[DataPlaylistEditor](Some(value), List())
                case Failure(_) => DataPayload[DataPlaylistEditor](None, List(
                  DataError("id", List("PlayListEditor Id not found"))
                ))
              }
            }
            }.get
          }),
      )
    )
  }

  val KroomSchema = Schema(Query, Some(Mutation), Some(Subscription))
}
