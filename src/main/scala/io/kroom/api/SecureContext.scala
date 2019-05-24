package io.kroom.api

import io.kroom.api.root.RepoRoot
import io.kroom.api.user.{DataUser, DataUserPrivacy}
import Authorization.{PermissionGroup, Permissions}

class SecureContext(private var token: Option[String], val repo: RepoRoot) {

  private lazy val (user: DataUser, permGrp: Set[PermissionGroup.Value]) = token.flatMap(repo.user.authorise)
    .getOrElse((
      DataUser(-1, "public", "", false, None, None, None, None, DataUserPrivacy(0, 0, 0, 0)),
      Set(PermissionGroup.root)
    ))

  private lazy val permissions: Set[Authorization.Permissions.Value] = Authorization.PermissionGroupsToPermissions(permGrp)

  def login(userName: String, password: String): DataUser =
    repo.user.authenticate(userName, password)
      .fold(throw AuthorisationException("UserName or password is incorrect"))(identity)

  def authorised[T](perms: Permissions.Value*)(fn: (DataUser, Set[PermissionGroup.Value], Set[Authorization.Permissions.Value]) ⇒ T): T =
    if (perms.forall(permissions.contains)) fn(user, permGrp, permissions)
    else throw AuthorisationException("You do not have permission to do this operation")

}
