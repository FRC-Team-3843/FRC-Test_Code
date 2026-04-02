package frc.lib.motor.tuning;

import frc.lib.motor.config.ControllerPreset;

/**
 * All configurable SysId analysis parameters in one place.
 *
 * <p>UI layers can populate these before each SysId run without redeploying.
 */
public class SysIdParams {
  // LQR parameters
  public double maxControlEffort = 7.0;     // volts
  public double maxVelocityError = 0.0;     // rot/s (0 = auto-compute from kS/kV)
  public double maxPositionError = 0.5;     // rotations

  // Preprocessing
  public int medianWindowSize = 3;          // WPILib default median filter window
  public double velocityThreshold = 0.0;    // 0 = auto-compute from noise floor
  public double testDurationSeconds = 10.0; // per-test duration

  // Controller timing (auto-set from ControllerPreset)
  public double measurementDelay = 0.0;     // seconds
  public double controllerPeriod = 0.001;   // seconds
  public double outputConversionFactor = 1.0;

  public SysIdParams() {}

  /** Creates a params object seeded from the controller preset. */
  public static SysIdParams defaultsFor(ControllerPreset preset) {
    SysIdParams params = new SysIdParams();
    params.measurementDelay = preset.measurementDelaySeconds;
    params.controllerPeriod = preset.periodSeconds;
    params.outputConversionFactor = preset.outputConversionFactor;
    return params;
  }
}
