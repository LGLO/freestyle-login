package io.scalac.frees.login.modules

import freestyle.module
import io.scalac.frees.login.algebras.{GithubClient, Log}

@module trait Github {
  val client: GithubClient
  val log: Log
}
