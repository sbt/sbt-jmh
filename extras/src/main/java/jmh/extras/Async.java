package jmh.extras;

import org.openjdk.jmh.profile.ProfilerException;
import pl.project13.scala.jmh.extras.profiler.FlightRecordingProfiler;

/** Convenience class for easy usage in terminal: `jmh:run -prof jmh.extras.Async */
public class Async extends pl.project13.scala.jmh.extras.profiler.AsyncProfiler {
  public Async(String initLine) throws ProfilerException {
    super(initLine);
  }
}
