package io.scalac.frees.login

import freestyle._
import io.scalac.frees.login.algebras.Github


object Programs {

  //Prevents annoying autoremoval of unused imports
  val freestyleImplicits = implicits

  def getUserData[F[_]](implicit A: Github[F]) = {
    import A._

    for {
      accessToken <- client.a("abc")
      _ <- log.info("Successfully got token")
      userData <- client.b(accessToken)
      _ <- log.info("Got user data")
    } yield userData
  }

}

