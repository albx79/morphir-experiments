import Dependencies._

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.credimi.morphir"
ThisBuild / organizationName := "experiments"
Compile / unmanagedSourceDirectories += baseDirectory.value / "dist"

val zioVersion = "2.0.5"

lazy val root = (project in file("."))
  .settings(
    name := "Morphir Experiments",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-cli" % "0.3.0-M02",
      "io.d11"  %% "zhttp" % "2.0.0-RC11",
      "dev.zio" %% "zio-json" % "0.4.2",
      "org.morphir" %% "morphir-sdk-core" % "0.10.0",
      scalaTest % Test
    )
  )

