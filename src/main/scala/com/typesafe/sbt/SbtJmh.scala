package com.typesafe.sbt

import sbt._
import Keys._
import org.openjdk.jmh.annotations.GenerateMicroBenchmark
import org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator
import sbt.inc.Analysis

object SbtJmh extends Plugin {

  import JmhKeys._

  lazy val jmhSettings = Seq(
    sourceGenerators in Compile <+= generateJavaSources in Compile,

    mainClass in (Compile, run) := Some("org.openjdk.jmh.Main"),

    fork in (Compile, run) := true, // makes sure that sbt manages classpath for JMH when forking

    generateJavaSources in Compile := {
      val out = (target in Compile).value / s"scala-${scalaBinaryVersion.value}"
      println("out = " + out)

      val compiledBytecodeDirectory = out / "classes"
      val outputSourceDirectory = out / "generated-sources" / "jmh"
      val outputResourceDirectory = compiledBytecodeDirectory

      val micro = classOf[GenerateMicroBenchmark]
      Thread.currentThread().setContextClassLoader(micro.getClassLoader)

      JmhBytecodeGenerator.main(Array(compiledBytecodeDirectory, outputSourceDirectory, outputResourceDirectory).map(_.toString))

      (outputSourceDirectory ** "*").filter(_.isFile).get
    },
  
    libraryDependencies ++= Seq(
      "org.openjdk.jmh" % "jmh-core"                 % "0.7.1",   // GPLv2
      "org.openjdk.jmh" % "jmh-generator-bytecode"   % "0.7.1",   // GPLv2
      "org.openjdk.jmh" % "jmh-generator-reflection" % "0.7.1"    // GPLv2
    )

  )

  object JmhKeys {
    val Jmh = config("jmh") extend Compile
    
    val generateJavaSources = taskKey[Seq[File]]("Generate benchmark JMH Java code")

    val generateInstrumentedClasses = taskKey[Seq[File]]("Generate instrumented JMH code")

  }

}
