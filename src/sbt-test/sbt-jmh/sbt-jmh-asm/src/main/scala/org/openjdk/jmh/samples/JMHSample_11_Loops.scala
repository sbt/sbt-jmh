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
class JMHSample_11_Loops {

  /*
   * It would be tempting for users to do loops within the benchmarked method.
   * (This is the bad thing Caliper taught everyone). This tests explains why
   * this is a bad idea.
   *
   * Looping uses the idea of minimize the overhead for calling the test method,
   * if we do the operations inside the loop inside the method call.
   * Don't buy this argument; you will see there is more magic
   * happening when we allow optimizers to merge the loop iterations.
   */

  /*
   * Suppose we want to measure how much it takes to sum two integers:
   */

  val x = 1
  val y = 2

  /*
   * This is what you do with JMH.
   */

  @Benchmark
  def measureRight: Int = x + y

  /*
   * The following tests emulate the naive looping.
   * This is the Caliper-style benchmark.
   */
  private def reps(reps: Int): Int = {
    var s = 0
    var i = 0
    while(i < reps) {
      s += (x + y)
      i += 1
    }
    s
  }

  /*
   * We would like to measure this with different repetitions count.
   * Special annotation is used to get the individual operation cost.
   */

  @Benchmark
  @OperationsPerInvocation(1)
  def measureWrong_1: Int = reps(1)

  @Benchmark
  @OperationsPerInvocation(10)
  def measureWrong_10: Int = reps(10)

  @Benchmark
  @OperationsPerInvocation(100)
  def measureWrong_100: Int = reps(100)

  @Benchmark
  @OperationsPerInvocation(1000)
  def measureWrong_1000: Int = reps(1000)

  @Benchmark
  @OperationsPerInvocation(10000)
  def measureWrong_10000: Int = reps(10000)

  @Benchmark
  @OperationsPerInvocation(100000)
  def measureWrong_100000: Int = reps(100000)

}
