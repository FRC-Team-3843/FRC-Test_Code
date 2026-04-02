# FRC-Test_Code

FRC Team 3843 test and reference projects.

## Active Projects

### Motor_System
Generic config-driven motor abstraction, setup, tuning, and characterization project.

Use it when you want:
- controller-family and transport abstraction
- external feedback support
- per-motor setup and tuning in DS `Test`
- on-robot SysId and generated Elastic dashboard layouts
- shared internal libraries for other test/robot projects

[View Documentation ->](Motor_System/README.md)

### Swerve_Base
YAGSL-based swerve drive reference implementation.

[View Documentation ->](Swerve_Base/README.md)

### Mecanum_Base
Mecanum drive reference implementation.

[View Documentation ->](Mecanum_Base/README.md)

### Wheeled_Base
Tank/arcade drive reference implementation.

[View Documentation ->](Wheeled_Base/README.md)

### _common
Copyable shared-code snapshots.

- `_common/motor_system` is now a legacy snapshot. Prefer the internal library modules under `Motor_System`.

[View Documentation ->](_common/README.md)

## Retired / Backup
- `Motor_Test_backup` remains in the repo as historical backup material and is not the active motor abstraction path.

## Build
Run Gradle commands from the specific project directory.

```bash
cd Motor_System
./gradlew build
```

## Notes
- See [NOTES.md](NOTES.md) for cross-project setup notes.
- See [STANDARDS.md](STANDARDS.md) for repo standards.
