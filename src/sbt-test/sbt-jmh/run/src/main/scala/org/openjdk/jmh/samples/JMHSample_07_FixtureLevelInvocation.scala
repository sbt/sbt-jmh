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

import java.util.concurrent._

/**
 * Fixtures have different Levels to control when they are about to run.
 * Level.Invocation is useful sometimes to do some per-invocation work
 * which should not count as payload (e.g. sleep for some time to emulate
 * think time)
 */
object JMHSample_07_FixtureLevelInvocation {

  /*
   * Fixtures have different Levels to control when they are about to run.
   * Level.Invocation is useful sometimes to do some per-invocation work,
   * which should not count as payload. PLEASE NOTE the timestamping and
   * synchronization for Level.Invocation helpers might significantly
   * offset the measurement, use with care. See Level.Invocation javadoc
   * for more discussion.
   *
   * Consider this sample:
   */

  /*
   * This state handles the executor.
   * Note we create and shutdown executor with Level.Trial, so
   * it is kept around the same across all iterations.
   */

  @State(Scope.Benchmark)
  class NormalState {
    var service: ExecutorService = _

    @Setup(Level.Trial)
    def up: Unit = service = Executors.newCachedThreadPool()

    @TearDown(Level.Trial)
    def down: Unit = service.shutdown

  }

  object LaggingState {
    final val SLEEP_TIME = Integer.getInteger("sleepTime", 10).intValue
  }

  /*
   * This is the *extension* of the basic state, which also
   * has the Level.Invocation fixture method, sleeping for some time.
   */
  class LaggingState extends NormalState {
    import LaggingState._
    @Setup(Level.Invocation)
    def lag: Unit = TimeUnit.MILLISECONDS.sleep(SLEEP_TIME)
  }

  /*
   * This is our scratch state which will handle the work.
   */

  @State(Scope.Thread)
  class Scratch {
    private var p: Double = _
    def doWork: Double = {
      p = Math.log(p)
      p
    }
  }

  class Task(s: Scratch) extends Callable[Double] {
    override def call: Double = s.doWork
  }

}

@OutputTimeUnit(TimeUnit.MICROSECONDS)
class JMHSample_07_FixtureLevelInvocation {

  import JMHSample_07_FixtureLevelInvocation._

  /*
   * This allows us to formulate the task: measure the task turnaround in
   * "hot" mode when we are not sleeping between the submits, and "cold" mode,
   * when we are sleeping.
   */

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def measureHot(e: NormalState, s: Scratch): Double = e.service.submit(new Task(s)).get

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  def measureCold(e: LaggingState, s: Scratch): Double = e.service.submit(new Task(s)).get

}
