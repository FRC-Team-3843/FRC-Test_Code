package frc.robot.drive;

import edu.wpi.first.math.kinematics.MecanumDriveWheelSpeeds;

public interface DriveIO {
  void setVoltages(double frontLeft, double rearLeft, double frontRight, double rearRight);

  void setWheelSpeeds(MecanumDriveWheelSpeeds speeds);

  MecanumDriveWheelSpeeds getWheelSpeeds();

  double[] getWheelPositionsMeters();

  void resetEncoders();

  void stop();

  void setBrake(boolean brake);
}
