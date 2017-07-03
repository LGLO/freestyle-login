package io.scalac.frees.modules

import cats.Id
import io.scalac.frees.login.modules._
import io.scalac.frees.login.types._
import org.scalatest.{MustMatchers, WordSpec}
import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.handlers.id.{Level, RecordingLogger}
import fs2.util.Attempt
import io.scalac.frees.login.algebras._
import com.github.t3hnar.bcrypt._

class ProgramsSpec extends WordSpec with MustMatchers {

  //Credentials expected by test interpreters.
  val expectedEmail: UserEmail = "expected@email.com"
  val expectedPasswd: PasswordHash = "abracadabra".bcrypt
  val existingLoginEmail = "existing@email.com"

  val expectedGitHubId: GitHubId = 1234321L
  val expectedGitHubEmail: UserEmail = "email@from.github"
  val existingGitHubId: GitHubId = 4321234L

  val newUser1Id = 101

  val validGHCode = "abcedf0987654321"

  "registerUser(Credentials)" when {
    "used email is unique" should {
      "register user" in new Context {
        val response = p.registerUser(Credentials(expectedEmail, expectedPasswd)).interpret[Id]
        response mustBe UserRegistered(newUser1Id)
      }
    }

    "DB fails" should {
      "report InternalFailure and log reason" in new Context {

        val err = new Exception("expected exception")

        override implicit val db = new Db {
          override def insertCredentialsUser(
            e: UserEmail,
            p: PasswordHash
          ): Id[Attempt[UserId]] = Left(err)
        }
        val response = p.registerUser(Credentials("will-cause-db-error", "abracadabra")).interpret[Id]
        response mustBe InternalFailure(err)
        log.getRecords.exists(e => e.lvl == Level.WARN && e.cause.contains(err))
      }
    }

    "credentials access already contains given email" should {
      "return AlreadyExists and log that conflict" in new Context {
        val duplicatedEmailCredentials = Credentials(existingLoginEmail, "")
        val response = p.registerUser(duplicatedEmailCredentials).interpret[Id]
        response mustBe AlreadyRegistered
        log.getRecords.exists { e =>
          e.lvl == Level.WARN &&
            e.msg.contains(s"'$existingLoginEmail' is already used")
        }
      }
    }
  }

  "registerGitHubUser" when {
    "GitHub Id is unique" should {
      "register user" in new Context {
        val response = p.registerWithGitHub(validGHCode).interpret[Id]
        response mustBe UserRegistered(newUser1Id)
      }
    }

    "GitHub Id is not unique" should {
      "return AlreadyExists and log that conflict" in new Context {
        override implicit val gh = new GH {
          override def login(code: String): Id[GitHubDataResponse] = GitHubData(existingGitHubId, "")
        }

        val response = p.registerWithGitHub(validGHCode).interpret[Id]
        response mustBe GHAlreadyRegistered
        log.getRecords.exists { e =>
          e.lvl == Level.WARN &&
            e.msg.contains(s"'$existingLoginEmail' is already used")
        }
      }
    }

    "gitHubLogin" when {
      "GitHub OAuth2.0 login is successful and user is registered" should {
        "issue token" in new Context {
          val response = p.gitHubLogin(validGHCode).interpret[Id]
          response mustBe LoggedIn(s"token-for-$newUser1Id")
        }
      }
    }
  }

  class Db extends LoginDatabase.Handler[Id] {
    override def insertCredentialsUser(
      e: UserEmail,
      p: PasswordHash
    ): Id[Attempt[UserId]] = (e, p) match {
      case (`expectedEmail`, pass) if expectedPasswd.isBcrypted(pass) =>
        Right(newUser1Id)
      case (`existingLoginEmail`, _) =>
        Left(new java.sql.SQLException("-Unique index or primary key violation-"))
      case _ =>
        Left(new RuntimeException("Unexpected parameter"))
    }

    override def insertGitHubUser(d: GitHubData): Id[Attempt[UserId]] = d match {
      case GitHubData(`expectedGitHubId`, `expectedGitHubEmail`) =>
        Right(newUser1Id)
      case GitHubData(`existingGitHubId`, _) =>
        Left(new java.sql.SQLException("-Unique index or primary key violation-"))
      case _ =>
        Left(new RuntimeException("Unexpected parameter"))
    }

    override def queryByLoginEmail(e: UserEmail): Id[Option[(UserId, PasswordHash)]] = ???

    override def queryByGitHubId(id: GitHubId): Id[Option[UserId]] = Some(newUser1Id)
  }

  class GH extends GitHubClient.Handler[Id] {

    override def login(code: String): Id[GitHubDataResponse] = code match {
      case `validGHCode` =>
        GitHubData(expectedGitHubId, expectedGitHubEmail)
      case _ =>
        GitHubFailure(new RuntimeException("unexpected parameter"))
    }
  }

  trait Context {
    implicit val log = new RecordingLogger

    implicit val db = new Db

    implicit val jwt = new JwtService.Handler[Id] {
      override protected[this] def issue(id: UserId): Id[JWT] = s"token-for-$id"

      override protected[this] def validate(jwt: JWT): Id[Option[Claims]] = ???
    }

    implicit val gh = new GH
    val p = new Programs[Deps.Op]()

  }

}