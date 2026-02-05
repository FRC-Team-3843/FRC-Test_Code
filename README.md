# FRC-Test_Code

FRC Team 3843 - Test Code

> **Documentation Guide:**
> - **This file (README):** Repository overview and project listing
> - **NOTES.md:** Cross-project setup notes and testing workflow
> - **STANDARDS.md:** Test-specific coding standards (extends FRC-2026\STANDARDS.md)

## Overview

Standalone hardware validation projects for FRC Team 3843. Each project is self-contained and focused on testing specific drive systems or components.

**Purpose:**
- Validate motor controller configurations
- Test drive base implementations
- Provide reference implementations for common drive systems
- Support hardware debugging and troubleshooting

---

## Test Projects

### Motor_Test
**Universal Motor Health & Control Utility**

Tests any motor controller (SparkMax, TalonFX, TalonSRX) with real-time diagnostics.

**Features:**
- Controller/motor type selection via Elastic Dashboard
- Multiple control modes (percent, position, velocity, current)
- Automated health test ("The Grader")
- WPILib DataLog integration
- JSON AI report generation

**Use Cases:**
- Validate new motor controllers
- Diagnose motor issues
- Test PID configurations
- Verify encoder functionality

[View Documentation →](Motor_Test/README.md)

---

### Swerve_Base
**YAGSL Swerve Drive Reference Implementation**

Base swerve chassis project built on YAGSL for testing swerve drive systems.

**Features:**
- Field-relative swerve driving
- YAGSL JSON-based configuration
- PathPlanner autonomous integration
- Choreo trajectory support (optional)
- PhotonVision pose estimation (optional)
- Verbose telemetry for validation

**Use Cases:**
- Validate swerve module configurations
- Test drive kinematics
- Calibrate absolute encoders
- Tune swerve PID loops

[View Documentation →](Swerve_Base/README.md)

---

### Mecanum_Base
**Mecanum Drive Reference Implementation**

Base mecanum chassis project for testing omnidirectional drive systems.

**Features:**
- Field-centric and robot-centric modes
- Multi-motor controller support (CAN and PWM)
- IO-layer abstraction for hardware flexibility
- Optional gyro and encoders
- PathPlanner and Choreo support
- Configurable via JSON

**Use Cases:**
- Validate mecanum drive configurations
- Test wheel inversions
- Calibrate mecanum kinematics
- Tune drive control

[View Documentation →](Mecanum_Base/README.md)

---

### Wheeled_Base
**Tank/Arcade Drive Reference Implementation**

Base tank/arcade chassis project for testing differential drive systems.

**Features:**
- Arcade and tank drive modes
- Multi-motor controller support (CAN and PWM)
- IO-layer abstraction for hardware flexibility
- Optional gyro and encoders
- PathPlanner and Choreo support
- Configurable via Constants

**Use Cases:**
- Validate differential drive configurations
- Test tank/arcade control modes
- Calibrate wheel encoders
- Tune drive PID

[View Documentation →](Wheeled_Base/README.md)

---

### _common
**Shared Code Library**

Canonical implementations of common classes copied into test projects.

**Contents:**
- Motor abstraction layer (UniversalMotor, MotorFactory)
- CAN and PWM motor wrappers
- Motor configuration builders
- Shared utility classes

**Workflow:**
- Copy files from `_common` into projects (not imported)
- Update `_common` first when modifying
- Document changes in `_common/README.md`

[View Documentation →](_common/README.md)

---

## Quick Start

1. **Choose a test project** based on your hardware
2. **Navigate to project directory**
   ```bash
   cd Motor_Test
   # or Swerve_Base, Mecanum_Base, Wheeled_Base
   ```
3. **Update configuration**
   - For Motor_Test: Configure via Elastic Dashboard after deployment
   - For drive bases: Edit JSON configs in `src/main/deploy/` or Constants.java
4. **Deploy to robot**
   ```bash
   ./gradlew deploy
   ```
5. **Test with Driver Station**
   - Enable teleop mode
   - Start with low power values
   - Monitor telemetry on dashboard

See [NOTES.md](NOTES.md) for detailed testing workflow and troubleshooting.

---

## Build Commands

Run from a specific project directory:

```bash
# Build the project
./gradlew build

# Deploy to RoboRIO
./gradlew deploy

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

**Team Number:** 3843 (configured in `.wpilib/wpilib_preferences.json`)

---

## Software Stack

- **WPILib:** 2026.1.1+
- **Java:** 17
- **Framework:** Command-Based Architecture
- **Vendor Libraries:**
  - REVLib 2025+ (SparkMax/NEO)
  - Phoenix6 (TalonFX - non-Pro features only)
  - Phoenix5 (TalonSRX)
  - YAGSL (Swerve drive)
  - PathPlanner (Autonomous)
  - PhotonLib (Vision - optional)

---

## Vendor Notes (2026)

- **Phoenix6:** Allowed, but Phoenix Pro features are NOT used by this team
- **Phoenix Replay:** May be used for analysis (does not require Pro subscription)
- **YAGSL:** Used for swerve drive configuration and control
- **REVLib:** Modern API with config objects (2025+)

---

## Documentation Structure

Each test project has:
- **README.md** - Project overview, hardware requirements, usage
- **NOTES.md** - Setup checklist, configuration notes, common issues
- **Agent Config Files** - CLAUDE.md, GEMINI.md, AGENTS.md (redirect to repo-level)

---

## Contributing

When adding or modifying test projects:
1. Follow [STANDARDS.md](STANDARDS.md) for test-specific coding rules
2. Refer to [FRC-2026 STANDARDS.md](../FRC-2026/STANDARDS.md) for common standards
3. Document all hardware requirements and CAN IDs
4. Add pre-deployment checklist to NOTES.md
5. Log changes to `.agent-log/changelog.md`

---

**Last Updated:** 2026-01-26
