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

import java.util.LinkedList
import java.util.List

@State(Scope.Thread)
class JMHSample_26_BatchSize {

  /*
   * Sometimes you need to evaluate operation which doesn't have
   * the steady state. The cost of a benchmarked operation may
   * significantly vary from invocation to invocation.
   *
   * In this case, using the timed measurements is not a good idea,
   * and the only acceptable benchmark mode is a single shot. On the
   * other hand, the operation may be too small for reliable single
   * shot measurement.
   *
   * We can use "batch size" parameter to describe the number of
   * benchmark calls to do per one invocation without looping the method
   * manually and protect from problems described in JMHSample_11_Loops.
   */

  /*
   * Suppose we want to measure insertion in the middle of the list.
   */

  var list: List[String] = new LinkedList[String]

  @Benchmark
  @Warmup(iterations = 5, time = 1)
  @Measurement(iterations = 5, time = 1)
  @BenchmarkMode(Array(Mode.AverageTime))
  def measureWrong_1: List[String] = {
    list.add(list.size / 2, "something")
    list
  }

  @Benchmark
  @Warmup(iterations = 5, time = 5)
  @Measurement(iterations = 5, time = 5)
  @BenchmarkMode(Array(Mode.AverageTime))
  def measureWrong_5(): List[String] = {
    list.add(list.size / 2, "something")
    list
  }

  /*
   * This is what you do with JMH.
   */
  @Benchmark
  @Warmup(iterations = 5, batchSize = 5000)
  @Measurement(iterations = 5, batchSize = 5000)
  @BenchmarkMode(Array(Mode.SingleShotTime))
  def measureRight: List[String] = {
    list.add(list.size / 2, "something")
    list
  }

  @Setup(Level.Iteration)
  def setup: Unit = list.clear

}
