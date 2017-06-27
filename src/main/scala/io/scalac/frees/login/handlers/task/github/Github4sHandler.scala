package io.scalac.frees.login.handlers.task.github

import fs2.{Strategy, Task}
import github4s.Github
import github4s.Github._
import github4s.GithubResponses.{GHIO, GHResponse}
import github4s.free.domain.OAuthToken
import github4s.jvm.Implicits._
import io.scalac.frees.login.algebras._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scalaj.http.HttpResponse

/** Handler that uses github4s client,
  * unfortunately it can return only public email,
  * so it will return `GitHubNoEmail` for most GH Users.
  * @param clientId `client_id` param of GitHub OAuth2 requests
  * @param clientSecret `client_secret` param of GitHub OAuth2 requests
  */
class Github4sHandler (
  val clientId: String,
  val clientSecret: String
) extends GitHubClient.Handler[Task] {

  def login(code: String): Task[GitHubDataResponse] = {
    val ec = scala.concurrent.ExecutionContext.global
    implicit val strategy = Strategy.fromExecutionContext(ec)

    val emptyStringAsEmptyValue = ""
    val tokenGHIO: GHIO[GHResponse[OAuthToken]] =
      Github(None).auth.getAccessToken(
        clientId,
        clientSecret,
        code,
        redirect_uri = emptyStringAsEmptyValue,
        state = emptyStringAsEmptyValue
      )

    def getUserData(access_token: String): Future[GitHubDataResponse] = {
      Github(Some(access_token)).users.getAuth
        .execFuture[HttpResponse[String]]()
        .map {
          case Right(ghResult) =>
            val user = ghResult.result
            user.email.map { email: String =>
              GitHubData(user.id.toLong, email)
            }.getOrElse(GitHubNoEmail)
          case Left(ex) => GitHubFailure(ex)
        }
    }

    val resp = tokenGHIO
      .execFuture[HttpResponse[String]]()
      .flatMap {
        case Right(ghResult) =>
          val oAuthToken = ghResult.result
          val access_token = oAuthToken.access_token
          val scopes = oAuthToken.scope.split(",")
          if (scopes.exists(s => s == "user" || s == "user:email")) {
            getUserData(access_token)
          } else {
            Future.successful(GitHubInsufficientPermissions)
          }
        case Left(ex) =>
          Future.successful(GitHubFailure(ex))
      }

    Task.fromFuture(resp)
  }
}