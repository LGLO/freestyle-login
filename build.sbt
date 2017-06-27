
val Versions = new Object {
  val doobie = "0.4.1"
  val http4s = "0.17.0-M3"
  val freestyle = "0.3.0"
  val jwt = "0.13.0"
}

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1"

addCompilerPlugin("org.scalameta" %% "paradise" % "3.0.0-M9" cross CrossVersion.full)

scalacOptions := Seq("-deprecation", "-feature", "-language:higherKinds", "-language:implicitConversions")

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
      "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
      "org.http4s" %% "http4s-blaze-client" % Versions.http4s,
      "org.http4s" %% "http4s-circe" % Versions.http4s,
      "org.http4s" %% "http4s-dsl" % Versions.http4s,
      "org.http4s" %% "http4s-twirl" % Versions.http4s,
      "com.github.t3hnar" %% "scala-bcrypt" % "3.0",
      "ch.qos.logback" % "logback-classic" % "1.2.1",
      "io.frees" %% "freestyle" % Versions.freestyle,
      "io.frees" %% "freestyle-http-http4s" % Versions.freestyle,
      "io.frees" %% "freestyle-doobie" % Versions.freestyle,
      "org.tpolecat" %% "doobie-core-cats" % Versions.doobie,
      "org.tpolecat" %% "doobie-h2-cats" % Versions.doobie,
      "com.47deg" %% "github4s" % "0.15.0",
      "com.pauldijou" %% "jwt-core" % Versions.jwt,
      "com.pauldijou" %% "jwt-circe" % Versions.jwt,
      scalaTest % Test,
      "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test
    )
  )
  .enablePlugins(SbtTwirl)
