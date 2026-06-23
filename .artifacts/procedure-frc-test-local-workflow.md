---
id: frc-test-local-workflow
artifact_kind: memory
memory_class: procedural
enforceability: preferred
schema_version: 2
title: FRC-Test_Code local workflow — read-order, changelog logging, working dirs, build, docs, hardware-testing practices
created: 2026-06-23T19:00:00Z
updated: 2026-06-23T19:00:00Z
author: claude
model: claude-opus-4-8
model_basis: confirmed
status: active
load_profile: scope_entry
scope: FRC-Test_Code
source_rel: FRC-Test_Code\.protocol.md
tags: [frc, workflow, changelog, gradle, documentation, hardware-testing]
---

# FRC-Test_Code local workflow — read-order, changelog logging, working dirs, build, docs, hardware-testing practices

> The repo-local operating procedure for FRC-Test_Code: session read-order, during-work discipline, changelog format, scratch/research rules, per-sub-project build, per-test-project documentation requirements, and hardware-testing best-practices. This repo must remain usable on its own, without `C:\GitHub` existing.

## Context

FRC-Test_Code is a standalone, self-contained repo of hardware-validation and reference drive-base test projects for [[frc-team-3843]]. Universal directives (team-lead, read-on-entry, single-Read, [ACC] tag, interaction modes, Constraints) are inherited from the universal `.protocol.md`; only project-specific workflow lives here. Spec authoritative for `§` refs: `C:\GitHub\.plans\agent-context-system-design.md`. This repo must remain usable on its own — if broader coordination context is available, `C:\GitHub\.protocol.md` is the preferred top-level entry point, but it is optional.

## Observations

- [constraint] Read order at session start: (1) `.changelog.md`, (2) `.project-context.md`, (3) `.standards.md`, (4) `.protocol.md`. #workflow (Transitional: these monoliths are being decomposed into scope_entry artifacts — load_profile: scope_entry artifacts in `.artifacts/` are the v2 source of truth.)
- [constraint] During work: follow `.standards.md` (now decomposed into the reference/procedure artifacts here); check `.changelog.md` before overlapping work; keep `.project-context.md` (now the scope artifacts) current when state changes. #workflow
- [constraint] Build from the specific sub-project directory, each has its own Gradle build — `cd <SubProject> && ./gradlew build|deploy|test|clean` (e.g. `cd Motor_System && ./gradlew build`; `cd ../Swerve_Base && ./gradlew deploy`). There is no top-level multi-project build. #build (requires JDK 17 — see [[frc-test-critical-rules]] and [[wpilib-build-env]])
- [constraint] Log substantive work to repo-root `.changelog.md` (CURRENT path). Format: `### [YYYY-MM-DD HH:MM] AGENT_NAME [ACTION_TYPE]` + bullets `Description / Repo: FRC-Test_Code / Files modified (paths from repo root) / Notes / Why / PENDING (optional)`. Action types: `[IMPLEMENT] [REFACTOR] [FIX] [TEST] [CONFIG] [DOCS] [REVIEW]`. #changelog
- [constraint] The OLD `.standards.md` log path `.agent-log/changelog.md` (with a Test/Results/Files/Notes template) is SUPERSEDED — use repo-root `.changelog.md` per the format above. #changelog
- [constraint] Changelog is append-only: add your entry, preserve everything prior, never Write-replace. #changelog
- [constraint] Working files: `.scratch/` for temporary scripts, debug artifacts, working files; `.research/` for reference materials, external tools, research. Both are gitignored — never commit their contents. #workflow
- [constraint] Track incomplete work in the project context: `TODO` for actionable follow-up, `Pending Decisions` for unresolved choices. #workflow
- [constraint] Each test project MUST have: `README.md` (purpose/description, hardware requirements, CAN ID assignments, usage instructions), `NOTES.md` (pre-deployment checklist, configuration steps, PathPlanner/Choreo setup if applicable, vision setup if applicable, common troubleshooting), and agent config redirect files (CLAUDE.md/GEMINI.md/AGENTS.md pointing at repo-level configs). #documentation
- [constraint] Each test project MUST document required hardware (motors, sensors, controllers), CAN ID assignments, wiring requirements, and expected behavior. #documentation
- [feedback] Hardware-testing best-practices: (1) Start small — begin with low values when testing unknown motors; (2) Document everything — CAN IDs, wiring, findings; (3) Test incrementally — one component at a time; (4) Log results — use DataLog + console output; (5) Safety first — ensure secure mounting before testing. #hardware-testing
- [registry] Open status (no active sprint): last recorded work 2026-03-01. Verify with the user before starting any new test deployments. PathPlanner autos/paths directories in the drive-base projects still need actual trajectories populated. #status

## Relations

- relates-to [[frc-test-critical-rules]] (the @always enforceable guards — build/JDK, _common copy, controller hazards)
- relates-to [[frc-test-subprojects]] (what each sub-project is)
- relates-to [[frc-test-code-source-202606120639]] (repo orientation)
- relates-to [[wpilib-build-env]] (JDK 17 build requirement)
- relates-to [[frc-team-3843]]
