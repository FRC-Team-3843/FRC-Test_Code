package frc.robot.drive;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.ADIS16470_IMU;

public class GyroIOAdis16470 implements GyroIO {
  private final ADIS16470_IMU gyro = new ADIS16470_IMU();

  @Override
  public Rotation2d getRotation() {
    return Rotation2d.fromDegrees(gyro.getAngle());
  }

  @Override
  public void reset() {
    gyro.reset();
  }
}
