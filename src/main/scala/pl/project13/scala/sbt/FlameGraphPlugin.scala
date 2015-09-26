package pl.project13.scala.sbt

import sbt._

object FlamegraphPlugin extends AutoPlugin {

  import JmhPlugin.JmhKeys.{Jmh, jvmArgsAppend}

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

  override def projectSettings = inConfig(Jmh)(Defaults.testSettings ++ Seq(
    // make sure agent will be loaded to JMH
    jvmArgsAppend ++= Seq("-XX:+PreserveFramePointer"),
    autoCloneFlamegraph := true,
    autoCloneInto := new File("/tmp/sbt-jmh-ignis-Flamegraph"),
    flamegraphDir := { // TODO use it somehow
      sys.props.get("FLAMEGRAPH_DIR") match {
        case Some(s) => new File(s)
        case _ if autoCloneFlamegraph.value =>
//          streams.value.log(s"Unable to find local FlameGraph, and autoCloneFlamegraph was set, cloning: $FlameGraphRepo")
          import sys.process._
          val cloneInto = autoCloneInto.value
          if (!cloneInto.exists()) s"""git clone $FlameGraphRepo ${cloneInto}""".!!
          cloneInto
      }
    }
    // TODO substitute main class, run perf from it
  )) ++ Seq(
    // settings in default
  )

}
