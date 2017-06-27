package io.scalac.frees.login.algebras

import cats.data.Validated
import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.types.{JWT, UserId}

case class Claims(user: UserId, issuedAt: Long, expires: Long)

trait JwtService[FF$71883[_]] extends _root_.freestyle.internal.EffectLike[FF$71883] {
  def issue(id: UserId): FS[JWT]

  def validate(jwt: JWT): FS[Option[Claims]]
}

object JwtService {

  sealed trait Op[_] extends scala.Product with java.io.Serializable {
    val FSAlgebraIndex71881: _root_.scala.Int
  }

  case class IssueOP(id: UserId) extends AnyRef with Op[JWT] {
    override val FSAlgebraIndex71881: _root_.scala.Int = 0
  }

  case class ValidateOP(jwt: JWT) extends AnyRef with Op[Option[Claims]] {
    override val FSAlgebraIndex71881: _root_.scala.Int = 1
  }

  type OpTypes = _root_.iota.KCons[Op, _root_.iota.KNil]

  trait Handler[MM$71916[_]] extends _root_.freestyle.FSHandler[Op, MM$71916] {
    protected[this] def issue(id: UserId): MM$71916[JWT]

    protected[this] def validate(jwt: JWT): MM$71916[Option[Claims]]

    override def apply[AA$71919](fa$71924: Op[AA$71919]): MM$71916[AA$71919] = ((fa$71924.FSAlgebraIndex71881: @_root_.scala.annotation.switch) match {
      case 0 =>
        val fresh71931: IssueOP = fa$71924.asInstanceOf[IssueOP]
        issue(fresh71931.id)
      case 1 =>
        val fresh71936: ValidateOP = fa$71924.asInstanceOf[ValidateOP]
        validate(fresh71936.jwt)
      case i =>
        throw new _root_.java.lang.Exception(s"freestyle internal error: index " + i + " out of bounds for " + this)
    }).asInstanceOf[MM$71916[AA$71919]]
  }

  class To[LL$71896[_]](implicit ii$71901: _root_.freestyle.InjK[Op, LL$71896]) extends JwtService[LL$71896] {
    private[this] val toInj71902 = _root_.freestyle.FreeS.inject[Op, LL$71896](ii$71901)

    override def issue(id: UserId): FS[JWT] = toInj71902(IssueOP(id))

    override def validate(jwt: JWT): FS[Option[Claims]] = toInj71902(ValidateOP(jwt))
  }

  implicit def to[LL$71896[_]](implicit ii$71901: _root_.freestyle.InjK[Op, LL$71896]): To[LL$71896] = new To[LL$71896]

  def apply[LL$71896[_]](implicit ev$71910: JwtService[LL$71896]): JwtService[LL$71896] = ev$71910
}
