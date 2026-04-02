package frc.lib.motor.core;

import frc.lib.motor.config.MotorConfiguration;

public final class MotorFactory {
  private MotorFactory() {}

  public static UniversalMotor create(MotorConfiguration config) {
    if (config.controllerType == ControllerType.UNSUPPORTED) {
      return new UnsupportedMotorWrapper(config);
    }
    if (config.controllerType == ControllerType.PWM_SERVO) {
      return new PwmServoWrapper(config);
    }
    if (config.controllerType == ControllerType.PWM_MOTOR) {
      return new PwmMotorWrapper(config);
    }
    return new CanMotorWrapper(config);
  }
}
