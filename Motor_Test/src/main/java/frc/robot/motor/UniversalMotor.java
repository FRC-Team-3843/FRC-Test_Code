package frc.robot.motor;

public interface UniversalMotor {
  enum Mode {
    DUTY_CYCLE,
    VOLTAGE,
    VELOCITY,
    POSITION,
    CURRENT,
    SMART_MOTION
  }

  void setControlMode(Mode mode);

  // _common methods
  void setVoltage(double volts);
  void setVelocityRps(double rps);
  void setPositionRotations(double rotations);
  double getVelocityRps();
  double getPositionRotations();
  default double getVelocity() { return getVelocityRps(); }
  default double getPosition() { return getPositionRotations(); }
  void setBrake(boolean brake);
  void stop();
  
  // Motor_Test specific methods
  Mode getControlMode();
  void set(double value); // Generic set based on mode?
  double getCurrent();
  double getTemperature();
  double getHealthScore();
  void setHealthScore(double score);
  String getDeviceName();
  boolean isServo();
}
