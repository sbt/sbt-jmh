package pl.project13.scala.benchmark

import org.openjdk.jmh.annotations.Benchmark

class RangeFoldingBenchmark {

  //noinspection SimplifiableFoldOrReduce
  @Benchmark
  def range_foldLeft(): Int =
    (1 to 300).foldLeft(0)(_ + _)

}