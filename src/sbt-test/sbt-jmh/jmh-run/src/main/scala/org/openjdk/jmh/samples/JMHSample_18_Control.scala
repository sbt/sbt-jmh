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
import org.openjdk.jmh.annotations.Group
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Control

import java.util.concurrent.atomic.AtomicBoolean

@State(Scope.Group)
class JMHSample_18_Control {

  /*
   * Sometimes you need the tap into the harness mind to get the info
   * on the transition change. For this, we have the experimental state object,
   * Control, which is updated by JMH as we go.
   */

  /*
   * In this example, we want to estimate the ping-pong speed for the simple
   * AtomicBoolean. Unfortunately, doing that in naive manner will livelock
   * one of the threads, because the executions of ping/pong are not paired
   * perfectly. We need the escape hatch to terminate the loop if threads
   * are about to leave the measurement.
   */

  val flag = new AtomicBoolean

  @Benchmark
  @Group("pingpong")
  def ping(cnt: Control): Unit = {
    while (!cnt.stopMeasurement && !flag.compareAndSet(false, true)) {
      // this body is intentionally left blank
    }
  }

  @Benchmark
  @Group("pingpong")
  def pong(cnt: Control) {
    while (!cnt.stopMeasurement && !flag.compareAndSet(true, false)) {
      // this body is intentionally left blank
    }
  }

}
