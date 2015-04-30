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

import java.util.ArrayList
import java.util.LinkedList
import java.util.List
import java.util.Queue
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._

object JMHSample_29_StatesDAG {

  class Counter {
    var x: Int = _

    def inc: Int = {
      val a = x
      x += 1
      a
    }

    def dispose: Unit = () // pretend this is something really useful

  }

  /*
   * Shared state maintains the set of Counters, and worker threads should
   * poll their own instances of Counter to work with. However, it should only
   * be done once, and therefore, Local state caches it after requesting the
   * counter from Shared state.
   */

  @State(Scope.Benchmark)
  class Shared {
    var all: List[Counter] = _
    var available: Queue[Counter] = _

    @Setup
    def setup: Unit = synchronized {
      all = new ArrayList[Counter]
      var c = 0
      while (c < 10) {
        all.add(new Counter)
        c += 1
      }

      available = new LinkedList[Counter]
      available.addAll(all)
    }

    @TearDown
    def tearDown: Unit = synchronized {
      for (c <- all.asScala) c.dispose
    }

    def getMine: Counter = available.poll

  }

  @State(Scope.Thread)
  class Local {
    var cnt: Counter = _

    @Setup
    def setup(shared: Shared): Unit = cnt = shared.getMine
  }

}

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Thread)
class JMHSample_29_StatesDAG {

  import JMHSample_29_StatesDAG._

  /**
   * WARNING:
   * THIS IS AN EXPERIMENTAL FEATURE, BE READY FOR IT BECOME REMOVED WITHOUT NOTICE!
   */

  /**
   * There are weird cases when the benchmark state is more cleanly described
   * by the set of @States, and those @States reference each other. JMH allows
   * linking @States in directed acyclic graphs (DAGs) by referencing @States
   * in helper method signatures. (Note that {@link org.openjdk.jmh.samples.JMHSample_28_BlackholeHelpers}
   * is just a special case of that.
   *
   * Following the interface for @Benchmark calls, all @Setups for
   * referenced @State-s are fired before it becomes accessible to current @State.
   * Similarly, no @TearDown methods are fired for referenced @State before
   * current @State is done with it.
   */

  /*
   * This is a model case, and it might not be a good benchmark.
   * // TODO: Replace it with the benchmark which does something useful.
   */

  @Benchmark
  def test(local: Local): Int = local.cnt.inc

}
