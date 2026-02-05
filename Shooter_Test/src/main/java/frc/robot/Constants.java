package frc.robot;

public final class Constants {
  private Constants() {}

  public static final class OperatorConstants {
    public static final int DRIVER_CONTROLLER_PORT = 0;
    public static final double SAFE_ENABLE_DEADBAND = 0.2;
  }

  public static final class ShooterConstants {
    // CAN IDs
    public static final int PRESHOOTER_CAN_ID = 20;
    public static final int MAIN_SHOOTER_CAN_ID = 21;

    // Gear Ratios
    public static final double PRESHOOTER_GEAR_RATIO = 1.0;
    public static final double MAIN_SHOOTER_GEAR_RATIO = 1.0;

    // PID Values - Preshooter (Kraken X44)
    public static final double PRESHOOTER_KP = 0.1;
    public static final double PRESHOOTER_KI = 0.0;
    public static final double PRESHOOTER_KD = 0.0;
    public static final double PRESHOOTER_KV = 0.12;
    public static final double PRESHOOTER_KS = 0.0;

    // PID Values - Main Shooter (Kraken X60)
    public static final double MAIN_SHOOTER_KP = 0.1;
    public static final double MAIN_SHOOTER_KI = 0.0;
    public static final double MAIN_SHOOTER_KD = 0.0;
    public static final double MAIN_SHOOTER_KV = 0.12;
    public static final double MAIN_SHOOTER_KS = 0.0;

    // RPM Setpoints
    public static final double PRESHOOTER_SETPOINT_1_RPM = 6500.0;
    public static final double PRESHOOTER_SETPOINT_2_RPM = 3250.0;
    public static final double MAIN_SHOOTER_SETPOINT_1_RPM = 5500.0;
    public static final double MAIN_SHOOTER_SETPOINT_2_RPM = 2750.0;

    // Servo Configuration
    public static final int SERVO_PWM_CHANNEL = 0;
    public static final double SERVO_POSITION_1 = 0.5;
    public static final double SERVO_POSITION_2 = 1.0;

    // Velocity Tolerance
    public static final double VELOCITY_TOLERANCE_RPM = 50.0;
  }

  public static final class LoggingConstants {
    // Enable WPILib DataLog manager for test data logging
    public static final boolean ENABLE_MOTOR_LOGGING = true;
  }
}
