package frc.robot.motor;

public final class MotorFactory {
  private MotorFactory() {}

  public static UniversalMotor create(MotorConfiguration config) {
    if (config.controllerType == ControllerType.PWM_SERVO) {
      return new PwmServoWrapper(config);
    }
    // TODO: Add PWM motor support if needed
    return new CanMotorWrapper(config);
  }
}
