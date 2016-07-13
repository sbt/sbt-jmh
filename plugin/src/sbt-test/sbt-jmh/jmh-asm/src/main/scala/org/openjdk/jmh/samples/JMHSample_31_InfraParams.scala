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
import org.openjdk.jmh.infra.{BenchmarkParams, ThreadParams}

import java.util.ArrayList
import java.util.List
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

import scala.collection.JavaConverters._

object JMHSample_31_InfraParams {
  final val THREAD_SLICE = 1000

  /*
   * Here is another neat trick. Generate the distinct set of keys for all threads:
   */

  @State(Scope.Thread)
  class Ids {
    private[samples] var ids: List[String] = _

    @Setup
    def setup(threads: ThreadParams) {
      ids = new ArrayList[String]
      var c = 0
      while (c < THREAD_SLICE) {
        ids.add("ID" + (THREAD_SLICE * threads.getThreadIndex() + c))
        c += 1
      }
    }
  }

}

@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
class JMHSample_31_InfraParams {

  import JMHSample_31_InfraParams._

  /*
   * There is a way to query JMH about the current running mode. This is
   * possible with three infrastructure objects we can request to be injected:
   *   - BenchmarkParams: covers the benchmark-global configuration
   *   - IterationParams: covers the current iteration configuration
   *   - ThreadParams: covers the specifics about threading
   *
   * Suppose we want to check how the ConcurrentHashMap scales under different
   * parallelism levels. We can put concurrencyLevel in @Param, but it sometimes
   * inconvenient if, say, we want it to follow the @Threads count. Here is
   * how we can query JMH about how many threads was requested for the current run,
   * and put that into concurrencyLevel argument for CHM constructor.
   */

  private var mapSingle: ConcurrentHashMap[String, String] = _
  private var mapFollowThreads: ConcurrentHashMap[String, String] = _

  @Setup
  def setup(params: BenchmarkParams) {
    val capacity = 16 * THREAD_SLICE * params.getThreads
    mapSingle = new ConcurrentHashMap[String, String](capacity, 0.75f, 1)
    mapFollowThreads = new ConcurrentHashMap[String, String](capacity, 0.75f, params.getThreads)
  }

  @Benchmark
  def measureDefault(ids: Ids) {
    for (s <- ids.ids.asScala) {
      mapSingle.remove(s)
      mapSingle.put(s, s)
    }
  }

  @Benchmark
  def measureFollowThreads(ids: Ids) {
    for (s <- ids.ids.asScala) {
      mapFollowThreads.remove(s)
      mapFollowThreads.put(s, s)
    }
  }

}
