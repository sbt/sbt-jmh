import bintray.Keys._
import java.io.FileInputStream
import java.util.Properties

val jmhVersion = {
  val props = new Properties()
  val is = new FileInputStream(new File("sbt-jmh.properties"))
  props.load(is)
  is.close()
  props.get("jmh.version").toString
}

val commonSettings = Seq(
  organization := "pl.project13.scala",

  scalaVersion := "2.10.6",
  scalacOptions ++= List(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-target:jvm-1.6",
    "-encoding", "UTF-8"
  ),

  libraryDependencies += "org.openjdk.jmh" % "jmh-core"                 % jmhVersion, // GPLv2
  libraryDependencies += "org.openjdk.jmh" % "jmh-generator-bytecode"   % jmhVersion, // GPLv2
  libraryDependencies += "org.openjdk.jmh" % "jmh-generator-reflection" % jmhVersion, // GPLv2
  libraryDependencies += "org.openjdk.jmh" % "jmh-generator-asm"        % jmhVersion  // GPLv2
)

val sonatypeSettings: Seq[Setting[_]] = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo <<= version { (v: String) =>
    val nexus = "https://oss.sonatype.org/"
    if (v.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  credentials += Credentials(Path.userHome / ".sbt" / "sonatype.properties"),
  pomExtra := 
    <url>https://github.com/ktoso/sbt-jmh</url>
    <licenses>
      <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:ktoso/sbt-jmh.git</url>
      <connection>scm:git:git@github.com:ktoso/sbt-jmh.git</connection>
    </scm>
    <developers>
      <developer>
        <id>ktoso</id>
        <name>Konrad 'ktoso' Malawski</name>
        <url>http://kto.so</url>
      </developer>
    </developers>
    <parent>
      <groupId>org.sonatype.oss</groupId>
      <artifactId>oss-parent</artifactId>
      <version>7</version>
    </parent>
  )

// publishing settings
val myScriptedSettings = scriptedSettings ++ Seq(
  scriptedLaunchOpts <+= version(v => s"-Dproject.version=$v")
) 

val _crossVersions = Seq(
  "2.10.6", 
  "2.11.11",
  "2.12.2"
)

// ---------------------------------------------------------------------------------------------------------------------

lazy val root =
  project
    .in(file("."))
    .settings(commonSettings: _*)
    .settings(crossScalaVersions := _crossVersions)
    .aggregate(plugin, extras)

lazy val plugin = project
  .in(file("plugin"))
  .settings(commonSettings: _*)
  .settings(myScriptedSettings: _*)
  .settings(
    name := "sbt-jmh",
    
    // plugin publishing settings
    sbtPlugin := true,
    publishTo <<= isSnapshot { snapshot =>
      if (snapshot) Some(Classpaths.sbtPluginSnapshots) else Some(Classpaths.sbtPluginReleases)
    },
    publishMavenStyle := false,
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    bintrayPublishSettings,
    repository in bintray := "sbt-plugins",
    bintrayOrganization in bintray := None
  ).dependsOn(extras)


lazy val extras = project
  .in(file("extras"))
  .settings(commonSettings: _*)
  .settings(sonatypeSettings: _*)
  .settings(
    name := "sbt-jmh-extras",
    autoScalaLibrary := false, // it is plain Java
    crossPaths := false // it is plain Java
    // publishing settings
  )
