package pl.project13.scala.sbt.flamegraph

import java.io.File

import pl.project13.scala.sbt._
import sbt._
import Keys._

object FlamegraphPlugin extends AutoPlugin {

  import JmhPlugin.JmhKeys.{Jmh, jvmArgsAppend}

  val FlameGraphRepo = "https://github.com/brendangregg/FlameGraph"

  object FlamegraphKeys {
    val Flames = config("flames")

    val flamegraphDir = settingKey[File](s"Directory where @brendangregg's `stackcollapse-perf.pl` is present. Clone it from: $FlameGraphRepo Defaults to env variable FLAMEGRAPH_DIR.")
    val autoCloneFlamegraph = settingKey[Boolean](s"If true, will automatically clone @brendangregg's `FlameGraphs repo from: $FlameGraphRepo  when unable to find local version.")
    val autoCloneInto = settingKey[File]("If autoCloneFlamegraph is enabled, the needed dependencies will be pulled into this directory.")

    val verbose = settingKey[Boolean]("If true will print output from shell scripts which orchestrate perf and attaching of the java agent. Useful for debugging.")
    val svgOut = settingKey[String]("Path of the SVG flamegraph output.")
    val perfRecordSeconds = settingKey[Int]("Number of seconds that perf should be recording events during the benchmark. Defaults to 15.")
    val attachJarPath = settingKey[Option[String]]("Optionally provide path to your custom java-agent which should be attached instead of the default one (as implemented by jrudolph).")

    val perfSettings = settingKey[PerfSettings]("Perf configuration, set by configuring the various options.")
    val settings     = settingKey[FlamesSettings]("Flamegraph configuration, set by configuring the various options.")
  }

  import FlamegraphKeys._

  val autoImport = FlamegraphKeys

  /** All we need is Love. -- and Java and the Jmh plugins. */
  override def requires = plugins.JvmPlugin && JmhPlugin

  /** Plugin must be enabled on the benchmarks project. See http://www.scala-sbt.org/0.13/tutorial/Using-Plugins.html */
  override def trigger = noTrigger

  override def projectConfigurations = Seq(Jmh)

  override def projectSettings = JmhPlugin.projectSettings ++ inConfig(Jmh)(
    Seq(
      // make sure agent will be loaded to JMH
      jvmArgsAppend ++= Seq("-XX:+PreserveFramePointer"),

      // use the custom runner
      mainClass in run := Some("pl.project13.scala.sbt.flamegraph.runner.FlamegraphJmhRunner"),

      libraryDependencies += "pl.project13.scala" %% "sbt-jmh-flamegraph-lib" % "0.3.0",

      autoCloneFlamegraph := true,
      autoCloneInto := new File("/tmp/sbt-jmh-flamegraph-Flamegraph"),
      flamegraphDir := {
        // TODO use it somehow
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
    })) ++ inConfig(Flames)(Seq(
      verbose := false,
      perfRecordSeconds := 15,
      attachJarPath := None,
      svgOut := "out.svg",

      // settings aggregation
      perfSettings := {
        PerfSettings() // TODO make configurable
      },
      settings := {
        FlamesSettings(svgOut.value, attachJarPath.value, verbose.value, null)//perfSettings.value)
      },

      // run app with flamegraph generation
      run := {
        (settings in Flames).value.emitToEnv() // mutate the env! (also known as: oh gosh, don't make me pass those arroung in args to the runner)
        ??? // TODO implement flames:run my.example.App
      }
    )) ++ Seq(
      // make custom runner available on classpath
      libraryDependencies += "pl.project13.scala" %% "sbt-jmh-flamegraph-lib" % "0.3.0"
    )

}
