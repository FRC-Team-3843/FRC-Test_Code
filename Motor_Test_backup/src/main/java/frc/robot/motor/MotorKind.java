package frc.robot.motor;

public enum MotorKind {
  CIM(5300.0, 2.7, 131.0, false, true),
  NEO(5676.0, 1.8, 105.0, false, false),
  NEO_550(11000.0, 1.4, 100.0, false, false),
  KRAKEN(6000.0, 1.5, 366.0, false, false),
  SERVO(0.0, 0.0, 0.0, true, false),
  CONTINUOUS_SERVO(0.0, 0.0, 0.0, true, false),
  CUSTOM_BRUSHED(0.0, 0.0, 0.0, false, true),
  CUSTOM_BRUSHLESS(0.0, 0.0, 0.0, false, false);

  private final double freeSpeedRpm;
  private final double freeCurrent;
  private final double stallCurrent;
  private final boolean servo;
  private final boolean brushed;

  MotorKind(double freeSpeedRpm, double freeCurrent, double stallCurrent, boolean servo, boolean brushed) {
    this.freeSpeedRpm = freeSpeedRpm;
    this.freeCurrent = freeCurrent;
    this.stallCurrent = stallCurrent;
    this.servo = servo;
    this.brushed = brushed;
  }

  public double getFreeSpeedRpm() {
    return freeSpeedRpm;
  }

  public double getFreeCurrent() {
    return freeCurrent;
  }

  public double getStallCurrent() {
    return stallCurrent;
  }

  public boolean isServo() {
    return servo;
  }

  public boolean isBrushed() {
    return brushed;
  }

  public boolean isBrushless() {
    return !servo && !brushed;
  }
}
