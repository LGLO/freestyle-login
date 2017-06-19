package io.scalac.frees.login.controllers

import io.scalac.frees.login._
import org.http4s._
import org.http4s.dsl.{->, /, GET, Ok, Root, _}

object OAuth2Callbacks {

  val Login = Root / "login"
  val ghClient = new InHouseGHClient
  private val clientId = "de3a5eea50cf961aea26"

  def service = HttpService {
    case GET -> Login =>
      import org.http4s.twirl._
      Ok(html.login("Freestyle demo", clientId))
    case req@GET -> Login / "github-callback" =>
      req.params.get("code") match {
        case None =>
          BadRequest("Missing 'code' parameter")
        case Some(code) =>
          import io.circe.generic.auto._
          import io.circe.syntax._
          import org.http4s.circe._
          import org.http4s.dsl._
          ghClient.login(code).flatMap {
            case d@GitHubData(_, _) => Ok(d.asJson)
            case GitHubInsufficientPermissions => Forbidden("inssuficient GH Permissions")
            case GitHubNoEmail => Forbidden("no verified primary email in GH")
            case GitHubFailure(t) => InternalServerError("Could not authenticate in GH")
          }
      }

  }
}
