package io.scalac.frees.login.types

sealed trait LogInRequest

case class UserId(v: Long) extends AnyVal

case class UserEmail(v: String) extends AnyVal

case class User(id: UserId, email: UserEmail)

case class CredentialsLoginRequest(userEmail: UserEmail, hash: PasswordHash)

sealed trait CredentialsLoginResponse

case object InvalidLogIn extends CredentialsLoginResponse

case class LoggedIn(jwt: JWT) extends CredentialsLoginResponse

sealed trait RegisterByEmailResponse
case object EmailAlreadyTaken extends RegisterByEmailResponse
case class UserRegistered(id: UserId) extends RegisterByEmailResponse


/**
  * @param v password hash, contains salt if used with sound crypto-algorithm
  */
case class PasswordHash(v: Array[Byte]) extends AnyVal

//Issued Java Web Token
case class JWT(v: String) extends AnyVal
  