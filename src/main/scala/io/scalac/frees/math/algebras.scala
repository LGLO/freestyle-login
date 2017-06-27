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
}
