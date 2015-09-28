package pl.project13.scala.sbt.flamegraph

import pl.project13.scala.sbt._
import java.io.File

import sbt._
import sbt.Keys._

object FlamegraphPlugin extends AutoPlugin {

  import JmhPlugin._
  import JmhPlugin.JmhKeys.Jmh
  import JmhPlugin.JmhKeys.jvmArgsAppend
  import JmhPlugin.JmhKeys.generatorType
  val FlameGraphRepo = "https://github.com/brendangregg/FlameGraph"

  object FlamegraphKeys {
    val flamegraphDir = settingKey[File](s"Directory where @brendangregg's `stackcollapse-perf.pl` is present. Clone it from: $FlameGraphRepo Defaults to env variable FLAMEGRAPH_DIR.")
    val autoCloneFlamegraph =  settingKey[Boolean](s"If true, will automatically clone @brendangregg's `FlameGraphs repo from: $FlameGraphRepo  when unable to find local version.")
    val autoCloneInto =  settingKey[File]("If autoCloneFlamegraph is enabled, the needed dependencies will be pulled into this directory.")
  }
  import FlamegraphKeys._

  val autoImport = FlamegraphKeys

  /** All we need is Love. -- and Java and the Jmh plugins. */
  override def requires = plugins.JvmPlugin && JmhPlugin

  /** Plugin must be enabled on the benchmarks project. See http://www.scala-sbt.org/0.13/tutorial/Using-Plugins.html */
  override def trigger = noTrigger

  override def projectConfigurations = Seq(Jmh)

  override def projectSettings = JmhPlugin.projectSettings ++ inConfig(Jmh)(Seq(
    // make sure agent will be loaded to JMH
    jvmArgsAppend ++= Seq("-XX:+PreserveFramePointer"),

    // use the custom runner
    mainClass in run := Some("pl.project13.scala.sbt.flamegraph.runner.FlamegraphJmhRunner"),

    autoCloneFlamegraph := true,
    autoCloneInto := new File("/tmp/sbt-jmh-flamegraph-Flamegraph"),
    flamegraphDir := { // TODO use it somehow
      sys.props.get("FLAMEGRAPH_DIR") match {
        case Some(s) => new File(s)
        case _ if autoCloneFlamegraph.value =>
          val cloneInto = autoCloneInto.value

          // TODO make more robust
          println(s"Unable to find local FlameGraph, and autoCloneFlamegraph was set, " +
            s"cloning: $FlameGraphRepo to $cloneInto")
          import sys.process._
          if (!cloneInto.exists()) s"""git clone $FlameGraphRepo $cloneInto""".!!
          cloneInto
      }
    })) ++ Seq(
      // make custom runner available on classpath
      libraryDependencies += "pl.project13.scala" %% "sbt-jmh-flamegraph-lib" % "0.3.0"
  )

}
