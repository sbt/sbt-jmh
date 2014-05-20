import com.typesafe.sbt.SbtJmh._
import java.io.PrintWriter
import JmhKeys._
import org.openjdk.jmh.annotations.GenerateMicroBenchmark
import org.openjdk.jmh.generators.bytecode.JmhBytecodeGenerator
import sbt.CommandStrings._
import sbt.Project.Initialize
import sbt.Task
import compiler._
import inc._

jmhSettings

def myCompileTask: Initialize[Task[inc.Analysis]] = Def.task {
  myCompileTaskImpl(streams.value, (compileInputs in compile).value)
}

def exported(w: PrintWriter, command: String): Seq[String] => Unit = args =>
  w.println((command +: args).mkString(" "))

def exported(s: TaskStreams, command: String): Seq[String] => Unit = args => {
  val w = s.text(ExportStream)
  try exported(w, command)
  finally w.close() // workaround for #937
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

generateJavaSources in Jmh := {
  streams.value.log.info("Generating sources.............")
  val out = (target in Compile).value / s"scala-${scalaBinaryVersion.value}"
  val compiledBytecodeDirectory = out / "classes"
  val outputSourceDirectory = out / "generated-sources" / "jmh"
  val outputResourceDirectory = compiledBytecodeDirectory
  val micro = classOf[GenerateMicroBenchmark]
  Thread.currentThread().setContextClassLoader(micro.getClassLoader)
  JmhBytecodeGenerator.main(Array(compiledBytecodeDirectory, outputSourceDirectory, outputResourceDirectory).map(_.toString))
  (outputSourceDirectory ** "*").filter(_.isFile).get
}

compile in Jmh := {
  streams.value.log.info("Compiling AGAIN from JMH.............")
  val generatedJava = (generateJavaSources in Jmh).value
  myCompileGeneratedJava(streams.value, (compileInputs in (Compile, compile)).value, generatedJava)
}

compileAgain in Jmh := {
  streams.value.log.info("Compiling from JMH.............")
  myCompileTaskImpl(streams.value, (compileInputs in (Compile, compile)).value)
}

sourceDirectories in Compile += target.value / s"scala-${scalaBinaryVersion.value}" / "generated-sources" / "jmh"

compile in Jmh <<= (compile in Jmh).dependsOn(generateJavaSources in Jmh, compile in Compile)

compile in Jmh <<= (compile in Jmh).dependsOn(compileAgain in Jmh)

compile in Jmh <<= (compile in Jmh).dependsOn(compile in Compile)


run in Jmh <<= (run in Compile).dependsOn(compile in Jmh)

run in Compile <<= (run in Compile).dependsOn(compile in Jmh)
