sbt-jmh
=======

[![Join the chat at https://gitter.im/ktoso/sbt-jmh](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ktoso/sbt-jmh?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/ktoso/sbt-jmh.svg?branch=master)](https://travis-ci.org/ktoso/sbt-jmh)

SBT plugin for running [OpenJDK JMH](http://openjdk.java.net/projects/code-tools/jmh/) benchmarks.

JMH about itself:
-----------------

JMH is a Java harness for building, running, and analysing nano/micro/milli/macro benchmarks written in Java and other languages targeting the JVM.

Please read [nanotrusting nanotime](http://shipilev.net/blog/2014/nanotrusting-nanotime/) and other blog posts on micro-benchmarking (or why most benchmarks are wrong) and make sure your benchmark is valid,
before you set out to implement your benchmarks.

Versions
--------

The latest published plugin version is: [![Download](https://api.bintray.com/packages/ktosopl/sbt-plugins/sbt-jmh/images/download.png) ](https://bintray.com/ktosopl/sbt-plugins/sbt-jmh/_latestVersion)

| Plugin version                   | JMH version & other information       | 
| -------------------------------- |:-------------------------------------:| 
| `0.3.4`  (sbt 13.17 / sbt 1.1.4) | `1.21`, support of GraalVM            |
| `0.3.3`  (sbt 13.17 / sbt 1.1.1) | `1.20`, JMH bugfix release            |
| `0.3.2`  (sbt 13.16 / sbt 1.0)   | `1.19`, minor bugfix release          |
| `0.3.1`  (sbt 13.16 / sbt 1.0)   | `1.19`, minor bugfix release          |
| `0.3.0`  (sbt 13.16 / sbt 1.0)   | `1.19`, async profiler, flame-graphs  |
| `0.2.27` (sbt 0.13.16 / sbt 1.0) | `1.19`                                |
| `0.2.26` (sbt 0.13.16-M1)        | `1.19`                                |
| `0.2.25` (sbt 0.13.x)            | `1.19`                                |
| `0.2.24` (sbt 0.13.x)            | `1.18`                                |
| ...                              | ...                                   |

Not interesting versions are skipped in the above listing. Always use the newest which has the JMH version you need.
You should stick to the latest version at all times anyway of course.

Adding to your project
----------------------

Since sbt-jmh is an **AutoPlugin** all you need to do in order to activate it in 
your project is to add the below line to your `project/plugins.sbt` file:

```scala
// project/plugins.sbt
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.4")
```

and enable it in the projects where you want to (useful in multi-project builds, as you can enable it only where you need it):

```scala
// build.sbt
enablePlugins(JmhPlugin)
```

If you define your project in a `Build.scala`, you also need the following import:

```scala
import pl.project13.scala.sbt.JmhPlugin
```

You can read more about [auto plugins in sbt on it's documentation page](http://www.scala-sbt.org/0.13/tutorial/Using-Plugins.html#Enabling+and+disabling+auto+plugins).

Write your benchmarks in `src/main/scala`. They will be picked up and instrumented by the plugin.

JMH has a very specific way of working (it generates loads of code), so you should prepare a separate project for your benchmarks. In it, just type `run` in order to run your benchmarks.
All JMH options work as expected. For help type `run -h`. Another example of running it is:

```sbt
jmh:run -i 3 -wi 3 -f1 -t1 .*FalseSharing.*
```

Which means "3 iterations" "3 warmup iterations" "1 fork" "1 thread". Please note that benchmarks should be usually executed at least in 10 iterations (as a rule of thumb), but more is better.

**For "real" results we recommend to at least warm up 10 to 20 iterations, and then measure 10 to 20 iterations again. Forking the JVM is required to avoid falling into specific optimisations (no JVM optimisation is really "completely" predictable)**

If your benchmark should be a module in a multimodule project and needs access to another modules test classes then you
might want to define your benchmarks in `src/test` as well (because [Intellij does not support "compile->test" dependencies](https://youtrack.jetbrains.com/issue/SCL-8396)).
While this is not directly supported it can be achieved with some tweaks. Assuming the benchmarks live in a module `bench` and need access
 to test classes from `anotherModule`, you have to define this dependency in your main `build.sbt`:
```scala
lazy val bench = project.dependsOn(anotherModule % "test->test").enablePlugins(JmhPlugin)
```
In `bench/build.sbt` you need to tweak some settings:
```scala
sourceDirectory in Jmh := (sourceDirectory in Test).value
classDirectory in Jmh := (classDirectory in Test).value
dependencyClasspath in Jmh := (dependencyClasspath in Test).value
// rewire tasks, so that 'jmh:run' automatically invokes 'jmh:compile' (otherwise a clean 'jmh:run' would fail)
compile in Jmh := (compile in Jmh).dependsOn(compile in Test).value
run in Jmh := (run in Jmh).dependsOn(Keys.compile in Jmh).evaluated
```

Options
-------

Please invoke `run -h` to get a full list of run as well as output format options.

**Useful hint**: If you plan to aggregate the collected data you should have a look at the available output formats (`-lrf`).
For example it's possible to keep the benchmark's results as csv or json files for later regression analysis.

Using Oracle Flight Recorder
----------------------------

Flight Recorder / *Java Mission Control* is an excellent tool shipped by default in the Oracle JDK distribution.
It is a profiler that uses internal APIs (commercial) and thus is way more precise and detailed than your every-day profiler.

To record a Flight Recorder file from a JMH run run it using the `jmh.extras.JFR` profiler:

```sbt
jmh:run -prof jmh.extras.JFR -t1 -f 1 -wi 10 -i 20 .*TestBenchmark.*
```

All options can be discovered by running the help task:

```
sbt> jmh:run Bench -prof jmh.extras.JFR:help
Option                              Description
------                              -----------
--debugNonSafepoints <Boolean>      (default: [true, false])
--dir <Output directory>
--events <JfrEventType>             (default: [CPU, ALLOCATION_TLAB,
                                      ALLOCATION_OUTSIDE_TLAB, EXCEPTIONS,
                                      LOCKS])
--flameGraphDir <directory>       Location of clone of https://github.
                                      com/brendangregg/FlameGraph. Also
                                      can be provided as $FLAME_GRAPH_DIR
--flameGraphDirection <Directions>  Directions to generate flamegraphs
--flameGraphOpts                    Options passed to FlameGraph.pl
--flightRecorderOpts
--help                              Display help.
--jfrFlameGraphDir <directory>    Location of clone of https://github.
                                      com/chrishantha/jfr-flame-graph.
                                      Also can be provided as
                                      $JFR_FLAME_GRAPH_DIR
--jfrFlameGraphOpts                 Options passed to flamegraph-output.sh
--stackDepth <Integer>              (default: 1024)
--verbose <Boolean>                 Output the sequence of commands
                                      (default: false)

```

This will result in flight recording file which you can then open and analyse offline using JMC.

Example output:

```
[info] Secondary result "JFR":
[info] JFR Messages:
[info] --------------------------------------------
[info] Flight Recording output saved to:
[info]     /Users/ktoso/code/sbt-jmh/sbt-jmh-tester/./test.TestBenchmark.range-Throughput-1.jfr
```

Export JFR to **specific directory**:
```sbt
jmh:run -prof jmh.extras.JFR:--dir={absolute}/{path}/{of}/{folder} -t1 -f 1 -wi 10 -i 20 .*TestBenchmark.*
```

Using async-profiler
--------------------

Using async profiler is done by using the `jmh.extras.Async` profiler like this:

```
sbt> jmh:run Bench -prof jmh.extras.Async ...
```

All additional options are documented in it's help task:

```
sbt> jmh:run Bench -prof jmh.extras.Async:help

Option                              Description
------                              -----------
--asyncProfilerDir <directory>    Location of clone of https://github.
                                      com/jvm-profiling-tools/async-
                                      profiler. Also can be provided as
                                      $ASYNC_PROFILER_DIR
--dir <<directory>>                 Output directory
--event <AsyncProfilerEventType>    Event to sample (default: [CPU, HEAP])
--flameGraphDir <directory>       Location of clone of https://github.
                                      com/brendangregg/FlameGraph. Also
                                      can be provided as $FLAME_GRAPH_DIR
--flameGraphDirection <Directions>  Directions to generate flamegraphs
                                      (default: [BOTH, NONE, FORWARD,
                                      REVERSE])
--flameGraphOpts                    Options passed to FlameGraph.pl
--framebuf <Long>                   Size of profiler framebuffer (default:
                                      8388608)
--help                              Display help.
--threads <Boolean>                 profile threads separately (default:
                                      [false, true])
--verbose <Boolean>                 Output the sequence of commands
                                      (default: false)
```

### Automatically generating flame-grapghs

Read more about flame graphs here:

To automatically generate flame graphs for a given benchmark you can invoke:

```
sbt> jmh:run Bench -f1 -wi 5 -i5 -prof jmh.extras.JFR:dir=/tmp/profile-jfr;flameGraphDir=/code/FlameGraph;jfrFlameGraphDir=/code/jfr-flame-graph;flameGraphOpts=--minwidth,2;verbose=true 
```

Where `/code/FlameGraph` and `/code/jfr-flame-graph` need to reflect actual paths of those tools on your system.


Examples
--------
The [examples are scala-fied examples from the original JMH repo](https://github.com/ktoso/sbt-jmh/tree/master/plugin/src/sbt-test/sbt-jmh/run/src/main/scala/org/openjdk/jmh/samples), check them out, and run them! 

The results will look somewhat like this:

```
...

[info] # Run progress: 92.86% complete, ETA 00:00:15
[info] # VM invoker: /Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/bin/java
[info] # VM options: <none>
[info] # Fork: 1 of 1
[info] # Warmup: 2 iterations, single-shot each
[info] # Measurement: 3 iterations, single-shot each
[info] # Threads: 1 thread, will synchronize iterations
[info] # Benchmark mode: Single shot invocation time
[info] # Benchmark: org.openjdk.jmh.samples.JMHSample_02_BenchmarkModes.measureSingleShot
[info] # Warmup Iteration   1: 100322.000 us
[info] # Warmup Iteration   2: 100556.000 us
[info] Iteration   1: 100162.000 us
[info] Iteration   2: 100468.000 us
[info] Iteration   3: 100706.000 us
[info]
[info] Result : 100445.333 ±(99.9%) 4975.198 us
[info]   Statistics: (min, avg, max) = (100162.000, 100445.333, 100706.000), stdev = 272.707
[info]   Confidence interval (99.9%): [95470.135, 105420.532]
[info]
[info]
[info] # Run progress: 96.43% complete, ETA 00:00:07
[info] # VM invoker: /Library/Java/JavaVirtualMachines/jdk1.7.0_60.jdk/Contents/Home/jre/bin/java
[info] # VM options: <none>
[info] # Fork: 1 of 1
[info] # Warmup: 2 iterations, single-shot each, 5000 calls per batch
[info] # Measurement: 3 iterations, single-shot each, 5000 calls per batch
[info] # Threads: 1 thread, will synchronize iterations
[info] # Benchmark mode: Single shot invocation time
[info] # Benchmark: org.openjdk.jmh.samples.JMHSample_26_BatchSize.measureRight
[info] # Warmup Iteration   1: 15.344 ms
[info] # Warmup Iteration   2: 13.499 ms
[info] Iteration   1: 2.305 ms
[info] Iteration   2: 0.716 ms
[info] Iteration   3: 0.473 ms
[info]
[info] Result : 1.165 ±(99.9%) 18.153 ms
[info]   Statistics: (min, avg, max) = (0.473, 1.165, 2.305), stdev = 0.995
[info]   Confidence interval (99.9%): [-16.988, 19.317]
[info]
[info]
[info] Benchmark                                                 Mode   Samples         Mean   Mean error    Units
[info] o.o.j.s.JMHSample_22_FalseSharing.baseline               thrpt         3      692.034      179.561   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.baseline:reader        thrpt         3      199.185      185.188   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.baseline:writer        thrpt         3      492.850        7.307   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.contended              thrpt         3      706.532      293.880   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.contended:reader       thrpt         3      210.202      277.801   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.contended:writer       thrpt         3      496.330       78.508   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.hierarchy              thrpt         3     1751.941      222.535   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.hierarchy:reader       thrpt         3     1289.003      277.126   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.hierarchy:writer       thrpt         3      462.938       55.329   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.padded                 thrpt         3     1745.650       83.783   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.padded:reader          thrpt         3     1281.877       47.922   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.padded:writer          thrpt         3      463.773      104.223   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.sparse                 thrpt         3     1362.515      461.782   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.sparse:reader          thrpt         3      898.282      415.388   ops/us
[info] o.o.j.s.JMHSample_22_FalseSharing.sparse:writer          thrpt         3      464.233       49.958   ops/us
```

Advanced: Using custom Runners
------------------------------
It is possible to hand over the running of JMH to an `App` implemented by you, which allows you to programmatically
access all test results and modify JMH arguments before you actually invoke it.

To use a custom runner class with `runMain`, simply use it: `jmh:runMain com.example.MyRunner -i 10 .*` –
an example for this is available in [plugin/src/sbt-test/sbt-jmh/runMain](plugin/src/sbt-test/sbt-jmh/runMain) (open the `test` file).

To replace the runner class which is used when you type `jmh:run`, you can set the class in your build file –
an example for this is available in [plugin/src/sbt-test/sbt-jmh/custom-runner](plugin/src/sbt-test/sbt-jmh/custom-runner) (open the `build.sbt` file).

Contributing
============

Yes, pull requests and opening issues is very welcome!

The plugin is maintained at an best-effort basis -- submitting a PR is the best way of getting something done :-)

Please test your changes using `sbt scripted`.

Special thanks
--------------

Special thanks for contributing async-profiler and flame-graphs support and other improvements
go to [@retronym](https://github.com/retronym) of Lightbend's Scala team.

License
-------

This plugin is released under the **Apache 2.0 License**
