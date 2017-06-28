package io.scalac.frees.login.algebras

import freestyle.free
import fs2.util.Attempt
import io.scalac.frees.login.types.{GitHubId, PasswordHash, UserEmail, UserId}

sealed trait UserInsertionResult

case class UserInserted(id: UserId) extends UserInsertionResult

case object AlreadyExists extends UserInsertionResult

case class DBFailure(err: Throwable) extends UserInsertionResult


@free trait LoginDatabase {
  def insertCredentialsUser(e: UserEmail, p: PasswordHash): FS[Attempt[UserId]]
  def insertGitHubUser(d: GitHubData): FS[Attempt[UserId]]
  def queryByLoginEmail(e: UserEmail): FS[Option[(UserId, PasswordHash)]]
  def queryByGitHubId(id: GitHubId): FS[Option[UserId]]
}
