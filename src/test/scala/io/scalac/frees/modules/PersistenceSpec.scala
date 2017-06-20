package io.scalac.frees.modules

import cats.Id
import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.algebras.UserInserted
import io.scalac.frees.login.handlers.id.{InMemoryDatabase, Level, RecordingLogger}
import io.scalac.frees.login.modules.Persistence
import io.scalac.frees.login.types.UserEmail
import org.scalatest.{MustMatchers, WordSpec}

class PersistenceSpec extends WordSpec with MustMatchers {
  /*
  import freestyle._
  import freestyle.implicits._
   */

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
      "log line in case of failure" in new Context {
        persistence.insertUser(UserEmail1).flatMap { _ =>
          persistence.insertUser(UserEmail1)
        }.interpret[Id]
        logHandler.getRecords.filter(e => e.lvl == Level.WARN && e.msg.matches(s".*$Email1.*already exists.*")) must have size 1
      }
      /*"be able to get user back by email" in new Context {
        val u = (for {
          result <- persistence.insertUser(UserEmail1)
          u <- persistence.database.getUserByEmail(UserEmail1)
        } yield u).interpret[Id]
        u.email mustEqual UserEmail1
      }*/
    }
  }

  trait Context {
    implicit val logHandler = new RecordingLogger
    implicit val dbHandler = new InMemoryDatabase
    implicit val persistence = implicitly[Persistence[Persistence.Op]]
  }

}
