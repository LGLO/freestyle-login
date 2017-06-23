package io.scalac.frees.login

import doobie.imports.{Transactor, Update0}
import fs2.Task
import _root_.doobie.imports._
import cats.implicits._
import fs2.interop.cats._

object DBSetup {

  def apply(xa: Transactor[Task]): Unit = {

    val createUsers: Update0 =
      sql"CREATE TABLE users (id IDENTITY)".update

    val createCredentials: Update0 =
      sql"""CREATE TABLE credentials (
            user_id BIGINT,
            email VARCHAR NOT NULL UNIQUE,
            password VARCHAR NOT NULL
          )
      """.update

    val createGHAccess: Update0 =
      sql"""CREATE TABLE github_access (
            user_id BIGINT,
            github_id BIGINT NOT NULL UNIQUE,
            github_email VARCHAR NOT NULL
          )
      """.update

    (createUsers.run *> createCredentials.run *> createGHAccess.run)
      .transact(xa).unsafeRun()
  }
}
