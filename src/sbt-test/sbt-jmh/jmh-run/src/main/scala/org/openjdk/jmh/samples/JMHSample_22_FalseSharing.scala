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

object JMHSample_22_FalseSharing {

  @State(Scope.Group)
  class StateBaseline {
    var readOnly: Int = _
    var writeOnly: Int = _
  }

  /*
   * APPROACH 1: PADDING
   *
   * We can try to alleviate some of the effects with padding.
   * This is not versatile because JVMs can freely rearrange the
   * field order.
   */

  @State(Scope.Group)
  class StatePadded {
    var readOnly: Int = _
    var p01, p02, p03, p04, p05, p06, p07, p08  = 0
    var p11, p12, p13, p14, p15, p16, p17, p18 = 0
    var writeOnly: Int = _
    var q01, q02, q03, q04, q05, q06, q07, q08 = 0
    var q11, q12, q13, q14, q15, q16, q17, q18 = 0
  }

  /*
   * APPROACH 2: CLASS HIERARCHY TRICK
   *
   * We can alleviate false sharing with this convoluted hierarchy trick,
   * using the fact that superclass fields are usually laid out first.
   * In this construction, the protected field will be squashed between
   * paddings.
   */

  class StateHierarchy_1 {
    var readOnly: Int = _
  }

  class StateHierarchy_2 extends StateHierarchy_1 {
    var p01, p02, p03, p04, p05, p06, p07, p08 = 0
    var p11, p12, p13, p14, p15, p16, p17, p18 = 0
  }

  class StateHierarchy_3 extends StateHierarchy_2 {
    var writeOnly: Int = _
  }

  class StateHierarchy_4 extends StateHierarchy_3 {
    var q01, q02, q03, q04, q05, q06, q07, q08 = 0
    var q11, q12, q13, q14, q15, q16, q17, q18 = 0
  }

  @State(Scope.Group)
  class StateHierarchy extends StateHierarchy_4

  /*
   * APPROACH 3: ARRAY TRICK
   *
   * This trick relies on the contiguous allocation of an array.
   * Instead of placing the fields in the class, we mangle them
   * into the array at very sparse offsets.
   */

  @State(Scope.Group)
  class StateArray {
    var arr = new Array[Int](128)
  }

  /*
   * APPROACH 4:
   *
   * @Contended (since JDK 8):
   *  Uncomment the annotation if building with JDK 8.
   *  Remember to flip -XX:-RestrictContended to enable.
   */

  @State(Scope.Group)
  class StateContended {
    var readOnly: Int = _

    // @sun.misc.Contended
    var writeOnly: Int = _
  }

}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(5)
class JMHSample_22_FalseSharing {

  import JMHSample_22_FalseSharing._

  /*
   * One of the unusual thing that can bite you back is false sharing.
   * If two threads access (and possibly modify) the adjacent values
   * in memory, chances are, they are modifying the values on the same
   * cache line. This can yield significant (artificial) slowdowns.
   *
   * JMH helps you to alleviate this: @States are automatically padded.
   * This padding does not extend to the State internals though,
   * as we will see in this example. You have to take care of this on
   * your own.
   */

  /*
   * Suppose we have two threads:
   *   a) innocuous reader which blindly reads its own field
   *   b) furious writer which updates its own field
   */

  /*
   * BASELINE EXPERIMENT:
   * Because of the false sharing, both reader and writer will experience
   * penalties.
   */

  @Benchmark
  @Group("baseline")
  def reader(s: StateBaseline): Int = s.readOnly

  @Benchmark
  @Group("baseline")
  def writer(s: StateBaseline): Unit = s.writeOnly += 1

  @Benchmark
  @Group("padded")
  def reader(s: StatePadded): Int = s.readOnly

  @Benchmark
  @Group("padded")
  def writer(s: StatePadded): Unit = s.writeOnly += 1

  @Benchmark
  @Group("hierarchy")
  def reader(s: StateHierarchy): Int = s.readOnly

  @Benchmark
  @Group("hierarchy")
  def writer(s: StateHierarchy): Unit = s.writeOnly += 1

  @Benchmark
  @Group("sparse")
  def reader(s: StateArray): Int = s.arr(0)

  @Benchmark
  @Group("sparse")
  def writer(s: StateArray): Unit = s.arr(64) += 1

  @Benchmark
  @Group("contended")
  def reader(s: StateContended): Int = s.readOnly

  @Benchmark
  @Group("contended")
  def writer(s: StateContended): Unit = s.writeOnly += 1

}
