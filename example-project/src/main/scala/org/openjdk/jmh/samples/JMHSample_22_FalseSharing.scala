package org.openjdk.jmh.samples

import org.openjdk.jmh.annotations._
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.runner.options.{OptionsBuilder, Options}
import org.openjdk.jmh.runner.Runner

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5) object JMHSample_22_FalseSharing {
  def main(args: Array[String]) {
    val opt: Options = new OptionsBuilder().include(".*" + classOf[JMHSample_22_FalseSharing].getSimpleName + ".*").threads(Runtime.getRuntime.availableProcessors).build
    new Runner(opt).run
  }

  @State(Scope.Group) class StateBaseline {
    private[samples] var readOnly: Int = 0
    private[samples] var writeOnly: Int = 0
  }

  @State(Scope.Group) class StatePadded {
    private[samples] var readOnly: Int = 0
    private[samples] var p01: Int = 0
    private[samples] var p02: Int = 0
    private[samples] var p03: Int = 0
    private[samples] var p04: Int = 0
    private[samples] var p05: Int = 0
    private[samples] var p06: Int = 0
    private[samples] var p07: Int = 0
    private[samples] var p08: Int = 0
    private[samples] var p11: Int = 0
    private[samples] var p12: Int = 0
    private[samples] var p13: Int = 0
    private[samples] var p14: Int = 0
    private[samples] var p15: Int = 0
    private[samples] var p16: Int = 0
    private[samples] var p17: Int = 0
    private[samples] var p18: Int = 0
    private[samples] var writeOnly: Int = 0
    private[samples] var q01: Int = 0
    private[samples] var q02: Int = 0
    private[samples] var q03: Int = 0
    private[samples] var q04: Int = 0
    private[samples] var q05: Int = 0
    private[samples] var q06: Int = 0
    private[samples] var q07: Int = 0
    private[samples] var q08: Int = 0
    private[samples] var q11: Int = 0
    private[samples] var q12: Int = 0
    private[samples] var q13: Int = 0
    private[samples] var q14: Int = 0
    private[samples] var q15: Int = 0
    private[samples] var q16: Int = 0
    private[samples] var q17: Int = 0
    private[samples] var q18: Int = 0
  }

  class StateHierarchy_1 {
    private[samples] var readOnly: Int = 0
  }

  class StateHierarchy_2 extends StateHierarchy_1 {
    private[samples] var p01: Int = 0
    private[samples] var p02: Int = 0
    private[samples] var p03: Int = 0
    private[samples] var p04: Int = 0
    private[samples] var p05: Int = 0
    private[samples] var p06: Int = 0
    private[samples] var p07: Int = 0
    private[samples] var p08: Int = 0
    private[samples] var p11: Int = 0
    private[samples] var p12: Int = 0
    private[samples] var p13: Int = 0
    private[samples] var p14: Int = 0
    private[samples] var p15: Int = 0
    private[samples] var p16: Int = 0
    private[samples] var p17: Int = 0
    private[samples] var p18: Int = 0
  }

  class StateHierarchy_3 extends StateHierarchy_2 {
    private[samples] var writeOnly: Int = 0
  }

  class StateHierarchy_4 extends StateHierarchy_3 {
    private[samples] var q01: Int = 0
    private[samples] var q02: Int = 0
    private[samples] var q03: Int = 0
    private[samples] var q04: Int = 0
    private[samples] var q05: Int = 0
    private[samples] var q06: Int = 0
    private[samples] var q07: Int = 0
    private[samples] var q08: Int = 0
    private[samples] var q11: Int = 0
    private[samples] var q12: Int = 0
    private[samples] var q13: Int = 0
    private[samples] var q14: Int = 0
    private[samples] var q15: Int = 0
    private[samples] var q16: Int = 0
    private[samples] var q17: Int = 0
    private[samples] var q18: Int = 0
  }

  @State(Scope.Group) class StateHierarchy extends StateHierarchy_4 {
  }

  @State(Scope.Group) class StateArray {
    private[samples] var arr: Array[Int] = new Array[Int](128)
  }

  @State(Scope.Group) class StateContended {
    private[samples] var readOnly: Int = 0
    private[samples] var writeOnly: Int = 0
  }

}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5) class JMHSample_22_FalseSharing {
  @GenerateMicroBenchmark
  @Group("baseline") def reader(s: JMHSample_22_FalseSharing.StateBaseline): Int = {
    s.readOnly
  }

  @GenerateMicroBenchmark
  @Group("baseline") def writer(s: JMHSample_22_FalseSharing.StateBaseline) {
    s.writeOnly += 1
  }

  @GenerateMicroBenchmark
  @Group("padded") def reader(s: JMHSample_22_FalseSharing.StatePadded): Int = {
    s.readOnly
  }

  @GenerateMicroBenchmark
  @Group("padded") def writer(s: JMHSample_22_FalseSharing.StatePadded) {
    s.writeOnly += 1
  }

  @GenerateMicroBenchmark
  @Group("hierarchy") def reader(s: JMHSample_22_FalseSharing.StateHierarchy): Int = {
    s.readOnly
  }

  @GenerateMicroBenchmark
  @Group("hierarchy") def writer(s: JMHSample_22_FalseSharing.StateHierarchy) {
    s.writeOnly += 1
  }

  @GenerateMicroBenchmark
  @Group("sparse") def reader(s: JMHSample_22_FalseSharing.StateArray): Int = {
    s.arr(0)
  }

  @GenerateMicroBenchmark
  @Group("sparse") def writer(s: JMHSample_22_FalseSharing.StateArray) {
    s.arr(64) += 1
  }

  @GenerateMicroBenchmark
  @Group("contended") def reader(s: JMHSample_22_FalseSharing.StateContended): Int = {
    s.readOnly
  }

  @GenerateMicroBenchmark
  @Group("contended") def writer(s: JMHSample_22_FalseSharing.StateContended) {
    s.writeOnly += 1
  }
}

