// COPY THIS FILE: Update package declaration when copying to your project
// Example packages: frc.robot.drive, frc.robot.motor
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
