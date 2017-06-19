package io.scalac.frees.dummy

import cats.Id

import scala.concurrent.Future

//`Id` monad handlers for `BasicMath` and `HighMath` algebras.
trait handlersId {

  implicit val basicMathHandler = new BasicMath.Handler[Id] {
    def add(a: Int, b: Int): Id[Int] = a + b

    def subtract(a: Int, b: Int): Id[Int] = a - b

    def multiply(a: Int, b: Int): Id[Int] = a * b
  }

  implicit val highMathHandler = new HighMath.Handler[Id] {
    def power(a: Int, b: Int): Id[Int] =
      (0 until b).fold(1)((acc, _) => acc * a)
  }

}

object handlersId extends handlersId

//`Future` monad handlers for `BasicMath` and `HighMath` algebras.
object handlersFuture {

  import scala.concurrent.ExecutionContext.Implicits.global

  val basicMathHandler = new BasicMath.Handler[Future] {
    def add(a: Int, b: Int): Future[Int] = Future(a + b)

    def subtract(a: Int, b: Int): Future[Int] = Future(a - b)

    def multiply(a: Int, b: Int): Future[Int] = Future(a * b)
  }

  val highMathHandler = new HighMath.Handler[Future] {
    def power(a: Int, b: Int): Future[Int] = Future {
      (0 until b).fold(1)((acc, _) => acc * a)
    }
  }

}

