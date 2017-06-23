package io.scalac.frees.login.modules

import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.algebras._
import io.scalac.frees.login.types._

import cats.free.Free

@module trait Persistence {
  val database: Database
  val log: Log

  def insertUser(credentials: Credentials) = {
    import credentials._
    for {
      _ <- log.info(s"Inserting User with email: '$email'")
      insertResult <- database.insertCredentialsUser(credentials)
      _ <- insertResult match {
        case UserInserted(id) =>
          log.info(s"User with email: '$email' inserted with id: '$id'")
        case AlreadyExists =>
          log.warn(s"Another user with email: '$email' is already exists!")
      }
    } yield insertResult
  }

  def insertGitHubUser(ghData: GitHubData) = {
    import ghData._
    for {
      _ <- log.info(s"Inserting User with GitHub id: '${ghData.id}'")
      insertResult <- database.insertGitHubUser(ghData)
      _ <- insertResult match {
        case UserInserted(uid) =>
          log.info(s"User with GitHub Id: '${ghData.id}' inserted with id: '$uid'")
        case AlreadyExists =>
          log.warn(s"Another user with GitHub Id: '${ghData.id}' is already exists!")
      }
    } yield insertResult
  }

}
