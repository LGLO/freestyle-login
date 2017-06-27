package io.scalac.frees.login.handlers.id

import java.security.{PrivateKey, PublicKey}
import java.util.concurrent.TimeUnit

import cats.Id
import io.scalac.frees.login.algebras.{Claims, JwtService}
import io.scalac.frees.login.types.{JWT, UserId}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtCirce, JwtClaim}

import scala.concurrent.duration.FiniteDuration
import scala.util.Try

class IdJwtHandler(
  pubKey: PublicKey,
  privKey: PrivateKey
) extends JwtService.Handler[Id] {
  val twoDays = FiniteDuration(2, TimeUnit.DAYS).toSeconds
  val algo = JwtAlgorithm.ES512

  override def issue(id: UserId): Id[JWT] = {

    val claim = JwtClaim()
      .about(id.toString)
      .issuedNow
      .expiresIn(twoDays)

    Jwt.encode(claim, privKey, algo)
  }

  override def validate(jwt: JWT): Id[Option[Claims]] = {
    JwtCirce.decode(jwt, pubKey, Seq(algo)).toOption.flatMap { c =>
      for {
        userId <- c.subject.flatMap(s => Try(s.toLong).toOption)
        expiration <- c.expiration.filter(_ > currentTimeSeconds)
        issuedAt <- c.issuedAt.filter(_ <= System.currentTimeMillis())
      } yield Claims(userId, issuedAt, expiration)
    }
  }

  private def currentTimeSeconds: Long = System.currentTimeMillis() / 1000

}
