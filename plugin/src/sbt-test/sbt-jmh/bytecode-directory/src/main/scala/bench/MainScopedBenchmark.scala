package bench

import org.openjdk.jmh.annotations.Benchmark

// Lives in the *default* scan directory. Since jmhBytecodeDirectory redirects the scan to
// (Test / classDirectory), no benchmark should be generated for this class.
class MainScopedBenchmark {
  @Benchmark
  def mainScoped(): Unit = {
    // intentionally left blank
  }
}
