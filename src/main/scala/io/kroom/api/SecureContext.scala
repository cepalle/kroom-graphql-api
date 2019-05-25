package io.kroom.api

import io.kroom.api.root.RepoRoot
import io.kroom.api.user.{DataUser, DataUserPrivacy}
import Authorization.{PermissionGroup, Permissions, Privacy}
import io.kroom.api.ExceptionCustom.AuthorisationException

class SecureContext(val token: Option[String], val repo: RepoRoot) {

  lazy val (user: DataUser, permGrp: Set[PermissionGroup.Value]) = token.flatMap(repo.user.getTokenPermGroup)
    .getOrElse((
      DataUser(-1, "public", "", false, None, None, None, None, DataUserPrivacy("private", "private", "private", "private")),
      Set(PermissionGroup.public)
    ))

  lazy val permissions: Set[Authorization.Permissions.Value] = Authorization.PermissionGroupsToPermissions(permGrp)

  def authorised[T](perms: Permissions.Value*)(fn: () ⇒ T): T = {
    if (perms.forall(permissions.contains)) fn()
    else throw AuthorisationException("You do not have permission to do this operation")
  }

  def checkPrivacy[T](foreignId: Int, privacy: Privacy.Value)(fn: () ⇒ T): Option[T] = {
    if (foreignId == user.id) {
      Some(fn())
    } else if (privacy == Privacy.public) {
      Some(fn())
    } else if (privacy == Privacy.amis) {
      if (repo.user.getFriends(foreignId).map(_.id).contains(user.id)) {
        Some(fn())
      } else {
        None
      }
    } else {
      None
    }
  }
}
