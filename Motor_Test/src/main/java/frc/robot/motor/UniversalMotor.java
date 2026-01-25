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

  Mode getControlMode();

  void set(double value);

  double getVelocity();

  double getPosition();

  double getCurrent();

  double getTemperature();

  double getHealthScore();

  void setHealthScore(double score);

  String getDeviceName();

  boolean isServo();

  default void stop() {
    set(0.0);
  }
}
