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
class JMHSample_08_DeadCode {

  /*
   * The culprit of many benchmarks is the dead-code elimination: compilers are smart
   * enough to deduce some computations are redundant, and eliminate them completely.
   * If that eliminated part was our benchmarked code, we are in trouble.
   *
   * Fortunately, JMH provides the essential infrastructure to fight this where appropriate:
   * returning the result of the computation will ask JMH to deal with the result to limit
   * the dead-code elimination.
   */

  private val x = Math.PI

  @Benchmark
  def baseline: Unit = {} // do nothing, this is a baseline

  @Benchmark
  def measureWrong: Unit =
    // This is wrong: result is not used, and the entire computation is optimized out.
    Math.log(x)

  @Benchmark
  def measureRight: Double =
    // This is correct: the result is being used.
    Math.log(x)

}
