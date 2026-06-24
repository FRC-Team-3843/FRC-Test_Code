---
id: frc-test-architecture-patterns
artifact_kind: reference
schema_version: 2
title: FRC-Test_Code test architecture — verbose telemetry, enable-flags, JSON config-loading, IO abstraction
created: 2026-06-23T19:00:00Z
updated: 2026-06-23T20:30:00Z
author: claude
model: claude-opus-4-8
model_basis: confirmed
status: active
load_profile: scope_entry
scope: FRC-Test_Code
source_rel: FRC-Test_Code\.standards.md
path: C:\GitHub\FRC-Test_Code
tags: [frc, architecture, telemetry, json-config, io-abstraction, command-based]
---

# FRC-Test_Code test architecture — verbose telemetry, enable-flags, JSON config-loading, IO abstraction

> The technical patterns shared across FRC-Test_Code's test projects: same WPILib 2026 command-based framework as competition code but simplified, with verbose telemetry, feature enable-flags, runtime JSON hardware config (no recompile), and IO-abstraction interfaces for hardware swap + simulation. Extends FRC-2026 `.standards.md` with test-specific rules.

## Context

Test projects use the same WPILib 2026 command-based framework as competition code (FRC-2026 `.standards.md` is the common base) but with a simplified structure focused on the hardware under test. These patterns are what make the drive-base references rapidly tunable and re-targetable to different hardware. Common coding standards (naming, motor API reference, safety/current-soft-limits/timeouts, autonomous, controller bindings, brake management, Elastic dashboard default) live in FRC-2026 `.standards.md`; only the test-specific deltas are captured here.

## Observations

- [registry] Simplified test structure: minimal subsystems (focus on hardware under test), simplified commands (command factories for test sequences). #architecture
- [registry] Verbose telemetry: test projects enable extensive logging for hardware validation — this DIFFERS from competition code where telemetry is minimized for performance. Trim before any competition deployment. #telemetry
- [registry] Feature enable-flags in `Constants` gate all features. `LoggingConstants` example: `ENABLE_LOGGING`, `ENABLE_MOTOR_LOGGING` (Motor_Test_backup), `ENABLE_DRIVE_TELEMETRY` (drive bases). #telemetry
- [registry] Vision and auto features are DISABLED by default via Constants flags. #architecture
- [registry] JSON configuration pattern — rapid tuning without recompilation: config files live in `src/main/deploy` (e.g. `motor-config.json`); `MotorConfigLoader` reads them at runtime; `DriveIO` implementations load configs instead of hardcoding constants. Usage: `var configs = MotorConfigLoader.loadConfigs("motor-config.json"); frontLeft = MotorFactory.createMotor(configs.get("frontLeft"));` #json-config (put CAN IDs + motor params in deploy JSON, not Constants.java)
- [registry] IO abstraction pattern — drive bases use IO interfaces for hardware: `DriveIO` interface (`setVoltages(left,right)`, `getWheelPositionsMeters()`, ...) with concrete impl `DriveIOTank implements DriveIO` (holds `UniversalMotor leftMotor/rightMotor`); the subsystem (`TankDriveSubsystem extends SubsystemBase`) depends on the `DriveIO` interface, not the concrete class. #io-abstraction
- [registry] IO-abstraction benefits: hardware swappable without changing subsystem logic; supports simulation (add a `DriveIOSim` implementation); facilitates unit testing. #io-abstraction
- [registry] Gyro uses the same swappable-IO pattern as `DriveIO`: `GyroIO` interface (`getRotation()`, `reset()`) in Wheeled_Base `frc.robot.drive` with concrete impls `GyroIOPigeon2` (takes `canId`) / `GyroIOAdis16470` / `GyroIOAdxrs450` / `GyroIONone`, selected at runtime by `GyroConfigLoader.createGyroIO(GyroConfig)` from the `gyro` section of the deploy JSON (type PIGEON2/ADIS16470/ADXRS450/NONE; NONE or missing section → `GyroIONone`). Note: `inverted` is parsed into `GyroConfig` but not yet applied by the impls (TODO in `GyroConfigLoader`). #io-abstraction #json-config

## Relations

- relates-to [[frc-test-subprojects]] (the bases that use these patterns)
- relates-to [[frc-test-config-driven-decisions]] (how config-driven loading evolved over Jan–Mar 2026)
- relates-to [[frc-test-common-copy-pattern]] (MotorConfigLoader/GyroConfigLoader + the deploy-JSON convention)
- relates-to [[motor-system-frc-test]] (Motor_System's own config-driven dashboard generation)
- relates-to [[frc-team-3843]]
