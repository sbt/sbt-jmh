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
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class AsyncProfiler implements InternalProfiler, ExternalProfiler {

    private static final String ASYNC_PROFILER_DIR = "ASYNC_PROFILER_DIR";

    private static final String DEFAULT_EVENT = "cpu";
    private static final long DEFAULT_FRAMEBUF = 8 * 1024 * 1024;
    private static final long DEFAULT_INTERVAL = 1000000;
    private final String event;
    private final Directions directions;
    private final Path asyncProfilerDir;
    private final boolean debugNonSafepoints;
    private final boolean threads;
    private final Boolean simpleName;
    private final Boolean jfr;
    private Path outputDir;
    private boolean started;

    private Path profiler;
    private Path jattach;
    private Long framebuf;
    private Long interval;
    private int measurementIterationCount;
    private Path flameGraphDir;
    private Collection<? extends String> flameGraphOpts = Collections.emptyList();
    private boolean verbose = false;
    private List<Path> generated = new ArrayList<>();

    /**
     * Creates an async profiler with an empty command line. This constructor is required
     * so that {@link ServiceLoader} can instantiate this class as an implementation
     * of {@link org.openjdk.jmh.profile.Profiler} interface. The instance will <em>not</em>
     * be used by JMH for anything but getting its class name.
     *
     * @see org.openjdk.jmh.profile.ProfilerFactory
     */
    public AsyncProfiler() throws ProfilerException {
        this("", false);
    }

    /**
     * Creates an async profiler with the given command line. Will verify the async profiler
     * installation.
     */
    public AsyncProfiler(String initLine) throws ProfilerException {
        this(initLine, true);
    }

    /**
     * Creates an async profiler with the given command line arguments. If requested,
     * will not verify async-profiler installation which is required for initial discovery.
     */
    private AsyncProfiler(String initLine, boolean verifyInstallation) throws ProfilerException {
        OptionParser parser = new OptionParser();
        OptionSpec<String> outputDir = parser.accepts("dir", "Output directory").withRequiredArg().describedAs("directory").ofType(String.class);
        OptionSpec<String> asyncProfilerDir = parser.accepts("asyncProfilerDir", "Location of clone of https://github.com/jvm-profiling-tools/async-profiler. Also can be provided as $" + ASYNC_PROFILER_DIR).withRequiredArg().ofType(String.class).describedAs("directory");
        OptionSpec<String> event = parser.accepts("event", "Event to sample: cpu, alloc, wall, lock, cache-misses etc.").withRequiredArg().ofType(String.class).defaultsTo("cpu");
        OptionSpec<Boolean> debugNonSafepoints = parser.accepts("debugNonSafepoints").withRequiredArg().ofType(Boolean.class).defaultsTo(true, false);
        OptionSpec<Long> framebuf = parser.accepts("framebuf", "Size of profiler framebuffer").withRequiredArg().ofType(Long.class).defaultsTo(DEFAULT_FRAMEBUF);
        OptionSpec<Long> interval = parser.accepts("interval", "Profiling interval, in nanoseconds").withRequiredArg().ofType(Long.class).defaultsTo(DEFAULT_INTERVAL);
        OptionSpec<Boolean> threads = parser.accepts("threads", "Profile threads separately").withRequiredArg().ofType(Boolean.class).defaultsTo(false,true);
        OptionSpec<Boolean> verbose = parser.accepts("verbose", "Output the sequence of commands").withRequiredArg().ofType(Boolean.class).defaultsTo(false);
        OptionSpec<String> flameGraphOpts = parser.accepts("flameGraphOpts", "Options passed to FlameGraph.pl").withRequiredArg().withValuesSeparatedBy(',').ofType(String.class);
        OptionSpec<Directions> flameGraphDirection = parser.accepts("flameGraphDirection", "Directions to generate flamegraphs").withRequiredArg().ofType(Directions.class).defaultsTo(Directions.values());
        OptionSpec<String> flameGraphDir = ProfilerUtils.addFlameGraphDirOption(parser);
        OptionSpec<Boolean> simpleName = parser.accepts("simpleName", "Use simple names in flamegraphs").withRequiredArg().ofType(Boolean.class);
        OptionSpec<Boolean> jfr = parser.accepts("jfr", "Also dump profiles from async-profiler in Java Flight Recorder format").withRequiredArg().ofType(Boolean.class);


        OptionSet options = ProfilerUtils.parseInitLine(initLine, parser);
        if (options.has(event)) {
            this.event = options.valueOf(event);
        } else {
            this.event = DEFAULT_EVENT;
        }
        if (options.has(framebuf)) {
            this.framebuf = options.valueOf(framebuf);
        } else {
            this.framebuf = DEFAULT_FRAMEBUF;
        }
        if (options.has(interval)) {
            this.interval = options.valueOf(interval);
        } else {
            this.interval = DEFAULT_INTERVAL;
        }
        if (options.has(outputDir)) {
            this.outputDir = Paths.get(options.valueOf(outputDir));
            createOutputDirectories();
        }

        if (options.has(flameGraphOpts)) {
            this.flameGraphOpts = options.valuesOf(flameGraphOpts);
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
        if (options.has(threads)) {
            this.threads = options.valueOf(threads);
        } else {
            this.threads = false;
        }
        if (options.has(verbose)) {
            this.verbose = options.valueOf(verbose);
        }
        if (options.has(simpleName)) {
            this.simpleName = options.valueOf(simpleName);
        } else {
            this.simpleName = false;
        }
        if (options.has(jfr)) {
            this.jfr = options.valueOf(jfr);
        } else {
            this.jfr = false;
        }

        if (verifyInstallation) {
            this.flameGraphDir = ProfilerUtils.findFlamegraphDir(flameGraphDir, options);
            this.asyncProfilerDir = lookupAsyncProfilerHome(asyncProfilerDir, options);
            Path build = this.asyncProfilerDir.resolve("build");
            Path profilerLib = build.resolve("libasyncProfiler.so");
            if (!Files.exists(profilerLib)) {
                throw new ProfilerException(profilerLib + " does not exist");
            } else {
                this.profiler = profilerLib;
                Path jattach1 = build.resolve("jattach");
                if (!Files.exists(jattach1)) {
                    throw new ProfilerException(jattach1 + " does not exist");
                } else {
                    this.jattach = jattach1;
                }
            }
        } else {
            this.asyncProfilerDir = Paths.get(".");
        }
    }

    private Path lookupAsyncProfilerHome(OptionSpec<String> asyncProfilerDir, OptionSet options) throws ProfilerException {
        if (options.has(asyncProfilerDir)) {
            return Paths.get(options.valueOf(asyncProfilerDir));
        } else {
            String env = System.getenv(ASYNC_PROFILER_DIR);
            if (env == null) {
                throw new ProfilerException("Location of async-profiler-dir must be set with environment variable ASYNC_PROFILER_DIR or corresponding profiler option");
            }
            return Paths.get(env);
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
        if (!started && iterationParams.getType() == IterationType.MEASUREMENT) {
            String threadOpt = this.threads ? ",threads" : "";
            if (outputDir == null) {
                outputDir = createTempDir(benchmarkParams.id().replaceAll("/", "-"));
            }
            String jfrOpt = this.jfr ? ",jfr,file=" + jfrFile().toAbsolutePath().toString() : "";
            profilerCommand(String.format("start,event=%s%s%s,framebuf=%d,interval=%d", event, jfrOpt, threadOpt, framebuf, interval));
            started = true;
        }
    }

    @Override
    public Collection<? extends Result> afterIteration(BenchmarkParams benchmarkParams, IterationParams iterationParams, IterationResult result) {
        if (iterationParams.getType() == IterationType.MEASUREMENT) {
            measurementIterationCount += 1;
            if (measurementIterationCount == iterationParams.getCount()) {
                if (jfr) {
                    Path jfrDump = jfrFile();
                    generated.add(jfrDump);
                    profilerCommand(String.format("stop,file=%s,jfr", jfrDump));
                }

                Path collapsedPath = outputDir.resolve("collapsed-" + event.toLowerCase() + ".txt");
                profilerCommand(String.format("stop,file=%s,collapsed", collapsedPath));
                generated.add(collapsedPath);
                try {
                    Path jfrPath = outputDir.resolve(event.toLowerCase() + ".jfr");
                    profilerCommand(String.format("file=%s,jfr", jfrPath));
                    generated.add(jfrPath);
                } catch (RuntimeException ex) {
                    System.out.println("Could not create .jfr output, consider upgrading async-profiler");
                }
                Path collapsedProcessedPath = collapsedPath;
                if (simpleName) {
                    collapsedProcessedPath = outputDir.resolve("collapsed-simple-" + event.toLowerCase() + ".txt");
                    generated.add(collapsedProcessedPath);
                    replaceAllInFileLines(collapsedPath, collapsedProcessedPath, Pattern.compile("(^|;)[^;]*\\/"));
                }

                Path summaryPath = outputDir.resolve("summary.txt");
                profilerCommand(String.format("stop,file=%s,summary", summaryPath));
                generated.add(summaryPath);
                if (flameGraphDir != null) {
                    if (EnumSet.of(Directions.FORWARD, Directions.BOTH).contains(directions)) {
                        flameGraph(collapsedProcessedPath, Collections.emptyList(), "");
                    }
                    if (EnumSet.of(Directions.REVERSE, Directions.BOTH).contains(directions)) {
                        flameGraph(collapsedProcessedPath, Arrays.asList("--reverse"), "-reverse");
                    }
                }
            }
        }

        return Collections.singletonList(result());
    }

    private Path jfrFile() {
        return outputDir.resolve("profile-" + event.toLowerCase() + ".jfr");
    }

    private void replaceAllInFileLines(Path in, Path out, Pattern pattern) {
        try (Stream<String> lines = Files.lines(in)){
            Stream<CharSequence> mapped = lines.map(line -> pattern.matcher(line).replaceAll("$1"));
            Files.write(out, mapped::iterator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void flameGraph(Path collapsedPath, List<String> extra, String suffix) {
        generated.add(ProfilerUtils.flameGraph(collapsedPath, extra, suffix, flameGraphDir, flameGraphOpts, outputDir, event, verbose));
    }

    private NoResult result() {
        StringBuilder result = new StringBuilder();
        for (Path path : generated) {
            result.append("\n").append(path.toAbsolutePath().toString());
        }
        return new NoResult("async-profiler", result.toString());
    }

    private void profilerCommand(String command) {
        long pid = Utils.getPid();

        ProcessBuilder processBuilder = new ProcessBuilder(jattach.toAbsolutePath().toString(), String.valueOf(pid), "load", profiler.toAbsolutePath().toString(), "true", command);
        if (verbose) {
            processBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        }
        ProfilerUtils.startAndWait(processBuilder, verbose);
    }

    private Path createTempDir(String prefix) {
        try {
            return Files.createTempDirectory(prefix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getDescription() {
        return "Profiling using async-profiler";
    }

    @Override
    public Collection<String> addJVMInvokeOptions(BenchmarkParams params) {
        return Collections.emptyList();
    }

    @Override
    public Collection<String> addJVMOptions(BenchmarkParams params) {

        List<String> args = new ArrayList<>();
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
