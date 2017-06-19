package io.scalac.frees.dummy

import java.util.concurrent.TimeUnit

import cats.Id
import io.scalac.frees.dummy.AllTheMath.Op

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

object Main extends App {

  import freestyle._
  import freestyle.implicits._
  import handlersId._

  val result: Id[Int] = Formulas.`(a+b)^2`[AllTheMath.Op](10, 3).interpret[Id]
  println(result)

  def computation[F[_]](a: Int, b: Int)(implicit A: AllTheMath[F]) =
    Formulas.`(a+b)^2`(a, b)

  {
    import scala.concurrent.Await
    import scala.concurrent.ExecutionContext.Implicits.global
    import cats.implicits._ //For `Monad` instance for `Future`
    implicit val basicHandler = handlersFuture.basicMathHandler
    implicit val highHandler = handlersFuture.highMathHandler

    val resultF: Future[Int] = computation[Op](3, 10).interpret[Future]
    resultF.foreach(println)
    Await.result(resultF, FiniteDuration(1, TimeUnit.SECONDS))
  }
}
