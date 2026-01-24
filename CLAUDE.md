# FRC-Test_Code Repository - Claude Configuration

## How to Use This Configuration

This file is for Claude-specific behavior when working in the FRC-Test_Code repository.

**Purpose:** This repo contains standalone hardware test projects for validating components and control loops.

---

## Test Project Guidelines

This repo does NOT have a STANDARDS.md file because each test project is self-contained and focused on hardware validation.

**General Principles:**
- Keep tests minimal and focused on a single hardware component
- Use current motor APIs (verify with latest season standards)
- Document hardware requirements, CAN IDs, and wiring
- Each test project is independent

---

## Claude-Specific Behavior

### When Working in FRC-Test_Code

1. **Before starting work:**
   - Check `.agent-log\changelog.md` for recent activity
   - Identify which test project you're working in
   - Document required hardware setup and CAN IDs

2. **During work:**
   - Keep tests minimal and focused
   - Document wiring requirements, CAN IDs, and expected behavior in code comments
   - Use current motor APIs (check FRC-2026 STANDARDS.md for latest APIs)
   - Each test project is self-contained

3. **After completing work:**
   - Log changes to `.agent-log\changelog.md`
   - Use this format:
     ```
     ### [YYYY-MM-DD HH:MM] CLAUDE [ACTION_TYPE]
     - Test: Which component/feature tested
     - Results: Findings and validation status
     - Files: <paths from repo root>
     - Notes: Hardware requirements, CAN IDs, wiring
     ```

### Claude Workflow Tips

- **Hardware Focus:** Tests are designed for hardware validation, not simulation
- **Documentation:** Always document required setup in comments or README
- **Minimal Code:** Keep test projects simple and focused
- **API Updates:** When creating new tests, use current motor APIs from latest season
- **Testing:** Suggest deploying to RoboRIO with `./gradlew deploy` from project directory

---

## Cross-Agent Protocol

### Activity Logging

**Location:** `.agent-log\changelog.md`

**Before work:** Check changelog for recent changes by other agents (Gemini, Codex).
**After work:** Log all test results and findings with `[CLAUDE]` tag.

### Handoff Tracking

**Location:** `.agent-log\handoffs.md`

If you leave work incomplete or encounter blockers:
1. Update handoffs.md with task status
2. Note what was completed and what's pending
3. Document hardware availability or setup blockers
4. Suggest which agent should continue (or mark as `ANY`)

---

## Repository Structure

```
FRC-Test_Code/
├── CLAUDE.md (this file)      ← Claude behavior for repo
├── GEMINI.md                  ← Gemini behavior for repo
├── AGENTS.md                  ← Codex behavior for repo
├── .agent-log/
│   ├── changelog.md           ← All activity and test results
│   └── handoffs.md            ← Task handoffs
│
├── ServoTest/                 ← Servo hardware test
│   ├── CLAUDE.md              ← Project-level redirect
│   ├── GEMINI.md              ← Project-level redirect
│   ├── AGENTS.md              ← Project-level redirect
│   ├── src/main/java/...
│   └── vendordeps/
│
├── SparkMaxPIDTest/           ← SparkMax PID tuning test
│   ├── CLAUDE.md              ← Project-level redirect
│   ├── GEMINI.md              ← Project-level redirect
│   ├── AGENTS.md              ← Project-level redirect
│   ├── src/main/java/...
│   └── vendordeps/
│
└── TallonPIDTest/             ← TalonFX PID tuning test
    ├── CLAUDE.md              ← Project-level redirect
    ├── GEMINI.md              ← Project-level redirect
    ├── AGENTS.md              ← Project-level redirect
    ├── src/main/java/...
    └── vendordeps/
```

---

## Key Reminders

- **Minimal and focused** - One test per hardware component
- **Document everything** - Future users need to know setup requirements
- **Current APIs** - Use latest motor APIs when creating new tests (check FRC-2026\STANDARDS.md)
- **Log test results** - Record findings in changelog
- **Hardware requirements** - Note what's needed to run each test

---

For cross-agent coordination protocol, see: `C:\github\CLAUDE.md`
