package frc.robot;

public final class Constants {
  private Constants() {}

  public static final class OperatorConstants {
    public static final int DRIVER_CONTROLLER_PORT = 0;
    public static final int OPERATOR_CONTROLLER_PORT = 1;
    public static final double DEADBAND = 0.12;
  }

  public static final class DriveConstants {
    // Max speed in meters per second. Match your module config/gear ratio.
    public static final double MAX_SPEED = 4.5;
  }

  public static final class AutoConstants {
    public enum AutoMode {
      NONE,
      PATHPLANNER,
      CHOREO
    }

    // Global enable for auto. Set false to run the project without auto setup.
    public static final boolean ENABLE_AUTO = true;

    // Default auto system to use when ENABLE_AUTO is true.
    public static final AutoMode AUTO_MODE = AutoMode.PATHPLANNER;

    // Default path/auto names for each system.
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
    // Global enable for vision. Robot should run normally when disabled.
    public static final boolean ENABLE_VISION = false;

    // AprilTag layout JSON in src/main/deploy (set to your 2026 field layout).
    public static final String APRILTAG_LAYOUT_FILE = "apriltag_layout.json";

    // Maximum acceptable latency (seconds) before rejecting vision measurements.
    public static final double MAX_LATENCY_SECONDS = 0.25;
  }

  public static final class LoggingConstants {
    // Enable verbose logging for base chassis testing. Trim for comp robot use.
    public static final boolean ENABLE_LOGGING = true;

    // YAGSL telemetry verbosity. HIGH is very chatty.
    public static final boolean ENABLE_SWERVE_TELEMETRY = true;
  }

  public static final class DrivebaseConstants {
    // Time to hold brake mode after disable before switching to coast.
    public static final double WHEEL_LOCK_TIME = 10.0;
  }
}

