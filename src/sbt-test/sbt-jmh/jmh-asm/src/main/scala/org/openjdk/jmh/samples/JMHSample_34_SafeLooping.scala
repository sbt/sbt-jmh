package org.openjdk.jmh.samples

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, CompilerControl, Fork, Measurement, Mode, OutputTimeUnit, Param, Scope, Setup, State, Warmup}
import org.openjdk.jmh.infra.Blackhole

object JMHSample_34_SafeLooping {

  /*
   * JMHSample_11_Loops warns about the dangers of using loops in @Benchmark methods.
   * Sometimes, however, one needs to traverse through several elements in a dataset.
   * This is hard to do without loops, and therefore we need to devise a scheme for
   * safe looping.
   */

  /*
   * Suppose we want to measure how much it takes to execute work() with different
   * arguments. This mimics a frequent use case when multiple instances with the same
   * implementation, but different data, is measured.
   */

}

@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class JMHSample_34_SafeLooping {

  @Param(Array("1", "10", "100", "1000"))
  var size: Int = _

  var xs: Array[Int] = _

  val BASE = 42

  def work(x: Int): Int = BASE + x

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  def sink(v: Int) {
  }

  @Setup
  def setup() {
    xs = Array.ofDim[Int](size)
    for (c <- 0 until size) {
      xs(c) = c
    }
  }

  @Benchmark
  def measureWrong_1(): Int = {
    var acc = 0
    for (x <- xs) {
      acc = work(x)
    }
    acc
  }

  @Benchmark
  def measureWrong_2(): Int = {
    var acc = 0
    for (x <- xs) {
      acc += work(x)
    }
    acc
  }

  @Benchmark
  def measureRight_1(bh: Blackhole) {
    for (x <- xs) {
      bh.consume(work(x))
    }
  }

  @Benchmark
  def measureRight_2() {
    for (x <- xs) {
      sink(work(x))
    }
  }
}
