package bench

import org.openjdk.jmh.annotations.Benchmark

class KeptBenchmark {
  @Benchmark
  def keepMe(): Unit = {
    // do nothing
  }
}
