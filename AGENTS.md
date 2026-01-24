# FRC-Test_Code Repository - Codex Configuration

## How to Use This Configuration

This file is for Codex-specific behavior when working in the FRC-Test_Code repository.

**Purpose:** This repo contains standalone hardware test projects for validating components and control loops.

---

## Codex-Specific Behavior

### When Working in FRC-Test_Code

1. **Before starting work:**
   - Check `.agent-log\changelog.md` for recent activity
   - Identify which test project you're working in
   - Document required hardware setup and CAN IDs

2. **During work:**
   - Keep tests minimal and focused on a single hardware component
   - Document wiring requirements, CAN IDs, and expected behavior
   - Use current motor APIs (verify with latest season standards)
   - Each test project is self-contained

3. **After completing work:**
   - Log changes to `.agent-log\changelog.md`
   - Use this format:
     ```
     ### [YYYY-MM-DD HH:MM] CODEX [ACTION_TYPE]
     - Test: Which component/feature tested
     - Results: Findings and validation status
     - Files: <paths from repo root>
     - Notes: Hardware requirements, CAN IDs, wiring
     ```

### Codex Workflow Tips

- **Hardware Focus:** Tests are designed for hardware validation, not simulation
- **Documentation:** Always document required setup in comments or README
- **Minimal Code:** Keep test projects simple and focused
- **API Updates:** When creating new tests, use current motor APIs from latest season

---

## Cross-Agent Protocol

### Activity Logging

**Location:** `.agent-log\changelog.md`

**Before work:** Check changelog for recent changes by other agents (Claude, Gemini).
**After work:** Log all test results and findings with `[CODEX]` tag.

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
├── AGENTS.md (this file)      ← Codex behavior for repo
├── .agent-log/
│   ├── changelog.md           ← All activity and test results
│   └── handoffs.md            ← Task handoffs
│
├── ServoTest/                 ← Servo hardware test
│   ├── src/main/java/...
│   └── vendordeps/
│
├── SparkMaxPIDTest/           ← SparkMax PID tuning test
│   ├── src/main/java/...
│   └── vendordeps/
│
└── TallonPIDTest/             ← TalonFX PID tuning test
    ├── src/main/java/...
    └── vendordeps/
```

---

## Build Commands

Run from specific test project directory:
```bash
./gradlew build          # Compile and package
./gradlew deploy         # Deploy to RoboRIO for hardware testing
./gradlew simulateJava   # Run simulation (if desktop support enabled)
./gradlew test           # Run JUnit 5 tests (if applicable)
```

---

## Test Project Guidelines

### Creating New Tests

1. Create self-contained GradleRIO project
2. Use current motor APIs (check latest season standards)
3. Document in code comments:
   - Required hardware and wiring
   - CAN IDs used
   - Expected behavior
   - Validation criteria

### Running Tests

1. Verify hardware is wired correctly
2. Check CAN IDs match code
3. Deploy to RoboRIO
4. Observe behavior and log results
5. Document findings in changelog

### Test Documentation

Each test should document:
- **Purpose:** What component or behavior is being tested
- **Hardware:** Required devices and wiring
- **CAN IDs:** All CAN devices used
- **Expected Behavior:** What should happen
- **Validation:** How to verify correct operation

---

## Key Reminders

- **Minimal and focused** - One test per hardware component
- **Document everything** - Future users need to know setup requirements
- **Current APIs** - Use latest motor APIs when creating new tests
- **Log test results** - Record findings in changelog
- **Hardware requirements** - Note what's needed to run each test

---

For cross-agent coordination protocol, see: `C:\github\AGENTS.md`
