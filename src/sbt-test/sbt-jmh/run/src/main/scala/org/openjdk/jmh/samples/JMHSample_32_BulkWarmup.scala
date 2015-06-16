package main.scala.org.openjdk.jmh.samples

import java.util.concurrent.TimeUnit

import main.scala.org.openjdk.jmh.samples.JMHSample_32_BulkWarmup.Counter
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.{OptionsBuilder, WarmupMode}

object JMHSample_32_BulkWarmup {

  /*
   * This is an addendum to JMHSample_12_Forking test.
   *
   * Sometimes you want an opposite configuration: instead of separating the profiles
   * for different benchmarks, you want to mix them together to test the worst-case
   * scenario.
   *
   * JMH has a bulk warmup feature for that: it does the warmups for all the tests
   * first, and then measures them. JMH still forks the JVM for each test, but once the
   * new JVM has started, all the warmups are being run there, before running the
   * measurement. This helps to dodge the type profile skews, as each test is still
   * executed in a different JVM, and we only "mix" the warmup code we want.
   */

  trait Counter {
    def inc(): Int
  }

  def main(args: Array[String]) {
    val opt = new OptionsBuilder().include(classOf[JMHSample_32_BulkWarmup].getSimpleName)
      .warmupMode(WarmupMode.BULK)
      .warmupIterations(5)
      .measurementIterations(5)
      .forks(1)
      .build()
    new Runner(opt).run()
  }
}

@State(Scope.Thread)
 @BenchmarkMode(Mode.AverageTime)
 @OutputTimeUnit(TimeUnit.NANOSECONDS)
class JMHSample_32_BulkWarmup {

   class Counter1 extends Counter {
     private var x: Int = 0
     override def inc(): Int = {
       x += 1
       x
     }
   }

   class Counter2 extends Counter {
     private var x: Int = 0
     override def inc(): Int = {
       x += 1
       x
     }
   }

   var c1: Counter = new Counter1()
   var c2: Counter = new Counter2()

   @CompilerControl(CompilerControl.Mode.DONT_INLINE)
   def measure(c: Counter): Int = {
     var s = 0
     for (i <- 0 until 10) {
       s += c.inc()
     }
     s
   }

   @Benchmark
   def measure_c1(): Int = measure(c1)

   @Benchmark
   def measure_c2(): Int = measure(c2)

 }
