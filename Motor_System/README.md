# Motor_System

Generic config-driven motor abstraction, setup, tuning, and characterization project for FRC mechanisms.

`Motor_System` is the source-of-truth project for the reusable motor stack in this repo. It now contains one bench app plus four internal libraries so changes can propagate into other projects without copying the whole stack by hand.

## Core Idea
- mechanism code talks to motor channels
- controller family, transport, and feedback source come from config
- if hardware changes, update config and re-run setup/tuning in `Test` mode
- higher-level mechanisms can compose multiple motor channels while each channel still tunes independently

## Support Model
- `supported`: controller/transport path is implemented and can be used normally
- `recognized_limited`: hardware is known, but only part of the path is implemented or useful
- `recognized_unimplemented`: hardware appears in config/metadata, but runtime support is intentionally blocked instead of silently falling back
- The current code fails loudly for unimplemented runtime combinations so unsupported hardware does not masquerade as a different controller path

## Current Runtime Coverage
- Strongest coverage: Phoenix 6 CAN, Phoenix 5 Talon SRX CAN, REV CAN, and PWM paths that use WPILib motor-control wrappers
- PWM without feedback stays open-loop/manual only
- PWM with external feedback supports software closed-loop and SysId
- Some legal controller families are recognized in metadata before their full vendor API support exists

## Support Model
- `supported`: implemented runtime path with the expected control/telemetry behavior
- `recognized but limited`: legal hardware/config path is known, but some telemetry or advanced control features are unavailable
- `recognized but unimplemented`: legal hardware/config path is named in config/metadata, but the wrapper does not implement that transport/API yet
- recognized but unimplemented paths should stay visible in the UI and report a clear unlock/unsupported reason instead of silently mapping to a different controller

## Workflow
- Put the robot in Driver Station `Test` mode.
- On the motor's `Setup` tab, configure limits, feedback wiring/scaling, current, and optional power telemetry, then use `Apply Setup`.
- On the motor's `Tuning` tab, arm the target motor, hold controller `A`, and run `SysId` or `Manual Run`.
- Use the `Testing` tab to verify telemetry, feedback state, control path, and live measured values.

## Main Pieces
- `motor-core`: `frc.lib.motor.config`, `frc.lib.motor.core`, `frc.lib.motor.feedback`
- `motor-tune`: `frc.lib.motor.tuning` and `frc.lib.motorsystem`
- `motor-test`: `frc.lib.motor.test`
- `motor-dashboard`: `frc.lib.motor.dashboard`
- `src/main/java/frc/robot`: bench app shell (`Robot`, `RobotContainer`)
- `src/main/deploy/motor-config.json`: runtime config and example setup
- `src/main/deploy/elastic-layout.json`: generated Elastic layout

## Reuse Direction
- Within this repo, prefer depending on the internal library modules instead of copying `_common/motor_system`.
- Future robot or test projects can consume `motor-core`, `motor-tune`, `motor-test`, and `motor-dashboard` as shared modules while keeping their own `RobotContainer` and subsystem code.

## Current Priorities
- finish the library-boundary cleanup so `motor-tune` stops owning dashboard concerns
- keep expanding the legal controller/motor lists and support matrix without pretending unsupported paths work
- port reusable health/spec checks from `Motor_Test_backup` into `motor-test`

## Build
```powershell
$env:JAVA_HOME='C:\Users\Public\wpilib\2026\jdk'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
./gradlew generateDashboard build
```

## Related Docs
- [`NOTES.md`](NOTES.md)
- [`TUNING_GUIDE.md`](TUNING_GUIDE.md)
- [`TUNING_STRATEGY.md`](TUNING_STRATEGY.md)
