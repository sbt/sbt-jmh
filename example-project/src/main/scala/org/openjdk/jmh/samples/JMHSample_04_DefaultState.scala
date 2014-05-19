package org.openjdk.jmh.samples

import org.openjdk.jmh.annotations.{State, GenerateMicroBenchmark, Scope}

/*
 * Fortunately, in many cases you just need a single state object.
 * In that case, we can mark the benchmark instance itself to be
 * the @State. Then, we can reference it's own fields as will any
 * Java program do.
 */
@State(Scope.Thread)
class JMHSample_04_DefaultState {
  @GenerateMicroBenchmark def measure() {
    x += 1
  }

  private[samples] var x: Double = Math.PI
}