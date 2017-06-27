package io.scalac.frees.login.handlers.task

import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

import fs2.{Strategy, Task}
import io.scalac.frees.login.algebras.{Claims, JwtService}
import io.scalac.frees.login.handlers.task.jwt.JwtHandler
import org.scalatest.{Inside, MustMatchers, WordSpec}
import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.types.{JWT, UserId}
import cats.instances._
import fs2.interop.cats._
import io.scalac.frees.login.crypto.EllipticCurveCrypto
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}

class JwtHandlerSpecs extends WordSpec with MustMatchers with Inside {

  implicit val ec = scala.concurrent.ExecutionContext.global
  implicit val fs2Strategy = Strategy.fromExecutionContext(ec)

  val userId1 = 10101
  val userId2 = 20202
  
  val kp = EllipticCurveCrypto.genKeyPair

  implicit val handler: JwtService.Handler[Task] = JwtHandler(kp.getPublic, kp.getPrivate)
  val service = implicitly[JwtService[JwtService.Op]]

  "JWTHandler" should {
    "validate just issued token" in {
      val validated =
        (for {
          token <- service.issue(userId1)
          validated <- service.validate(token)
        } yield validated).interpret[Task].unsafeRun()
      inside(validated) {
        case Some(Claims(id, _, _)) =>
          id mustEqual userId1
      }
    }

    "not validate token encrypted with not matching key" in {
      val token = {
        val kp = EllipticCurveCrypto.genKeyPair
        implicit val handler: JwtService.Handler[Task] = JwtHandler(kp.getPublic, kp.getPrivate)
        val service = implicitly[JwtService[JwtService.Op]]
        service.issue(userId1).interpret[Task].unsafeRun()
      }
      val validated = service.validate(token).interpret[Task].unsafeRun()
      validated mustEqual None
    }

    "not validate expired token" in {
      val token = {
        val currentTime = System.currentTimeMillis() / 1000
        val claim = JwtClaim()
          .about(userId2.toString)
          .issuedAt(currentTime - 1000)
          .expiresAt(currentTime - 500)
        Jwt.encode(claim, kp.getPrivate, JwtAlgorithm.ES512)
      }
      val validated = service.validate(token).interpret[Task].unsafeRun()
      validated mustEqual None
    }

    "not validate token encrypted other than ES512" in {
      val token = Jwt.encode(validClaims, kp.getPrivate, JwtAlgorithm.ES384)
      val validated = service.validate(token).interpret[Task].unsafeRun()
      validated mustEqual None
    }

    "not validate unencrypted token" in {
      val token = Jwt.encode(validClaims)
      val validated = service.validate(token).interpret[Task].unsafeRun()
      validated mustEqual None
    }
  }

  private def validClaims: JwtClaim = {
    val currentTime = System.currentTimeMillis() / 1000
    val claim = JwtClaim()
      .about(userId2.toString)
      .issuedAt(currentTime - 1000)
      .expiresAt(currentTime + 500)
    claim
  }
}
