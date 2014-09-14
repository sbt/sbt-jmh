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

object JMHSample_23_AuxCounters {

  @AuxCounters
  @State(Scope.Thread)
  class AdditionalCounters {
    var case1: Int = _
    var case2: Int = _

    @Setup(Level.Iteration)
    def clean() {
      case1 = 0
      case2 = 0
    }

    def total: Int = case1 + case2

  }

}

@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
class JMHSample_23_AuxCounters {

  import JMHSample_23_AuxCounters._

  /*
   * In some weird cases you need to get the separate throughput/time
   * metrics for the benchmarked code depending on the outcome of the
   * current code. Trying to accommodate the cases like this, JMH optionally
   * provides the special annotation which treats @State objects
   * as the object bearing user counters. See @AuxCounters javadoc for
   * the limitations.
   */

  /*
   * This code measures the "throughput" in two parts of the branch.
   * The @AuxCounters state above holds the counters which we increment
   * ourselves, and then let JMH to use their values in the performance
   * calculations. Note how we reset the counters on each iteration.
   */

  @Benchmark
  def measure(counters: AdditionalCounters): Unit =
    if (Math.random() < 0.1) counters.case1 += 1
    else counters.case2 += 1

}
