package io.scalac.frees.modules

import java.sql.Connection

import cats.Id
import io.scalac.frees.login.modules._
import io.scalac.frees.login.types._
import org.scalatest.{MustMatchers, WordSpec}
import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.handlers.id.{Level, PrintlnLogger, RecordingLogger}
import _root_.doobie.imports.ConnectionIO
import _root_.doobie.h2.h2transactor.H2Transactor
import _root_.doobie.imports.Transactor
import cats.arrow.FunctionK
import fs2.Task
import fs2.util.Attempt
import io.scalac.frees.login.DB
import io.scalac.frees.login.algebras._
import sun.reflect.generics.reflectiveObjects.NotImplementedException

import scala.util.{Failure, Success}

class ProgramsSpec extends WordSpec with MustMatchers {

  val existingLoginEmail = "existing@email.com"
  val existingPlainPassword = "asdf"

  val expectedNewUser1Email = "expected@email.com"
  val newUser1Id = 101

  "registerUser(Credentials)" when {
    "used email is unique" should {
      "register user" in new Context {
        val response = p.registerUser(Credentials(expectedNewUser1Email, "abracadabra")).interpret[Id]
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


  class Db extends LoginDatabase.Handler[Id] {
    override def insertCredentialsUser(
      e: UserEmail,
      p: PasswordHash
    ): Id[Attempt[UserId]] = e match {
      case `expectedNewUser1Email` => Right(newUser1Id)
      case `existingLoginEmail` => Left(new java.sql.SQLException("-Unique index or primary key violation-"))
      case _ => Left(new RuntimeException("Unexpected parameter"))
    }

    override def insertGitHubUser(d: GitHubData): Id[Attempt[UserId]] = ???

    override def queryByLoginEmail(e: UserEmail): Id[Option[(UserId, PasswordHash)]] = ???

    override def queryByGitHubId(id: GitHubId): Id[Option[UserId]] = ???
  }

  trait Context {
    implicit val log = new RecordingLogger

    implicit val db = new Db

    implicit val jwt = new JwtService.Handler[Id] {
      override protected[this] def issue(id: UserId): Id[JWT] = s"token-for-$id"

      override protected[this] def validate(jwt: JWT): Id[Option[Claims]] = ???
    }

    implicit val github = new GitHubClient.Handler[Id] {
      override protected[this] def login(code: String): Id[GitHubDataResponse] = ???
    }

    val p = Programs[Deps.Op]

  }

}
