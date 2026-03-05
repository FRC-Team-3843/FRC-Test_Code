# Shooter_Test - Claude Configuration

This is a project-level redirect file.

## Configuration Hierarchy

When working in the Shooter_Test project, Claude should read configurations in this order:

1. **Root coordination protocol:** `C:\github\CLAUDE.md`
   - Cross-agent coordination rules
   - Activity logging format
   - Handoff procedures

2. **Repository configuration:** `C:\github\FRC-Test_Code\CLAUDE.md`
   - Test project guidelines
   - Hardware test focus
   - API usage standards
   - Claude-specific workflow for test projects

3. **This project:**
   - Universal motor system with config-driven motor creation
   - On-robot SysId matching WPILib's desktop tool
   - Automatic Elastic Dashboard layout generation
   - Clean 2026 command-based template

## Quick Reference

- **Activity log:** `C:\github\FRC-Test_Code\Shooter_Test\.agent-log\changelog.md`
- **Handoffs:** `C:\github\FRC-Test_Code\.agent-log\handoffs.md`
- **Repo rules:** `C:\github\FRC-Test_Code\CLAUDE.md`
- **Root protocol:** `C:\github\CLAUDE.md`

## Shooter_Test Project Details

**Purpose:** Universal motor test bench with on-robot SysId, config-driven motor creation, and automatic dashboard generation

**Architecture:**
- `motor-config.json` defines all motors, setpoints, servo, and button bindings
- `MotorSystemConfig` loads config and creates motors dynamically via `MotorFactory`
- `ShooterSubsystem` is a generic motor system subsystem (works for any mechanism)
- `DashboardGenerator` creates `elastic-layout.json` from config at startup
- `SysIdAnalyzer` runs WPILib-matching characterization on-robot
- `ControllerPreset` provides per-controller timing for accurate LQR/SysId

**Default Hardware (from motor-config.json):**
- Kraken X44 (Preshooter): CAN ID 20, TalonFX
- Kraken X60 (MainShooter): CAN ID 21, TalonFX
- PWM Servo: Channel 0

**Key Features:**
- JSON-configured motor system (any number of motors, any controller type)
- On-robot SysId with acceleration-based OLS, median filter, dynamic trimming
- Hot-reload PID tuning with save-to-JSON persistence
- Automatic Elastic Dashboard layout generation
- SysId results auto-applied and persisted to config
- Servo control with named positions

---

## 🔥 CRITICAL: AI-Driven PID Tuning Workflow

**This project uses AI-driven iterative tuning that leverages Claude's neural network intelligence.**

**BEFORE doing ANY tuning work, you MUST read:**
```
Read: C:/GitHub/FRC-Test_Code/Shooter_Test/TUNING_STRATEGY.md
```

**Key points:**
- ✅ Use your actual intelligence to analyze real data
- ✅ Form specific hypotheses based on patterns
- ✅ Design custom tests to validate theories
- ❌ DO NOT write hardcoded tuning rules
- ❌ DO NOT use generic formulas without understanding data

**The workflow:**
1. User provides CSV data from performance tests
2. You analyze with pattern recognition
3. You form hypotheses about root causes
4. You suggest next test parameters in test-config.json
5. Iterate until goals met

**When user says "analyze the data" or "let's tune":**
- Read TUNING_STRATEGY.md immediately
- Read the CSV data file
- Use intelligence, not rules
- Provide context-aware recommendations

---

## Standard Work Guidelines

**When working here:**
- Document PID tuning results in changelog
- Note any hardware-specific observations
- Use current motor APIs (check FRC-2026 standards)
- Log test results and velocity control performance

---

For complete instructions, read the files listed above in order.
