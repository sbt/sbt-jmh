package pl.project13.scala.benchmark

import org.openjdk.jmh.annotations.Benchmark

class RangeFoldingBenchmark {

  //noinspection SimplifiableFoldOrReduce
  @Benchmark
  def range_foldLeft(): Integer = {
    (1 to 1000).foldLeft(new Integer(0))(_ + _)
  }
}