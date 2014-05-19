package com.example

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Array(Mode.Throughput))
class ExampleBenchmark {

  @GenerateMicroBenchmark
  def count_adding_numbers(): Int = {
    Thread.sleep(1)
    val x = 21 + 21
    x * 21
  }

}