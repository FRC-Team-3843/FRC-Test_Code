---
id: frc-test-critical-rules
artifact_kind: memory
memory_class: procedural
enforceability: mandatory
schema_version: 2
title: FRC-Test_Code critical rules — JDK17 from sub-project dir, _common copy-not-import, controller hazards, backup/secrets guards
created: 2026-06-23T19:00:00Z
updated: 2026-06-23T19:00:00Z
author: claude
model: claude-opus-4-8
model_basis: confirmed
status: active
load_profile: scope_entry
scope: FRC-Test_Code
source_rel: FRC-Test_Code\.protocol.md
tags: [frc, rules, build, jdk17, pathplanner, secrets]
---

# FRC-Test_Code critical rules — JDK17 from sub-project dir, _common copy-not-import, controller hazards, backup/secrets guards

> The always-on enforceable guards for FRC-Test_Code. These are RULES (hard guards), not explanations — violate one and a build fails, hardware misbehaves, or path-following breaks.

## Context

These are the `@always` critical rules from FRC-Test_Code's local protocol — the high-cost mistakes that recur in a multi-sub-project hardware-test repo. Spec authoritative for `§` refs: `C:\GitHub\.plans\agent-context-system-design.md`.

## Observations

- [constraint] Build from the specific sub-project directory and always set JAVA_HOME to WPILib JDK 17: `JAVA_HOME="C:/Users/Public/wpilib/2026/jdk" ./gradlew build` run from inside the sub-project (e.g. `cd Motor_System && ...`). System Java is Java 8 and WILL fail the build. #build ([[wpilib-build-env]])
- [constraint] `_common` is COPY-not-import: update `_common` FIRST, then propagate the change to all consuming sub-projects and verify each builds. NEVER import from `_common` directly. #architecture (full rationale + class list in [[frc-test-common-copy-pattern]] — this is the enforceable guard, that artifact is the explanation)
- [constraint] `Motor_Test_backup` is historical backup ONLY — use `Motor_System` for any new motor-abstraction work. #reference
- [constraint] Wheeled_Base's PathPlanner controller is `PPLTVController` (differential/tank drive), NOT `PPHolonomicDriveController`. Mixing these up breaks path following. #pathplanner (hazard: holonomic controller on a differential base produces incorrect trajectory tracking)
- [constraint] No secrets or credentials live in this repo. No secret-scan exceptions. #security

## Relations

- relates-to [[frc-test-common-copy-pattern]] (the _common copy-not-import pattern explained in full)
- relates-to [[frc-test-local-workflow]] (the broader local workflow these guards sit inside)
- relates-to [[frc-test-subprojects]] (Motor_Test_backup / Wheeled_Base / Motor_System context)
- relates-to [[wpilib-build-env]] (JDK 17)
- relates-to [[frc-team-3843]]
