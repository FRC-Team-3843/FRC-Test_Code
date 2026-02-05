package frc.robot;

import frc.robot.drive.MotorConfig;

public final class Constants {
  private Constants() {}

  public static final class OperatorConstants {
    public static final int DRIVER_CONTROLLER_PORT = 0;
    public static final double DEADBAND = 0.12;
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

    public enum GyroType {
      NONE,
      ADIS16470,
      ADXRS450,
      PIGEON2
    }

    public static final double TRACK_WIDTH_METERS = 0.6;
    public static final double WHEELBASE_METERS = 0.6;
    public static final double WHEEL_DIAMETER_METERS = 0.1524; // 6 in
    public static final double WHEEL_CIRCUMFERENCE_METERS = WHEEL_DIAMETER_METERS * Math.PI;

    public static final double DRIVE_GEAR_RATIO = 10.71;
    public static final double MAX_WHEEL_SPEED_MPS = 3.0;

    public static final boolean USE_GYRO = true;
    public static final GyroType GYRO_TYPE = GyroType.ADIS16470;
    public static final int GYRO_CAN_ID = 0;

    public static final boolean USE_WHEEL_ENCODERS = true;
    public static final boolean USE_CLOSED_LOOP = false;
    public static final boolean FIELD_CENTRIC_DEFAULT = true;

    public static final String MOTOR_CONFIG_FILE = "motor-config.json";
  }

  public static final class AutoConstants {
    public enum AutoMode {
      NONE,
      PATHPLANNER,
      CHOREO
    }

    public static final boolean ENABLE_AUTO = true;
    public static final AutoMode AUTO_MODE = AutoMode.PATHPLANNER;

    public static final String PATHPLANNER_DEFAULT_AUTO = "ExampleAuto";
    public static final String CHOREO_DEFAULT_AUTO = "ExampleChoreo";

    public static final double TRANSLATION_P = 5.0;
    public static final double TRANSLATION_I = 0.0;
    public static final double TRANSLATION_D = 0.0;

    public static final double ROTATION_P = 5.0;
    public static final double ROTATION_I = 0.0;
    public static final double ROTATION_D = 0.0;
  }

  public static final class VisionConstants {
    public static final boolean ENABLE_VISION = false;
    public static final String APRILTAG_LAYOUT_FILE = "apriltag_layout.json";
    public static final double MAX_LATENCY_SECONDS = 0.25;
  }

  public static final class LoggingConstants {
    public static final boolean ENABLE_LOGGING = true;
    public static final boolean ENABLE_DRIVE_TELEMETRY = true;
  }

  public static final class DrivebaseConstants {
    public static final double WHEEL_LOCK_TIME = 10.0;
  }
}
