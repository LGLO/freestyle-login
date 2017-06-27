package io.scalac.frees.login.algebras

import cats.data.Validated
import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.types.{JWT, UserId}

case class Claims(user: UserId, issuedAt: Long, expires: Long)
/**
  * Algebra for JWT operations like issuing and validating tokens.
  */
@free trait JwtService {
  def issue(id: UserId): FS[JWT]
  def validate(jwt: JWT): FS[Option[Claims]]
}
