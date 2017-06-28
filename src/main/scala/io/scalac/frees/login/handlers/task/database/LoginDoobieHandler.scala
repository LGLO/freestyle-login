package io.scalac.frees.login.handlers.task.database

import doobie.imports._
import cats._, cats.data._, cats.implicits._
import fs2.interop.cats._
import fs2.util.Attempt
import io.scalac.frees.login.algebras.{GitHubData, LoginDatabase}
import io.scalac.frees.login.types.{GitHubId, PasswordHash, UserEmail, UserId}

class LoginDoobieHandler[F[_]](xa: Transactor[F]) extends LoginDatabase.Handler[F]{
  /**
    * Inserts new user.
    * In real-world there would be some creation date and details here.
    *
    * @return DB created identifier
    */
  private def insertUser(): ConnectionIO[Long] =
    for {
      _ <- sql"insert into users () values ()".update.run //just getting next autoincremented Id!
      id <- sql"select lastval()".query[Long].unique
    } yield id

  def insertCredentialsUser(
    email: UserEmail,
    hash: PasswordHash
  ): F[Attempt[UserId]] = {
    val id: ConnectionIO[UserId] =
      for {
        id <- insertUser()
        _ <- sql"insert into credentials (user_id, email, password_hash) values($id, $email, $hash)"
          .update.run
      } yield id
    id.attempt.transact(xa)
  }

  def insertGitHubUser(d: GitHubData): F[Attempt[UserId]] = {
    val id: ConnectionIO[UserId] =
      for {
        id <- insertUser()
        _ <- sql"insert into github_access (user_id, github_id, github_email) values($id, ${d.id}, ${d.email})"
          .update.run
      } yield id
    id.attempt.transact(xa)
  }

  def queryByLoginEmail(email: String): F[Option[(UserId, PasswordHash)]] =
    sql"select user_id, password_hash from credentials where email = $email"
      .query[(Long, String)]
      .option.transact(xa)

  def queryByGitHubId(gitHubId: GitHubId): F[Option[UserId]] =
    sql"select user_id from github_access where github_id = $gitHubId"
      .query[Long]
      .option.transact(xa)
}
