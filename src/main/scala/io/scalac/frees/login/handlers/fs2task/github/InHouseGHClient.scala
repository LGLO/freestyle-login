package io.scalac.frees.login.handlers.fs2task.github

import fs2.Task
import fs2.util.NonFatal
import io.scalac.frees.login.algebras._
import org.http4s._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl.POST

/**
  * This is simple implementation of GitHubClient,
  * which is also `GitHubClient.Handler[Task]`.
  * It was created as part of learning when writing blog post but it is not really
  * related to Free Monads.
  * I wanted to use `github4s` but I could not obtain emails list with it.
  */
class InHouseGHClient(
  val clientId: String,
  val clientSecret: String
) extends GitHubClient.Handler[Task] {

  private val httpClient = PooledHttp1Client()

  def login(code: String): Task[GitHubDataResponse] = {
    def requestAccessToken: Task[UrlForm] = {
      val form = UrlForm(
        "client_id" -> clientId,
        "client_secret" -> clientSecret,
        "code" -> code
      )
      for {
        entity <- UrlForm.entityEncoder.toEntity(form)
        request = Request(
          method = POST,
          uri = Uri.uri("https://github.com/login/oauth/access_token"),
          body = entity.body
        )
        resp <- httpClient.expect[UrlForm](request)
      } yield resp
    }

    def requestUserData(accessToken: String): Task[GitHubDataResponse] = {
      import io.circe.generic.auto._
      import org.http4s.circe._

      val requestUserId: Task[GitHubId] = {
        val uri = Uri.uri("https://api.github.com/user")
          .withQueryParam("access_token", accessToken)
        httpClient
          .expect(uri)(jsonOf[GitHubUser])
          .map(u => GitHubId(u.id))
      }

      val requestPrimaryVerifiedEmail: Task[Option[String]] = {
        val uri = Uri.uri("https://api.github.com/user/emails")
          .withQueryParam("access_token", accessToken)
        httpClient.expect(uri)(jsonOf[Vector[GitHubEmail]])
          .map(_.filter(e => e.primary && e.verified).map(_.email).headOption)
      }

      for {
        id <- requestUserId
        emailOpt <- requestPrimaryVerifiedEmail
      } yield {
        emailOpt match {
          case Some(email) =>
            GitHubData(id, GitHubEmail(email, primary = true, verified = true))
          case None =>
            GitHubNoEmail
        }
      }
    }

    requestAccessToken.flatMap { f =>
      val hasScope = f.get("scope").exists(s => s == "user" || s == "user:email")
      val accessTokenOpt: Option[String] = f.getFirst("access_token")
      if (hasScope && accessTokenOpt.isDefined)
        requestUserData(accessTokenOpt.get)
      else
        Task.now(GitHubInsufficientPermissions)
    }.handle {
      case NonFatal(t) =>
        GitHubFailure(t)
    }
  }

}
