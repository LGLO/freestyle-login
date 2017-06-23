package io.scalac.frees.login.handlers.fs2task.dummies

import fs2.Task
import io.scalac.frees.login.algebras.Log

class PrintlnLogger extends Log.Handler[Task] {

  val underlying = new io.scalac.frees.login.handlers.id.PrintlnLogger

  override protected[this] def info(msg: String): Task[Unit] =
    Task.now(underlying.info(msg))

  override protected[this] def warn(msg: String): Task[Unit] =
    Task.now(underlying.warn(msg))

  override protected[this] def warnWithCause(msg: String, cause: Throwable): Task[Unit] =
    Task.now(underlying.warnWithCause(msg, cause))
}
