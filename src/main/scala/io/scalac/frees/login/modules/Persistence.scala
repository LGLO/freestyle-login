package io.scalac.frees.login.modules

import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.algebras.{Database, EmailNotUnique, Log, UserInserted}
import io.scalac.frees.login.types.{EmailAlreadyTaken, RegisterByEmailResponse, UserEmail, UserRegistered}

@module trait Persistence {
  val database: Database
  val log: Log

  def insertUser(email: UserEmail) =
    for {
      _ <- log.info(s"Inserting User with email: '$email'")
      insertResult <- database.insertUser(email)
      _ <- insertResult match {
        case UserInserted(id) =>
          log.info(s"User with email: '$email' inserted with id: '$id'")
        case EmailNotUnique(_) =>
          log.warn(s"Another user with email: '$email' is already exists!")
      }
    } yield insertResult

}
