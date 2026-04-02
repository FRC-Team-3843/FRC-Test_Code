# Working Notes - FRC-Test_Code

## Active Project Notes
- `Motor_System`: extracted generic motor abstraction, setup, tuning, characterization, and shared library project
- `Swerve_Base`: swerve reference chassis
- `Mecanum_Base`: mecanum reference chassis
- `Wheeled_Base`: tank/arcade reference chassis

## Cross-Project Setup
- Verify CAN IDs before deployment.
- Confirm motor controller firmware and vendordeps are current.
- Start with low output on first hardware test.
- Keep an accessible e-stop and secure the mechanism before characterization.

## Current Repo Direction
- `Motor_System` is now the source-of-truth generic motor project.
- `Motor_System` now contains the shared `motor-core`, `motor-tune`, `motor-test`, and `motor-dashboard` modules.
- `_common/motor_system` is now a legacy snapshot, not the preferred integration path.
- `Motor_Test_backup` is retained only as legacy backup material.
