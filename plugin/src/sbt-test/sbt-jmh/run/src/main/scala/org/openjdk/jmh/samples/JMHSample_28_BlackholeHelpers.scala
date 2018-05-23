/*
 * Copyright (c) 2005, 2014, Oracle and/or its affiliates. All rights reserved.
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
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
class JMHSample_28_BlackholeHelpers {

  /**
   * Sometimes you need the black hole not in @GMB method, but in
   * helper methods, because you want to pass it through to the concrete
   * implementation which is instantiated in helper methods. In this case,
   * you can request the black hole straight in the helper method signature.
   * This applies to both @Setup and @TearDown methods, and also to other
   * JMH infrastructure objects, like Control.
   *
   * Below is the variant of {@link org.openjdk.jmh.samples.JMHSample_08_DeadCode}
   * test, but wrapped in the anonymous classes.
   */

  trait Worker {
    def work: Unit
  }

  private var workerBaseline: Worker = _
  private var workerRight: Worker = _
  private var workerWrong: Worker = _

  @Setup
  def setup(bh: Blackhole): Unit = {
    workerBaseline = new Worker() {
      var x: Double = _
      def work: Unit = () // do nothing
    }

    workerWrong = new Worker() {
      var x: Double = _
      def work: Unit = Math.log(x)
    }

    workerRight = new Worker() {
      var x: Double = _
      def work: Unit = bh.consume(Math.log(x))
    }
  }

  @Benchmark
  def baseline: Unit = workerBaseline.work

  @Benchmark
  def measureWrong: Unit = workerWrong.work

  @Benchmark
  def measureRight: Unit = workerRight.work

}
