import sbt._
import sbt.Keys._

sbtPlugin := true

name := "sbt-jmh"

publishTo <<= isSnapshot { snapshot =>
  if (snapshot) Some(Classpaths.sbtPluginSnapshots) else Some(Classpaths.sbtPluginReleases)
}

// publishing settings

publishMavenStyle := false
licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

scriptedSettings
scriptedLaunchOpts <+= version(v => s"-Dproject.version=$v")

