package io.scalac.frees.login

import cats.Id
import io.scalac.frees.login.handlers.id.{IdGitHubHandler, InMemoryDatabase, PrintlnLogger}
import io.scalac.frees.login.modules.{Deps, Programs}
import io.scalac.frees.login.types.{PasswordHash, UserEmail}
import freestyle._
import freestyle.implicits._
import fs2.Task
import fs2.util.{Attempt, Catchable, Suspendable}
//import fs2.interop.cats._
import _root_.doobie.imports.Transactor
import _root_.doobie.h2.h2transactor.H2Transactor
import freestyle.doobie._
import freestyle.doobie.implicits._

object IdInterpreted { //extends App {

  implicit val log = new PrintlnLogger
  implicit val db = new InMemoryDatabase
  implicit val gh = IdGitHubHandler.create()
  
  implicit val catchableId = new Catchable[Id] with Suspendable[Id]{
    override def fail[A](err: Throwable): Id[A] = throw err

    override def attempt[A](fa: Id[A]): Id[Attempt[A]] = Attempt.success(fa)

    override def flatMap[A, B](a: Id[A])(f: (A) => Id[B]): Id[B] = f(a)

    override def pure[A](a: A): Id[A] = a

    override def suspend[A](fa: => Id[A]): Id[A] = fa
  }

  implicit val xa: Transactor[Id] =
    H2Transactor[Id]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "")


  private val password = PasswordHash("b".getBytes)
  private val email = UserEmail("a")
  val p = new Programs[Deps.Op]()
  val result = p.registerUser(email, password).interpret[Id]
  println(result)
}
