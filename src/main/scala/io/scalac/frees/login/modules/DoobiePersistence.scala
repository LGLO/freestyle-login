package io.scalac.frees.login.modules

import freestyle.doobie.DoobieM
import freestyle.{FreeS, module}
import io.scalac.frees.login.algebras._
import io.scalac.frees.login.types._
import freestyle._
import freestyle.implicits._
import freestyle.doobie._
import freestyle.doobie.implicits._
import _root_.doobie.imports._
import cats._
import cats.data._
import cats.implicits._
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
    * @param email - user login email
    * @param hash - hashed password
    * @return `AlreadyExists` in case of email already used for some
    *         credentials access, `UserInserted(id)` otherwise.
    */
  def saveCredentialsUser(email: UserEmail, hash: PasswordHash): FS[UserInsertionResult] =
    for {
      _ <- log.info(s"Inserting User with email: '$email'")
      insertAttempt <- doobie.transact(insertCredentialsUser(email, hash))
      result <- insertAttempt match {
        case Right(id) =>
          for {
            _ <- log.info(s"User with email: '$email' inserted with id: '$id'")
          } yield UserInserted(id): UserInsertionResult
        case Left(err) if err.getMessage.contains("Unique index or primary key violation") =>
          for {
            _ <- log.info(s"Another user with email: '$email' is already exists")
          } yield AlreadyExists: UserInsertionResult
        case Left(err) =>
          for {
            _ <- log.warnWithCause(s"DB failed: '$err'", err)
          } yield DBFailure(err): UserInsertionResult
      }
    } yield result

  /**
    * Creates new user with GitHub OAuth 2.0 access.
    *
    * @param ghData - GitHub details for newly created user
    * @return `AlreadyExists` in case of email already used for some
    *         credentials access, `UserInserted(id)` otherwise.
    */
  def saveGitHubUser(ghData: GitHubData): FS[UserInsertionResult] =
    for {
      _ <- log.info(s"Inserting User with GitHub id: '${ghData.id}'")
      insertAttempt <- doobie.transact(insertGitHubUser(ghData))
      result <- insertAttempt match {
        case Right(uid) =>
          for {
            _ <- log.info(s"User with GitHub Id: '${ghData.id}' inserted with id: '$uid'")
          } yield UserInserted(uid): UserInsertionResult
        case Left(err) if err.getMessage.contains("Unique index or primary key violation") =>
          for {
            _ <- log.info(s"Another user with GitHub Id: '${ghData.id}' is already exists")
          } yield AlreadyExists: UserInsertionResult
        case Left(err) =>
          for {
            _ <- log.warnWithCause(s"DB failed: '$err'", err)
          } yield DBFailure(err): UserInsertionResult
      }
    } yield result


  /**
    * Domain details like user account status
    * and password validity (perhaps expiring passwords)
    * are omitted for brevity. 
    *
    * @param email - login email of user
    * @return id of user and password if there is credentials access for given email
    */
  def getPasswordByEmail(email: UserEmail): FS[Option[(UserId, PasswordHash)]] =
    for {
      _ <- log.info(s"Looking for User with email: '$email'")
      maybeIdAndPassword <- doobie.transact(queryCredentialsByEmail(email))
    } yield maybeIdAndPassword

  def getUserByGitHubId(id: GitHubId): FS[Option[UserId]] =
    for {
      _ <- log.info(s"Looking for User with GitHub id: '$id'")
      userId <- doobie.transact(queryUserByGitHubId(id))
    } yield userId

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

  private def insertCredentialsUser(
    email: UserEmail,
    hash: PasswordHash
  ): ConnectionIO[Attempt[Long]] = {
    val id: ConnectionIO[Long] =
      for {
        id <- insertUser()
        _ <- sql"insert into credentials (user_id, email, password_hash) values($id, $email, $hash)"
          .update.run
      } yield id
    id.attempt
  }

  private def insertGitHubUser[A](d: GitHubData): ConnectionIO[Attempt[Long]] = {
    val id: ConnectionIO[Long] =
      for {
        id <- insertUser()
        _ <- sql"insert into github_access (user_id, github_id, github_email) values($id, ${d.id}, ${d.email})"
          .update.run
      } yield id
    id.attempt
  }

  private def queryCredentialsByEmail(email: String): ConnectionIO[Option[(Long, String)]] =
    sql"select user_id, password_hash from credentials where email = $email"
      .query[(Long, String)]
      .option

  private def queryUserByGitHubId(gitHubId: Long): ConnectionIO[Option[Long]] =
    sql"select user_id from github_access where github_id = $gitHubId"
      .query[Long]
      .option
}
