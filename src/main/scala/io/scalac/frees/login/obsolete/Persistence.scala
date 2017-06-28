package io.scalac.frees.login.obsolete

import freestyle._
import io.scalac.frees.login.algebras._
import io.scalac.frees.login.types._

/**
  * This module is not used in application.
  * It is an alternative to a module that uses `DoobieM`.
  */
@module trait Persistence {
  val database: Database
  val log: Log

  def insertUser(email: UserEmail, hash: PasswordHash) =
    for {
      _ <- log.info(s"Inserting User with email: '$email'")
      insertResult <- database.insertCredentialsUser(email, hash)
      _ <- insertResult match {
        case UserInserted(id) =>
          log.info(s"User with email: '$email' inserted with id: '$id'")
        case AlreadyExists =>
          log.warn(s"Another user with email: '$email' is already exists!")
        case DBFailure(err) =>
          log.warnWithCause(s"DB failed: $err", err)
      }
    } yield insertResult

  def insertGitHubUser(ghData: GitHubData) =
    for {
      _ <- log.info(s"Inserting User with GitHub id: '${ghData.id}'")
      insertResult <- database.insertGitHubUser(ghData)
      _ <- insertResult match {
        case UserInserted(uid) =>
          log.info(s"User with GitHub Id: '${ghData.id}' inserted with id: '$uid'")
        case AlreadyExists =>
          log.warn(s"Another user with GitHub Id: '${ghData.id}' is already exists!")
        case DBFailure(err) =>
          log.warnWithCause(s"DB failed: $err", err)
      }
    } yield insertResult

}
