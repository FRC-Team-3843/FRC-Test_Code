package frc.robot.drive;

import frc.robot.Constants.DriveConstants.MotorControllerType;

public final class MotorFactory {
  private MotorFactory() {}

  public static UniversalMotor createMotor(MotorConfig config) {
    MotorControllerType type = config.controllerType;
    switch (type) {
      case SPARK_MAX:
      case SPARK_FLEX:
      case TALON_FX:
      case TALON_FXS:
      case TALON_SRX:
        return new CanMotorWrapper(config);
      case PWM_SPARKMAX:
      case PWM_TALONSRX:
      case PWM_VICTORSPX:
        return new PwmMotorWrapper(config);
      default:
        throw new IllegalArgumentException("Unknown motor type: " + type);
    }
  }
}
