val http4sVersion = "0.17.0-M3"

lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"

addCompilerPlugin("org.scalameta" %% "paradise" % "3.0.0-M9" cross CrossVersion.full)

scalacOptions := Seq("-deprecation", "-feature", "-language:higherKinds")

lazy val root = (project in file(".")).
  settings(
    inThisBuild(
      List(
        organization := "io.scalac",
        scalaVersion := "2.12.2",
        version := "0.1.0-SNAPSHOT"
      )
    ),
    name := "freestyle-login",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-twirl" % http4sVersion,
      "com.github.t3hnar" %% "scala-bcrypt" % "3.0",
      "ch.qos.logback" % "logback-classic" % "1.2.1",
      "io.frees" %% "freestyle" % "0.3.0",
      "com.47deg" %% "github4s" % "0.15.0",
      scalaTest % Test,
      "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test
    )
  )
  .enablePlugins(SbtTwirl)
