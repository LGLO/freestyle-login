package io.scalac.frees.login

import freestyle._
import freestyle.implicits._
import cats.implicits._
import io.scalac.frees.login.algebras.Log
import io.scalac.frees.login.types._


@free trait GithubClient {
  def a(x: String): FS[String]

  def b(x: String): FS[String]
}

@module trait Github {
  val client: GithubClient
  val log: Log
}


