package io.scalac.frees.math

object Formulas {

  import freestyle._
  import freestyle.implicits._

  def `(a+b)^2`[F[_]](a: Int, b: Int)
    (implicit A: AllTheMath[F]): FreeS[F, Int] = {
    import A._
    for {
      s <- basic.add(a, b)
      r <- high.power(s, 2)
    } yield r
  }

  def `a^2+2ab+b^2`[F[_]](a: Int, b: Int)(implicit A: AllTheMath[F]) = {
    import A._
    for {
      aa <- high.power(a, 2)
      ab <- basic.multiply(a, b)
      ab2 <- basic.multiply(2, ab)
      bb <- high.power(b, 2)
      r1 <- basic.add(aa, ab2)
      r <- basic.add(r1, bb)
    } yield r
  }
}
