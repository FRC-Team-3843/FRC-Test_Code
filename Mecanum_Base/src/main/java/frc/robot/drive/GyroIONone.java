package frc.robot.drive;

import edu.wpi.first.math.geometry.Rotation2d;

public class GyroIONone implements GyroIO {
  @Override
  public Rotation2d getRotation() {
    return new Rotation2d();
  }

  @Override
  public void reset() {
  }
}
