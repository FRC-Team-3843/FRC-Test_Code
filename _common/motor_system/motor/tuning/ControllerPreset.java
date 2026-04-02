package frc.robot.motor.tuning;

import frc.robot.motor.config.ControllerFamily;
import frc.robot.motor.config.FeedbackSource;
import frc.robot.motor.config.MotorConfig;
import frc.robot.motor.config.TransportType;

/**
 * Controller timing presets used by the on-robot SysId analyzer.
 */
public enum ControllerPreset {
  PHOENIX6(0.001, 0.001, 1.0),
  PHOENIX5(0.001, 0.0815, 1023.0 / 12.0),
  SPARK_CAN(0.001, 0.112, 1.0 / 12.0),
  ROBORIO_SOFTWARE(0.02, 0.02, 1.0);

  public final double periodSeconds;
  public final double measurementDelaySeconds;
  public final double outputConversionFactor;

  ControllerPreset(double periodSeconds, double measurementDelaySeconds,
                   double outputConversionFactor) {
    this.periodSeconds = periodSeconds;
    this.measurementDelaySeconds = measurementDelaySeconds;
    this.outputConversionFactor = outputConversionFactor;
  }

  public static ControllerPreset from(MotorConfig config) {
    if (config.getTransportType() == TransportType.PWM) {
      return ROBORIO_SOFTWARE;
    }

    if (config.getFeedbackSource() != FeedbackSource.INTEGRATED) {
      return ROBORIO_SOFTWARE;
    }

    switch (config.getControllerFamily()) {
      case TALON_FX:
      case TALON_FXS:
        return PHOENIX6;
      case TALON_SRX:
        return PHOENIX5;
      case SPARK_MAX:
      case SPARK_FLEX:
        return SPARK_CAN;
      default:
        return ROBORIO_SOFTWARE;
    }
  }
}
