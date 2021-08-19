sbt-jmh
=======

[![Join the chat at https://gitter.im/ktoso/sbt-jmh](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ktoso/sbt-jmh?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

SBT plugin for running [OpenJDK JMH](http://openjdk.java.net/projects/code-tools/jmh/) benchmarks.

JMH about itself:
-----------------

JMH is a Java harness for building, running, and analysing nano/micro/milli/macro benchmarks written in Java and other languages targeting the JVM.

Please read [nanotrusting nanotime](http://shipilev.net/blog/2014/nanotrusting-nanotime/) and other blog posts on micro-benchmarking (or why most benchmarks are wrong) and make sure your benchmark is valid,
before you set out to implement your benchmarks.

Versions
--------

| Plugin version                                                                             | Default JMH version                                        | Notes                           | 
| ------------------------------------------------------------------------------------------ |------------------------------------------------------------|:-------------------------------:|
| [`0.4.3`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.4.3)  (sbt 1.3.0+)              | [`1.32`](https://github.com/openjdk/jmh/releases/tag/1.32) |                                 | 
| [`0.4.2`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.4.2)  (sbt 1.3.0+)              | [`1.31`](https://github.com/openjdk/jmh/releases/tag/1.31) | JMH `-prof async` supports 2.x  | 
| [`0.4.1`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.4.1)  (sbt 1.3.0+)              | [`1.30`](https://github.com/openjdk/jmh/releases/tag/1.30) |                                 | 
| [`0.4.0`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.4.0)  (sbt 1.3.0+)              | [`1.25`](https://github.com/openjdk/jmh/releases/tag/1.25) | profilers now in JMH core       | 
| [`0.3.7`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.3.7)  (sbt 0.13.17 / sbt 1.1.4) | [`1.21`](https://github.com/openjdk/jmh/releases/tag/1.21) | support JDK 11                  | 
| [`0.3.6`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.3.6)  (sbt 0.13.17 / sbt 1.1.4) | [`1.21`](https://github.com/openjdk/jmh/releases/tag/1.21) | support JDK 11                  |
| [`0.3.4`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.3.4)  (sbt 0.13.17 / sbt 1.1.4) | [`1.21`](https://github.com/openjdk/jmh/releases/tag/1.21) | support of GraalVM              |
| [`0.3.3`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.3.3)  (sbt 0.13.17 / sbt 1.1.1) | [`1.20`](https://github.com/openjdk/jmh/releases/tag/1.20) | JMH bugfix release              |
| [`0.3.2`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.3.2)  (sbt 0.13.16 / sbt 1.0)   | [`1.19`](https://github.com/openjdk/jmh/releases/tag/1.19) | minor bugfix release            |
| [`0.3.1`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.3.1)  (sbt 0.13.16 / sbt 1.0)   | [`1.19`](https://github.com/openjdk/jmh/releases/tag/1.19) | minor bugfix release            |
| [`0.3.0`](https://github.com/ktoso/sbt-jmh/releases/tag/v0.3.0)  (sbt 0.13.16 / sbt 1.0)   | [`1.19`](https://github.com/openjdk/jmh/releases/tag/1.19) | async profiler, flame-graphs    |
| ...                                | ...                                                        |                                 |

Not interesting versions are skipped in the above listing. Always use the newest which has the JMH version you need.
You should stick to the latest version at all times anyway of course.

Adding to your project
----------------------

Since sbt-jmh is an **AutoPlugin** all you need to do in order to activate it in 
your project is to add the below line to your `project/plugins.sbt` file:

```scala
// project/plugins.sbt
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.3")
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

Using Java Flight Recorder / async-profiler.
----------------------------

**NOTE**: `sbt-jmh`-s integration with async-profiler and Java Flight Recorder has been contributed to the
JMH project as of JMH 1.25 and removed from this project. Please migrate to using `-prof jfr` / `-prof async`.
Use `-prof jfr:help` / `-prof async:help` to list available options.

Examples
--------
The [examples are scala-fied examples from the original JMH repo](https://github.com/ktoso/sbt-jmh/tree/main/plugin/src/sbt-test/sbt-jmh/run/src/main/scala/org/openjdk/jmh/samples), check them out, and run them!

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

You can locally publish the plugin with:

```
sbt '; project plugin; ^publishLocal'
```

Please test your changes by adding to the [scripted test suite][sbt-jmh/plugin/src/sbt-test/sbt-jmh/]
which can be run with:

```
 sbt '; project plugin; ^scripted'
```

Special thanks
--------------

Special thanks for contributing async-profiler and flame-graphs support and other improvements
go to [@retronym](https://github.com/retronym) of Lightbend's Scala team.
