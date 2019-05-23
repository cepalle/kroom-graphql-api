package io.kroom.api

import io.kroom.api.root.RepoRoot
import io.kroom.api.user.{DataUser, DataUserPrivacy}
import Authorization.PermissionGroup

class SecureContext(private var token: Option[String], val repo: RepoRoot) {

  private lazy val (user: DataUser, permGrp: List[PermissionGroup.Value]) = token.flatMap(repo.user.authorise)
    .getOrElse((
      DataUser(-1, "public", "", false, None, None, None, None, DataUserPrivacy(0, 0, 0, 0)),
      List(PermissionGroup.public)
    ))

  private lazy val permissions = Authorization.PermissionGroupsToPermissions(permGrp)

  def login(userName: String, password: String): DataUser =
    repo.user.authenticate(userName, password)
      .fold(throw AuthorisationException("UserName or password is incorrect"))(identity)

  def authorised[T](permissions: String*)(fn: DataUser ⇒ T): T =
    if (permissions.forall(permissions.contains)) fn(user)
    else throw AuthorisationException("You do not have permission to do this operation")

}
