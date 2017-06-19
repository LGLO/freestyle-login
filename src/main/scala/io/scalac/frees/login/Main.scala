package io.scalac.frees.login

import java.util.concurrent.{ExecutorService, Executors}

import fs2.{Stream, Task}
import io.scalac.frees.login.controllers.OAuth2Callbacks
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Properties.envOrNone


object Main extends App {

  val server = new StreamApp {

    val port: Int = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 9000
    val ip: String = "0.0.0.0"
    val pool: ExecutorService = Executors.newCachedThreadPool()

    override def stream(args: List[String]): Stream[Task, Nothing] =
      BlazeBuilder
        .bindHttp(port, ip)
        .mountService(OAuth2Callbacks.service)
        .withServiceExecutor(pool)
        .serve

  }

  import scala.concurrent.ExecutionContext.Implicits.global

  Future(server.main(args))
  println("Press Enter to shutdown server")
  StdIn.readLine()
  server.requestShutdown.unsafeRunAsync(_ => ())
}
