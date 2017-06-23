package io.scalac.frees.login.algebras

import freestyle.free
import io.scalac.frees.login.{GitHubData, GitHubId}
import io.scalac.frees.login.types._

sealed trait UserInsertionResult
case class UserInserted(id: UserId) extends UserInsertionResult
case object AlreadyExists extends UserInsertionResult

@free trait Database {

  def insertCredentialsUser(credentials: Credentials): FS[UserInsertionResult]

  def insertGitHubUser(ghData: GitHubData): FS[UserInsertionResult]

  def getUserByEmail(email: UserEmail): FS[Option[UserId]]

  def getPassword(email: UserEmail): FS[Option[PasswordHash]]

  def getUserByGitHubId(ghId: GitHubId): FS[Option[UserId]]
}
