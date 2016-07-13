package jmh.extras;

import pl.project13.scala.jmh.extras.profiler.FlightRecordingProfiler;

import java.io.IOException;

/** Convenience class for easy usage in terminal: `jmh:run -prof jmh.extras.JFR */
public class JFR extends FlightRecordingProfiler {
  public JFR() throws IOException {
  }
}
