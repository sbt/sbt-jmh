import sbt._
import sbt.Keys._

val Jmh = config("jmh") extend Test

scalaVersion := "2.10.6"

val commonSettings = Seq(
  isSnapshot := true, // hack to get around warnings when testing `scripted`
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

// source of Truth for JMH version
val jmhVersion: String = "1.11.3"
version in Jmh := jmhVersion

lazy val jmhDependencies = Seq(
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

lazy val flamegraphLib = project.in(file("sbt-jmh-flamegraph-lib"))
  .settings(commonSettings: _*)
  .settings(
    name := "sbt-jmh-flamegraph-lib",
    version in Jmh := jmhVersion,
    libraryDependencies ++= jmhDependencies
  )
lazy val flamegraph = project.in(file("sbt-jmh-flamegraph"))
  .settings(commonSettings: _*)
  .settings(
    name := "sbt-jmh-flamegraph",
    version in Jmh := jmhVersion,
    libraryDependencies ++= jmhDependencies
  ).dependsOn(plugin, flamegraphLib)

lazy val root = project.in(file("."))
  .aggregate(plugin, flamegraph)
