package org.openjdk.jmh.samples

import org.openjdk.jmh.annotations._

@State(Scope.Thread)
class JMHSample_26_BatchSize {

  var list: List[String] = Nil

  @GenerateMicroBenchmark
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  @BenchmarkMode(Array(Mode.AverageTime))
  def measureWrong_1: List[String] = {
    list ::= "something"
    list
  }

  @GenerateMicroBenchmark
  @Warmup(iterations = 5, time = 5)
  @Measurement(iterations = 5, time = 5)
  @BenchmarkMode(Array(Mode.AverageTime))
  def measureWrong_5: List[String] = {
     list ::= "something"
    list
  }

  @GenerateMicroBenchmark
  @Warmup(iterations = 5, batchSize = 5000)
  @Measurement(iterations = 5, batchSize = 5000)
  @BenchmarkMode(Array(Mode.SingleShotTime))
  def measureRight: List[String] = {
    list ::= "something"
    list
  }

  @Setup(Level.Iteration)
  def setup() {
    list = Nil
  }

}