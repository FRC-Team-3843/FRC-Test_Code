---
id: frc-test-motor-rebuild-close-gotcha
artifact_kind: memory
memory_class: procedural
enforceability: mandatory
schema_version: 2
title: FRC motor reconfiguration gotcha — close() the old motor before rebuilding (CANSparkMax "instance already created")
created: 2026-06-23T19:00:00Z
updated: 2026-06-23T19:00:00Z
author: claude
model: claude-opus-4-8
model_basis: confirmed
status: active
load_profile: scope_entry
scope: FRC-Test_Code
source_rel: FRC-Test_Code\.context.md
tags: [frc, motor, gotcha, revlib, cansparkmax, reconfiguration]
---

# FRC motor reconfiguration gotcha — close() the old motor before rebuilding (CANSparkMax "instance already created")

> When rebuilding a motor object on reconfiguration, you MUST `close()` the old instance before constructing the new one — otherwise REVLib throws "CANSparkMax instance already created" on the same CAN ID. Sharp reusable lesson from Motor_System's `MotorTestSubsystem.rebuildMotor()`.

## Context

Motor_System lets a bench operator change motor hardware/config at runtime (a key reason it is config-driven). That means the subsystem reconstructs motor objects live, on the same CAN ID, while the robot code keeps running. REVLib enforces single-instance-per-device, so reconstruction without releasing the prior handle collides. Captured from `.changelog.md` 2026-01-30; lives in `MotorTestSubsystem.rebuildMotor()`.

## Observations

- [constraint] In `rebuildMotor()` (and any live motor-reconfiguration path), call `close()` on the OLD motor instance BEFORE creating the new one. #motor (skipping this throws "CANSparkMax instance already created" on reconfiguration)
- [feedback] Generalizes beyond REVLib/CANSparkMax: any vendor controller object holding a device handle should be released before a same-device re-instantiation. The single-instance constraint is per CAN device, so a leaked handle blocks the rebuild even with identical config. #motor
- [registry] This is the kind of bug that only appears on the SECOND configure (first build succeeds, reconfigure fails) — easy to miss in a single-shot test, surfaces immediately in the config-driven runtime-swap workflow. #motor

## Relations

- relates-to [[motor-system-frc-test]] (MotorTestSubsystem lives here)
- relates-to [[frc-test-config-driven-decisions]] (runtime reconfiguration is why this gotcha exists)
- relates-to [[frc-test-motor-health-phases]] (same Motor_System test subsystem)
- relates-to [[frc-team-3843]]
