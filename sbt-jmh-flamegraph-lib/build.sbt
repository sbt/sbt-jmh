import sbt._
import sbt.Keys._

version := "0.3.0"

libraryDependencies += ("com.sun" % "tools" % "1.8" % "provided")
  .from("file://" + System.getProperty("java.home").dropRight(3)+"lib/tools.jar")

// publishing settings

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

packageOptions in (Compile, packageBin) +=
  Package.ManifestAttributes("Premain-Class" -> "pl.project13.jmh.agent.AttachAgent" )