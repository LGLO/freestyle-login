package io.scalac.frees.login.handlers.id

import cats.Id
import io.scalac.frees.login.algebras.{Database, EmailNotUnique, InsertUserResult, UserInserted}
import io.scalac.frees.login.types.{PasswordHash, User, UserEmail, UserId}

class InMemoryDatabase extends Database.Handler[Id] {

  var nextId = 1L
  var byId = Map.empty[UserId, User]
  var byEmail = Map.empty[UserEmail, User]
  var passwords = Map.empty[UserId, PasswordHash]

  def insertUser(v: UserEmail): Id[InsertUserResult] =
    synchronized {
      if (byEmail.contains(v)) {
        EmailNotUnique(v)
      } else {
        val id = UserId(nextId)
        nextId += 1
        val u = User(id, v)
        byId += (id -> u)
        byEmail += (v -> u)
        UserInserted(id)
      }
    }

  def getUserByEmail(email: UserEmail): Id[Option[User]] =
    byEmail.get(email)

  def savePassword(user: UserId, password: PasswordHash): Id[Unit] =
    passwords += user -> password

  def getPassword(email: UserEmail): Id[Option[PasswordHash]] =
    byEmail.get(email).flatMap(u => passwords.get(u.id))

}
