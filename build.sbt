import bintray.Keys._
import java.io.FileInputStream
import java.util.Properties

sbtPlugin := true

organization := "pl.project13.scala"
name := "sbt-jmh"

scalaVersion := "2.10.5"
scalacOptions ++= List(
  "-unchecked",
  "-deprecation",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)

val jmhVersion = {
  val props = new Properties()
  val is = new FileInputStream(new File("src/main/resources/sbt-jmh.properties"))
  props.load(is)
  is.close()
  props.get("jmh.version").toString
}

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
