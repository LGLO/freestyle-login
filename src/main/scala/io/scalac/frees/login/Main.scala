package io.scalac.frees.login

import java.util.concurrent.{ExecutorService, Executors}

import fs2.{Stream, Task}
import io.scalac.frees.login.algebras.{Database, GithubClient, Log}
import io.scalac.frees.login.controllers.RegisterService
import io.scalac.frees.login.handlers.fs2task.dummies.{GitHubClientHandler, InMemoryDatabase, PrintlnLogger}
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Properties.envOrNone


object Main extends App {

  implicit val logHandler: Log.Handler[Task] = new PrintlnLogger
  implicit val db: Database.Handler[Task] = new InMemoryDatabase
  implicit val gh: GithubClient.Handler[Task] = new GitHubClientHandler

  val ghClient = new InHouseGHClient
  
  val server = new StreamApp {

    val port: Int = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 9000
    val ip: String = "0.0.0.0"
    val pool: ExecutorService = Executors.newCachedThreadPool()

    override def stream(args: List[String]): Stream[Task, Nothing] =
      BlazeBuilder
        .bindHttp(port, ip)
        .mountService(new RegisterService(ghClient).service)
        .withServiceExecutor(pool)
        .serve

  }

  import scala.concurrent.ExecutionContext.Implicits.global

  Future(server.main(args))
  println("Press Enter to shutdown server")
  StdIn.readLine()
  server.requestShutdown.unsafeRunAsync(_ => ())
}
