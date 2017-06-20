package io.scalac.frees.login.handlers.id

import cats.Id
import io.scalac.frees.login.algebras.Log
import io.scalac.frees.login.handlers.id.Level.Level

object Level extends Enumeration {
  type Level = Value
  val INFO, WARN = Value
}

case class LogEntry(lvl: Level, msg: String, cause: Option[Throwable])

class RecordingLogger extends Log.Handler[Id] {

  private var records = Vector.empty[LogEntry]

  def info(msg: String): Id[Unit] =
    records = records :+ LogEntry(Level.INFO, msg, None)

  def warn(msg: String): Id[Unit] =
    records = records :+ LogEntry(Level.WARN, msg, None)

  def warnWithCause(msg: String, cause: Throwable): Id[Unit] =
    records = records :+ LogEntry(Level.WARN, msg, Some(cause))

  def getRecords = records
}
