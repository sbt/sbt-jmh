/*
 * Originally imported from Jason Zaugg's: https://github.com/retronym/scala-jmh-suite/blob/master/src/main/java/scala/tools/nsc/benchmark/FlightRecordingProfiler.java
 * Scala is licensed under the [BSD 3-Clause License](http://opensource.org/licenses/BSD-3-Clause).
 */
package pl.project13.scala.jmh.extras.profiler;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.profile.ExternalProfiler;
import org.openjdk.jmh.profile.InternalProfiler;
import org.openjdk.jmh.profile.ProfilerException;
import org.openjdk.jmh.results.BenchmarkResult;
import org.openjdk.jmh.results.IterationResult;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.runner.IterationType;
import org.openjdk.jmh.util.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The flight recording profiler enables flight recording for benchmarks. Recording starts after warmup iterations
 * are complete.
 */
public class FlightRecordingProfiler implements InternalProfiler, ExternalProfiler {

    private static final List<String> DEFAULT_FLIGHT_RECORDER_OPTIONS = Arrays.asList("settings=profile");
    private static final String JFR_FLAME_GRAPH_DIR = "JFR_FLAME_GRAPH_DIR";
    private static final String PROFILE_JFR = "profile.jfr";
    private static final List<String> DEFEAULT_JFR_OPTS = Arrays.asList("--use-simple-names");
    private static final int DEFAULT_STACK_DEPTH = 1024;
    private final Path jfrFlameGraphDir;
    private final Directions directions;
    private final List<String> jfrFlameGraphOpts;
    private final Path flameGraphDir;
    private final boolean debugNonSafepoints;
    private boolean verbose = false;
    private List<String> flameGraphOpts = Collections.emptyList();
    private final List<JfrEventType> events;

    private List<String> flightRecorderOptions;

    private boolean warmupStarted;
    private boolean started;
    private int measurementIterationCount;
    private Path outputDir;
    private String name;
    private int stackDepth;
    private List<Path> generated = new ArrayList<>();

    public FlightRecordingProfiler(String initLine) throws ProfilerException {
        OptionParser parser = new OptionParser();
        OptionSpec<String> outputDir = parser.accepts("dir").withRequiredArg().describedAs("Output directory").ofType(String.class);
        OptionSpec<JfrEventType> event = parser.accepts("events").withRequiredArg().ofType(JfrEventType.class).withValuesSeparatedBy(',').defaultsTo(JfrEventType.values());
        OptionSpec<Boolean> debugNonSafepoints = parser.accepts("debugNonSafepoints").withRequiredArg().ofType(Boolean.class).defaultsTo(true, false);
        OptionSpec<Integer> stackDepth = parser.accepts("stackDepth").withRequiredArg().ofType(Integer.class).defaultsTo(DEFAULT_STACK_DEPTH);
        OptionSpec<Boolean> verbose = parser.accepts("verbose", "Output the sequence of commands").withRequiredArg().ofType(Boolean.class).defaultsTo(false);
        OptionSpec<String> flightRecorderOpts = parser.accepts("flightRecorderOpts").withRequiredArg().ofType(String.class).withValuesSeparatedBy(",");
        OptionSpec<String> flameGraphDir = ProfilerUtils.addFlameGraphDirOption(parser);
        OptionSpec<String> jfrFlameGraphDir = parser.accepts("jfrFlameGraphDir", "Location of clone of https://github.com/chrishantha/jfr-flame-graph. Also can be provided as $" + JFR_FLAME_GRAPH_DIR).withRequiredArg().ofType(String.class).describedAs("directory");
        OptionSpec<String> jfrFlameGraphOpts = parser.accepts("jfrFlameGraphOpts", "Options passed to flamegraph-output.sh").withRequiredArg().withValuesSeparatedBy(',').ofType(String.class);
        OptionSpec<String> flameGraphOpts = parser.accepts("flameGraphOpts", "Options passed to FlameGraph.pl").withRequiredArg().withValuesSeparatedBy(',').ofType(String.class);
        OptionSpec<Directions> flameGraphDirection = parser.accepts("flameGraphDirection", "Directions to generate flamegraphs").withRequiredArg().ofType(Directions.class);
        OptionSet options = ProfilerUtils.parseInitLine(initLine, parser);

        if (options.has(event)) {
            this.events = options.valuesOf(event);
        } else {
            this.events = Arrays.asList(JfrEventType.CPU, JfrEventType.ALLOCATION_TLAB);
        }
        if (options.has(outputDir)) {
            this.outputDir = Paths.get(options.valueOf(outputDir));
            createOutputDirectories();
        } else {
            String outputDirFromProp = System.getProperty("jmh.jfr.saveTo");
            if (outputDirFromProp != null) {
                this.outputDir = Paths.get(outputDirFromProp);
                createOutputDirectories();
            }
            // else create later once we know the benchmark params
        }
        if (options.has(flightRecorderOpts)) {
            this.flightRecorderOptions = options.valuesOf(flightRecorderOpts);
        } else {
            this.flightRecorderOptions = DEFAULT_FLIGHT_RECORDER_OPTIONS;
        }
        this.flameGraphDir = ProfilerUtils.findFlamegraphDir(flameGraphDir, options);
        if (this.flameGraphDir != null) {
            if (options.has(jfrFlameGraphDir)) {
                this.jfrFlameGraphDir = Paths.get(options.valueOf(jfrFlameGraphDir));
            } else {
                String jfrFlameGraphHome = System.getenv(JFR_FLAME_GRAPH_DIR);
                if (jfrFlameGraphHome != null) {
                    this.jfrFlameGraphDir = Paths.get(jfrFlameGraphHome);
                } else {
                    this.jfrFlameGraphDir = null;
                }
            }
        } else {
            this.jfrFlameGraphDir = null;
        }
        if (options.has(flameGraphOpts)) {
            this.flameGraphOpts = options.valuesOf(flameGraphOpts);
        }
        if (options.has(jfrFlameGraphOpts)) {
            this.jfrFlameGraphOpts = options.valuesOf(jfrFlameGraphOpts);
        } else {
            this.jfrFlameGraphOpts = DEFEAULT_JFR_OPTS;
        }
        if (options.has(flameGraphDirection)) {
            this.directions = options.valueOf(flameGraphDirection);
        } else {
            this.directions = Directions.BOTH;
        }
        if (options.has(debugNonSafepoints)) {
            this.debugNonSafepoints = options.valueOf(debugNonSafepoints);
        } else {
            this.debugNonSafepoints = true;
        }
        if (options.has(stackDepth)) {
            this.stackDepth = options.valueOf(stackDepth);
        } else {
            this.stackDepth = DEFAULT_STACK_DEPTH;
        }
        if (options.has(verbose)) {
            this.verbose = options.valueOf(verbose);
        }
    }

    private void createOutputDirectories() {
        try {
            Files.createDirectories(this.outputDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams) {
        if (!warmupStarted) {
            try {
                jcmd(benchmarkParams.getJvm(), "VM.unlock_commercial_features", Collections.emptyList());
            } catch (Throwable t) {
                // ignore, we might be running on OpenJDK 11
            }
            name = "JMH-profile-" + benchmarkParams.getBenchmark().replaceAll("\\s+", "-");
            startJfr(benchmarkParams);
            warmupStarted = true;
        }
        if (!started && iterationParams.getType() == IterationType.MEASUREMENT) {
            stopDiscardJfr(benchmarkParams);
            startJfr(benchmarkParams);
            started = true;
        }
    }

    private void stopDiscardJfr(BenchmarkParams benchmarkParams) {
        ArrayList<String> options = new ArrayList<>();
        options.add("name=" + name);
        options.add("discard=true");
        jcmd(benchmarkParams.getJvm(), "JFR.stop", options);
    }

    private void startJfr(BenchmarkParams benchmarkParams) {
        ArrayList<String> options = new ArrayList<>();
        options.add("name=" + name);
        options.addAll(flightRecorderOptions);
        jcmd(benchmarkParams.getJvm(), "JFR.start", options);
    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams, IterationResult result) {
        if (iterationParams.getType() == IterationType.MEASUREMENT) {
            measurementIterationCount += 1;
            if (measurementIterationCount == iterationParams.getCount()) {
                if (outputDir == null) {
                    outputDir = createTempDir(benchmarkParams.id().replaceAll("/", "-"));
                }

                ArrayList<String> options = new ArrayList<>();

                options.add("name=" + name);
                Path jfrDump = outputDir.resolve(PROFILE_JFR);
                options.add("filename=" + jfrDump.toAbsolutePath().toString());
                jcmd(benchmarkParams.getJvm(), "JFR.stop", options);
                generated.add(jfrDump);
                if (jfrFlameGraphDir != null) {
                    for (JfrEventType event : events) {
                        String eventName = event.name().toLowerCase().replace('_', '-');
                        Path collapsed = createCollapsed(eventName);
                        generated.add(collapsed);
                        if (flameGraphDir != null) {
                            if (EnumSet.of(Directions.FORWARD, Directions.BOTH).contains(directions)) {
                                generated.add(ProfilerUtils.flameGraph(collapsed, Collections.emptyList(), "", flameGraphDir, flameGraphOpts, outputDir, eventName, verbose));
                            }
                            if (EnumSet.of(Directions.REVERSE, Directions.BOTH).contains(directions)) {
                                generated.add(ProfilerUtils.flameGraph(collapsed, Arrays.asList("--reverse"), "-reverse", flameGraphDir, flameGraphOpts, outputDir, eventName, verbose));
                            }
                        }
                    }
                }
            }
        }

        return Collections.singletonList(result());
    }

    private Path createCollapsed(String eventName) {
        ArrayList<String> args = new ArrayList<>();
        args.add("bash");
        args.add("-e");
        args.add(jfrFlameGraphDir.resolve("flamegraph-output.sh").toAbsolutePath().toString());
        args.add("--output-type");
        args.add("folded");
        args.add("--jfrdump");
        args.add(outputDir.resolve(PROFILE_JFR).toAbsolutePath().toString());
        args.add("--event");
        args.add(eventName);
        Path outFile = outputDir.resolve("jfr-collapsed-cpu.txt");
        args.add("--output");
        args.add(outFile.toAbsolutePath().toString());
        args.addAll(jfrFlameGraphOpts);
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        if (verbose) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
        ProfilerUtils.startAndWait(processBuilder, verbose);
        return outFile;
    }


    private NoResult result() {
        StringBuilder result = new StringBuilder();
        for (Path path : generated) {
            result.append("\n").append(path.toAbsolutePath().toString());
        }
        return new NoResult("JFR", result.toString());
    }

    private void jcmd(String jvm, String command, List<String> options) {
        long pid = Utils.getPid();
        ArrayList<String> args = new ArrayList<>();
        Path jcmd = findJcmd(jvm);

        args.add(jcmd.toAbsolutePath().toString());
        args.add(String.valueOf(pid));
        args.add(command);
        args.addAll(options);
        ProcessBuilder processBuilder = new ProcessBuilder(args);
        if (verbose) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
        ProfilerUtils.startAndWait(processBuilder, verbose);
    }

    private Path findJcmd(String jvm) {
        Path jcmd;
        Path firstTry = Paths.get(jvm.replaceAll("java", "jcmd"));
        if (Files.exists(firstTry)) {
            jcmd = firstTry;
        } else {
            jcmd = Paths.get(jvm.replace("jre/bin/java", "bin/jcmd"));
        }
        return jcmd;
    }

    private Path createTempDir(String name) {
        try {
            return Files.createTempDirectory(name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return "Java Flight Recording profiler runs for every benchmark.";
    }

    @Override
    public Collection<String> addJVMInvokeOptions(BenchmarkParams params) {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> addJVMOptions(BenchmarkParams params) {

        List<String> args = new ArrayList<>();
        args.add("-XX:FlightRecorderOptions=samplethreads=true,stackdepth=" + stackDepth);
        if (debugNonSafepoints) {
            args.add("-XX:+UnlockDiagnosticVMOptions");
            args.add("-XX:+DebugNonSafepoints");
        }
        return args;

    }

    @Override
    public void beforeTrial(BenchmarkParams benchmarkParams) {
    }

    @Override
    public Collection<? extends Result> afterTrial(BenchmarkResult br, long pid, File stdOut, File stdErr) {
        return Collections.emptyList();
    }

    @Override
    public boolean allowPrintOut() {
        return true;
    }

    @Override
    public boolean allowPrintErr() {
        return true;
    }
}
