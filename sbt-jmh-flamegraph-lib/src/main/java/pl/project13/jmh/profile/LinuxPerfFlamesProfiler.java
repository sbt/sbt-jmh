package pl.project13.jmh.profile;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.profile.ExternalProfiler;
import org.openjdk.jmh.profile.ProfilerException;
import org.openjdk.jmh.results.*;
import org.openjdk.jmh.util.FileUtils;
import org.openjdk.jmh.util.ScoreFormatter;
import org.openjdk.jmh.util.Utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinuxPerfFlamesProfiler implements ExternalProfiler {

  private final boolean isDelayed;
  private final int delayMs;

  public LinuxPerfFlamesProfiler(String initLine) throws ProfilerException {
    OptionParser parser = new OptionParser();

    OptionSpec<Integer> optDelay = parser.accepts("delay",
                                                  "Delay collection for a given time, in milliseconds; -1 to detect automatically.")
                                         .withRequiredArg().ofType(Integer.class).describedAs("ms").defaultsTo(-1);

    OptionSet set = parseInitLine(initLine, parser);

    try {
      delayMs = set.valueOf(optDelay);
    } catch (OptionException e) {
      throw new ProfilerException(e.getMessage());
    }

    Collection<String> msgs = Utils.tryWith("perf", "stat", "--log-fd", "2", "echo", "1");
    if (!msgs.isEmpty()) {
      throw new ProfilerException(msgs.toString());
    }

    Collection<String> delay = Utils.tryWith("perf", "stat", "--log-fd", "2", "--delay", "1", "echo", "1");
    isDelayed = delay.isEmpty();
  }

  @Override
  public Collection<String> addJVMInvokeOptions(BenchmarkParams params) {
    long delay;
    if (delayMs == -1) { // not set
      delay = TimeUnit.NANOSECONDS.toMillis(params.getWarmup().getCount() *
                                                params.getWarmup().getTime().convertTo(TimeUnit.NANOSECONDS))
          + TimeUnit.SECONDS.toMillis(1); // loosely account for the JVM lag
    } else {
      delay = delayMs;
    }

    // TODO make configurable
    final String PERF_RECORD_FREQ = String.valueOf(99);
    final String PERF_DATA_FILE = "/tmp/perf.data";

    if (isDelayed) {
      return Arrays.asList("perf", "record", "-F", PERF_RECORD_FREQ, "-o", PERF_DATA_FILE, "-g", "--delay", String.valueOf(delay));
    } else {
      return Arrays.asList("perf", "record", "-F", PERF_RECORD_FREQ, "-o", PERF_DATA_FILE, "-g");
    }
  }

  @Override
  public Collection<String> addJVMOptions(BenchmarkParams params) {
    final String agentJarName = "sbt-jmh-flamegraph-lib_2.10.jar";
    final String agentJarPath = "/home/ktoso/.ivy2/local/pl.project13.scala/sbt-jmh-flamegraph-lib_2.10/0.3.0/jars/" + agentJarName;
    return Arrays.asList(
        "-XX:+PreserveFramePointer",
        "-javaagent:" + agentJarPath
        );
  }

  @Override
  public void beforeTrial(BenchmarkParams params) {
    // do nothing
  }

  @Override
  public Collection<? extends Result> afterTrial(BenchmarkResult br, long pid, File stdOut, File stdErr) {
    PerfResult result = process(stdOut, stdErr);
    try {
      processFlames(br, pid);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Collections.singleton(result);
  }

  private void processFlames(BenchmarkResult br, long pid) throws IOException, InterruptedException {
    final StandardCopyOption replaceExisting = StandardCopyOption.REPLACE_EXISTING;

    // prep existing data, and copy to pid file for ease of tracking and more analysis
    final String perfData = "/tmp/perf.data";
    final String perfDataPid = String.format("/tmp/perf-%d.data", pid);
    Files.copy(new File(perfData).toPath(), new File(perfDataPid).toPath(), replaceExisting);

    // prepare names of intermediate files
    final String stacksPid = String.format("/tmp/out-%d.stacks", pid);
    final String collapsedPid = String.format("/tmp/out-%d.collapsed", pid);

    // where Brendan Gregg's flame-graph was cloned into
    final String FLAMEGRAPH_DIR = "/tmp/sbt-jmh-flamegraph-Flamegraph"; // TODO not hardcode this

    // output files
    final String perfFlamesPidOutput = String.format("flamegraph-%d.svg", pid);
    final File perfFlamesOutputPidFile = new File(perfFlamesPidOutput);

    final String perfFlamesLatestOutput = "flamegraph-latest.svg";
    final File perfFlamesOutputLatestFile = new File(perfFlamesLatestOutput);

    /*
      The below set of processes models this bash script:

      perf script -i $PERF_DATA_FILE \
        > $STACKS

      $FLAMEGRAPH_DIR/stackcollapse-perf.pl $STACKS \
        | tee $COLLAPSED \
        | $FLAMEGRAPH_DIR/flamegraph.pl --color=java \
        > $PERF_FLAME_OUTPUT
     */

    // step 1
    final Process perfScriptProcess = new ProcessBuilder()
        .command("perf", "script", "-i", perfData)
        .redirectOutput(new File(stacksPid))
        .start();

    perfScriptProcess.waitFor(30, TimeUnit.SECONDS);

    // step 2
    final Process stackcollapseProcess = new ProcessBuilder()
        .directory(new File(FLAMEGRAPH_DIR))
        .command("./stackcollapse-perf.pl", stacksPid)
        .redirectOutput(new File(collapsedPid))
        .start();
    stackcollapseProcess.waitFor(30, TimeUnit.SECONDS);

    // TODO should work, no?
//    final ProcessBuilder.Redirect tee = new ProcessBuilder()
//        .redirectInput(stackcollapsePerf)
//        .command("tee", collapsedPid)
//        .redirectOutput();

    final Process flamegraphProcess = new ProcessBuilder()
        .directory(new File(FLAMEGRAPH_DIR))
        .redirectInput(new File(collapsedPid))
        .command("./flamegraph.pl", "--color=java")
        .redirectOutput(perfFlamesOutputPidFile)
        .start();

    flamegraphProcess.waitFor(30, TimeUnit.SECONDS);

    Files.copy(perfFlamesOutputPidFile.toPath(), perfFlamesOutputLatestFile.toPath(), replaceExisting);
    Files.copy(perfFlamesOutputPidFile.toPath(), new File("/tmp/output.svg").toPath(), replaceExisting);

    System.out.println("\n# Flame graph SVG written to: " + perfFlamesOutputPidFile.getAbsolutePath());
  }

  @Override
  public boolean allowPrintOut() {
    return true;
  }

  @Override
  public boolean allowPrintErr() {
    return false;
  }

  @Override
  public String getDescription() {
    return "Linux perf Statistics, additionally generating a FlameGraph from the collected data";
  }

  private PerfResult process(File stdOut, File stdErr) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);

    FileReader fr = null;
    try {
      fr = new FileReader(stdErr);
      BufferedReader reader = new BufferedReader(fr);

      long cycles = 0;
      long insns = 0;

      boolean printing = false;
      String line;
      while ((line = reader.readLine()) != null) {
        if (printing) {
          pw.println(line);
        }
        if (line.contains("Performance counter stats")) {
          printing = true;
        }

        Matcher m = Pattern.compile("(.*)#(.*)").matcher(line);
        if (m.matches()) {
          String pair = m.group(1).trim();
          if (pair.contains(" cycles")) {
            try {
              cycles = NumberFormat.getInstance().parse(pair.split("[ ]+")[0]).longValue();
            } catch (ParseException e) {
              // do nothing, processing code will handle
            }
          }
          if (line.contains(" instructions")) {
            try {
              insns = NumberFormat.getInstance().parse(pair.split("[ ]+")[0]).longValue();
            } catch (ParseException e) {
              // do nothing, processing code will handle
            }
          }
        }
      }

      if (!isDelayed) {
        pw.println();
        pw.println("WARNING: Your system uses old \"perf\", which can not delay data collection.\n" +
                       "Therefore, perf performance data includes benchmark warmup.");
      }

      pw.flush();
      pw.close();

      return new PerfResult(
          sw.toString(),
          cycles,
          insns
      );
    } catch (IOException e) {
      throw new IllegalStateException(e);
    } finally {
      FileUtils.safelyClose(fr);
    }
  }

  static class PerfResult extends Result<PerfResult> {
    private static final long serialVersionUID = -1262685915873231436L;

    private final String output;
    private final long cycles;
    private final long instructions;

    // TODO the \u007b was:    public static final String PREFIX = "\u00b7";

    public PerfResult(String output, long cycles, long instructions) {
      super(ResultRole.SECONDARY, "\u00b7" + "cpi", of(1.0 * cycles / instructions), "CPI", AggregationPolicy.AVG);
      this.output = output;
      this.cycles = cycles;
      this.instructions = instructions;
    }

    @Override
    protected Aggregator<PerfResult> getThreadAggregator() {
      return new PerfResultAggregator();
    }

    @Override
    protected Aggregator<PerfResult> getIterationAggregator() {
      return new PerfResultAggregator();
    }

    @Override
    public String toString() {
      return String.format("%s cycles per instruction", ScoreFormatter.format(1.0 * cycles / instructions));
    }

    @Override
    public String extendedInfo() {
      return "Perf stats:\n--------------------------------------------------\n" + output;
    }
  }

  static class PerfResultAggregator implements Aggregator<PerfResult> {

    @Override
    public PerfResult aggregate(Collection<PerfResult> results) {
      long cycles = 0;
      long instructions = 0;
      String output = "";
      for (PerfResult r : results) {
        cycles += r.cycles;
        instructions += r.instructions;
        output += r.output;
      }
      return new PerfResult(output, cycles, instructions);
    }
  }

  public static OptionSet parseInitLine(String initLine, OptionParser parser) throws ProfilerException {
    parser.accepts("help", "Display help.");

    OptionSpec<String> nonOptions = parser.nonOptions();

    String[] split = initLine.split(";");
    for (int c = 0; c < split.length; c++) {
      if (!split[c].isEmpty()) {
        split[c] = "-" + split[c];
      }
    }

    OptionSet set;
    try {
      set = parser.parse(split);
    } catch (OptionException e) {
      try {
        StringWriter sw = new StringWriter();
        sw.append(e.getMessage());
        sw.append("\n");
        parser.printHelpOn(sw);
        throw new ProfilerException(sw.toString());
      } catch (IOException e1) {
        throw new ProfilerException(e1);
      }
    }

    if (set.has("help")) {
      try {
        StringWriter sw = new StringWriter();
        parser.printHelpOn(sw);
        throw new ProfilerException(sw.toString());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    String s = set.valueOf(nonOptions);
    if (s != null && !s.isEmpty()) {
      throw new ProfilerException("Unhandled options: " + s + " in " + initLine);
    }
    return set;
  }
}
