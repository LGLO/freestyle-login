package io.scalac.frees.login.types

sealed trait LogInRequest

case class UserId(v: Long) extends AnyVal

case class UserEmail(v: String) extends AnyVal

case class User(id: UserId, email: UserEmail)

case class Credentials(email: UserEmail, password: PasswordHash)

sealed trait CredentialsLoginResponse

case object InvalidLogIn extends CredentialsLoginResponse

case class LoggedIn(jwt: JWT) extends CredentialsLoginResponse

sealed trait RegistrationResponse
case object AlreadyRegistered extends RegistrationResponse
case class UserRegistered(id: UserId) extends RegistrationResponse


/**
  * @param v password hash, contains salt if used with sound crypto-algorithm
  */
case class PasswordHash(v: Array[Byte]) extends AnyVal

//Issued Java Web Token
case class JWT(v: String) extends AnyVal
  