package io.scalac.frees.login.handlers.id

import cats.Id
import io.scalac.frees.login.algebras.Log

class PrintlnLogger extends Log.Handler[Id] {

  def info(msg: String): Id[Unit] =
    println(s"[INFO]: $msg")

  def warn(msg: String): Id[Unit] =
    println(s"[WARN]: $msg")

  def warnWithCause(msg: String, cause: Throwable): Id[Unit] = {
    println(s"[WARN]: $msg, caused by:\n")
    cause.printStackTrace(System.out)
  }
}