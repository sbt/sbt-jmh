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

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Group)
class JMHSample_30_Interrupts {

  /*
   * JMH can also detect when threads are stuck in the benchmarks, and try
   * to forcefully interrupt the benchmark thread. JMH tries to do that
   * when it is arguably sure it would not affect the measurement.
   */

  /*
   * In this example, we want to measure the simple performance characteristics
   * of the ArrayBlockingQueue. Unfortunately, doing that without a harness
   * support will deadlock one of the threads, because the executions of
   * take/put are not paired perfectly. Fortunately for us, both methods react
   * to interrupts well, and therefore we can rely on JMH to terminate the
   * measurement for us. JMH will notify users about the interrupt actions
   * nevertheless, so users can see if those interrupts affected the measurement.
   * JMH will start issuing interrupts after the default or user-specified timeout
   * had been reached.
   *
   * This is a variant of org.openjdk.jmh.samples.JMHSample_18_Control, but without
   * the explicit control objects. This example is suitable for the methods which
   * react to interrupts gracefully.
   */

  private var q: BlockingQueue[Integer] = _

  @Setup
  def setup: Unit = q = new ArrayBlockingQueue(1)

  @Group("Q")
  @Benchmark
  def take: Integer = q.take

  @Group("Q")
  @Benchmark
  def put: Unit = q.put(42)

}
