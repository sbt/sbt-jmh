import bintray.Keys._

sbtPlugin := true

organization := "pl.project13.scala"
name := "sbt-jmh"

scalaVersion := "2.10.5"
scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)

val jmhVersion = "1.9.1"

libraryDependencies += "org.openjdk.jmh" % "jmh-core"                 % jmhVersion    // GPLv2
libraryDependencies += "org.openjdk.jmh" % "jmh-generator-bytecode"   % jmhVersion    // GPLv2
libraryDependencies += "org.openjdk.jmh" % "jmh-generator-reflection" % jmhVersion    // GPLv2
libraryDependencies += "org.openjdk.jmh" % "jmh-generator-asm"        % jmhVersion    // GPLv2

publishTo <<= isSnapshot { snapshot =>
  if (snapshot) Some(Classpaths.sbtPluginSnapshots) else Some(Classpaths.sbtPluginReleases)
}

// publishing settings

publishMavenStyle := false
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
bintrayPublishSettings
repository in bintray := "sbt-plugins"
bintrayOrganization in bintray := None

scriptedSettings
scriptedLaunchOpts <+= version(v => s"-Dproject.version=$v")
