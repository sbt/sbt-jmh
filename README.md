sbt-jmh
=======
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

| Plugin version | Shipped JMH version | 
| -------------- |:-------------------:| 
| `0.1.6`        | `1.1`               |
| `0.1.5`        | `1.0` (!)           |
| `0.1.4`        | `0.9`               |
| `0.1.3`        | `0.8.1`             |

You should of course always prefer the latest release. For minor JMH releases you can override the shipped version by writing:

```scala
version in Jmh := "1.x"
```

Quickstart
----------
Just use the Typesafe Activator to get the template downloaded:

```
activator new sbt-jmh-seed
```

And start writing benchmarks!

Adding to your project
----------------------

Add the below snippet to your `project/plugins.sbt`:

```scala
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.1.6")
```

And then create a new project in your build, to which you should add the jmhSettings. 
For example create another directory with `build.sbt` in it, and paste:

```scala
jmhSettings
```

If you're using a `project/Build.scala`-style build file, you should instead write:

```scala
import pl.project13.scala.sbt.SbtJmh._
```

and then add jmhSettings as a setting to a `Project`.

Write your benchmarks in `src/main/scala`. They will be picked up and instrumented by the plugin.

JMH has a very specific way of working (it generates loads of code), so you should prepare a separate project for your benchmarks. In it, just type `run` in order to run your benchmarks.
All JMH options work as expected. For help type `run -h`. Another example of running it is:

```
run -i 3 -wi 3 -f1 -t1 .*FalseSharing.*
```

Which means "3 iterations" "3 warmup iterations" "1 fork" "1 thread". Please note that benchmarks should be usually executed at least in 10 iterations (as a rule of thumb), but more is better.

**For "real" results we recommend to at least warm up 10 to 20 iterations, and then measure 10 to 20 iterations again. Forking the JVM is required to avoid falling into specific optimisations (no JVM optimisation is really "completely" predictable)**

Options
-------

Please invoke `run -h` to get a full list of run as well as output format options.

Examples
--------
The examples are scala-fied examples from the original JMH repo, check them out, and run them! The results will look somewhat like this:

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

License
-------

This plugin is released under the **Apache 2.0 License**

Contributing
------------

Test plugin with `sbt scripted`
