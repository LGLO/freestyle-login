package io.scalac.frees.modules

import java.sql.Connection

import cats.Id
import io.scalac.frees.login.modules.{Deps, DoobiePersistence, DoobiePersistencePrograms, Programs}
import io.scalac.frees.login.types.{Credentials, JWT, UserId}
import org.scalatest.{MustMatchers, WordSpec}
import freestyle._
import freestyle.doobie.DoobieM
import freestyle.implicits._
import io.scalac.frees.login.handlers.id.PrintlnLogger
import _root_.doobie.imports.ConnectionIO
import _root_.doobie.h2.h2transactor.H2Transactor
import _root_.doobie.imports.Transactor
import cats.arrow.FunctionK
import fs2.Task
import io.scalac.frees.login.DB
import io.scalac.frees.login.algebras._
import sun.reflect.generics.reflectiveObjects.NotImplementedException

class ProgramsSpec extends WordSpec with MustMatchers {

  "registerUser(Credentials)" when {
    "used email is unique" should {
      "register user" in {

        val p = Programs[Deps.Op]
        
        implicit val dph = new DoobiePersistence.Handler[Id]{
          
        }
        
        implicit def log = new PrintlnLogger

        //I give up on trying to test it "pure-way" 
        /*implicit val xa: Transactor[Task] =
          H2Transactor[Id]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "").unsafeRunSync.
            toOption.getOrElse(throw new Exception("Could not create example transactor"))

        DB.setup(xa)*/


        implicit def jwt = new JwtService.Handler[Id] {
          override protected[this] def issue(id: UserId): Id[JWT] = s"token-for-$id"

          override protected[this] def validate(jwt: JWT): Id[Option[Claims]] = ???
        }

        implicit def github = new GitHubClient.Handler[Id] {
          override protected[this] def login(code: String): Id[GitHubDataResponse] = ???
        }

        p.registerUser(Credentials("joe.doe@email.com", "abracadabra")).interpret[Id]
      }
    }
  }

}
