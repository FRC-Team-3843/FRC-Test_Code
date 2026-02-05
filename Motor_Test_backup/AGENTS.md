# Motor_Test - Codex Configuration

This is a project-level redirect file.

## Configuration Hierarchy

When working in the Motor_Test project, Codex should read configurations in this order:

1. **Root coordination protocol:** `C:\github\AGENTS.md`
   - Cross-agent coordination rules
   - Activity logging format
   - Handoff procedures

2. **Repository configuration:** `C:\github\FRC-Test_Code\AGENTS.md`
   - Test project guidelines
   - Hardware test focus
   - API usage standards
   - Codex-specific workflow for test projects

3. **This project:**
   - Motor_Test is a standalone motor hardware test project
   - Clean 2026 command-based template
   - Focused on motor control validation

## Quick Reference

- **Activity log:** `C:\github\FRC-Test_Code\.agent-log\changelog.md`
- **Handoffs:** `C:\github\FRC-Test_Code\.agent-log\handoffs.md`
- **Repo rules:** `C:\github\FRC-Test_Code\AGENTS.md`
- **Root protocol:** `C:\github\AGENTS.md`

## Motor_Test Project Details

**Purpose:** Hardware validation for motor controllers (SparkMax, TalonFX, etc.)

**Key Features:**
- 2026 command-based architecture
- Focused motor control testing
- Hardware validation and tuning

**When working here:**
- Document CAN IDs and wiring requirements
- Use current motor APIs (check FRC-2026 standards)
- Keep tests minimal and focused
- Log test results to changelog

---

For complete instructions, read the files listed above in order.
