package io.scalac.frees.modules

import cats.Id
import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.algebras.{Database, AlreadyExists, UserInsertionResult, UserInserted}
import io.scalac.frees.login.handlers.id.{InMemoryDatabase, Level, RecordingLogger}
import io.scalac.frees.login.modules.Persistence
import io.scalac.frees.login.types._
import org.scalatest.{MustMatchers, WordSpec}


class PersistenceSpec extends WordSpec with MustMatchers {

  private val Email1 = "email1"
  val UserEmail1 = UserEmail(Email1)

  "Persistence" when {
    "inserting user" should {
      "log line before trying to insert to DB" in new Context {
        persistence.insertUser(UserEmail1).interpret[Id]
        logHandler.getRecords.filter(e => e.lvl == Level.INFO && e.msg.matches(s"Inserting.*$Email1.*")) must have size 1
      }
      "log line in case of success" in new Context {
        persistence.insertUser(UserEmail1).interpret[Id]
        logHandler.getRecords.filter(e => e.lvl == Level.INFO && e.msg.contains("inserted")) must have size 1
      }

      "fail when same email address is used again" in new Context {
        val result = persistence.insertUser(UserEmail1).flatMap { _ =>
          persistence.insertUser(UserEmail1)
        }.interpret[Id]
        result mustBe AlreadyExists(UserEmail1)
      }

      "log line in case of failure" in new Context {
        persistence.insertUser(UserEmail1).flatMap { _ =>
          persistence.insertUser(UserEmail1)
        }.interpret[Id]
        logHandler.getRecords.filter(e => e.lvl == Level.WARN && e.msg.matches(s".*$Email1.*already exists.*")) must have size 1
      }
      
      "be able to get user back by email" in new Context {
        val u = (for {
          result <- persistence.insertUser(UserEmail1)
          u <- persistence.database.getUserByEmail(UserEmail1)
        } yield u).interpret[Id]
        u.map(_.email) mustEqual Some(UserEmail1)
      }
    }
  }

  trait Context {
    implicit lazy val logHandler = new RecordingLogger
    implicit lazy val dbHandler: Database.Handler[Id] = new InMemoryDatabase
    implicit lazy val persistence = implicitly[Persistence[Persistence.Op]]
  }

}
