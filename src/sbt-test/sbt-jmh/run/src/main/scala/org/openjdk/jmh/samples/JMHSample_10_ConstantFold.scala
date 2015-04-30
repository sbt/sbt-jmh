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
class JMHSample_10_ConstantFold {

  /*
   * The flip side of dead-code elimination is constant-folding.
   *
   * If JVM realizes the result of the computation is the same no matter what, it
   * can cleverly optimize it. In our case, that means we can move the computation
   * outside of the internal JMH loop.
   *
   * This can be prevented by always reading the inputs from the state, computing
   * the result based on that state, and the follow the rules to prevent DCE.
   */

  // IDEs will say "Oh, you can convert this field to local variable". Don't. Trust. Them.
  private val x = Math.PI

  @Benchmark
  def baseline: Double =
    // simply return the value, this is a baseline
    Math.PI

  @Benchmark
  def measureWrong: Double =
    // This is wrong: the source is predictable, and computation is foldable.
    Math.log(Math.PI)

  @Benchmark
  def measureRight: Double =
    // This is correct: the source is not predictable.
    Math.log(x)

}
