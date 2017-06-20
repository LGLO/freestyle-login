package io.scalac.frees.login.algebras

import freestyle._


@free trait GithubClient {
  def a(x: String): FS[String]

  def b(x: String): FS[String]
}

@module trait Github {
  val client: GithubClient
  val log: Log
}


