package pl.project13.scala.benchmark

import org.openjdk.jmh.annotations.{Benchmark, OperationsPerInvocation}

class RangeFoldingBenchmark {

  @Benchmark
  @OperationsPerInvocation(1000)
  def range_foldLeft(): List[Int] = {
    (1 to 1000)
    .foldLeft("")(_ + _)
    .map(_.toInt)
    .toList
    .filter(_ % 2 == 0)
  }

}