package pl.project13.scala.sbt.flamegraph

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

object FlamesSettings {
  def fromEnv(): FlamesSettings = {
    FlamesSettings(
      sys.env("PERF_FLAME_OUTPUT"),
      sys.env.get("ATTACH_JAR_PATH"),
      sys.env.get("FLAMES_VERBOSE").exists(_ == "1"),
      PerfSettings(
        sys.env.get("PERF_RECORD_SECONDS").map(_.toInt).getOrElse(15).seconds,
        sys.env.get("PERF_RECORD_FREQ").map(_.toInt).getOrElse(99))
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
