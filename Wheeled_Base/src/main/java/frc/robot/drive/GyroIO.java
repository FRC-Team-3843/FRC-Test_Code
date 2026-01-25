package frc.robot.drive;

import edu.wpi.first.math.geometry.Rotation2d;

public interface GyroIO {
  Rotation2d getRotation();

  void reset();
}
