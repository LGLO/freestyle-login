package io.scalac.frees.login.algebras

import freestyle.free

@free trait Log {
  def info(msg: String): FS[Unit]

  def warn(msg: String): FS[Unit]

  def warnWithCause(msg: String, cause: Throwable): FS[Unit]
}
