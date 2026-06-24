---
id: frc-test-subprojects
artifact_kind: reference
schema_version: 2
title: FRC-Test_Code sub-projects — Motor_System, Swerve/Mecanum/Wheeled bases, _common, Motor_Test_backup
created: 2026-06-23T19:00:00Z
updated: 2026-06-23T19:00:00Z
author: claude
model: claude-opus-4-8
model_basis: confirmed
status: active
load_profile: scope_entry
scope: FRC-Test_Code
source_rel: FRC-Test_Code\.context.md
path: C:\GitHub\FRC-Test_Code
tags: [frc, repo-structure, swerve, mecanum, tank, motor]
---

# FRC-Test_Code sub-projects — Motor_System, Swerve/Mecanum/Wheeled bases, _common, Motor_Test_backup

> The map of FRC-Test_Code: standalone, independently-deployable test projects (one Gradle build each) plus the copyable `_common` library and the archived `Motor_Test_backup`. Each sub-project is self-contained and may use different hardware configs.

## Context

FRC-Test_Code holds standalone hardware-validation test projects and reusable drive-base reference implementations for [[frc-team-3843]]. Repository purpose: validate motor-controller configurations, test drive-base implementations (swerve/mecanum/tank+arcade), provide reference implementations for common drive systems, and support hardware debugging/troubleshooting. Java 17, WPILib 2026 command-based. Each project is independent and deploys separately (`cd <SubProject> && ./gradlew deploy`).

## Observations

- [registry] **Motor_System** — ACTIVE motor abstraction library: config-driven setup, SysId, Elastic dashboard layout generation; multi-module Gradle (motor-core/motor-tune/motor-test/motor-dashboard). Replaces the older Motor_Test (now `Motor_Test_backup`). Detailed in [[motor-system-frc-test]]. #motor
- [registry] **Swerve_Base** — YAGSL-based swerve drive reference implementation, with PathPlanner + PhotonVision scaffold. #swerve
- [registry] **Mecanum_Base** — Mecanum drive reference implementation: IO abstraction, JSON motor/gyro config. #mecanum
- [registry] **Wheeled_Base** — Tank/arcade (standard wheeled) drive reference: IO abstraction, JSON motor/gyro config; uses `PPLTVController` for PathPlanner, NOT `PPHolonomicDriveController` (see [[frc-test-critical-rules]]). #tank
- [registry] **_common** — copyable canonical snapshots of shared code (motor + gyro abstraction layer), COPIED not imported into projects. `_common/motor_system` is a LEGACY snapshot — prefer the Motor_System internal library modules. Pattern in [[frc-test-common-copy-pattern]]. #shared
- [registry] **Motor_Test_backup** — archive/historical backup of the older Motor_Test utility; not the active path. Use Motor_System. #archive
- [registry] Open item: PathPlanner autos/paths directories in the drive-base projects still need actual trajectories populated (source: .changelog.md 2026-01-25). #status

## Relations

- relates-to [[motor-system-frc-test]] (Motor_System internals — do not re-explain here)
- relates-to [[frc-test-common-copy-pattern]] (_common copy-not-import)
- relates-to [[frc-test-architecture-patterns]] (the IO-abstraction + JSON-config + telemetry patterns these bases share)
- relates-to [[frc-test-code-source]] (repo orientation)
- relates-to [[frc-team-3843]]
- relates-to [[wpilib-build-env]]
