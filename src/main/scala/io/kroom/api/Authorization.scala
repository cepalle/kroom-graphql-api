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
    PlayListEditorsPublic,
    PlayListEditorById,
    PlayListEditorByUserId,
    UserGetById,
    UserNameAutocompletion,

    TrackVoteEventNew,
    TrackVoteEventDel,
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
    UserUpdatePrivacy,
    PlayListEditorNew,
    PlayListEditorDel,
    PlayListEditorAddUser,
    PlayListEditorDelUser,
    PlayListEditorAddTrack,
    PlayListEditorDelTrack,
    PlayListEditorMoveTrack = Value
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
    Permissions.PlayListEditorsPublic,
    Permissions.PlayListEditorById,
    Permissions.PlayListEditorByUserId,
    Permissions.UserGetById,
    Permissions.UserNameAutocompletion,

    Permissions.TrackVoteEventNew,
    Permissions.TrackVoteEventDel,
    Permissions.TrackVoteEventUpdate,
    Permissions.TrackVoteEventAddUser,
    Permissions.TrackVoteEventDelUser,
    Permissions.TrackVoteEventAddVote,
    Permissions.TrackVoteEventDelVote,
    Permissions.UserAddFriend,
    Permissions.UserDelFriend,
    Permissions.UserAddMusicalPreference,
    Permissions.UserDelMusicalPreference,
    Permissions.UserUpdatePrivacy,
    Permissions.PlayListEditorNew,
    Permissions.PlayListEditorDel,
    Permissions.PlayListEditorAddUser,
    Permissions.PlayListEditorDelUser,
    Permissions.PlayListEditorAddTrack,
    Permissions.PlayListEditorDelTrack,
    Permissions.PlayListEditorMoveTrack,
  )

  val permissionsOfRoot: Set[Permissions.Value] = permissionsOfUser ++ Set[Permissions.Value](
  )

  def permissionGroupToPermissions(grp: PermissionGroup.Value): Set[Permissions.Value] = {
    grp match {
      case PermissionGroup.root => permissionsOfRoot
      case PermissionGroup.public => permissionsOfPublic
      case PermissionGroup.user => permissionsOfUser
    }
  }

  def permissionGroupsToPermissions(grps: Set[PermissionGroup.Value]): Set[Permissions.Value] = {
    grps.foldLeft(Set[Permissions.Value]()) { (acc: Set[Permissions.Value], cur: PermissionGroup.Value) =>
      acc ++ permissionGroupToPermissions(cur)
    }
  }

  def permissionGroupToString(g: PermissionGroup.Value): String = {
    g match {
      case PermissionGroup.root => "root"
      case PermissionGroup.public => "public"
      case PermissionGroup.user => "user"
    }
  }

  def stringToPermissionGroup(g: String): PermissionGroup.Value = {
    g match {
      case "root" => PermissionGroup.root
      case "user" => PermissionGroup.user
      case _ => PermissionGroup.public
    }
  }

  object Privacy extends Enumeration {
    val public, friends, `private` = Value

    def stringToPrivacy(nb: String): Privacy.Value = {
      nb match {
        case "public" => public
        case "friends" => friends
        case _ => `private`
      }
    }

    def privacyToString(e: Privacy.Value): String = {
      e match {
        case Privacy.public => "public"
        case Privacy.friends => "friends"
        case _ => "private"
      }
    }
  }

}
