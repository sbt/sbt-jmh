package pl.project13.scala.benchmark

import java.util.concurrent.ThreadLocalRandom

import org.openjdk.jmh.annotations.{Benchmark, OperationsPerInvocation}

class RangeFoldingBenchmark {

  @Benchmark
  @OperationsPerInvocation(1000)
  def baseline(): String = {
    var i = 0
    val b = StringBuilder.newBuilder
    while (i < 1000) {
      b.append(i)
      i += 1
    }
    b.result()
  }

  @Benchmark
  @OperationsPerInvocation(1000)
  def range_foldLeft(): String = {
    (1 to 1000).foldLeft("")(_ + _)
  }

  @Benchmark
  def random(): Int =
    ThreadLocalRandom.current().ints().findAny().getAsInt

}