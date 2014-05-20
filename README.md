sbt-jmh
=======

**THIS IS AN EARLY DRAFT - THOUGH IT WORKS, IT IS CURRENTLY NOT PROPERLY MAINTAINED AS PLUGIN**

Sbt plugin for running JMH tests.

http://openjdk.java.net/projects/code-tools/jmh/

JMH about itself:
-----------------

JMH is a Java harness for building, running, and analysing nano/micro/milli/macro benchmarks written in Java and other languages targetting the JVM.

Please read shipilev.net/blog/2014/nanotrusting-nanotime/ and other blog posts on microbenchmarking (or why most benchmarks are wrong) and make sure your benchmark is valid, before using this plugin to prove performance of things :-)

Usage
-----

**THIS PLUGIN IS NOT RELEASED NOR SUPPORTED (yet?) Please open issues and pull requests though :-)**


You need to publish it locally to use, as it's not released yet. 

```scala
publishLocal
```

Add the below snippet to your `project/plugins.sbt`:

```scala
addSbtPlugin("com.typesafe.sbt" % "sbt-jmh" % version_here)
```

and that one to your `build.sbt`:

```scala
jmhSettings
```

JMH has a very specific way of working (it generates loads of code), so you should prepare a separate project for for youe benchmarks. In it, just type `run` in order to run your benchmarks.
All JMH options work as expected. For help type `run -h`. Another example of running it is:

```
run -i 3 -wi 3 -f1 -t1 org.openjdk.jmh.samples.JMHSample_26_BatchSize.*
```

Which means "3 iterations" "3 warmup iterations" "1 fork" "1 thread". Please note that benchmarks should be usually executed at least in 10 iterations (as a rule of thumb), but more is better.

Examples
--------
The examples are scala-fied examples from tethe original JMH repo, check them out, and run them! The results will look somewhat like this:

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
[info] o.o.j.s.JMHSample_01_HelloWorld.wellHelloThere           thrpt         3  3227211.757   484384.392   ops/ms
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureAll           thrpt         3        0.000        0.000   ops/us
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureMultiple      thrpt         3        0.000        0.000   ops/us
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureThroughput    thrpt         3        9.922        0.176    ops/s
[info] o.o.j.s.JMHSample_03_States.measureShared                thrpt         3   357034.385    30303.559   ops/ms
[info] o.o.j.s.JMHSample_03_States.measureUnshared              thrpt         3   358039.299    36189.573   ops/ms
[info] o.o.j.s.JMHSample_04_DefaultState.measure                thrpt         3   356046.876    51480.555   ops/ms
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
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureAll            avgt         3   100751.985     1654.449    us/op
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureAvgTime        avgt         3   100733.303      446.266    us/op
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureMultiple       avgt         3   100854.000      575.621    us/op
[info] o.o.j.s.JMHSample_26_BatchSize.measureWrong_1             avgt         3        0.000        0.000    ms/op
[info] o.o.j.s.JMHSample_26_BatchSize.measureWrong_5             avgt         3        0.000        0.000    ms/op
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureAll          sample        33   100703.015      210.163    us/op
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureMultiple     sample        33   100575.915      256.931    us/op
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureSamples      sample        33   100544.140      227.595    us/op
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureAll              ss         3   101053.667     3841.347       us
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureMultiple         ss         3   100683.667     9844.620       us
[info] o.o.j.s.JMHSample_02_BenchmarkModes.measureSingleShot       ss         3   100445.333     4975.198       us
[info] o.o.j.s.JMHSample_26_BatchSize.measureRight                 ss         3        1.165       18.153       ms
```

License
-------

This plugin is reeased under the **Apache 2.0 License**
