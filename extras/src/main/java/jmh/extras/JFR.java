package jmh.extras;

import org.openjdk.jmh.profile.ProfilerException;
import pl.project13.scala.jmh.extras.profiler.FlightRecordingProfiler;

import java.io.IOException;

/** Convenience class for easy usage in terminal: `jmh:run -prof jmh.extras.JFR */
public class JFR extends FlightRecordingProfiler {
  public JFR(String initLine) throws ProfilerException {
    super(initLine);
  }
}
