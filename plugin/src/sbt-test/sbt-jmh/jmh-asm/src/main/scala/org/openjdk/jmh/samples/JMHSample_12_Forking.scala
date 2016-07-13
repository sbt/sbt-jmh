/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.jmh.samples

import org.openjdk.jmh.annotations._

import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class JMHSample_12_Forking {

  /*
   * JVMs are notoriously good at profile-guided optimizations. This is bad for benchmarks,
   * because different tests can mix their profiles together, and then render the "uniformly bad"
   * code for every test. Forking each test can help to evade this issue.
   *
   * JMH will fork the tests by default.
   */

  /*
   * Suppose we have this simple counter interface, and also have two implementations.
   * Even though those are semantically the same, from the JVM standpoint, those are
   * distinct classes.
   */

  trait Counter {
    def inc: Int
  }

  class Counter1 extends Counter {
    private var x: Int = _

    override def inc: Int = {
      val a = x
      x += 1
      a
    }
  }

  class Counter2 extends Counter {
    private var x: Int = _

    override def inc: Int = {
      val a = x
      x += 1
      a
    }
  }

  /*
   * And this is how we measure it.
   * Note this is susceptible for same issue with loops we mention in previous examples.
   */

  def measure(c: Counter): Int = {
    var s = 0
    var i = 0
    while (i < 10) {
      s += c.inc
      i += 1
    }
    s
  }

  /*
   * These are two counters.
   */
  val c1 = new Counter1
  val c2 = new Counter2

  /*
   * We first measure the Counter1 alone...
   * Fork(0) helps to run in the same JVM.
   */

  @Benchmark
  @Fork(0)
  def measure_1_c1: Int = measure(c1)

  /*
   * Then Counter2...
   */

  @Benchmark
  @Fork(0)
  def measure_2_c2: Int = measure(c2)

  /*
   * Then Counter1 again...
   */

  @Benchmark
  @Fork(0)
  def measure_3_c1_again: Int = measure(c1)

  /*
   * These two tests have explicit @Fork annotation.
   * JMH takes this annotation as the request to run the test in the forked JVM.
   * It's even simpler to force this behavior for all the tests via the command
   * line option "-f". The forking is default, but we still use the annotation
   * for the consistency.
   *
   * This is the test for Counter1.
   */

  @Benchmark
  @Fork(1)
  def measure_4_forked_c1 = measure(c1)

  /*
   * ...and this is the test for Counter2.
   */

  @Benchmark
  @Fork(1)
  def measure_5_forked_c2 = measure(c2)

}
