package io.scalac.frees.login.algebras

import freestyle._
import io.scalac.frees.login.types.GitHubId

sealed trait GitHubDataResponse

sealed trait GitHubErrorResponse

case class GitHubData(id: GitHubId, email: String) extends GitHubDataResponse

case class GitHubFailure(th: Throwable) extends GitHubDataResponse with GitHubErrorResponse

case object GitHubInsufficientPermissions extends GitHubDataResponse with GitHubErrorResponse

case object GitHubNoEmail extends GitHubDataResponse with GitHubErrorResponse


@free trait GitHubClient {
  def login(code: String): FS[GitHubDataResponse]
}
