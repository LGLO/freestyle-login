addCompilerPlugin("org.scalameta" %% "paradise" % "3.0.0-M9" cross CrossVersion.full)

lazy val domain = project in file("../domain")

lazy val algebras = (project in file(".")).
  settings(
    inThisBuild(
      List(
        organization := "io.scalac",
        scalaVersion := "2.12.2",
        version := "0.1.0-SNAPSHOT"
      )
    ),
    name := "freestyle-login-algebras",
    libraryDependencies ++= Seq(
      "io.frees" %% "freestyle" % "0.3.0"
    )
  ).dependsOn(domain)