package frc.robot.drive;

public interface UniversalMotor {
  void setVoltage(double volts);

  void setVelocityRps(double rps);

  void setPositionRotations(double rotations);

  double getVelocityRps();

  double getPositionRotations();

  void setBrake(boolean brake);

  void stop();
}
