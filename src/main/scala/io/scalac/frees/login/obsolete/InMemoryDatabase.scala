package io.scalac.frees.login.obsolete

import cats.Id
import io.scalac.frees.login.algebras._
import io.scalac.frees.login.obsolete.UserInserted
import io.scalac.frees.login.types._

class InMemoryDatabase extends Database.Handler[Id] {

  var lastId = 0L
  var credentialsUsers = Vector.empty[(UserId, (UserEmail, PasswordHash))]
  var gitHubUsers = Vector.empty[(UserId, GitHubData)]

  def insertCredentialsUser(email: UserEmail, hash: PasswordHash): Id[UserInsertionResult] =
    synchronized {
      if (findUserByEmail(email).isDefined) {
        AlreadyExists
      } else {
        val uid: UserId = getNextId()
        credentialsUsers = credentialsUsers :+ (uid, (email, hash))
        UserInserted(uid)
      }
    }

  private def getNextId(): UserId =
    synchronized {
      lastId += 1
      lastId
    }

  private def findUserByEmail(email: UserEmail): Option[(UserId, (UserEmail, PasswordHash))] = {
    credentialsUsers.find(_._2._1 == email)
  }

  def getUserByEmail(email: UserEmail): Id[Option[UserId]] =
    findUserByEmail(email).map(_._1)

  def getPassword(email: UserEmail): Id[Option[PasswordHash]] =
    findUserByEmail(email).map(_._2._2)

  def insertGitHubUser(ghData: GitHubData): Id[UserInsertionResult] =
    synchronized {
      import ghData._
      if (gitHubUsers.exists(_._2.id == id)) {
        AlreadyExists
      } else {
        val uid: UserId = getNextId()
        gitHubUsers = gitHubUsers :+ (uid, ghData)
        UserInserted(uid)
      }
    }

  override def getUserByGitHubId(ghId: GitHubId): Id[Option[UserId]] =
    gitHubUsers.find(_._2.id == ghId).map(_._1)

}
