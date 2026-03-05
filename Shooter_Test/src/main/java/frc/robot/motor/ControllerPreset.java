package frc.robot.motor;

/**
 * Per-controller timing and output characteristics for accurate SysId analysis and LQR tuning.
 *
 * <p>These values are sourced from WPILib's SysId tool and represent the control loop period,
 * measurement delay, and output conversion factor for each supported motor controller.
 *
 * <p>The measurement delay is critical for latency-compensated LQR feedback gain computation.
 * The output conversion factor converts voltage-domain gains to the controller's native units.
 */
public enum ControllerPreset {
  PHOENIX6(0.001, 0.001, 1.0),
  PHOENIX5(0.001, 0.0815, 1023.0 / 12.0),
  SPARK_MAX(0.001, 0.112, 1.0 / 12.0),
  SPARK_FLEX(0.001, 0.112, 1.0 / 12.0);

  /** Control loop period in seconds. */
  public final double periodSeconds;

  /** Measurement delay in seconds (sensor → controller latency). */
  public final double measurementDelaySeconds;

  /**
   * Factor to convert voltage-domain gains to the controller's native output units.
   * Phoenix 6 uses volts (1.0), Phoenix 5 uses 1023-scale, REV uses duty cycle (1/12).
   */
  public final double outputConversionFactor;

  ControllerPreset(double periodSeconds, double measurementDelaySeconds,
                   double outputConversionFactor) {
    this.periodSeconds = periodSeconds;
    this.measurementDelaySeconds = measurementDelaySeconds;
    this.outputConversionFactor = outputConversionFactor;
  }

  /**
   * Returns the appropriate preset for a given controller type.
   *
   * @param type the motor controller type
   * @return the matching preset (defaults to PHOENIX6 for unknown types)
   */
  public static ControllerPreset fromControllerType(ControllerType type) {
    switch (type) {
      case TALON_FX:
      case TALON_FXS:
        return PHOENIX6;
      case TALON_SRX:
        return PHOENIX5;
      case SPARK_MAX:
        return SPARK_MAX;
      case SPARK_FLEX:
        return SPARK_FLEX;
      default:
        return PHOENIX6;
    }
  }
}
