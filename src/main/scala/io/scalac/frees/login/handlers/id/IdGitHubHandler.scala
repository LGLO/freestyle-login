package io.scalac.frees.login.handlers.id

import cats.Id
import io.scalac.frees.login.algebras._

class IdGitHubHandler(ghData: GitHubData) extends GitHubClient.Handler[Id] {
  override protected[this] def login(code: String): Id[GitHubDataResponse] =
    ghData
}

object IdGitHubHandler {
  def create(
    ghData: GitHubData = GitHubData(-1L, "not@exist")
  ) = new IdGitHubHandler(ghData)
}
