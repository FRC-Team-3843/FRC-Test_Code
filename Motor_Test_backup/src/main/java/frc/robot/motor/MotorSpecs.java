package frc.robot.motor;

public final class MotorSpecs {
  private MotorSpecs() {}

  public static double expectedFreeSpeedRps(MotorKind kind, double gearRatio) {
    if (kind.isServo()) {
      return 0.0;
    }
    double rpm = kind.getFreeSpeedRpm();
    if (rpm <= 0.0) {
      return 0.0;
    }
    return (rpm / 60.0) / Math.max(gearRatio, 1e-6);
  }

  public static double expectedFreeCurrent(MotorKind kind) {
    return kind.getFreeCurrent();
  }

  public static double expectedStallCurrent(MotorKind kind) {
    return kind.getStallCurrent();
  }
}
