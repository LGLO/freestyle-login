package io.scalac.frees.login

import fs2.Task
import fs2.util.NonFatal
import org.http4s._
import org.http4s.client.blaze.PooledHttp1Client
import org.http4s.dsl.POST

class InHouseGHClient {

  private val clientSecret = System.getenv("GH_CLIENT_SECRET")
  private val clientId = "de3a5eea50cf961aea26"

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

      val requestUserId: Task[Long] = {
        val uri = Uri.uri("https://api.github.com/user")
          .withQueryParam("access_token", accessToken)
        httpClient
          .expect(uri)(jsonOf[GitHubUser])
          .map(_.id) //.map { j =>
        //Really unsafe Json traversal
        //j.asObject.get.toMap("id").asNumber.get.toLong.get
        //}
      }

      val requestPrimaryEmail: Task[Option[String]] = {
        val uri = Uri.uri("https://api.github.com/user/emails")
          .withQueryParam("access_token", accessToken)
        httpClient.expect(uri)(jsonOf[Vector[GitHubEmail]])
          .map(_.filter(e => e.primary && e.verified).map(_.email).headOption)
      }

      for {
        id <- requestUserId
        emailOpt <- requestPrimaryEmail
      } yield {
        emailOpt match {
          case Some(email) => GitHubData(id, email)
          case None => GitHubNoEmail
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

sealed trait GitHubDataResponse

case class GitHubData(id: Long, email: String) extends GitHubDataResponse

case class GitHubFailure(th: Throwable) extends GitHubDataResponse

case object GitHubInsufficientPermissions extends GitHubDataResponse

case object GitHubNoEmail extends GitHubDataResponse

//Following classes are for circe decoding and basic logic over these
case class GitHubUser(id: Long)

case class GitHubEmail(email: String, primary: Boolean, verified: Boolean)