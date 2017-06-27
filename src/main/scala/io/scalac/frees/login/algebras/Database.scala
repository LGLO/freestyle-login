package io.scalac.frees.login.algebras

import freestyle.free
import io.scalac.frees.login.types._

sealed trait UserInsertionResult

case class UserInserted(id: UserId) extends UserInsertionResult

case object AlreadyExists extends UserInsertionResult

case class DBFailure(err: Throwable) extends UserInsertionResult

/**
  * Free algebra for user login and registration.
  * It was replaced by `DoobiePersistence` algebra build on top of
  * `freestyle.doobie.DoobieM` for low level operations.
  */
@free trait Database {

  def insertCredentialsUser(email: UserEmail, hash: PasswordHash): FS[UserInsertionResult]

  def insertGitHubUser(ghData: GitHubData): FS[UserInsertionResult]

  def getUserByEmail(email: UserEmail): FS[Option[UserId]]

  def getPassword(email: UserEmail): FS[Option[PasswordHash]]

  def getUserByGitHubId(ghId: GitHubId): FS[Option[UserId]]
}
