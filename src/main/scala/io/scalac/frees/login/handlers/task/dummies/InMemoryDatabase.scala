package io.scalac.frees.login.handlers.task.dummies

import fs2.Task
import io.scalac.frees.login.algebras.{Database, GitHubData, UserInsertionResult}
import io.scalac.frees.login.types._

class InMemoryDatabase extends Database.Handler[Task] {
  val underlying = new io.scalac.frees.login.handlers.id.InMemoryDatabase

  override protected[this] def insertCredentialsUser(
    email: UserEmail,
    hash: PasswordHash
  ): Task[UserInsertionResult] =
    Task.now(underlying.insertCredentialsUser(email, hash))

  override protected[this] def insertGitHubUser(ghData: GitHubData): Task[UserInsertionResult] =
    Task.now(underlying.insertGitHubUser(ghData))

  override protected[this] def getUserByEmail(email: UserEmail): Task[Option[UserId]] =
    Task.now(underlying.getUserByEmail(email))

  override protected[this] def getPassword(email: UserEmail): Task[Option[PasswordHash]] =
    Task.now(underlying.getPassword(email))

  override protected[this] def getUserByGitHubId(ghId: GitHubId): Task[Option[UserId]] =
    Task.now(underlying.getUserByGitHubId(ghId))

}
