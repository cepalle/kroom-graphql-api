package io.kroom.api.root

import io.kroom.api.Authorization.Privacy
import io.kroom.api.Authorization.Permissions
import io.kroom.api.SecureContext
import io.kroom.api.user.{DataUser, SchemaUser}
import io.kroom.api.deezer._
import io.kroom.api.trackvoteevent.DataTrackVoteEvent
import io.kroom.api.util.{DataError, DataPayload}
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

  import scala.concurrent.ExecutionContext.Implicits.global

  lazy val PrivacyEnum = EnumType(
    "PrivacyEnum",
    Some("PrivacyEnum"),
    List(
      EnumValue("PUBLIC",
        value = Privacy.public),
      EnumValue("AMIS",
        value = Privacy.amis),
      EnumValue("PRIVATE",
        value = Privacy.`private`),
    )
  )

  lazy val ErrorField: ObjectType[SecureContext, DataError] = ObjectType(
    "Error",
    "Error description.",
    () ⇒ fields[SecureContext, DataError](
      Field("field", StringType, resolve = _.value.field),
      Field("errors", ListType(StringType), resolve = _.value.errors),
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

      Field("TrackVoteEventsPublic", ListType(TrackVoteEventField),
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

      Field("TrackVoteEventByUserId", TrackVoteEventByUserIdPayload,
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

      Field("TrackVoteEventUpdate", TrackVoteEventUpdatePayload,
        arguments = Argument("eventId", IntType)
          :: Argument("userIdMaster", IntType)
          :: Argument("name", StringType)
          :: Argument("public", BooleanType)
          :: Argument("schedule", OptionInputType(StringType))
          :: Argument("location", OptionInputType(StringType))
          :: Nil,
        resolve = ctx ⇒ Future {
          ctx.ctx.authorised(Permissions.TrackVoteEventUpdate) { () =>

            val eventId = ctx.arg[Int]("eventId")
            val userIdMaster = ctx.arg[Int]("userIdMaster")
            val name = ctx.arg[String]("name")
            val public = ctx.arg[Boolean]("public")
            val schedule = ctx.argOpt[String]("schedule")
            val location = ctx.argOpt[String]("location")

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

              List(eventIdErrors, userIdMasterErrors, nameErrors).filter(e => e.errors.nonEmpty)
            }

            if (errors.isEmpty) {
              val trackEvent = ctx.ctx.repo.trackVoteEvent.update(eventId, userIdMaster, name, public, schedule, location).get
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
                  }
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
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserSignUp) { () => {
          // TODO send email
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
      ),

      Field("UserSignIn", UserSignInPayload,
        arguments = Argument("userName", StringType)
          :: Argument("pass", StringType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserSignIn) { () => {
          val res = ctx.ctx.repo.user.signIn(
            ctx.arg[String]("userName"),
            ctx.arg[String]("pass"),
          ) match {
            case Success(value) =>
              DataPayload[DataUser](Some(value), List())
            case Failure(_) => DataPayload[DataUser](None, List(
              DataError("login", List("username or password invalid")))
            )
          }
          UpdateCtx(res) { userPayload ⇒
            new SecureContext(userPayload.data.flatMap(_.token), ctx.ctx.repo)
          }
        }
        }.get
      ),

      Field("UserAddFriend", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("friendId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserAddFriend) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.addFriend(
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("friendId"),
            ).get
          }
        }.get
      ),

      Field("UserDelFriend", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("friendId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserDelFriend) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.delFriend(
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("friendId"),
            ).get
          }
        }.get
      ),

      Field("UserAddMusicalPreference", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("genreId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserAddMusicalPreference) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.addMusicalPreference(
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("genreId"),
            ).get
          }
        }.get
      ),

      Field("UserDelMusicalPreference", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("genreId", IntType)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserDelMusicalPreference) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.delMusicalPreference(
              ctx.arg[Int]("userId"),
              ctx.arg[Int]("genreId"),
            ).get
          }
        }.get
      ),

      Field("UserUpdatePrivacy", OptionType(UserField),
        arguments = Argument("userId", IntType)
          :: Argument("email", PrivacyEnum)
          :: Argument("location", PrivacyEnum)
          :: Argument("friends", PrivacyEnum)
          :: Argument("musicalPreferencesGenre", PrivacyEnum)
          :: Nil,
        resolve = ctx ⇒ ctx.ctx.authorised(Permissions.UserUpdatePrivacy) { () =>
          Future {
            // TODO check if is good user
            ctx.ctx.repo.user.updatePrivacy(
              ctx.arg[Int]("userId"),
              ctx.arg[Privacy.Value]("email"),
              ctx.arg[Privacy.Value]("location"),
              ctx.arg[Privacy.Value]("friends"),
              ctx.arg[Privacy.Value]("musicalPreferencesGenre"),
            ).get
          }
        }.get
      ),

    )
  )

  val KroomSchema = Schema(Query, Some(Mutation))
}
