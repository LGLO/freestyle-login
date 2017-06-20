package io.scalac.frees.login.algebras

import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.types.UserEmail

@module trait UseDatabase {
  val db:Database
  
  def use = for {
    r <- db.insertUser(UserEmail("a"))
    u <- db.getUserByEmail(UserEmail(r.toString))
  } yield u
}
