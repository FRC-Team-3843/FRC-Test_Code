package frc.robot.drive;

import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;

public interface DriveIO {
  void setVoltages(double leftVolts, double rightVolts);

  void setWheelSpeeds(DifferentialDriveWheelSpeeds speeds);

  DifferentialDriveWheelSpeeds getWheelSpeeds();

  double[] getWheelPositionsMeters();

  void resetEncoders();

  void stop();

  void setBrake(boolean brake);
}
