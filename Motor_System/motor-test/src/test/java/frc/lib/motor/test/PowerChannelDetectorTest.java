package frc.lib.motor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PowerChannelDetectorTest {
  @Test
  void clearWinnerProducesDetectedChannelAndConfirmationPrompt() {
    double[] baseline = {0.2, 0.3, 0.1, 0.2};
    double[] peaks = {0.4, 7.1, 0.8, 0.5};

    PowerChannelDetector.Result result = PowerChannelDetector.analyze(baseline, peaks);

    assertTrue(result.success());
    assertEquals(1, result.detectedChannel());
    assertTrue(result.statusText().contains("confirm before saving"));
  }

  @Test
  void weakSurgeFailsInsteadOfGuessing() {
    double[] baseline = {0.2, 0.2, 0.1};
    double[] peaks = {1.1, 1.0, 0.8};

    PowerChannelDetector.Result result = PowerChannelDetector.analyze(baseline, peaks);

    assertFalse(result.success());
    assertEquals(-1, result.detectedChannel());
    assertTrue(result.statusText().contains("surge too small"));
  }

  @Test
  void ambiguousSurgeFailsInsteadOfReturningConfidence() {
    double[] baseline = {0.2, 0.2, 0.1};
    double[] peaks = {5.4, 4.8, 0.3};

    PowerChannelDetector.Result result = PowerChannelDetector.analyze(baseline, peaks);

    assertFalse(result.success());
    assertEquals(-1, result.detectedChannel());
    assertTrue(result.statusText().contains("no isolated surge"));
  }
}
