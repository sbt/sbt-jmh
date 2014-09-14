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
class JMHSample_16_CompilerControl {

  /*
   * We can use HotSpot-specific functionality to tell the compiler what
   * do we want to do with particular methods. To demonstrate the effects,
   * we end up with 3 methods in this sample.
   */

  /**
   * These are our targets:
   *   - first method is prohibited from inlining
   *   - second method is forced to inline
   *   - third method is prohibited from compiling
   *
   * We might even place the annotations directly to the benchmarked
   * methods, but this expresses the intent more clearly.
   */

  def target_blank: Unit = () // this method was intentionally left blank

  @CompilerControl(CompilerControl.Mode.DONT_INLINE)
  def target_dontInline: Unit = () // this method was intentionally left blank

  @CompilerControl(CompilerControl.Mode.INLINE)
  def target_inline: Unit = () // this method was intentionally left blank

  @CompilerControl(CompilerControl.Mode.EXCLUDE)
  def target_exclude: Unit = () // this method was intentionally left blank

  /*
   * These method measures the calls performance.
   */

  @Benchmark
  def baseline: Unit = () // this method was intentionally left blank

  @Benchmark
  def blank: Unit = target_blank

  @Benchmark
  def dontinline: Unit = target_dontInline

  @Benchmark
  def inline: Unit = target_inline

  @Benchmark
  def exclude: Unit = target_exclude

}
