import java.io.FileInputStream
import java.util.Properties
import _root_.bintray.BintrayPlugin.bintrayPublishSettings

val jmhVersion = {
  val props = new Properties()
  val is = new FileInputStream(new File("sbt-jmh.properties"))
  props.load(is)
  is.close()
  props.get("jmh.version").toString
}

val commonSettings = Seq(
  organization := "pl.project13.scala",

  crossSbtVersions := Vector("1.2.1", "0.13.17"),

  scalacOptions ++= List(
    "-unchecked",
    "-deprecation",
    "-language:_",
    "-encoding", "UTF-8"
  ),

  publishConfiguration := {
    val javaVersion = System.getProperty("java.specification.version")
    if (javaVersion != "1.8")
      throw new RuntimeException("Cancelling publish, please use JDK 1.8")
    publishConfiguration.value
  },

  libraryDependencies += "org.openjdk.jmh" % "jmh-core"                 % jmhVersion, // GPLv2 with classpath exception
  libraryDependencies += "org.openjdk.jmh" % "jmh-generator-bytecode"   % jmhVersion, // GPLv2 with classpath exception
  libraryDependencies += "org.openjdk.jmh" % "jmh-generator-reflection" % jmhVersion, // GPLv2 with classpath exception
  libraryDependencies += "org.openjdk.jmh" % "jmh-generator-asm"        % jmhVersion  // GPLv2 with classpath exception
)

val sonatypeSettings: Seq[Setting[_]] = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
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

// ---------------------------------------------------------------------------------------------------------------------

lazy val root =
  project
    .in(file("."))
    .settings(commonSettings: _*)
    .aggregate(plugin, extras)

lazy val plugin = project
  .in(file("plugin"))
  .settings(commonSettings: _*)
  .enablePlugins(ScriptedPlugin)
  .settings(scriptedLaunchOpts += s"-Dproject.version=${version.value}")
  .settings(
    name := "sbt-jmh",
    
    sbtPlugin := true,
    publishTo := {
      if (isSnapshot.value)
        Some(Resolver.sbtPluginRepo("snapshots"))
      else
        Some(Resolver.sbtPluginRepo("releases"))
    },
    publishMavenStyle := false,
    startYear := Some(2014),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    bintrayPublishSettings,
    bintray / bintrayRepository := "sbt-plugins",
    bintray / bintrayOrganization := None
  ).dependsOn(extras)
  .enablePlugins(AutomateHeaderPlugin)


lazy val extras = project
  .in(file("extras"))
  .settings(commonSettings: _*)
  .settings(sonatypeSettings: _*)
  .settings(
    name := "sbt-jmh-extras",
    scalaVersion := "2.12.6",
    autoScalaLibrary := false, // it is plain Java
    crossPaths := false // it is plain Java
  )
