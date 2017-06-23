package io.scalac.frees.login

import cats.Id
import io.scalac.frees.login.handlers.IdHandlers
import io.scalac.frees.login.handlers.id.{InMemoryDatabase, PrintlnLogger}
import io.scalac.frees.login.modules.{Deps, Programs}
import io.scalac.frees.login.types.{PasswordHash, UserEmail}
import freestyle._
import freestyle.implicits._
//import fs2.interop.cats._

object IdInterpreted { //extends App {

  implicit val log = new PrintlnLogger
  implicit val db = new InMemoryDatabase
  implicit val gh = IdHandlers.githubClientHandler

  private val p = PasswordHash("b".getBytes)
  private val e = UserEmail("a")
  val xxx = new Programs[Deps.Op]()
  val result = xxx.registerUser(e, p).interpret[Id]
  println(result)
}
