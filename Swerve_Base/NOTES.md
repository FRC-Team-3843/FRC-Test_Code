# Notes - Swerve_Base

## Before Deploying
- Verify all CAN IDs in `src/main/deploy/swerve/modules/*.json`.
- Update `absoluteEncoderOffset` values per module.
- Confirm module locations match your chassis dimensions.
- Ensure the gyro type in `src/main/deploy/swerve/swervedrive.json` matches hardware.
- Review `physicalproperties.json` for wheel diameter and gear ratios.
- Update `Constants.DriveConstants.MAX_SPEED` to match the chosen module ratio.

## PathPlanner Setup
- Run PathPlanner GUI once and update `src/main/deploy/pathplanner/settings.json`.
- Add autos/paths under `src/main/deploy/pathplanner/autos` and `paths`.
- Keep `Constants.AutoConstants.AUTO_MODE = PATHPLANNER` to use AutoBuilder.

## Choreo Setup
- Export Choreo trajectories into `src/main/deploy` per Choreo docs.
- Set `Constants.AutoConstants.AUTO_MODE = CHOREO`.
- If Choreo APIs change, update `frc.robot.auto.ChoreoAutos`.

## Vision Setup
- Place AprilTag layout JSON in `src/main/deploy/apriltag_layout.json`.
- Add camera entries in `Vision.Cameras` and set the PhotonVision camera name.
- Set `Constants.VisionConstants.ENABLE_VISION = true` once hardware is ready.

## Logging
- Logging is intentionally verbose for base chassis testing.
- Before using this as a competition robot base, reduce logging:
  - Set `Constants.LoggingConstants.ENABLE_SWERVE_TELEMETRY = false`.
  - Consider disabling DataLog in `Robot.java`.
