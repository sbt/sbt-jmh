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
import java.util.concurrent.atomic.AtomicInteger

@State(Scope.Group)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class JMHSample_15_Asymmetric {

  /*
   * So far all the tests were symmetric: the same code was executed in all the threads.
   * At times, you need the asymmetric test. JMH provides this with the notion of @Group,
   * which can bind several methods together, and all the threads are distributed among
   * the test methods.
   *
   * Each execution group contains of one or more threads. Each thread within a particular
   * execution group executes one of @Group-annotated @Benchmark methods. Multiple execution
   * groups may participate in the run. The total thread count in the run is rounded to the
   * execution group size, which will only allow the full execution groups.
   *
   * Note that two state scopes: Scope.Benchmark and Scope.Thread are not covering all
   * the use cases here -- you either share everything in the state, or share nothing.
   * To break this, we have the middle ground Scope.Group, which marks the state to be
   * shared within the execution group, but not among the execution groups.
   *
   * Putting this all together, the example below means:
   *  a) define the execution group "g", with 3 threads executing inc(), and 1 thread
   *     executing get(), 4 threads per group in total;
   *  b) if we run this test case with 4 threads, then we will have a single execution
   *     group. Generally, running with 4*N threads will create N execution groups, etc.;
   *  c) each execution group has one @State instance to share: that is, execution groups
   *     share the counter within the group, but not across the groups.
   */

  private var counter: AtomicInteger = _

  @Setup
  def up {
    counter = new AtomicInteger
  }

  @Benchmark
  @Group("g")
  @GroupThreads(3)
  def inc: Int = counter.incrementAndGet

  @Benchmark
  @Group("g")
  @GroupThreads(1)
  def get: Int = counter.get

}
