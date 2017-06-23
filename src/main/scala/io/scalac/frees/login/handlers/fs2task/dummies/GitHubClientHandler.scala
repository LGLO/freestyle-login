package io.scalac.frees.login.handlers.fs2task.dummies

import fs2.Task
import io.scalac.frees.login.algebras.GithubClient

class GitHubClientHandler extends GithubClient.Handler[Task] {
  override protected[this] def a(x: String): Task[String] =
    Task.now("a")

  override protected[this] def b(x: String): Task[String] =
    Task.now("b")
}
