# Swerve_Base

Base swerve chassis project built on WPILib 2026 + YAGSL.

> **Documentation Guide:**
> - **This file (README):** Project overview and quick start
> - **NOTES.md:** Setup procedures, tuning values, troubleshooting
> - **STANDARDS.md:** Coding standards (see C:\GitHub\FRC-Test_Code\STANDARDS.md)

## What This Project Does
- Loads a YAGSL swerve config from `src/main/deploy/swerve`.
- Provides field-relative swerve driving with Xbox controller bindings.
- Integrates PathPlanner auto setup (when enabled).
- Provides a Choreo auto hook (reflection-based) for quick enable.
- Scaffolds PhotonVision-based pose estimation (optional).
- Enables verbose logging for test chassis work (trim for competition robots).

## Quick Start
1. Update the swerve JSON config files in `src/main/deploy/swerve`.
2. Pick your auto mode in `src/main/java/frc/robot/Constants.java`.
3. Deploy to the RoboRIO and drive with the controller.

## Configuration Highlights
- `Constants.AutoConstants` controls auto enable and system selection.
- `Constants.VisionConstants` controls vision enable and tag layout file.
- Swerve module motors, encoders, and gyro are set in the YAGSL JSON files.

## Vision Integration
- Add camera entries to `frc.robot.subsystems.swervedrive.Vision.Cameras`.
- Provide an AprilTag layout JSON at deploy path
  `src/main/deploy/apriltag_layout.json` (name must match `Constants`).
- Vision is fully optional. The robot runs without it.

## PathPlanner + Choreo
- PathPlanner autos are loaded from `src/main/deploy/pathplanner`.
- Choreo autos are loaded by name through `ChoreoAutos`. If API changes,
  update the reflection helper in `frc.robot.auto.ChoreoAutos`.

## Logging
This project enables verbose logging for chassis testing. For a full robot
project, reduce or disable logging in `Constants.LoggingConstants`.
