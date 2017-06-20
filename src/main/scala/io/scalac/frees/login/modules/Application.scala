package io.scalac.frees.login.modules

import freestyle.module
import io.scalac.frees.login.algebras.{Github, Log}

@module trait Application {
  val github: Github
  val persistence: Persistence
  val log: Log
}
