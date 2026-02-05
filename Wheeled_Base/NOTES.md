# Working Notes - Wheeled_Base

> **Documentation Guide:**
> - **This file (NOTES):** Setup, tuning, troubleshooting, TODOs
> - **README.md:** Project overview and quick start
> - **STANDARDS.md:** Coding standards (see C:\GitHub\FRC-Test_Code\STANDARDS.md)

## Before Deploying
- Confirm motor controller types and IDs in `Constants.DriveConstants`.
- Verify inversion on each motor.
- Update track width and wheel diameter.
- Set `USE_WHEEL_ENCODERS` to false for brushed motors without sensors.
- Choose drive mode: ARCADE or TANK.
- Choose the correct gyro or disable it if not present.

## PathPlanner Setup
- Run PathPlanner GUI and update `src/main/deploy/pathplanner/settings.json`.
- Add autos/paths under `src/main/deploy/pathplanner/autos` and `paths`.
- Differential drive PathPlanner setup is a placeholder; wire in a proper diff config when needed.

## Choreo Setup
- Export Choreo trajectories into `src/main/deploy` per Choreo docs.
- Set `AUTO_MODE = CHOREO`.
- If Choreo APIs change, update `frc.robot.auto.ChoreoAutos`.

## Vision Setup
- Place AprilTag layout JSON in `src/main/deploy/apriltag_layout.json`.
- Add camera entries in `Vision.Cameras` with correct transforms.
- Enable vision with `Constants.VisionConstants.ENABLE_VISION`.

## Logging
- Logging is intentionally verbose for base chassis testing.
- Trim logs before using as a competition robot base:
  - Disable or reduce `Constants.LoggingConstants`.
  - Consider limiting SmartDashboard output in `TankDriveSubsystem`.
