---
id: motor-system-frc-test
title: Motor_System (FRC-Test_Code) — config-driven multi-module motor abstraction library
schema_version: 2
created: 2026-06-14T12:30:00Z
updated: 2026-06-14T12:30:00Z
valid_until: null
author: claude
session: null
tags: [frc, robotics, motor, library, gradle, wpilib]
aliases: [motor system, motor-system, frc motor abstraction, motor-core, motor-tune, motor-test, motor-dashboard]
status: active
supersedes: null
confidence: 55
source_basis: document
human_edited: false
sensitivity: normal
decisions: []
model: claude-sonnet-4-6
model_basis: confirmed
provenance:
  harvest: deterministic
  recall-extract: claude-sonnet-4-6
  find-missing: claude-sonnet-4-6
  precision-judge: claude-sonnet-4-6
lifecycle: paused
artifact_kind: memory
memory_class: semantic
semantic_kind: entity_profile
---

# Motor_System (FRC-Test_Code) — config-driven multi-module motor abstraction library

> The canonical generic motor abstraction for FRC-Test_Code: a Gradle multi-module project (motor-core, motor-tune, motor-test, motor-dashboard) that abstracts controller family, transport, and feedback source behind a config-driven interface; replaces the older `_common/motor_system` snapshot.

## Context

Lives at `C:\GitHub\FRC-Test_Code\Motor_System`. Structured as a multi-module Gradle project with four internal library modules and a bench-app shell:

- `motor-core` — `frc.lib.motor.config`, `frc.lib.motor.core`, `frc.lib.motor.feedback`
- `motor-tune` — `frc.lib.motor.tuning`, `frc.lib.motorsystem`
- `motor-test` — `frc.lib.motor.test`
- `motor-dashboard` — `frc.lib.motor.dashboard`, `DashboardGenerator`
- `src/main/java/frc/robot/` — bench app shell (`Robot`, `RobotContainer`)

Runtime config flows from `src/main/deploy/motor-config.json`; Elastic dashboard layout is generated from config via `DashboardGenerator` (`generateDashboard` Gradle task — run before deploy). Build: `JAVA_HOME` must point to WPILib JDK 17 (see [[wpilib-build-env]]).

**Vendor deps (active):** Phoenix 6, Phoenix 5, REVLib, PhotonLib, PathPlanner, ChoreoLib, Redux, Thrifty, Studica, URCL, AdvantageKit (scaffolded — not yet imported in Java source as of 2026-03).

## Observations

- [registry] Multi-module Gradle layout: bench app at root, four library modules as `:motor-core`, `:motor-tune`, `:motor-test`, `:motor-dashboard` — internal dependencies only, no external module publish. #architecture
- [registry] `generateDashboard` Gradle task runs `DashboardGenerator` before deploy to generate `elastic-layout.json` from `motor-config.json`. #build
- [registry] Strongest runtime coverage: Phoenix 6 CAN, Phoenix 5 Talon SRX CAN, REV CAN, PWM with/without external feedback. #motor
- [decision] `_common/motor_system` is a legacy snapshot of an older iteration; `Motor_System` internal library modules are the current canonical path. `Motor_Test_backup` is archive only. #architecture
- [decision] Workflow: DS Test mode → Setup tab (limits, feedback wiring) → Tuning tab (arm motor, hold A, run SysId or Manual Run) → Testing tab (verify telemetry). #testing

## Open Questions

- AdvantageKit vendordep is present in all modules but not imported in Java source — unclear if planned for future use or scaffolded accidentally.
- `motor-tune` owns some dashboard concerns (boundary cleanup listed as active priority in README); may refactor.

## Relations

- relates-to [[frc-test-code-source-202606120639]] (sub-project of FRC-Test_Code repo)
- relates-to [[frc-test-common-copy-pattern-202606120639]] (Motor_System replaces _common/motor_system snapshot)
- relates-to [[wpilib-build-env]] (requires JDK 17)
- relates-to [[frc-team-3843]] (team 3843 project)
