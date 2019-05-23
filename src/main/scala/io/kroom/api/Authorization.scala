package io.kroom.api

import io.kroom.api.Authorization.Permissions

object Authorization {

  object Permissions extends Enumeration {
    val DeezerTrack, DeezerArtist, DeezerAlbum, DeezerGenre, DeezerSearch = Value
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
  )
  val permissionsOfRoot: Set[Permissions.Value] = Set[Permissions.Value](
  )
  val permissionsOfUser: Set[Permissions.Value] = Set[Permissions.Value](
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

    def IntToPrivacy(nb: Int): Privacy.Value = {
      nb match {
        case 1 => public
        case 2 => amis
        case _ => `private`
      }
    }

    def PrivacyToInt(e: Privacy.Value): Int = {
      e match {
        case Privacy.public => 1
        case Privacy.amis => 2
        case _ => 3
      }
    }
  }

}
