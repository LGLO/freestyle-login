package io.scalac.frees.login.handlers.task.jwt

import java.security.{PrivateKey, PublicKey}
import fs2.{Strategy, Task}
import io.scalac.frees.login.algebras.{Claims, JwtService}
import io.scalac.frees.login.handlers.id.IdJwtHandler
import io.scalac.frees.login.types.{JWT, UserId}

class JwtHandler(
  underlying: IdJwtHandler
)(
  implicit strategy: Strategy
) extends JwtService.Handler[Task] {
  override protected[this] def issue(id: UserId): Task[JWT] =
    Task(underlying.issue(id))

  override protected[this] def validate(jwt: JWT): Task[Option[Claims]] =
    Task(underlying.validate(jwt))

}

object JwtHandler {
  def apply(
    publicKey: PublicKey,
    privateKey: PrivateKey
  )(implicit strategy: Strategy): JwtHandler =
    new JwtHandler(new IdJwtHandler(publicKey, privateKey))
}