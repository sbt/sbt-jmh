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

//def myCompileTask: Initialize[Task[inc.Analysis]] = Def.task {
//  myCompileTaskImpl(streams.value, (compileInputs in compile).value)
//}


