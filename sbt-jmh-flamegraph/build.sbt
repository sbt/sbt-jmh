import bintray.Keys._
import sbt._
import sbt.Keys._

sbtPlugin := true

publishTo <<= isSnapshot { snapshot =>
  if (snapshot) Some(Classpaths.sbtPluginSnapshots) else Some(Classpaths.sbtPluginReleases)
}

libraryDependencies += ("com.sun" % "tools" % "1.8" % "provided")
  .from("file://" + System.getProperty("java.home").dropRight(3)+"lib/tools.jar")

//// exluding the tools.jar file from the build
//excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
//    cp filter {_.data.getName == "tools.jar"}
//}

// publishing settings

publishMavenStyle := false
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))
bintrayPublishSettings
repository in bintray := "sbt-plugins"
bintrayOrganization in bintray := None

scriptedSettings
scriptedLaunchOpts <+= version(v => s"-Dproject.version=$v")

