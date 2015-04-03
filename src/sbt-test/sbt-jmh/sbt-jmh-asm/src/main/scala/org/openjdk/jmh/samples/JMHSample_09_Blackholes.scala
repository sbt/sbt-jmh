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
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
class JMHSample_09_Blackholes {

  /*
   * Should you need returning multiple results, you have to consider two options.
   *
   * NOTE: If you are only producing a single result, it is more readable to use
   * the implicit return, as in org.openjdk.jmh.samples.JMHSample_08_DeadCode.
   * Do not make your benchmark code less readable with explicit Blackholes!
   */

  val x1 = Math.PI
  val x2 = Math.PI * 2

  /*
   * Baseline measurement: how much single Math.log costs.
   */

  @Benchmark
  def baseline: Double = Math.log(x1)

  /*
   * While the Math.log(x2) computation is intact, Math.log(x1)
   * is redundant and optimized out.
   */

  @Benchmark
  def measureWrong: Double = {
    Math.log(x1)
    Math.log(x2)
  }

  /*
   * This demonstrates Option A:
   *
   * Merge multiple results into one and return it.
   * This is OK when is computation is relatively heavyweight, and merging
   * the results does not offset the results much.
   */

  @Benchmark
  def measureRight_1: Double = Math.log(x1) + Math.log(x2)

  /*
   * This demonstrates Option B:
   *
   * Use explicit Blackhole objects, and sink the values there.
   * (Background: Blackhole is just another @State object, bundled with JMH).
   */

  @Benchmark
  def measureRight_2(bh: Blackhole): Unit = {
    bh.consume(Math.log(x1))
    bh.consume(Math.log(x2))
  }

}
