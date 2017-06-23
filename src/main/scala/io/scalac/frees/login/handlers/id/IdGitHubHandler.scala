package io.scalac.frees.login.handlers.id

import cats.Id
import io.scalac.frees.login.algebras._

class IdGitHubHandler(ghData: GitHubData) extends GitHubClient.Handler[Id] {
  override protected[this] def login(code: String): Id[GitHubDataResponse] =
    ghData
}

object IdGitHubHandler {
  def create(
    ghData: GitHubData =
    GitHubData(GitHubId(-1L), GitHubEmail("not@exist", primary = true, verified = true))
  ) = new IdGitHubHandler(ghData)
}
