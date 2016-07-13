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

object JMHSample_13_RunToRun {

  /*
   * In order to introduce readily measurable run-to-run variance, we build
   * the workload which performance differs from run to run. Note that many workloads
   * will have the similar behavior, but we do that artificially to make a point.
   */

  @State(Scope.Thread)
  class SleepyState {
    var sleepTime: Long = _

    @Setup
    def setup: Unit = sleepTime = (Math.random() * 1000).toLong
  }

}

@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class JMHSample_13_RunToRun {

  import JMHSample_13_RunToRun._

  /*
   * Forking also allows to estimate run-to-run variance.
   *
   * JVMs are complex systems, and the non-determinism is inherent for them.
   * This requires us to always account the run-to-run variance as the one
   * of the effects in our experiments.
   *
   * Luckily, forking aggregates the results across several JVM launches.
   */

  /*
   * Now, we will run this different number of times.
   */

  @Benchmark
  @Fork(1)
  def baseline(s: SleepyState): Unit = TimeUnit.MILLISECONDS.sleep(s.sleepTime)

  @Benchmark
  @Fork(5)
  def fork_1(s: SleepyState): Unit = TimeUnit.MILLISECONDS.sleep(s.sleepTime)

  @Benchmark
  @Fork(20)
  def fork_2(s: SleepyState): Unit = TimeUnit.MILLISECONDS.sleep(s.sleepTime)

}
