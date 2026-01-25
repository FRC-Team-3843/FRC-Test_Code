package frc.robot.drive;

import com.ctre.phoenix6.hardware.Pigeon2;
import edu.wpi.first.math.geometry.Rotation2d;

public class GyroIOPigeon2 implements GyroIO {
  private final Pigeon2 pigeon;

  public GyroIOPigeon2(int canId) {
    pigeon = new Pigeon2(canId);
  }

  @Override
  public Rotation2d getRotation() {
    return Rotation2d.fromDegrees(pigeon.getYaw().getValueAsDouble());
  }

  @Override
  public void reset() {
    pigeon.setYaw(0.0);
  }
}
