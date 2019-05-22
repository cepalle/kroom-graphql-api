package io.kroom.api

import io.kroom.api.root.RepoRoot
import io.kroom.api.user.DataUser
import io.kroom.api.util.{AuthenticationException, AuthorisationException}

case class SecureContext(token: Option[String], repo: RepoRoot) {
  /*
  def login(userName: String, password: String) = repo.user.authenticate(userName, password)
    .getOrElse(
      throw new AuthenticationException("UserName or password is incorrect")
    )

  def authorised[T](permissions: String*)(fn: DataUser ⇒ T) =
    token.flatMap(repo.user.authorise).fold(throw AuthorisationException("Invalid token")) { user ⇒
      if (permissions.forall(user.permissions.contains)) fn(user)
      else throw AuthorisationException("You do not have permission to do this operation")
    }

  def ensurePermissions(permissions: List[String]): Unit =
    token.flatMap(repo.user.authorise).fold(throw AuthorisationException("Invalid token")) { user ⇒
      if (!permissions.forall(user.permissions.contains))
        throw AuthorisationException("You do not have permission to do this operation")
    }

  def user = token.flatMap(repo.user.authorise).fold(throw AuthorisationException("Invalid token"))(identity)
  */
}
