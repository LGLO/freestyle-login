package io.scalac.frees.login.algebras

import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.types.{JWT, UserId}

case class Claims(user: UserId, issuedAt: Long, expires: Long)

@free
trait JwtService {
  def issue(id: UserId): FS[JWT]
  def validate(jwt: JWT): FS[Option[Claims]]
}
