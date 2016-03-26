package pl.project13.jmh.agent;

import net.virtualvoid.perf.AttachOnce;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class AttachAgent {

  public static void premain(String agentArgs, Instrumentation inst) {
    try {
      final File unpackTo = new File("target");
      unpackPerfJavaFlamesLibs(unpackTo);
      unpackPerfJavaFlamesLibs(unpackTo);

      long pid = getPid();
      System.out.println("> Attaching agent to PID (self): " + pid);

      AttachOnce.loadAgent(String.valueOf(pid), agentArgs);
      Thread.sleep(2000);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static long getPid() {
    final String DELIM = "@";

    String name = ManagementFactory.getRuntimeMXBean().getName();

    if (name != null) {
      int idx = name.indexOf(DELIM);

      if (idx != -1) {
        String str = name.substring(0, name.indexOf(DELIM));
        try {
          return Long.valueOf(str);
        } catch (NumberFormatException nfe) {
          throw new IllegalStateException("Process PID is not a number: " + str);
        }
      }
    }
    throw new IllegalStateException("Unsupported PID format: " + name);
  }


  @SuppressWarnings("ResultOfMethodCallIgnored")
  private static void unpackPerfJavaFlamesScripts(File to) throws IOException {
    if (!to.exists()) {
      Files.createDirectory(to.toPath());
    }
    if (!new File(to, "bin").exists()) {
      Files.createDirectory(new File(to, "bin").toPath());
    }


    final List<String> scriptNames = Arrays.asList("create-java-perf-map.sh",
                                                   "perf-java-flames",
                                                   "perf-java-record-stack",
                                                   "perf-java-report-stack",
                                                   "perf-java-top");


    for (String scriptName : scriptNames) {
      final File targetFile = new File(new File(to, "bin"), scriptName);

      if (!targetFile.exists() || !targetFile.canExecute()) {
        targetFile.delete();
        try {
          final InputStream is = AttachAgent.class.getClassLoader().getResourceAsStream("bin/" + scriptName);
          Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
          targetFile.setExecutable(true);
        } catch (Exception ex) {
          throw new RuntimeException("Unable to extract bin/" + scriptName + "!", ex);
        }
      }
    }
  }

  @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
  private static void unpackPerfJavaFlamesLibs(File to) throws IOException {
    if (!to.exists()) {
      Files.createDirectory(to.toPath());
    }

    // TODO compile 32bit, and emit the one we need only
    final List<String> libs = Arrays.asList("libperfmap-64bit.so");
    for (String lib : libs) {
      final File target = new File(to, lib);
      if (!target.exists()) {
        try {
          final InputStream is = AttachAgent.class.getClassLoader().getResourceAsStream(lib);
          Files.copy(is, target.toPath());
        } catch (Exception ex) {
          throw new RuntimeException("Unable to extract " + lib + "!", ex);
        }
      }
    }
  }
}
