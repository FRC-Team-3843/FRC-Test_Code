# FRC-Test_Code - Shared Agent Protocol

**Root coordination protocol:** `C:\GitHub\PROTOCOL.md`
**Technical standards:** `STANDARDS.md` (in this directory)
**Project context:** `.agent-context.md` (in this directory)

---

## How to Use This Configuration

This file contains the shared multi-agent protocol for the FRC-Test_Code repository. All agents (Claude, Gemini, Codex) follow the same workflow below. Agent-specific configs (CLAUDE.md, GEMINI.md, AGENTS.md) contain only logging tags.

**Purpose:** This repo contains standalone hardware test projects for validating FRC subsystem components and control loops.

---

## Before Starting Work

1. **Read `FRC-Test_Code\.agent-log\changelog.md` by direct path** - Review recent changes by all agents
2. **Read `FRC-Test_Code\.agent-log\handoffs.md` by direct path** - Check for pending tasks or blockers
3. **Read `STANDARDS.md`** - Technical rules for this repository
4. **Read `.agent-context.md`** - Current project status and notes
5. **Identify which test project** you are working in and document required hardware setup

## During Work

- Keep tests minimal and focused on a single hardware component
- Document wiring requirements, CAN IDs, and expected behavior in code comments
- Use current motor APIs (check `C:\GitHub\FRC-2026\STANDARDS.md` for latest APIs)
- Each test project is self-contained and independent
- Deploy to RoboRIO with `./gradlew deploy` from the project directory

## After Completing Work

1. **Log to `.agent-log\changelog.md`** using this format:
   ```
   ### [YYYY-MM-DD HH:MM] <TAG> [ACTION_TYPE]
   - Test: Which component/feature tested
   - Results: Findings and validation status
   - Repo: FRC-Test_Code
   - Files: <paths from repo root>
   - Notes: Hardware requirements, CAN IDs, wiring
   ```

2. **Action types:** `[IMPLEMENT]`, `[REFACTOR]`, `[FIX]`, `[TEST]`, `[CONFIG]`, `[DOCS]`, `[REVIEW]`

3. **If leaving work incomplete:** Update `.agent-log\handoffs.md` with task status, what was completed, what is pending, hardware blockers, and which agent should continue.

4. **Agent log files are append-only** - always Read first, then add your entry while preserving existing content.

---

## Cross-Agent Protocol

- **No Duplication:** Check changelog before implementing new features
- **Clear Handoffs:** Use handoffs.md when leaving incomplete work
- **Log Everything:** Better to over-communicate than under-communicate
- **Respect Standards:** Follow `STANDARDS.md` strictly
- **Preserve Logs:** Never overwrite changelog or handoffs - always append

---

## Repository Structure

```
FRC-Test_Code/
├── PROTOCOL.md (this file)    <- Shared agent protocol
├── STANDARDS.md               <- Technical standards
├── .agent-context.md          <- Project context and status
├── CLAUDE.md                  <- Claude logging config
├── GEMINI.md                  <- Gemini logging config
├── AGENTS.md                  <- Codex logging config
├── .agent-log/
│   ├── changelog.md           <- All activity and test results
│   └── handoffs.md            <- Task handoffs
│
├── Mecanum_Base/              <- Mecanum drive test platform
├── Motor_System/              <- Motor controller testing
├── Motor_Test_backup/         <- Motor test backup/archive
├── Swerve_Base/               <- Swerve drive test platform
├── Wheeled_Base/              <- Standard wheeled drive test platform
└── _common/                   <- Shared code (copy, not import)
```

---

## Key Reminders

- **Minimal and focused** - One test per hardware component
- **Document everything** - Future users need to know setup requirements
- **Current APIs** - Use latest motor APIs when creating new tests
- **Log test results** - Record findings in changelog
- **Hardware requirements** - Note what is needed to run each test
- **Root protocol** - For full cross-agent coordination details, see `C:\GitHub\PROTOCOL.md`
