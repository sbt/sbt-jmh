import sbt.Keys._
import sbt._

scalaVersion := "2.10.5"

val commonSettings = Seq(
  scalaVersion := "2.10.5",
  organization := "pl.project13.scala",
  scalacOptions ++= List(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.6",
    "-encoding", "UTF-8"
  )
)

val jmhVersion = "1.11"

val jmhDependencies = Seq(
  "org.openjdk.jmh" % "jmh-core"                 % jmhVersion, // GPLv2
  "org.openjdk.jmh" % "jmh-generator-bytecode"   % jmhVersion, // GPLv2
  "org.openjdk.jmh" % "jmh-generator-reflection" % jmhVersion, // GPLv2
  "org.openjdk.jmh" % "jmh-generator-asm"        % jmhVersion // GPLv2
)

lazy val plugin = project.in(file("sbt-jmh-plugin"))
  .settings(commonSettings: _*)
  .settings(
    name := "sbt-jmh",
    libraryDependencies ++= jmhDependencies
  )

lazy val flamegraph = project.in(file("sbt-jmh-flamegraph"))
  .settings(commonSettings: _*)
  .settings(
    name := "sbt-jmh-flamegraph",
    libraryDependencies ++= jmhDependencies,
    libraryDependencies += ("com.sun" % "tools" % "1.8" % "provided")
      .from("file://" + System.getProperty("java.home").dropRight(3)+"lib/tools.jar")
  )

lazy val root = project.in(file("."))
  .settings(commonSettings: _*)
  .aggregate(plugin, flamegraph)
