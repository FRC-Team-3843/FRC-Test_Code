package frc.robot.drive;

/**
 * Configuration record for gyroscope sensors.
 * Supports various gyro types commonly used in FRC.
 */
public final class GyroConfig {
  public final GyroType type;
  public final int canId;      // CAN ID for CAN-based gyros (0 for SPI gyros)
  public final boolean inverted;

  public GyroConfig(GyroType type, int canId, boolean inverted) {
    this.type = type;
    this.canId = canId;
    this.inverted = inverted;
  }

  /**
   * Supported gyro types for FRC robots.
   */
  public enum GyroType {
    NONE,        // No gyro present
    ADIS16470,   // Analog Devices ADIS16470 IMU (SPI)
    ADXRS450,    // Analog Devices ADXRS450 Gyro (SPI)
    PIGEON2      // CTRE Pigeon 2.0 IMU (CAN)
  }
}
