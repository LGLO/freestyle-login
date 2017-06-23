package io.scalac.frees.login.modules

import cats.free.Free
import doobie.free.connection.ConnectionOp
import freestyle.doobie.DoobieM
import freestyle.{FreeS, module}
import io.scalac.frees.login.algebras._
import io.scalac.frees.login.types.{Credentials, UserId}
import freestyle._
import freestyle.implicits._
import freestyle.doobie._
import freestyle.doobie.implicits._
import _root_.doobie.imports._
import cats._, cats.data._, cats.implicits._
import fs2.interop.cats._
import fs2.util.Attempt

@module trait DoobiePersistence {
  val log: Log
  val doobie: DoobieM
}

class DoobiePersistencePrograms[F[_]]()(implicit D: DoobiePersistence[F]) {

  import D._

  type FS[A] = FreeS[F, A]

  /**
    * Creates new user with email-password access.
    * @param c - email and password for newly created user
    * @return `AlreadyExists` in case of email already used for some
    *        credentials access, `UserInserted(id)` otherwise.
    */
  def saveCredentialsUser(c: Credentials): FS[UserInsertionResult] = {
    import c.email
    for {
      _ <- log.info(s"Inserting User with email: '$email'")
      insertResult <- doobie.transact(insertCredentialsUser(c))
      r <- insertResult match {
        case Right(id) =>
          for {
            _ <- log.info(s"User with email: '$email' inserted with id: '$id'")
          } yield UserInserted(UserId(id)): UserInsertionResult
        case Left(t) =>
          for {
            _ <- log.warn(s"Another user with email: '$email' is already exists!")
          } yield AlreadyExists: UserInsertionResult
      }
    } yield r
  }

  /**
    * Creates new user with GitHub OAuth 2.0 access.
    * @param ghData - GitHub details for newly created user
    * @return `AlreadyExists` in case of email already used for some
    *        credentials access, `UserInserted(id)` otherwise.
    */
  def saveGitHubUser(ghData: GitHubData): FS[UserInsertionResult] =
    for {
      _ <- log.info(s"Inserting User with GitHub id: '${ghData.id}'")
      insertResult <- doobie.transact(insertGitHubUser(ghData))
      r <- insertResult match {
        case Right(uid) =>
          for {
            _ <- log.info(s"User with GitHub Id: '${ghData.id}' inserted with id: '$uid'")
          } yield UserInserted(UserId(uid)): UserInsertionResult
        case Left(t) =>
          for {
            _ <- log.warnWithCause(s"Another user with GitHub Id: '${ghData.id}' is already exists!", t)
          } yield AlreadyExists: UserInsertionResult
      }
    } yield r

  /**
    * Inserts new user.
    * In real-world there would be some creation date and details here.
    * @return DB created identifier
    */
  private def insertUser(): ConnectionIO[Long] =
    for {
      _ <- sql"insert into users () values ()".update.run //just getting next autoincremented Id!
      id <- sql"select lastval()".query[Long].unique
    } yield id

  private def insertCredentialsUser(c: Credentials): ConnectionIO[Attempt[Long]] = {
    val passwordStr = new String(c.password.v)
    val id: ConnectionIO[Long] =
      for {
        id <- insertUser()
        _ <- sql"insert into credentials (user_id, email, password_hash) values($id, ${c.email}, $passwordStr)"
          .update.run
      } yield id
    id.attempt
  }

  private def insertGitHubUser[A](d: GitHubData): ConnectionIO[Attempt[Long]] = {
    val id: ConnectionIO[Long] =
      for {
        id <- insertUser()
        _ <- sql"insert into github_access (user_id, github_id, github_email) values($id, ${d.id.id}, ${d.email.email})"
          .update.run
      } yield id
    id.attempt
  }
}
