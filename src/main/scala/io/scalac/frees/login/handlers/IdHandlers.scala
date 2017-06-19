package io.scalac.frees.login.handlers

import cats.Id
import io.scalac.frees.login.algebras.{Database, Log, UserInserted}
import io.scalac.frees.login.GithubClient
import io.scalac.frees.login.types.{PasswordHash, User, UserEmail, UserId}
import org.log4s.Logger
import org.slf4j.LoggerFactory

object IdHandlers {
  val databaseHandler = new Database.Handler[Id] {
    def insertUser(v: UserEmail): Id[UserInserted] = UserInserted(UserId(v.hashCode.toLong))

    def getUserByEmail(email: UserEmail): Id[Option[User]] = None

    def savePassword(
      user: UserId,
      password: PasswordHash
    ): Id[Unit] = ()

    def getPassword(email: UserEmail): Id[Option[PasswordHash]] = None
    
  }

  val logHandler = new Log.Handler[Id] {

    def info(msg: String): Id[Unit] =
      println(s"[INFO]: $msg")

    def warn(msg: String): Id[Unit] =
      println(s"[WARN]: $msg")

    def warnWithCause(
      msg: String,
      cause: Throwable
    ): Id[Unit] = {
      println(s"[WARN]: $msg, caused by:\n")
      cause.printStackTrace(System.out)
    }

  }

  class IdGithubHandler extends GithubClient.Handler[Id] {

    override protected[this] def a(x: String): Id[String] = {
      println(s"a($x)")
      x + x
    }

    override protected[this] def b(x: String): Id[String] = {
      println(s"b($x)")
      "---" + x + "---"
    }
  }

  val githubClientHandler = new IdGithubHandler

  val githubFailedClientHandler = new IdGithubHandler {

    override protected[this] def b(x: String): Id[String] = throw new RuntimeException("Boom!")
  }
}
