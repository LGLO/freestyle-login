package io.scalac.frees.login.modules

import freestyle._
import freestyle.implicits._
import freestyle.module
import io.scalac.frees.login.algebras.{GitHubClient, GitHubDataResponse, Log}

@module trait GitHub {
  val client: GitHubClient
  val log: Log

  def login(code: String) = {
    for {
      _ <- log.info(s"Trying to log in with GitHub with code: '$code'")
      resp <- client.login(code)
      _ <- log.info(s"Registration response for code: '$code' is '$resp'")
    } yield resp
  }

}
