# Swerve_Base - Gemini Configuration

This is a project-level redirect file.

## Configuration Hierarchy

When working in the Swerve_Base project, Gemini should read configurations in this order:

1. **Root coordination protocol:** `C:\github\GEMINI.md`
   - Cross-agent coordination rules
   - Activity logging format
   - Handoff procedures

2. **Repository configuration:** `C:\github\FRC-Test_Code\GEMINI.md`
   - Test project guidelines
   - Hardware test focus
   - API usage standards
   - Gemini-specific workflow for test projects

3. **This project:**
   - Swerve_Base is a base swerve drive test project
   - 2026 command-based template with vendor libraries
   - Intended for flexible motor and sensor selection

## Quick Reference

- **Activity log:** `C:\github\FRC-Test_Code\.agent-log\changelog.md`
- **Handoffs:** `C:\github\FRC-Test_Code\.agent-log\handoffs.md`
- **Repo rules:** `C:\github\FRC-Test_Code\GEMINI.md`
- **Root protocol:** `C:\github\GEMINI.md`

## Swerve_Base Project Details

**Purpose:** Base swerve project for hardware validation and calibration.

**Key Features:**
- 2026 command-based architecture
- Vendor libraries preloaded
- Designed for motor/sensor selection and calibration

**When working here:**
- Document CAN IDs and wiring requirements
- Use current swerve-related APIs
- Keep tests minimal and focused
- Log test results to changelog

---

For complete instructions, read the files listed above in order.
