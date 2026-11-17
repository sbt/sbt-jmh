import java.io.FileInputStream
import java.util.Properties

val jmhVersion = {
  val props = new Properties()
  val is = new FileInputStream(new File("sbt-jmh.properties"))
  props.load(is)
  is.close()
  props.get("jmh.version").toString
}

ThisBuild / scalaVersion := "2.12.21"

val commonSettings = Seq(
  organization := "pl.project13.scala",
  startYear := Some(2014),
  licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  homepage := Some(url("https://github.com/sbt/sbt-jmh")),
  scmInfo := Some(ScmInfo(url("https://github.com/sbt/sbt-jmh"), "git@github.com:sbt/sbt-jmh.git")),
  developers := List(
    Developer("ktoso", "Konrad 'ktoso' Malawski", "<ktoso@project13.pl>", url("https://github.com/ktoso")),
    Developer("retronym", "Jason Zaugg", "<jzaugg@gmail.com>", url("https://github.com/retronym")),
    Developer("johanandren", "Johan Andrén", "<johan@markatta.com>", url("https://github.com/johanandren"))
  ),

  crossScalaVersions := Seq("2.12.21", "3.9.0"),

  pluginCrossBuild / sbtVersion := {
    scalaBinaryVersion.value match {
      case "2.12" =>
        "1.13.1"
      case _ =>
        "2.1.0"
    }
  },

  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "2.12" =>
        Seq("-Xsource:3", "-release:8")
      case "3" =>
        Nil
    }
  },

  scalacOptions ++= List(
    "-unchecked",
    "-deprecation",
    "-encoding", "UTF-8"
  ),

  libraryDependencies += "org.openjdk.jmh" % "jmh-core"                 % jmhVersion, // GPLv2 with classpath exception
  libraryDependencies += "org.openjdk.jmh" % "jmh-generator-bytecode"   % jmhVersion, // GPLv2 with classpath exception
  libraryDependencies += "org.openjdk.jmh" % "jmh-generator-reflection" % jmhVersion, // GPLv2 with classpath exception
  libraryDependencies += "org.openjdk.jmh" % "jmh-generator-asm"        % jmhVersion  // GPLv2 with classpath exception
)

// sbt-scripted settings
val myScriptedSettings = Seq(
  scriptedLaunchOpts += s"-Dproject.version=${version.value}"
) 

// ---------------------------------------------------------------------------------------------------------------------

lazy val root =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(publish / skip := true)
    .aggregate(plugin)

lazy val plugin = project
  .in(file("plugin"))
  .settings(commonSettings)
  .enablePlugins(SbtPlugin)
  .settings(myScriptedSettings)
  .settings(
    name := "sbt-jmh",
    sbtPlugin := true,
  ).enablePlugins(AutomateHeaderPlugin)
