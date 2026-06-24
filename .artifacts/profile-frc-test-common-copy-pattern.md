---
id: frc-test-common-copy-pattern
title: FRC-Test_Code _common folder — copy-not-import pattern for shared motor code
schema_version: 2
created: 2026-06-12T06:39:00Z
updated: 2026-06-12T06:39:00Z
valid_until: null
author: claude
session: phase7-onboarding-20260612
tags: [frc, robotics, architecture, reference]
aliases: [frc-test-common-copy-pattern-202606120639, common folder, copy not import, motor abstraction pattern, shared frc code]
status: active
supersedes: null
confidence: 55
source_basis: conversation
human_edited: false
sensitivity: normal
decisions: []
artifact_kind: memory
memory_class: semantic
model: unattributed
model_basis: unattributed
scope: FRC-Test_Code
---

# FRC-Test_Code _common folder — copy-not-import pattern for shared motor code

> Shared motor and gyro abstractions live in `_common/` and are COPIED into each sub-project, never imported — this is an intentional design decision for competition reliability.

## Context

FRC competition robots can't depend on internet/external build infrastructure during events. The `_common/` pattern ensures each sub-project remains fully self-contained. (source: .standards.md _common section)

## Discussion

**Pattern:** `_common/motor/` holds canonical versions of `UniversalMotor`, `MotorFactory`, `CanMotorWrapper`, `PwmMotorWrapper`, `MotorConfig`. When updating a common class: update `_common` first, document the change in `_common/README.md`, then copy to all affected projects and test.

**Why copy, not import:** competition reliability — works without internet or external dependencies; project independence — each project has its own copy it controls; no build system complexity during hardware testing.

**Motor_System vs _common legacy snapshot:** `_common/motor_system` is a legacy snapshot of an older iteration. Current canonical motor abstraction is the internal library modules under `Motor_System/`. Don't extend the legacy snapshot — extend Motor_System.

**JSON config pattern:** test projects load motor and gyro configuration from `src/main/deploy/*.json` at runtime, avoiding recompilation to swap hardware. `MotorConfigLoader` + `GyroConfigLoader` handle deserialization. This was standardized across Mecanum_Base and Wheeled_Base in January 2026. (source: .changelog.md 2026-01-31)

## Observations

- [decision] `_common` workflow is copy-not-import for competition reliability #architecture (avoids external build dependencies at competition)
- [constraint] When modifying a common class, update `_common` FIRST, then propagate to all consuming projects #frc (divergent copies create confusing bugs)
- [constraint] `Motor_Test_backup` is historical backup — use Motor_System for any new work on motor abstractions #reference

## Open Questions

- [ ] None currently.

## Notes for Future Sessions

If spinning up a new FRC sub-project that needs motor abstractions, pull from Motor_System internal libraries (not `_common/motor_system`). JSON config files in deploy/ are the right place for CAN IDs and motor parameters — don't hardcode in Constants.java.

## Relations

[[frc-test-code-source]] [[frc-team-3843]] [[wpilib-build-env]]
