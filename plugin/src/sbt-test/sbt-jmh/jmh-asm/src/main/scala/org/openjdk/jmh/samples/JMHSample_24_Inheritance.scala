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

import java.util.concurrent.TimeUnit

object JMHSample_24_Inheritance {

  /*
   * In very special circumstances, you might want to provide the benchmark
   * body in the (abstract) superclass, and specialize it with the concrete
   * pieces in the subclasses. Unfortunately, the annotation processors
   * limit our ways in figuring out whether there are subclasses to the class
   * annotated with @GMB.
   *
   * JMH has the provisional hack over annotation processors, which still
   * enables us to do this. The rule of thumb is: if some class has @GMB
   * method, then all the subclasses are also having the "synthetic"
   * @GMB method. The caveat is, because we only know the type hierarchy
   * during the compilation, it is only possible during the same compilation
   * session. That is, mixing in the subclass extending your benchmark class
   * *after* the JMH compilation would have no effect.
   *
   * Note how annotations now have two possible places. The closest annotation
   * in the hierarchy wins.
   */

  @BenchmarkMode(Array(Mode.AverageTime))
  @Fork(1)
  @State(Scope.Thread)
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  abstract class AbstractBenchmark {
    var x = 42

    @Benchmark
    @Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    def bench: Double = doWork * doWork

    protected def doWork: Double
  }

  class BenchmarkLog extends AbstractBenchmark {
    override protected def doWork = Math.log(x)
  }

  class BenchmarkSin extends AbstractBenchmark {
    override protected def doWork = Math.sin(x)
  }

  class BenchmarkCos extends AbstractBenchmark {
    override protected def doWork = Math.cos(x)
  }

}
