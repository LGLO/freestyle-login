package io.scalac.frees.login.controllers

import cats.Id
import fs2.Task
import io.scalac.frees.login._
import io.scalac.frees.login.algebras.{Database, GithubClient, Log}
import io.scalac.frees.login.modules.{Deps, Programs}
import io.scalac.frees.login.types.{AlreadyRegistered, PasswordHash, UserEmail, UserRegistered}
import org.http4s._
import org.http4s.dsl._
import freestyle._
import freestyle.implicits._
import freestyle.http.http4s._
import fs2.interop.cats._

class RegisterService(ghClient: InHouseGHClient)(
  implicit logHandler: Log.Handler[Task],
  db: Database.Handler[Task],
  gh: GithubClient.Handler[Task]
) {

  val Register = Root / "register"
  val Login = Root / "login"

  private val clientId = "de3a5eea50cf961aea26"

  implicit val app = new Programs[Deps.Op]()

  def service = HttpService {
    case GET -> Register =>

      import org.http4s.twirl._

      Ok(html.register(clientId))
    case req@POST -> Register / "credentials" =>
      registerWithCredentials(req)
    case req@GET -> Login / "github-callback" =>
      req.params.get("code") match {
        case None =>
          BadRequest("Missing 'code' parameter")
        case Some(code) =>
          import org.http4s.dsl._
          val register = req.params.get("state").contains("register")
          ghClient.login(code).flatMap {
            case d@GitHubData(_, _) =>
              if (register)
                registerWithGitHub(d)
              else
                loginWithGitHub(d)
            case GitHubInsufficientPermissions => Forbidden("inssuficient GH Permissions")
            case GitHubNoEmail => Forbidden("no verified primary email in GH")
            case GitHubFailure(t) => InternalServerError("Could not authenticate in GH")
          }

      }
  }

  private def registerWithCredentials(req: Request) = {
    req.as[UrlForm].flatMap {
      form =>
        form.getFirst("email").zip(form.getFirst("password")).headOption match {
          case Some((e, p)) =>
            val email = UserEmail(e)
            val passwordHash = PasswordHash(p.getBytes)
            app.registerUser(email, passwordHash).interpret[Task].flatMap {
              case UserRegistered(id) =>
                Ok(s"registered: $id")
              case AlreadyRegistered =>
                Conflict(s"email is already used")
              case other =>
                val msg = s"register returned: $other, ${
                  other.getClass
                }"
                println(msg)
                InternalServerError(msg)
            }
          case _ =>
            BadRequest("'email' and 'password' are mandatory!")
        }
    }
  }

  private def registerWithGitHub(ghData: GitHubData): Task[Response] = {

    import io.circe.generic.auto._
    import io.circe.syntax._
    import org.http4s.circe._

    app.registerWithGitHub(ghData).interpret[Task].flatMap {
      case UserRegistered(id) =>
        Ok(s"registered: $id")
      case AlreadyRegistered =>
        Conflict(s"GitHub Id is already used")
      case other =>
        val msg = s"register returned: $other, ${
          other.getClass
        }"
        println(msg)
        InternalServerError(msg)
    }
    //Ok(ghData.asJson)
  }

  private def loginWithGitHub(ghData: GitHubData): Task[Response] = {

    import io.circe.generic.auto._
    import io.circe.syntax._
    import org.http4s.circe._

    /*app.registerWithGitHub(ghData).interpret[Task].flatMap {
      case UserRegistered(id) =>
        Ok(s"registered: $id")
      case AlreadyRegistered =>
        Conflict(s"GitHub Id is already used")
      case other =>
        val msg = s"register returned: $other, ${
          other.getClass
        }"
        println(msg)
        InternalServerError(msg)
    }*/
    Forbidden("not implemented")
    //Ok(ghData.asJson)
  }

}
