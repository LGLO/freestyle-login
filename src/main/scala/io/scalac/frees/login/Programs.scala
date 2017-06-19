package io.scalac.frees.login

import freestyle._


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

object AAA extends App {

  import freestyle.implicits._
  import io.scalac.frees.login.handlers.IdHandlers
  implicit val logHandler = IdHandlers.logHandler
  implicit val databaseHandler = IdHandlers.databaseHandler
  implicit val githubHandler = IdHandlers.githubFailedClientHandler

  //val z = Programs.whole[Application.Op].interpret[Id]
  //println(z)
}
