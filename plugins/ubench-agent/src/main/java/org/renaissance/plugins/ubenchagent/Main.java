package org.renaissance.plugins.ubenchagent;

import org.renaissance.Plugin;

import java.util.List;

import cz.cuni.mff.d3s.perf.Measurement;
import cz.cuni.mff.d3s.perf.BenchmarkResults;

public class Main implements Plugin,
    Plugin.BenchmarkSetUpListener,
    Plugin.OperationSetUpListener,
    Plugin.OperationTearDownListener,
    Plugin.MeasurementResultPublisher {

  final int eventSet;

  public Main(String[] args) {
    if (args.length != 1) {
      warn("One argument should have been provided to the constructor.");
      eventSet = -1;
      return;
    }
    String[] events = args[0].split(",");

    eventSet = Measurement.createEventSet(1, events);
  }

  @Override
  public void afterBenchmarkSetUp(String benchmark) {
    Measurement.start(eventSet);
    Measurement.stop(eventSet);
  }

  @Override
  public void afterOperationSetUp(String benchmark, int opIndex, boolean isLastOp) {
    Measurement.start(eventSet);
  }

  @Override
  public void beforeOperationTearDown(String benchmark, int opIndex, long harnessDuration) {
    Measurement.stop(eventSet);
  }

  @Override
  public void onMeasurementResultsRequested(String benchmark, int opIndex, Plugin.MeasurementResultListener dispatcher) {
    BenchmarkResults results = Measurement.getResults(eventSet);
    String[] events = results.getEventNames();
    List<long[]> data = results.getData();
    if (data.size() != 1) {
      warn("Ignoring invalid data from this loop.");
      return;
    }
    long[] values = data.get(0);
    if (values.length != events.length) {
      warn("Ignoring invalid data from this loop.");
      return;
    }
    for (int i = 0; i < values.length; i++) {
      dispatcher.onMeasurementResult(benchmark, "ubench_agent_" + events[i], values[i]);
    }
  }

  private void warn(String msg, Object... args) {
    System.out.printf("[ubench plugin] WARNING: " + msg + "\n", args);
  }
}

