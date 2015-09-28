package pl.project13.scala.sbt.flamegraph

import java.io.File

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.Try

object FlamesSettings {
  def fromEnv(dir: File): FlamesSettings = {
    FlamesSettings(
      svgOut = dir.getAbsolutePath.replaceAll("\\.", "") + "out.svg", // TODO THIS MUST BE PASSED IN AS PARAM...
      attachJarPath = Try(System.getProperty("ATTACH_JAR_PATH")).toOption,
      verbose = Try(System.getProperty("FLAMES_VERBOSE")).toOption.exists(_ == "1"),
      perf = PerfSettings(
        recordSeconds = Try(System.getProperty("PERF_RECORD_SECONDS")).map(_.toInt).getOrElse(15).seconds,
        recordFreq = Try(System.getProperty("PERF_RECORD_FREQ")).map(_.toInt).getOrElse(99))
    )
  }
}

final case class FlamesSettings(
  svgOut: String,
  attachJarPath: Option[String] = None,
  verbose: Boolean = false,
  perf: PerfSettings = PerfSettings()
) {

  // format: OFF
  def settingsList: List[(String, String)] =
    List(
      "PERF_FLAME_OUTPUT"   -> svgOut,
      "PERF_RECORD_SECONDS" -> perf.recordSeconds.toSeconds.toString,
      "PERF_RECORD_FREQ"    -> perf.recordFreq.toString) ++
      attachJarPath.toList.map { p => "ATTACH_JAR_PATH" -> p } ++
      (if (verbose) List("FLAMES_VERBOSE" -> "1") else Nil)
  // format: ON

  /**
   * Emit all values to environment.
   */
  def emitToEnv(): Unit =
    for {
      (k, v) <- settingsList
    } System.setProperty(k, v)

}

final case class PerfSettings(
  recordSeconds: FiniteDuration = 15.seconds,
  recordFreq: Int = 99
)
