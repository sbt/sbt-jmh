package bench

import org.openjdk.jmh.annotations.Benchmark

class TestScopedBenchmark {
  @Benchmark
  def testScoped(): Unit = {
    // intentionally left blank
  }
}
