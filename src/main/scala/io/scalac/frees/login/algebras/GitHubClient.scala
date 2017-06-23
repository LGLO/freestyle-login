package io.scalac.frees.login.algebras

import freestyle._

case class GitHubId(id: Long) extends AnyVal

sealed trait GitHubDataResponse
sealed trait GitHubErrorResponse

case class GitHubData(id: GitHubId, email: GitHubEmail) extends GitHubDataResponse

case class GitHubFailure(th: Throwable) extends GitHubDataResponse with GitHubErrorResponse

case object GitHubInsufficientPermissions extends GitHubDataResponse with GitHubErrorResponse

case object GitHubNoEmail extends GitHubDataResponse with GitHubErrorResponse


//Following classes are for circe decoding and basic logic over these
case class GitHubUser(id: Long) extends AnyVal

case class GitHubEmail(email: String, primary: Boolean, verified: Boolean)

@free trait GitHubClient {
  def login(code: String): FS[GitHubDataResponse]
}
