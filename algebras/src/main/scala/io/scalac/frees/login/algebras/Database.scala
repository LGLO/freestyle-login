package io.scalac.frees.login.algebras

import freestyle.free
import io.scalac.frees.login.types.{PasswordHash, User, UserEmail, UserId}

sealed trait InsertUserResult
case class UserInserted(id: UserId) extends InsertUserResult
case class EmailNotUnique(email: UserEmail) extends InsertUserResult

@free trait Database {

  /**
    * @param email - new user email, has to be unique
    * @return Success
    */
  def insertUser(email: UserEmail): FS[InsertUserResult]

  def getUserByEmail(email: UserEmail): FS[Option[User]]

  def savePassword(user: UserId, password: PasswordHash): FS[Unit]

  def getPassword(email: UserEmail): FS[Option[PasswordHash]]

  //def getUserByGitHubId(ghId: GitHubId): FS[User]
}
