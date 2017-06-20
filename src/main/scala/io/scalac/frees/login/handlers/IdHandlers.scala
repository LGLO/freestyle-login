package io.scalac.frees.login.handlers

import cats.Id
import io.scalac.frees.login.algebras.{Database, GithubClient, Log, UserInserted}
import io.scalac.frees.login.types.{PasswordHash, User, UserEmail, UserId}
import org.log4s.Logger
import org.slf4j.LoggerFactory

object IdHandlers {

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
