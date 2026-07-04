---
id: motor-system-support-matrix
title: Motor_System support matrix — supported/recognized_limited/recognized_unimplemented failure-loud pattern
schema_version: 2
created: 2026-06-14T12:30:00Z
updated: 2026-06-14T12:30:00Z
valid_until: null
author: claude
session: null
tags: [frc, motor, architecture, lore, testing]
aliases: [motor support matrix, fail loud pattern, motor controller support levels, recognized unimplemented]
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
artifact_kind: memory
memory_class: procedural
enforceability: preferred
scope: FRC-Test_Code
load_profile: on_demand
---

# Motor_System support matrix — supported/recognized_limited/recognized_unimplemented failure-loud pattern

> Motor_System uses a three-tier support model for motor controller paths; unimplemented paths fail loudly instead of silently mapping to a different controller — a hard rule to prevent hardware masquerade during hardware testing.

## Context

Defined in `Motor_System/README.md` and enforced in the wrapper layer. Applied whenever a new controller family or transport path is scaffolded but not fully implemented.

## Support tiers

| tier | meaning |
|---|---|
| `supported` | controller/transport path implemented; normal control + telemetry behavior expected |
| `recognized_limited` | hardware known; only part of the path is implemented or useful (e.g., some telemetry unavailable) |
| `recognized_unimplemented` | hardware appears in config/metadata but runtime support is intentionally blocked; surfaces a clear unlock/unsupported reason in the UI |

## Observations

- [lore] The `recognized_unimplemented` tier exists specifically to prevent a controller from silently masquerading as a different type — a hard failure is the intended behavior, not a bug. #safety
- [lore] Unimplemented paths should still appear in the UI with a clear unlock reason; they must not be hidden or silently remapped. #ux
- [lore] AdvantageKit vendordep is scaffolded (present in vendordeps) but has no Java imports as of 2026-03, so it falls in the `recognized_unimplemented` category at the dependency level. #reference

## Open Questions

- None; pattern is stable and documented.

## Relations

- relates-to [[motor-system-frc-test]] (this pattern is specific to Motor_System's wrapper architecture)
- relates-to [[frc-test-code-source]] (repo this pattern lives in)
