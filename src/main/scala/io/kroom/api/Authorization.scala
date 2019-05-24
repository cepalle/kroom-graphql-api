package io.kroom.api

object Authorization {

  object Permissions extends Enumeration {
    val DeezerTrack,
    DeezerArtist,
    DeezerAlbum,
    DeezerGenre,
    DeezerSearch,
    TrackVoteEventsPublic,
    TrackVoteEventById,
    TrackVoteEventByUserId,
    UserGetById,

    TrackVoteEventNew,
    TrackVoteEventUpdate,
    TrackVoteEventAddUser,
    TrackVoteEventDelUser,
    TrackVoteEventAddVote,
    TrackVoteEventDelVote,
    UserSignUp,
    UserSignIn,
    UserAddFriend,
    UserDelFriend,
    UserAddMusicalPreference,
    UserDelMusicalPreference,
    UserUpdatePrivacy = Value
  }

  object PermissionGroup extends Enumeration {
    val root, public, user = Value
  }

  val permissionsOfPublic: Set[Permissions.Value] = Set[Permissions.Value](
    Permissions.DeezerTrack,
    Permissions.DeezerArtist,
    Permissions.DeezerAlbum,
    Permissions.DeezerGenre,
    Permissions.DeezerSearch,

    Permissions.UserSignUp,
    Permissions.UserSignIn,
  )

  val permissionsOfUser: Set[Permissions.Value] = permissionsOfPublic ++ Set[Permissions.Value](
    Permissions.TrackVoteEventsPublic,
    Permissions.TrackVoteEventById,
    Permissions.TrackVoteEventByUserId,
    Permissions.UserGetById,

    Permissions.TrackVoteEventNew,
    Permissions.TrackVoteEventUpdate,
    Permissions.TrackVoteEventAddUser,
    Permissions.TrackVoteEventDelUser,
    Permissions.TrackVoteEventAddVote,
    Permissions.TrackVoteEventDelVote,
    Permissions.UserAddFriend,
    Permissions.UserDelFriend,
    Permissions.UserAddMusicalPreference,
    Permissions.UserDelMusicalPreference,
    Permissions.UserUpdatePrivacy
  )

  val permissionsOfRoot: Set[Permissions.Value] = permissionsOfUser ++ Set[Permissions.Value](
  )

  def PermissionGroupToPermissions(grp: PermissionGroup.Value): Set[Permissions.Value] = {
    grp match {
      case PermissionGroup.root => permissionsOfRoot
      case PermissionGroup.public => permissionsOfPublic
      case PermissionGroup.user => permissionsOfUser
    }
  }

  def PermissionGroupsToPermissions(grps: Set[PermissionGroup.Value]): Set[Permissions.Value] = {
    grps.foldLeft(Set[Permissions.Value]()) { (acc: Set[Permissions.Value], cur: PermissionGroup.Value) =>
      acc ++ PermissionGroupToPermissions(cur)
    }
  }

  def PermissionGroupToString(g: PermissionGroup.Value): String = {
    g match {
      case PermissionGroup.root => "root"
      case PermissionGroup.public => "public"
      case PermissionGroup.user => "user"
    }
  }

  def StringToPermissionGroup(g: String): PermissionGroup.Value = {
    g match {
      case "root" => PermissionGroup.root
      case "user" => PermissionGroup.user
      case _ => PermissionGroup.public
    }
  }

  object Privacy extends Enumeration {
    val public, amis, `private` = Value

    def StringToPrivacy(nb: String): Privacy.Value = {
      nb match {
        case "public" => public
        case "amis" => amis
        case _ => `private`
      }
    }

    def PrivacyToString(e: Privacy.Value): String = {
      e match {
        case Privacy.public => "public"
        case Privacy.amis => "amis"
        case _ => "private"
      }
    }
  }

}
