package io.kroom.api

import io.kroom.api.root.RepoRoot
import io.kroom.api.user.{DataUser, DataUserPrivacy}
import Authorization.{PermissionGroup, Permissions, Privacy}
import io.kroom.api.ExceptionCustom.AuthorisationException

import scala.util.{Failure, Success, Try}

class SecureContext(val token: Option[String], val repo: RepoRoot) {

  lazy val (user: DataUser, permGrp: Set[PermissionGroup.Value]) = token
    .flatMap(t => repo.user.getTokenPermGroup(t).toOption)
    .getOrElse((
      DataUser(-1, "public", "", false, None, None, None, None, None, None, None, DataUserPrivacy("private", "private", "private", "private")),
      Set(PermissionGroup.public)
    ))

  lazy val permissions: Set[Authorization.Permissions.Value] = Authorization.permissionGroupsToPermissions(permGrp)

  def authorised[T](perms: Permissions.Value*)(fn: () ⇒ T): Try[T] = {
    if (!perms.forall(permissions.contains))
      Failure(AuthorisationException("You do not have permission to do this operation"))
    else if (!perms.forall(Authorization.permissionsOfPublic.contains) && !user.emailIsconfirmed)
      Failure(AuthorisationException("Your email has not been confirmed"))
    else
      Success(fn())
  }

  def checkPrivacyUser[T](foreignId: Int, privacy: Privacy.Value)(fn: () ⇒ T): Option[T] = {
    if (foreignId == user.id) {
      Some(fn())
    } else if (privacy == Privacy.public) {
      Some(fn())
    } else if (privacy == Privacy.friends) {
      if (repo.user.getFriends(foreignId).toOption.exists(_.map(_.id).contains(user.id))) {
        Some(fn())
      } else {
        None
      }
    } else {
      None
    }
  }

  def checkPrivacyTrackEvent[T](eventId: Int, public: Boolean)(fn: () ⇒ T): Option[T] = {
    if (public) {
      Some(fn())
    } else if (repo.trackVoteEvent.getUserInvited(eventId).get.map(_.id).contains(user.id)) {
      Some(fn())
    } else {
      None
    }
  }

}
