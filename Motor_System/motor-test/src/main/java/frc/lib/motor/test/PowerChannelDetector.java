package frc.lib.motor.test;

import edu.wpi.first.wpilibj.PowerDistribution;

/** Utility methods for detecting which PDH/PDP channel responds to a test motor pulse. */
public final class PowerChannelDetector {
  private static final double MIN_CLEAR_DELTA_AMPS = 4.0;
  private static final double MIN_CLEAR_MARGIN_AMPS = 2.0;
  private static final double MIN_CLEAR_RATIO = 1.5;

  private PowerChannelDetector() {}

  public record Result(
      int detectedChannel,
      double bestDeltaAmps,
      double secondDeltaAmps,
      boolean clearWinner,
      String failureReason) {
    public boolean success() {
      return clearWinner && detectedChannel >= 0;
    }

    public String statusText() {
      if (success()) {
        return String.format(
            "Detected ch %d (delta %.1fA) - confirm before saving",
            detectedChannel, bestDeltaAmps);
      }
      return "Detection failed - " + failureReason;
    }
  }

  public static void sampleCurrents(PowerDistribution pd, double[] currents) {
    for (int channel = 0; channel < currents.length; channel++) {
      currents[channel] = pd.getCurrent(channel);
    }
  }

  public static void capturePeakCurrents(PowerDistribution pd, double[] peaks) {
    for (int channel = 0; channel < peaks.length; channel++) {
      peaks[channel] = Math.max(peaks[channel], pd.getCurrent(channel));
    }
  }

  public static Result analyze(double[] baseline, double[] peaks) {
    int bestChannel = -1;
    double bestDelta = 0.0;
    double secondDelta = 0.0;

    for (int channel = 0; channel < baseline.length; channel++) {
      double delta = peaks[channel] - baseline[channel];
      if (delta > bestDelta) {
        secondDelta = bestDelta;
        bestDelta = delta;
        bestChannel = channel;
      } else if (delta > secondDelta) {
        secondDelta = delta;
      }
    }

    if (bestChannel < 0 || bestDelta <= 0.0) {
      return new Result(-1, bestDelta, secondDelta, false, "no channel response");
    }

    double margin = bestDelta - secondDelta;
    double ratio = secondDelta <= 0.1 ? bestDelta : bestDelta / secondDelta;

    if (bestDelta < MIN_CLEAR_DELTA_AMPS) {
      return new Result(-1, bestDelta, secondDelta, false,
          String.format("surge too small (best %.1fA)", bestDelta));
    }
    if (margin < MIN_CLEAR_MARGIN_AMPS || ratio < MIN_CLEAR_RATIO) {
      return new Result(-1, bestDelta, secondDelta, false,
          String.format("no isolated surge (best %.1fA, second %.1fA)", bestDelta, secondDelta));
    }

    return new Result(bestChannel, bestDelta, secondDelta, true, "");
  }
}
