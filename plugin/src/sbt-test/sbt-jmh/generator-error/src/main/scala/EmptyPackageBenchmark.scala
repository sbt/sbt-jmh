import org.openjdk.jmh.annotations.Benchmark

class EmptyPackageBenchmark {
  @Benchmark
  def notAllowed(): Unit = {
  }
}