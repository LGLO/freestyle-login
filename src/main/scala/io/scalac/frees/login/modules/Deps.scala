package io.scalac.frees.login.modules

import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.GitHubData
import io.scalac.frees.login.algebras.{AlreadyExists, _}
import io.scalac.frees.login.types.{RegistrationResponse, _}

@module trait Deps {
  val github: Github
  val persistence: Persistence
  val log: Log
}

/**
  * Defines programs over `Deps`.
  * Because of some bug in scalameta, which I don't understand,
  * Constructs like this:
  * for {
  * a <- A.funB
  * x <- if (a > 0)
  * for {
  * v <- B.funB(x)
  * z <- C.funC(v) // <- second generator makes it fail
  * } yield z
  * else
  * for {
  * z <- D.funD(x)
  * } yield z
  * } yield x
  *
  * where there are two generators in nested for-comprehension fail due to:
  * "<macro>:10: value map is not a member of Product with Serializable"
  *
  * This class enables us to capture `F` type and use call other methods from within methods.
  *
  * @param D
  * @tparam F
  */
class Programs[F[_]]()(implicit D: Deps[F]) {

  import D._

  type FS[A] = FreeS[F, A]

  def registerUser(email: UserEmail, password: PasswordHash): FS[RegistrationResponse] = {

    def emailAlreadyTaken: FS[RegistrationResponse] =
      for {
        _ <- log.info("Cannot create user, email '$email' already taken")
      } yield AlreadyRegistered: RegistrationResponse

    def userRegistered(id: UserId): FS[RegistrationResponse] =
      for {
        _ <- log.info("User account with credentials access created")
      } yield UserRegistered(id): RegistrationResponse

    for {
      _ <- log.info(s"Trying to register new user with email: '$email'")
      insertResult <- persistence.insertUser(Credentials(email, password))
      registrationResult <- insertResult match {
        case UserInserted(id) =>
          userRegistered(id)
        case AlreadyExists =>
          emailAlreadyTaken
      }
    } yield registrationResult
  }

  def registerWithGitHub(ghData: GitHubData): FS[RegistrationResponse] = {
    import ghData._

    def ghAccountAlreadyLinked: FS[RegistrationResponse] =
      for {
        _ <- log.info("Cannot create user, GitHub Id is already linked to existing account.")
      } yield AlreadyRegistered: RegistrationResponse

    for {
      _ <- log.info(s"Trying to register new user with email: '$email'")
      insertResult <- persistence.insertGitHubUser(ghData)
      registrationResult <- insertResult match {
        case UserInserted(uid) =>
          //No logging, to show use of `pure`
          FreeS.pure[F, RegistrationResponse](UserRegistered(uid))
        case AlreadyExists =>
          ghAccountAlreadyLinked
      }
    } yield registrationResult
  }
}