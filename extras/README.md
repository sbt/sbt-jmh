# Extra JMH Profilers

![Maven Central](https://img.shields.io/maven-central/v/pl.project13.scala/sbt-jmh-extras)

This library contains JMH integrations for the following profiler tools:
  * Java Flight Recorder.
  * [Async Profiler](https://github.com/jvm-profiling-tools/async-profiler/).
  
## Usage

The JMH profilers do not require SBT and can be used by _any_ JMH benchmark:

1. Add a compile dependency on `pl.project13.scala:sbt-jmh-extras`
2. Build the project
3. Verify that the profiler integrations are loaded by listing
   the available implementations with `-lprof` flag.

Please note that the profiler tools need to be installed separately,
they are not distributed with this library.

### Maven Example

Add a dependency:

```xml
<dependency>
  <groupId>pl.project13.scala</groupId>
  <artifactId>sbt-jmh-extras</artifactId>
  <version>VERSION</version>
</dependency>
```

It is also recommended to add a ServicesResourceTransformer to the shade plugin configuration
to ensure reliable detection in the presence of other profiler providers:

```xml
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.2.1</version>
    <executions>
      <execution>
        <phase>package</phase>
        <goals>
          <goal>shade</goal>
        </goals>
        <configuration>
          <transformers>
            <!-- ¡Add this transformer if not already present: -->
            <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
            <!-- Keep the other transformers -->
          </transformers>
          <!-- and the rest of the configuration -->
        </configuration>
      </execution>
    </executions>
  </plugin>
```

Build the benchmark project:

```sh
mvn clean package
```

Verify the profilers are discovered:

```sh
java -jar target/benchmarks.jar -lprof
```

<details>

<summary>Expected output:</summary>

```
Supported profilers:
                                                              cl: Classloader profiling via standard MBeans 
                                                            comp: JIT compiler profiling via standard MBeans 
                                                              gc: GC profiling via standard MBeans 
                                                                 ⋮
                                                           stack: Simple and naive Java stack profiler 
            pl.project13.scala.jmh.extras.profiler.AsyncProfiler: Profiling using async-profiler (discovered)
  pl.project13.scala.jmh.extras.profiler.FlightRecordingProfiler: Java Flight Recording profiler runs for every benchmark. (discovered)
```

</details>