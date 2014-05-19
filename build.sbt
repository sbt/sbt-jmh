sbtPlugin := true

organization := "com.typesafe.sbt"

name := "sbt-jmh"

version := "0.1.0-SNAPSHOT"

val jmhVersion = "0.7.1"

libraryDependencies += "org.openjdk.jmh" % "jmh-core"                 % jmhVersion    // GPLv2

libraryDependencies += "org.openjdk.jmh" % "jmh-generator-bytecode"   % jmhVersion    // GPLv2

libraryDependencies += "org.openjdk.jmh" % "jmh-generator-reflection" % jmhVersion    // GPLv2

//publishMavenStyle := false

publishTo <<= isSnapshot { snapshot =>
  if (snapshot) Some(Classpaths.sbtPluginSnapshots) else Some(Classpaths.sbtPluginReleases)
}

//crossBuildingSettings
//
//CrossBuilding.crossSbtVersions := Seq("0.12", "0.13")
//
//CrossBuilding.scriptedSettings
//
//scriptedLaunchOpts := Seq("-Xms512m", "-Xmx512m", "-XX:MaxPermSize=256m")
