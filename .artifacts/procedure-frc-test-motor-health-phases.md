---
id: frc-test-motor-health-phases
artifact_kind: memory
memory_class: procedural
enforceability: preferred
schema_version: 2
title: Motor_System MotorHealthTest — five-phase health-test sequence + Grade A/B >=80 score
created: 2026-06-23T19:00:00Z
updated: 2026-06-23T19:00:00Z
author: claude
model: claude-opus-4-8
model_basis: confirmed
status: active
load_profile: scope_entry
scope: FRC-Test_Code
source_rel: FRC-Test_Code\.context.md
tags: [frc, motor, health-test, motor-system, testing]
---

# Motor_System MotorHealthTest — five-phase health-test sequence + Grade A/B >=80 score

> Motor_System's `MotorHealthTest` characterizes a motor through five ordered phases (BREAKAWAY -> STARTUP -> STEADY_STATE -> COAST_DOWN -> TEMP_MONITOR), producing a health score where >=80 grades A/B. A report is produced even on early button release.

## Context

Part of [[motor-system-frc-test]] — the active config-driven motor abstraction library. `MotorHealthTest` is the diagnostic routine that quantifies a motor's mechanical/electrical health so a bench operator can grade it before trusting it on a robot. Captured from `.changelog.md` 2026-01-31. This complements the Setup -> Tuning -> Testing DS-test workflow in the Motor_System profile; the health test is its own dedicated sequence.

## Observations

- [registry] Phase 1 BREAKAWAY — ramp 0–25% output to detect stiction (the breakaway point where the motor first overcomes static friction). #health-test
- [registry] Phase 2 STARTUP — resistance estimation via V/I (voltage over current). #health-test
- [registry] Phase 3 STEADY_STATE — velocity constant Kv = RPM/Volt. #health-test
- [registry] Phase 4 COAST_DOWN — measure deceleration after power-off; a 15s timeout was added (prevents a hang waiting for the motor to stop). #health-test
- [registry] Phase 5 TEMP_MONITOR — 45s post-test temperature monitoring; releasing the test button still produces a report (the report is not lost on early release). #health-test
- [registry] Scoring: health score >=80 = Grade A/B. #health-test
- [feedback] Robustness fixes from the same work: the COAST_DOWN 15s timeout and the "button release still produces a report" behavior both exist so the routine always terminates and always yields an artifact, rather than hanging or discarding partial results. #health-test

## Relations

- relates-to [[motor-system-frc-test]] (the library this health test lives in)
- relates-to [[motor-system-support-matrix]] (the fail-loud controller-support model in the same wrapper layer)
- relates-to [[frc-test-motor-rebuild-close-gotcha]] (the reconfiguration close() fix in the same Motor_System test subsystem)
- relates-to [[frc-team-3843]]
- relates-to [[wpilib-build-env]]
