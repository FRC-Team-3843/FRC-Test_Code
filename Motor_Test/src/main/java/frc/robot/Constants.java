package frc.robot;

public final class Constants {
  private Constants() {}

  public static final class OperatorConstants {
    public static final int DRIVER_CONTROLLER_PORT = 0;
    public static final double SAFE_ENABLE_DEADBAND = 0.2;
  }

  public static final class DriveConstants {
    public enum MotorControllerType {
      SPARK_MAX,
      SPARK_FLEX,
      TALON_FX,
      TALON_FXS,
      TALON_SRX,
      PWM_SPARKMAX,
      PWM_VICTORSPX,
      PWM_TALONSRX
    }

    public enum MotorKind {
      BRUSHLESS,
      BRUSHED
    }
  }

  public static final class MotorConstants {
    // Default configuration values for motor testing
    public static final int DEFAULT_CAN_ID = 1;
    public static final int DEFAULT_PWM_CHANNEL = 0;
    public static final double DEFAULT_GEAR_RATIO = 1.0;
    public static final int DEFAULT_QUAD_CPR = 4096;
  }

  public static final class LoggingConstants {
    // Enable WPILib DataLog manager for test data logging
    public static final boolean ENABLE_MOTOR_LOGGING = true;
  }
}
