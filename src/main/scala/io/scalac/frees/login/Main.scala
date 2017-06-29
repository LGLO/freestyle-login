package io.scalac.frees.login

import java.util.concurrent.{ExecutorService, Executors}

import _root_.doobie.h2.h2transactor.H2Transactor
import _root_.doobie.imports.Transactor
import cats.Id
import cats.arrow.FunctionK
import freestyle.FSHandler
import fs2.{Strategy, Stream, Task}
import io.scalac.frees.login.algebras.{GitHubClient, JwtService, Log, LoginDatabase}
import io.scalac.frees.login.controllers.RegisterService
import io.scalac.frees.login.crypto.EllipticCurveCrypto
import io.scalac.frees.login.handlers.id.{IdJwtHandler, PrintlnLogger}
import io.scalac.frees.login.handlers.task.database.LoginDoobieHandler
import io.scalac.frees.login.handlers.task.github.InHouseGHClient
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp

import scala.concurrent.Future
import scala.io.StdIn
import scala.util.Properties.envOrNone

object Main extends App {

  private val clientSecret = {
    val envValue = System.getenv("GH_CLIENT_SECRET")
    if (envValue == null) {
      val msg = "GitHub secret environment variable GH_CLIENT_SECRET is not set!"
      sys.error(msg)
      throw new RuntimeException(msg)
    } else {
      envValue
    }
  }
  private val clientId = {
    val envValue = System.getenv("GH_CLIENT_ID")
    if (envValue == null) {
      val msg = "GitHub id environment variable GH_CLIENT_ID is not set!"
      sys.error(msg)
      throw new RuntimeException(msg)
    } else {
      envValue
    }
  }

  val idToTask = new FunctionK[Id, Task] {
    override def apply[A](fa: Id[A]): Task[A] = Task(fa)(Strategy.sequential)
  }

  implicit val logHandler: FSHandler[Log.Op, Task] = (new PrintlnLogger).andThen(idToTask)
  implicit val gh: GitHubClient.Handler[Task] = new InHouseGHClient(clientId, clientSecret)
  implicit val xa: Transactor[Task] =
    H2Transactor[Task]("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", "").unsafeRunSync.
      toOption.getOrElse(throw new Exception("Could not create example transactor"))
  implicit val db: LoginDatabase.Handler[Task] = new LoginDoobieHandler(xa)

  implicit val jwtService: FSHandler[JwtService.Op, Task] = {
    val kp = EllipticCurveCrypto.genKeyPair
    new IdJwtHandler(kp.getPublic, kp.getPrivate).andThen(idToTask)
  }

  DB.setup(xa)

  val server = new StreamApp {

    val port: Int = envOrNone("HTTP_PORT") map (_.toInt) getOrElse 9000
    val ip: String = "0.0.0.0"
    val pool: ExecutorService = Executors.newCachedThreadPool()

    override def stream(args: List[String]): Stream[Task, Nothing] =
      BlazeBuilder
        .bindHttp(port, ip)
        .mountService(new RegisterService().service)
        .withServiceExecutor(pool)
        .serve

  }

  import scala.concurrent.ExecutionContext.Implicits.global

  Future(server.main(args))
  println("Press Enter to shutdown server")
  StdIn.readLine()
  server.requestShutdown.unsafeRunAsync(_ => ())
}
