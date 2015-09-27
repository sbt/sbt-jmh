package pl.project13.scala.sbt.flamegraph.runner

import java.util

import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.CommandLineOptions

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

object FlamegraphJmhRunner
  extends JvmProcessSupport
  with PerfSupport {

  def main(args: Array[String]): Unit = {
    val opts = new CommandLineOptions(args: _*) // parse command line arguments, and then bend them to your will! ;-)

    val runner = new Runner(opts) // full access to all JMH features, you can also provide a custom output Format here

    // TODO support multiple forks, need PID monitoring
    val (resultsFuture, pid) = detectJvmProcessId {
      runner.run() // actually run the benchmarks
    }

    attachPerf(pid)

    val results = Await.result(resultsFuture, 24.hours * 7)
  }

}

trait JvmProcessSupport {
  implicit private val ex = ExecutionContext.Implicits.global

  def detectJvmProcessId(block: => util.Collection[RunResult]): (Future[List[RunResult]], Long) = {
    val pidsBefore = findJavaPids()
    val results = Future {
      // run benchmarks
      block.asScala.toList
    }
    val newPids = pidsBefore -- findJavaPids()
    if (newPids.size != 1) throw new Exception("Unable to determina PID of benchmarks JVM! Found new PIDs: " + newPids.mkString(","))
    else results -> newPids.head
  }

  def findJavaPids(): Set[Long] = {
    import scala.sys.process._
    (for {
      line <- ("""jps""" !!).split("\n")
      pid <- Try(line.split(" ").head.toLong).toOption
    } yield pid).toSet
  }
}


trait PerfSupport {
  implicit private val ex = ExecutionContext.Implicits.global

  def attachPerf(pid: Long): Future[Unit] = {
    println(s"${Console.RED}[sbt-jmh-flamegraph]${Console.RESET} Attaching perf to jvm process: ${Console.BOLD}$pid${Console.RESET}")
    import scala.sys.process._
    Future {
      // TODO how to find this file
      s"""./perf-java-flames $pid""" !!
    }
  }
}