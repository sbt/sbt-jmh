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

  crossSbtVersions := Vector("1.3.0"),

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

// sbt-scripted settings
val myScriptedSettings = Seq(
  scriptedLaunchOpts += s"-Dproject.version=${version.value}"
) 

// ---------------------------------------------------------------------------------------------------------------------

lazy val root =
  project
    .in(file("."))
    .settings(commonSettings: _*)
    .settings(publish / skip := true)
    .aggregate(plugin)

lazy val plugin = project
  .in(file("plugin"))
  .settings(commonSettings: _*)
  .enablePlugins(SbtPlugin)
  .settings(myScriptedSettings: _*)
  .settings(
    name := "sbt-jmh",
    sbtPlugin := true,
    publishMavenStyle := false,
    startYear := Some(2014),
    licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
    bintrayRepository := "sbt-plugins",
    bintrayOrganization in bintray := None,
    bintrayPackageLabels := Seq("sbt-plugin", "jmh", "benchmarking")
  ).enablePlugins(AutomateHeaderPlugin)
