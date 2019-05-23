package io.kroom.api

object Authorization {

  def PermissionGroupsToPermissions(grps: List[PermissionGroup.Value]): List[Permissions.Value] = {
    List[Permissions.Value]()
  }

  object PermissionGroup extends Enumeration {
    val root, public, user = Value
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

  object Permissions extends Enumeration {
    val public, amis, `private` = Value
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
