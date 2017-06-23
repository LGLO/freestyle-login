package io.scalac.frees.login.handlers.id

import cats.Id
import io.scalac.frees.login.algebras.{GitHubClient, GitHubDataResponse, GitHubFailure}

class FailedIdGitHubHandler extends GitHubClient.Handler[Id] {
  override protected[this] def login(code: String): Id[GitHubDataResponse] =
    GitHubFailure(new RuntimeException("You are using client that always fails!"))
}
