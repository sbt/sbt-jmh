package com.typesafe.sbt

import sbt._
import sbt.Keys._
import org.openjdk.jmh.annotations.GenerateMicroBenchmark
import org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator
import sbt.inc.Analysis
import sbt.CommandStrings._
import java.io.PrintWriter

object SbtJmh extends Plugin {

  import JmhKeys._

  lazy val jmhSettings = Seq(
    sourceGenerators in Compile <+= generateJavaSources in Compile,

    sourceDirectories in Compile += sourceDirectory.value / "jmh",

    mainClass in (Compile, run) := Some("org.openjdk.jmh.Main"),

    fork in (Compile, run) := true, // makes sure that sbt manages classpath for JMH when forking

    sourceDirectories in Compile += target.value / s"scala-${scalaBinaryVersion.value}" / "generated-sources" / "jmh",

    generateJavaSources in Jmh := generateBenchmarkJavaSources(streams.value, (target in Compile).value, scalaBinaryVersion.value),
    generateJavaSources in Compile := generateBenchmarkJavaSources(streams.value, (target in Compile).value, scalaBinaryVersion.value),

    compileAgain in Jmh := {
      myCompileTaskImpl(streams.value, (compileInputs in (Compile, compile)).value)
    },

    compile in Jmh := {
      streams.value.log.info("Compiling generated JMH benchmarks...")
      val generatedJava = (generateJavaSources in Jmh).value
      myCompileGeneratedJava(streams.value, (compileInputs in (Compile, compile)).value, generatedJava)
    },

    version in Jmh := "0.7.1",

    libraryDependencies ++= Seq(
      "org.openjdk.jmh" % "jmh-core"                 % (version in Jmh).value,   // GPLv2
      "org.openjdk.jmh" % "jmh-generator-bytecode"   % (version in Jmh).value,   // GPLv2
      "org.openjdk.jmh" % "jmh-generator-reflection" % (version in Jmh).value    // GPLv2
    ),

    compile in Jmh <<= (compile in Jmh).dependsOn(generateJavaSources in Jmh, compile in Compile),
    compile in Jmh <<= (compile in Jmh).dependsOn(compileAgain in Jmh),
    compile in Jmh <<= (compile in Jmh).dependsOn(compile in Compile),

    run in Jmh <<= (run in Compile).dependsOn(compile in Jmh),
    run in Compile <<= (run in Compile).dependsOn(compile in Jmh)
  )



  def generateBenchmarkJavaSources(s: TaskStreams, target: File, scalaBinaryV: String): Seq[File] = {
    s.log.info("Generating JMH benchmark Java source files...")

    val out = target / s"scala-$scalaBinaryV"

    val compiledBytecodeDirectory = out / "classes"
    val outputSourceDirectory = out / "generated-sources" / "jmh"
    val outputResourceDirectory = compiledBytecodeDirectory

    // assuring the classes are loaded in case of same thread execution
    val micro = classOf[GenerateMicroBenchmark]
    Thread.currentThread().setContextClassLoader(micro.getClassLoader)

    JmhBytecodeGenerator.main(Array(compiledBytecodeDirectory, outputSourceDirectory, outputResourceDirectory).map(_.toString))
    (outputSourceDirectory ** "*").filter(_.isFile).get
  }

  /** Compiler run, with additional files to compile (JMH generated sources) */
  def myCompileGeneratedJava(s: TaskStreams, ci: Compiler.Inputs, javaToCompile: Seq[File]): inc.Analysis = {
    lazy val x = s.text(ExportStream)
    def onArgs(cs: Compiler.Compilers) = {
      cs.copy(
        scalac = cs.scalac.onArgs(exported(x, "scalac")),
        javac = cs.javac.onArgs(exported(x, "javac")))
    }
    val i = ci
      .copy(compilers = onArgs(ci.compilers))
      .copy(config = ci.config.copy(sources = ci.config.sources ++ javaToCompile))
    try Compiler(i, s.log) finally x.close() // workaround for #937
  }

  def myCompileTaskImpl(s: TaskStreams, ci: Compiler.Inputs): inc.Analysis = {
    lazy val x = s.text(ExportStream)
    def onArgs(cs: Compiler.Compilers) = {
      cs.copy(
        scalac = cs.scalac.onArgs(exported(x, "scalac")),
        javac = cs.javac.onArgs(exported(x, "javac")))
    }
    val i = ci.copy(compilers = onArgs(ci.compilers))
    try Compiler(i, s.log) finally x.close() // workaround for #937
  }

  def exported(w: PrintWriter, command: String): Seq[String] => Unit = args =>
    w.println((command +: args).mkString(" "))


  object JmhKeys {
    val Jmh = config("jmh") extend Compile

    val generateJavaSources = taskKey[Seq[File]]("Generate benchmark JMH Java code")

    val generateInstrumentedClasses = taskKey[Seq[File]]("Generate instrumented JMH code")

    val compileAgain = taskKey[Analysis]("Compile the generated sources")

  }

}
