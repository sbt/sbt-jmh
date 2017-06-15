package com.example

import java.io.{File, FileOutputStream}

import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.CommandLineOptions

object CustomRunnerApp {
  import scala.collection.JavaConversions._

  def main(args: Array[String]): Unit = {
    val opts = new CommandLineOptions(args: _*) // parse command line arguments, and then bend them to your will! ;-)

    val runner = new Runner(opts) // full access to all JMH features, you can also provide a custom output Format here

    val results = runner.run() // actually run the benchmarks

    val f = new FileOutputStream(new File("custom.out"))
    results.foreach { result: RunResult ⇒
      // usually you'd use these results to report into some external aggregation tool for example
      f.write(s"custom reporting result: ${result.getAggregatedResult.getPrimaryResult}".getBytes("UTF-8"))
    }
    f.close()
  }
}
