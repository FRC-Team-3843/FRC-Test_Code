# Mecanum_Base

Base mecanum chassis project built on WPILib 2026.

## What This Project Does
- Provides configurable mecanum drive with field-centric option.
- Supports multiple motor controllers (CAN and PWM) through a common IO layer.
- Optional gyro, optional encoders, optional vision pose updates.
- PathPlanner auto hookup and a Choreo hook for quick enable.
- Verbose logging for chassis testing (trim for competition robots).

## Quick Start
1. Set motor types, IDs, and inversion in `src/main/java/frc/robot/Constants.java`.
2. Set wheelbase/track width and wheel diameter for odometry math.
3. Decide on gyro type (or disable gyro).
4. Deploy and drive with the Xbox controller.

## Configuration Highlights
- `Constants.DriveConstants` defines drive geometry, motor configs, and sensor usage.
- `DriveIOMecanum` converts wheel speeds to motor setpoints.
- `MecanumDriveSubsystem` handles kinematics and optional odometry.

## Vision Integration (Optional)
- Add camera entries in `frc.robot.vision.Vision.Cameras`.
- Provide an AprilTag layout JSON at `src/main/deploy/apriltag_layout.json`.
- Toggle `Constants.VisionConstants.ENABLE_VISION` when ready.

## PathPlanner + Choreo
- PathPlanner autos live in `src/main/deploy/pathplanner`.
- Choreo autos are loaded by name via `frc.robot.auto.ChoreoAutos`.

## Logging
Logging is intentionally verbose for base chassis testing. For a competition robot,
reduce or disable logging in `Constants.LoggingConstants`.
