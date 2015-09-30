package pl.project13.scala.sbt.flamegraph

import java.io.{FileNotFoundException, File}

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

    // TODO can't figure out why the lib is not available here
    // fails with [info] Caused by: java.lang.ClassNotFoundException: pl.project13.scala.sbt.flamegraph.PerfSettings
//    val perfSettings = settingKey[PerfSettings]("Perf configuration, set by configuring the various options.")
//    val settings     = settingKey[FlamesSettings]("Flamegraph configuration, set by configuring the various options.")
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

//      // use the custom runner
//      mainClass in run := Some("pl.project13.scala.sbt.flamegraph.runner.FlamegraphJmhRunner"),

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
      verbose := {
        System.setProperty("FLAMES_VERBOSE", "1")
        true // TODO make false
      },
      perfRecordSeconds := 15,
      attachJarPath := None,
      svgOut := {
        System.setProperty("PERF_FLAME_OUTPUT", "out.svg")
        "out.svg"
      }

      // settings aggregation
//      perfSettings := {
//        PerfSettings() // TODO make configurable
//      },
//      settings := {
//        FlamesSettings(svgOut.value, attachJarPath.value, verbose.value, null)//perfSettings.value)
//      },
    )) ++ Seq(
      // make custom runner available on classpath
      libraryDependencies += "pl.project13.scala" %% "sbt-jmh-flamegraph-lib" % "0.3.0",

      internalDependencyClasspath in Compile += { Attributed.blank(JavaTools) },
      internalDependencyClasspath in Test += { Attributed.blank(JavaTools) },
      internalDependencyClasspath in Jmh += { Attributed.blank(JavaTools) }
    )

  // epic hack to get the tools.jar JDK dependency
  val JavaTools = List[Option[String]] (
    // manual
    sys.env.get("JDK_HOME"),
    sys.env.get("JAVA_HOME"),
    // osx
    try Some("/usr/libexec/java_home".!!.trim)
    catch {
      case _: Throwable => None
    },
    // fallback
    sys.props.get("java.home").map(new File(_).getParent),
    sys.props.get("java.home")
  ).flatten.map { n =>
    new File(n + "/lib/tools.jar")
  }.find(_.exists).getOrElse (
    throw new FileNotFoundException (
      """Could not automatically find the JDK/lib/tools.jar.
        |You must explicitly set JDK_HOME or JAVA_HOME.""".stripMargin
    )
  )

}
