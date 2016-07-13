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

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class JMHSample_21_ConsumeCPU {

  /*
   * At times you require the test to burn some of the cycles doing nothing.
   * In many cases, you *do* want to burn the cycles instead of waiting.
   *
   * For these occasions, we have the infrastructure support. Blackholes
   * can not only consume the values, but also the time! Run this test
   * to get familiar with this part of JMH.
   *
   * (Note we use static method because most of the use cases are deep
   * within the testing code, and propagating blackholes is tedious).
   */

  @Benchmark
  def consume_0000: Unit = Blackhole.consumeCPU(0)

  @Benchmark
  def consume_0001: Unit = Blackhole.consumeCPU(1)

  @Benchmark
  def consume_0002: Unit = Blackhole.consumeCPU(2)

  @Benchmark
  def consume_0004: Unit = Blackhole.consumeCPU(4)

  @Benchmark
  def consume_0008: Unit = Blackhole.consumeCPU(8)

  @Benchmark
  def consume_0016: Unit = Blackhole.consumeCPU(16)

  @Benchmark
  def consume_0032: Unit = Blackhole.consumeCPU(32)

  @Benchmark
  def consume_0064: Unit = Blackhole.consumeCPU(64)

  @Benchmark
  def consume_0128: Unit = Blackhole.consumeCPU(128)

  @Benchmark
  def consume_0256: Unit = Blackhole.consumeCPU(256)

  @Benchmark
  def consume_0512: Unit = Blackhole.consumeCPU(512)

  @Benchmark
  def consume_1024: Unit = Blackhole.consumeCPU(1024)

}
