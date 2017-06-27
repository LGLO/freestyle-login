package io.scalac.frees.login.crypto

import java.security.spec.ECGenParameterSpec
import java.security.{KeyPair, KeyPairGenerator}

/**
  * Util to generate new key pair for JWT generation.
  * Side effect: each restart invalidates tokens.
  */
object EllipticCurveCrypto {
  def genKeyPair: KeyPair = {
    val kpg = KeyPairGenerator.getInstance("EC")
    val ecParamSpec = new ECGenParameterSpec("sect163r2")
    kpg.initialize(ecParamSpec)
    kpg.genKeyPair()
  }
}
