package io.scalac.frees.login.controllers

import io.scalac.frees.login.algebras._
import io.scalac.frees.login.modules.{Deps, Programs}
import io.scalac.frees.login.types.{Credentials => UserCredentials, _}
import org.http4s._
import org.http4s.dsl._
import org.http4s.headers.`Authorization`
import org.http4s.twirl._
import org.http4s.Credentials.Token
import freestyle._
import freestyle.doobie._
import freestyle.doobie.implicits._
import freestyle.http.http4s._
import freestyle.implicits._
import fs2.interop.cats._
import fs2.Task
import _root_.doobie.imports.Transactor

class RegisterService()(
  implicit logHandler: FSHandler[Log.Op, Task],
  //db: FSHandler[Database.Op, Task],
  gh: FSHandler[GitHubClient.Op, Task],
  xa: FSHandler[DoobieM.Op, Task],
  jwt: FSHandler[JwtService.Op, Task]
) {

  val Register = Root / "register"
  val Login = Root / "login"

  private val clientId = "de3a5eea50cf961aea26"

  implicit val app = new Programs[Deps.Op]()

  def service = HttpService {
    case GET -> Login =>
      Ok(html.login(clientId))
    case GET -> Register =>
      Ok(html.register(clientId))
    case req@POST -> Register / "credentials" =>
      registerWithCredentials(req)
    case req@POST -> Login / "credentials" =>
      loginWithCredentials(req)
    case req@GET -> Login / "github-callback" =>
      handleGitHubCallback(req)
  }

  private def registerWithCredentials(req: Request): Task[Response] = {
    req.as[UrlForm].flatMap { form =>
      readEmailAndPassword(form) match {
        case Some((email, password)) =>
          app.registerUser(UserCredentials(email, password))
            .interpret[Task].flatMap {
            case UserRegistered(id) =>
              Ok(s"registered: $id")
            case AlreadyRegistered =>
              Conflict(s"email is already used")
            case InternalFailure(err) =>
              InternalServerError(s"$err")
          }
        case _ =>
          BadRequest("'email' and 'password' are mandatory!")
      }
    }
  }

  private def loginWithCredentials(req: Request) = {
    req.as[UrlForm].flatMap { form =>
      readEmailAndPassword(form) match {
        case Some((email, password)) =>
          app.login(UserCredentials(email, password))
            .interpret[Task].flatMap {
            case LoggedIn(token) =>
              Ok().putHeaders(`Authorization`(Token(AuthScheme.Bearer, token)))
            case InvalidCredentials =>
              Forbidden(s"invalid credentials")
          }
        case _ =>
          BadRequest("'email' and 'password' are mandatory!")
      }
    }
  }

  private def readEmailAndPassword(form: UrlForm): Option[(String, String)] =
    form.getFirst("email").zip(form.getFirst("password")).headOption

  private def handleGitHubCallback(req: Request): Task[Response] = {
    req.params.get("code") match {
      case None =>
        BadRequest("Missing 'code' parameter")
      case Some(code) =>
        if (req.params.get("state").contains("register"))
          registerWithGitHub(code)
        else
          loginWithGitHub(code)
    }
  }

  private def registerWithGitHub(code: String): Task[Response] =
    app.registerWithGitHub(code).interpret[Task].flatMap {
      case UserRegistered(id) =>
        Ok(s"registered: $id")
      case GHAlreadyRegistered =>
        Conflict(s"GitHub Id is already used")
      case GHError(GitHubFailure(err)) =>
        InternalServerError(s"GitHub login did not succeed: $err")
      case GHError(reason) =>
        Forbidden(s"$reason")
      case InternalFailure(err) =>
        InternalServerError(s"$err")
    }

  private def loginWithGitHub(code: String): Task[Response] =
    app.gitHubLogin(code).interpret[Task].flatMap {
      case LoggedIn(token) =>
        Ok().putHeaders(`Authorization`(Token(AuthScheme.Bearer, token)))
      case GHUserNotRegistered =>
        Forbidden(s"Not registered")
      case GHLoginError =>
        InternalServerError("GitHub login failed")
    }

}
