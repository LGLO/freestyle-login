package io.scalac.frees.login

import cats.Id
import cats.arrow.FunctionK
import freestyle._
import freestyle.implicits._
import cats.implicits._

//TODO: Make PR to document this issue.

@free trait A {
  def abc(): FS[String]
}

class AHandler extends A.Handler[Id] {
  override protected[this] def abc: Id[String] = "abc"
}

object IdToOptionNPE extends App {

  implicit val idHandler: A.Handler[Id] = new AHandler
  implicit val optionHandler: A.Handler[Option] = implicitly[A.Handler[Option]]

  A[A.Op].abc().interpret[Option]
}

object IdToOption extends App {

  val idToOption = new FunctionK[Id, Option] {
    override def apply[A](fa: Id[A]): Option[A] = Option(fa)
  }

  implicit val idHandler: A.Handler[Id] = new AHandler
  implicit val taskHandler: FSHandler[A.Op, Option] = idHandler.andThen(idToOption)

  A[A.Op].abc().interpret[Option]
}
