package org.openjdk.jmh.samples

import java.net.{URL, URLClassLoader}
import java.util.{HashMap, Map, TreeMap}
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole

object JMHSample_35_Profilers {

  /*
   * This sample serves as the profiler overview.
   *
   * JMH has a few very handy profilers that help to understand your benchmarks. While
   * these profilers are not the substitute for full-fledged external profilers, in many
   * cases, these are handy to quickly dig into the benchmark behavior. When you are
   * doing many cycles of tuning up the benchmark code itself, it is important to have
   * a quick turnaround for the results.
   *
   * Use -lprof to list the profilers. There are quite a few profilers, and this sample
   * would expand on a handful of most useful ones. Many profilers have their own options,
   * usually accessible via -prof <profiler-name>:help.
   *
   * Since profilers are reporting on different things, it is hard to construct a single
   * benchmark sample that will show all profilers in action. Therefore, we have a couple
   * of benchmarks in this sample.
   */

  @State(Scope.Thread)
  @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
  @Fork(3)
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  class Maps {

    private var map: Map[Integer, Integer] = _

    @Param(Array("hashmap", "treemap"))
    private var `type`: String = _

    private var begin: Int = _

    private var end: Int = _

    @Setup
    def setup() {
      if (`type` == "hashmap") {
        map = new HashMap[Integer, Integer]()
      } else if (`type` == "treemap") {
        map = new TreeMap[Integer, Integer]()
      } else {
        throw new IllegalStateException("Unknown type: " + `type`)
      }
      begin = 1
      end = 256
      for (i <- begin until end) {
        map.put(i, i)
      }
    }

    @Benchmark
    def test(bh: Blackhole) {
      for (i <- begin until end) {
        bh.consume(map.get(i))
      }
    }
  }

  object XLoader {
     val X_BYTECODE = Array(0xCA, 0xFE, 0xBA, 0xBE, 0x00, 0x00, 0x00, 0x34, 0x00, 0x0D,
       0x0A, 0x00, 0x03, 0x00, 0x0A, 0x07, 0x00, 0x0B, 0x07, 0x00, 0x0C, 0x01, 0x00, 0x06, 0x3C, 0x69, 0x6E, 0x69,
       0x74, 0x3E, 0x01, 0x00, 0x03, 0x28, 0x29, 0x56, 0x01, 0x00, 0x04, 0x43, 0x6F, 0x64, 0x65, 0x01, 0x00, 0x0F,
       0x4C, 0x69, 0x6E, 0x65, 0x4E, 0x75, 0x6D, 0x62, 0x65, 0x72, 0x54, 0x61, 0x62, 0x6C, 0x65, 0x01, 0x00, 0x0A,
       0x53, 0x6F, 0x75, 0x72, 0x63, 0x65, 0x46, 0x69, 0x6C, 0x65, 0x01, 0x00, 0x06, 0x58, 0x2E, 0x6A, 0x61, 0x76,
       0x61, 0x0C, 0x00, 0x04, 0x00, 0x05, 0x01, 0x00, 0x01, 0x58, 0x01, 0x00, 0x10, 0x6A, 0x61, 0x76, 0x61, 0x2F,
       0x6C, 0x61, 0x6E, 0x67, 0x2F, 0x4F, 0x62, 0x6A, 0x65, 0x63, 0x74, 0x00, 0x20, 0x00, 0x02, 0x00, 0x03, 0x00,
       0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x04, 0x00, 0x05, 0x00, 0x01, 0x00, 0x06, 0x00, 0x00, 0x00,
       0x1D, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x2A, 0xB7, 0x00, 0x01, 0xB1, 0x00, 0x00,
       0x00, 0x01, 0x00, 0x07, 0x00, 0x00, 0x00, 0x06, 0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x08,
       0x00, 0x00, 0x00, 0x02, 0x00, 0x09).map(_.toByte)
  }

  class XLoader extends URLClassLoader(Array.ofDim[URL](0), ClassLoader.getSystemClassLoader) {
    protected override def findClass(name: String): Class[_] = {
      defineClass(name, XLoader.X_BYTECODE, 0, XLoader.X_BYTECODE.length)
    }
  }

  @State(Scope.Thread)
  @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
  @Fork(3)
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  class Classy {
    @Benchmark
    def load(): Class[_] = Class.forName("X", true, new XLoader())
  }

  @State(Scope.Benchmark)
  @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
  @Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
  @Fork(1)
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  class Atomic {

    private var n: AtomicLong = _

    @Setup
    def setup() {
      n = new AtomicLong()
    }

    @Benchmark
    def test(): Long = n.incrementAndGet()
  }
}
