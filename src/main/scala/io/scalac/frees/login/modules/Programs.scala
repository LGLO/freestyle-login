package io.scalac.frees.login.modules

import freestyle._
import freestyle.implicits._
import io.scalac.frees.login.algebras.{AlreadyExists, GitHubDataResponse, _}
import io.scalac.frees.login.types.{RegistrationResponse, _}
import com.github.t3hnar.bcrypt._

/**
  * Gathers GitHub (OAuth 2.0) module with Persistence and Log.
  * Intended to write high level programs over complete login algebra.
  */
@module trait Deps {
  val gitHub: GitHub
  val doobiePersistence: DoobiePersistence
  val log: Log
  val jwt: JwtService
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

  val persistence = new DoobiePersistencePrograms[F]()(D.doobiePersistence)

  /**
    * Register new user with credentials access.
    *
    * @param c - user's email and password hash
    * @return `RegistrationResponse`.
    */
  def registerUser(c: Credentials): FS[RegistrationResponse] = {
    import c.email
    val hash = c.password.bcrypt

    def emailAlreadyTaken: FS[RegistrationResponse] =
      for {
        _ <- log.info(s"Cannot create user, email '$email' already taken")
      } yield AlreadyRegistered: RegistrationResponse

    def userRegistered(id: UserId): FS[RegistrationResponse] =
      for {
        _ <- log.info("User account with credentials access created")
      } yield UserRegistered(id): RegistrationResponse

    for {
      _ <- log.info(s"Trying to register new user with email: '$email'")
      insertResult <- persistence.saveCredentialsUser(email, hash)
      registrationResult <- insertResult match {
        case UserInserted(id) =>
          userRegistered(id)
        case AlreadyExists =>
          emailAlreadyTaken
        case DBFailure(err) =>
          FreeS.pure[F, RegistrationResponse](InternalFailure(err))
      }
    } yield registrationResult
  }

  /**
    * Login user with GitHub access option.
    *
    * @param ghCode - "code" parameter obtained from OAuth 2.0 first step.    
    * @return `GHLoginResponse`
    */
  def gitHubLogin(ghCode: String): FS[GHLoginResponse] = {

    def loginGitHubUser(ghId: GitHubId): FS[GHLoginResponse] =
      for {
        _ <- log.info(s"GitHub login succeeded, id: '$ghId'")
        idOpt <- persistence.getUserByGitHubId(ghId)
        response <- idOpt match {
          case Some(id) =>
            for {
              token <- jwt.issue(id)
            } yield LoggedIn(token): GHLoginResponse
          case None =>
            for {
              _ <- log.info(s"GitHub user id: '$ghId' is not registered.")
            } yield GHUserNotRegistered: GHLoginResponse
        }
      } yield response

    for {
      loginResult <- gitHub.login(ghCode)
      result <- loginResult match {
        case GitHubData(ghId, _) =>
          loginGitHubUser(ghId)
        case GitHubFailure(err) =>
          (for {
            _ <- log.warnWithCause("Login failed due GitHub error", err)
          } yield GHLoginError: GHLoginResponse): FS[GHLoginResponse]
        case GitHubInsufficientPermissions | GitHubNoEmail =>
          (for {
            _ <- log.warn("Login failed due insufficient GitHub permissions")
          } yield GHLoginError: GHLoginResponse): FS[GHLoginResponse]
      }
    } yield result
  }

  /**
    * Logs in user with his credentials.
    *
    * @param credentials - user's email and password hash
    * @return `LoginResponse`.
    */
  def login(credentials: Credentials): FS[LoginResponse] = {
    for {
      userAndPassword <- persistence.getPasswordByEmail(credentials.email)
      response <- userAndPassword match {
        case Some((id, pass)) if credentials.password.isBcrypted(pass) =>
          (for {
            _ <- log.info("User '$id' provided valid email and password")
            token <- jwt.issue(id)
            _ <- log.info(s"Token is $token")
          } yield LoggedIn(token): LoginResponse): FS[LoginResponse]
        case Some((id, pass)) =>
          (for {
            _ <- log.info(s"User '$id' provided invalid password, expected: ${new String(pass)}")
          } yield InvalidCredentials: LoginResponse): FS[LoginResponse]
        case None =>
          (for {
            _ <- log.info(s"Login failed due no access for '${credentials.email}' found")
          } yield InvalidCredentials: LoginResponse): FS[LoginResponse]
      }
    } yield response
  }


  /**
    * Registers new user with GitHub access option.
    *
    * @param ghCode - "code" parameter obtained from OAuth 2.0 first step.
    * @return `RegistrationResponse`
    */
  def registerWithGitHub(ghCode: String): FS[GHRegistrationResponse] = {

    def handleGitHubResponse(resp: GitHubDataResponse): FS[GHRegistrationResponse] = resp match {
      case d: GitHubData =>
        saveUserWithGHAccess(d)
      case err: GitHubErrorResponse =>
        //Types annotations required by compiler,
        //only way to inline w/o another helper methods (valid 0.3.0)
        for {
          _ <- log.info(s"Could not get all required data from GitHub, reason: '$err'")
        } yield GHError(err): GHRegistrationResponse
    }

    def saveUserWithGHAccess(ghData: GitHubData): FS[GHRegistrationResponse] =
      for {
        insertResult <- persistence.saveGitHubUser(ghData)
        registrationResult <- insertResult match {
          case UserInserted(uid) =>
            //No logging to show use of `pure`
            FreeS.pure[F, GHRegistrationResponse](UserRegistered(uid))
          case AlreadyExists =>
            (for {
              _ <- log.info("Cannot create user, GitHub Id is already linked to existing account.")
            } yield GHAlreadyRegistered: GHRegistrationResponse): FS[GHRegistrationResponse]
          case DBFailure(err) =>
            FreeS.pure[F, GHRegistrationResponse](InternalFailure(err))
        }
      } yield registrationResult

    for {
      _ <- log.info(s"Trying to register new user with GitHub code: '$ghCode'")
      ghResp <- gitHub.login(ghCode)
      result <- handleGitHubResponse(ghResp)
    } yield result
  }
}