import sbt.Keys._
import sbt._

object Version {
  val scalaVersions = Seq("2.11.11", "2.12.2")

  val scalatestVersion = "3.0.0"
  val akkaVersion = "2.5.3"
}

object Library {
  val scalatest = "org.scalatest" %% "scalatest" % Version.scalatestVersion % Test
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akkaVersion
  val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % Version.akkaVersion
}

object Common {
  val commonSettings =  Seq(
    crossScalaVersions := Version.scalaVersions,
    scalaVersion := Version.scalaVersions.head,
    version := "1.0",

    startYear := Some(2017),
    fork in Test := true,
    logBuffered in Test := false,
    parallelExecution in Test := false,

   scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-unchecked",
      "-deprecation",
      "-Xfatal-warnings",
      "-feature",
      "-language:_"
    ),

    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    unmanagedSourceDirectories.in(Compile) := Seq(scalaSource.in(Compile).value),
    unmanagedSourceDirectories.in(Test) := Seq(scalaSource.in(Test).value),

    shellPrompt in ThisBuild := { state =>
      val project = Project.extract(state).currentRef.project
      s"[$project]> "
    },

    libraryDependencies ++= Seq(
      Library.scalatest,
      Library.akkaStream,
      Library.akkaSlf4j
    )
  )
}