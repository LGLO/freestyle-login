package io.scalac.frees.login.algebras

import freestyle._

case class GitHubId(id: Long) extends AnyVal

sealed trait GitHubDataResponse

case class GitHubData(id: GitHubId, email: String) extends GitHubDataResponse

case class GitHubFailure(th: Throwable) extends GitHubDataResponse

case object GitHubInsufficientPermissions extends GitHubDataResponse

case object GitHubNoEmail extends GitHubDataResponse

@free trait GithubClient {
  def login(code: String): FS[GitHubDataResponse]
}
