package io.scalac.frees.login.handlers.id

import cats.Id
import io.scalac.frees.login.algebras._
import io.scalac.frees.login.types._

class InMemoryDatabase extends Database.Handler[Id] {

  var lastId = 0L
  var credentialsUsers = Vector.empty[(UserId, Credentials)]
  var gitHubUsers = Vector.empty[(UserId, GitHubData)]

  def insertCredentialsUser(credentials: Credentials): Id[UserInsertionResult] =
    synchronized {
      import credentials._
      if (credentialsUsers.exists(_._2.email == email)) {
        AlreadyExists
      } else {
        val uid: UserId = getNextId()
        credentialsUsers = credentialsUsers :+ (uid, credentials)
        UserInserted(uid)
      }
    }

  private def getNextId(): UserId =
    synchronized {
      lastId += 1
      UserId(lastId)
    }

  private def findUserByEmail(email: UserEmail): Option[(UserId, Credentials)] = {
    credentialsUsers.find(_._2.email == email)
  }

  def getUserByEmail(email: UserEmail): Id[Option[UserId]] =
    findUserByEmail(email).map(_._1)

  def getPassword(email: UserEmail): Id[Option[PasswordHash]] =
    findUserByEmail(email).map(_._2.password)

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
