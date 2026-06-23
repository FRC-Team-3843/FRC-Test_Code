---
id: frc-test-config-driven-decisions
artifact_kind: reference
schema_version: 2
title: FRC-Test_Code config-driven evolution — Shooter_Test refactor + JSON motor/gyro config standardization
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
tags: [frc, config-driven, json-config, shooter, gyro, changelog]
---

# FRC-Test_Code config-driven evolution — Shooter_Test refactor + JSON motor/gyro config standardization

> The Jan–Mar 2026 arc that moved FRC-Test_Code drive/shooter projects to runtime JSON configuration: motor+gyro config standardized across the bases (2026-01-31), then Shooter_Test fully config-driven with PID save-back (2026-03-01). These are engineering facts/decisions captured from the changelog, not new design choices.

## Context

Last recorded commit-level work in FRC-Test_Code was 2026-03-01. The repo's direction over early 2026 was to push hardware-specific values out of compiled Java and into deploy-time JSON, so a board, motor type, or PID set can change without a recompile. Captured from `.changelog.md` 2026-01-31 / 2026-03-01.

## Observations

- [registry] 2026-03-01 — Shooter_Test refactored to a config-driven approach: all motor types, controller types, CAN bus, PID values, and button bindings now flow from `shooter-config.json` via `ShooterConfigLoader`; `applyPidConfig()` saves tuned PID back to the JSON. #shooter (source: .changelog.md 2026-03-01)
- [registry] 2026-01-31 — JSON-based motor and gyro config standardized across Mecanum_Base and Wheeled_Base; a shared `GyroConfig` / `GyroConfigLoader` system added to `_common`. #json-config (source: .changelog.md 2026-01-31)
- [registry] Net effect: hardware config (CAN IDs, motor params, PID, gyro) belongs in `src/main/deploy/*.json` loaded at runtime by the `*ConfigLoader` classes — not hardcoded in Constants.java. This is the convention that the IO-abstraction and `_common` patterns assume. #json-config

## Relations

- relates-to [[frc-test-architecture-patterns]] (the JSON config-loading pattern this evolution produced)
- relates-to [[frc-test-common-copy-pattern-202606120639]] (GyroConfig/GyroConfigLoader landed in _common; the deploy-JSON convention)
- relates-to [[frc-test-motor-rebuild-close-gotcha]] (the 2026-01-30 reconfiguration fix from the same period)
- relates-to [[motor-system-frc-test]] (Motor_System's own config-driven dashboard generation)
- relates-to [[frc-team-3843]]
