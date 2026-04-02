package frc.robot.motor.core;

import frc.robot.motor.config.MotorConfiguration;

public final class MotorFactory {
  private MotorFactory() {}

  public static UniversalMotor create(MotorConfiguration config) {
    if (config.controllerType == ControllerType.PWM_SERVO) {
      return new PwmServoWrapper(config);
    }
    if (config.controllerType == ControllerType.PWM_MOTOR) {
      return new PwmMotorWrapper(config);
    }
    return new CanMotorWrapper(config);
  }
}
