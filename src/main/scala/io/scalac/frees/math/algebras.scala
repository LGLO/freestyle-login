package io.scalac.frees.math

import freestyle._

@free trait BasicMath {
  def add(a: Int, b: Int): FS[Int]

  def subtract(a: Int, b: Int): FS[Int]

  def multiply(a: Int, b: Int): FS[Int]
}

@free trait HighMath {
  def power(a: Int, b: Int): FS[Int]
}

@module trait AllTheMath {
  val basic: BasicMath
  val high: HighMath

  def squareOfSum(a: Int, b: Int) =
    for {
      sum <- basic.add(a, b)
      square <- high.power(sum, 2)
    } yield square

  def `ab2`(a: Int, b: Int) =
    for {
      sum <- basic.add(a, b)
      square <- high.power(sum, 2)
    } yield square
}

class AllTheMathPrograms[F[_]](implicit A: AllTheMath[F]) {

  import A._

  type FS[A] = FreeS[F, A]

  def `(a+b)^2`(a: Int, b: Int): FS[Int] =
    for {
      sum <- basic.add(a, b)
      square <- high.power(sum, 2)
    } yield square
}