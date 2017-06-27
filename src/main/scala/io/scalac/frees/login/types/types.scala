package io.scalac.frees.login.types

import io.scalac.frees.login.algebras.GitHubErrorResponse

sealed trait LogInRequest

case class User(id: UserId, email: UserEmail)

case class Credentials(email: UserEmail, password: String)

//Hierarchies (with common classes) for credentials and OAuth sign-ins
sealed trait LoginResponse

sealed trait GHLoginResponse

case class LoggedIn(jwt: JWT) extends LoginResponse with GHLoginResponse

case object InvalidCredentials extends LoginResponse

case object GHUserNotRegistered extends GHLoginResponse

case object GHLoginError extends GHLoginResponse

//Hierarchies (with common classes) for credentials and OAuth registrations
sealed trait RegistrationResponse

sealed trait GHRegistrationResponse

case object AlreadyRegistered extends RegistrationResponse

case class UserRegistered(id: UserId) extends RegistrationResponse with GHRegistrationResponse

case class InternalFailure(err: Throwable) extends RegistrationResponse with GHRegistrationResponse

case object GHAlreadyRegistered extends GHRegistrationResponse

case class GHError(err: GitHubErrorResponse) extends GHRegistrationResponse
  