package io.scalac.frees.login.modules

import freestyle.{FreeS, module}
import io.scalac.frees.login.algebras._
import io.scalac.frees.login.types._
import freestyle._
import freestyle.implicits._
import cats._
import cats.data._
import cats.implicits._

@module trait LoginPersistence {
  val log: Log
  val db: LoginDatabase
}

class PersistencePrograms[F[_]]()(implicit D: LoginPersistence[F]) {

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
      insertAttempt <- db.insertCredentialsUser(email, hash)
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
      insertAttempt <- db.insertGitHubUser(ghData)
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
      maybeIdAndPassword <- db.queryByLoginEmail(email)
    } yield maybeIdAndPassword

  def getUserByGitHubId(id: GitHubId): FS[Option[UserId]] =
    for {
      _ <- log.info(s"Looking for User with GitHub id: '$id'")
      userId <- db.queryByGitHubId(id)
    } yield userId


}
