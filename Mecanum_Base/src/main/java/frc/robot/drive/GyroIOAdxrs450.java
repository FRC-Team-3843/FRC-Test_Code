package frc.robot.drive;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.ADXRS450_Gyro;

public class GyroIOAdxrs450 implements GyroIO {
  private final ADXRS450_Gyro gyro = new ADXRS450_Gyro();

  @Override
  public Rotation2d getRotation() {
    return Rotation2d.fromDegrees(gyro.getAngle());
  }

  @Override
  public void reset() {
    gyro.reset();
  }
}
