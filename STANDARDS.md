# FRC-Test_Code Standards

## Common Standards Reference

For all common coding standards, see: **[FRC-2026 STANDARDS.md](../FRC-2026/STANDARDS.md)**

This includes:
- Naming conventions (m_ prefix, PascalCase, etc.)
- Motor API reference (SparkMax, TalonFX, etc.)
- Command-based architecture
- Safety standards (current limits, soft limits, timeouts)
- Logging & telemetry
- Vision integration
- Autonomous standards
- Controller bindings
- Brake management pattern

**This document contains only test-specific standards** that differ from or extend the common standards.

---

## Repository Purpose

This repository contains **standalone hardware validation test projects**. Each project is self-contained and focused on testing specific drive systems or components.

**Purpose:**
- Validate motor controller configurations
- Test drive base implementations (swerve, mecanum, tank/arcade)
- Provide reference implementations for common drive systems
- Support hardware debugging and troubleshooting

**Java Version:** 17
**Framework:** WPILib 2026 Command-Based

---

## Project Structure

### Test Project Types

1. **Motor_Test** - Universal motor controller validation utility
2. **Swerve_Base** - YAGSL swerve drive reference implementation
3. **Mecanum_Base** - Mecanum drive reference implementation
4. **Wheeled_Base** - Tank/arcade drive reference implementation

Each project is **independent** and can be deployed separately.

---

## Common Classes (_common Folder)

### Purpose

The `_common` folder contains **canonical implementations** of shared code that is **copied** (not imported) into projects.

### Why Copy Instead of Import?

- **Competition Reliability**: Works without internet/external dependencies
- **Project Independence**: Each project remains self-contained
- **Simplicity**: No build system complexity during hardware testing

### Workflow

1. **Using Common Classes**: Copy files from `_common` into your project's appropriate package
2. **Modifying Common Classes**:
   - Update `_common` FIRST
   - Document the change in `_common/README.md`
   - Copy updated version to all projects using it
   - Test in all affected projects

### Current Common Classes

- **Motor Abstraction Layer** (`_common/motor/`)
  - `UniversalMotor.java` - Common interface for all motor types
  - `MotorFactory.java` - Factory for creating motor instances
  - `CanMotorWrapper.java` - CAN motor wrapper
  - `PwmMotorWrapper.java` - PWM motor wrapper
  - `MotorConfig.java` - Motor configuration builder

---

## Test-Specific Architecture Notes

Test projects follow the same Command-Based Framework as competition code (see FRC-2026 STANDARDS.md), but with simplified structure:

- **Minimal subsystems** - Focus on hardware under test
- **Verbose telemetry** - Enable extensive logging for validation
- **Simplified commands** - Use command factories for test sequences
- **Enable flags** - All features (vision, logging) controlled by Constants flags

---

## IO Abstraction Pattern

Drive bases use IO abstraction interfaces for hardware:

### Pattern

```java
// DriveIO.java - Abstract hardware interface
public interface DriveIO {
  void setVoltages(double left, double right);
  double[] getWheelPositionsMeters();
  // ...
}

// DriveIOTank.java - Concrete implementation
public class DriveIOTank implements DriveIO {
  private final UniversalMotor leftMotor;
  private final UniversalMotor rightMotor;
  // ...
}

// Subsystem uses interface
public class TankDriveSubsystem extends SubsystemBase {
  private final DriveIO driveIO = new DriveIOTank();
  // ...
}
```

### Benefits

- Hardware can be swapped without changing subsystem logic
- Supports simulation (create DriveIOSim implementation)
- Facilitates unit testing

---

## Documentation Requirements

### Each Test Project Must Have

1. **README.md** - Project overview, hardware requirements, usage
2. **NOTES.md** - Setup checklist, configuration notes, common issues
3. **Agent Config Files** - CLAUDE.md, GEMINI.md, AGENTS.md (redirect to repo-level configs)

### Required Documentation Sections

**README.md:**
- Purpose and description
- Hardware requirements
- CAN ID assignments
- Usage instructions

**NOTES.md:**
- Pre-deployment checklist
- Configuration steps
- PathPlanner/Choreo setup (if applicable)
- Vision setup (if applicable)
- Common troubleshooting

---

## Build and Deploy

### Standard Commands

```bash
# Build project
./gradlew build

# Deploy to RoboRIO
./gradlew deploy

# Run tests
./gradlew test

# Clean build artifacts
./gradlew clean
```

### Project Selection

Each test project has its own Gradle build. Navigate to the project directory:

```bash
cd Motor_Test
./gradlew deploy

cd ../Swerve_Base
./gradlew deploy
```

---

## Git Workflow

### Agent Activity Logging

All changes must be logged to `.agent-log/changelog.md`:

```
### [YYYY-MM-DD HH:MM] CLAUDE [ACTION_TYPE]
- Test: Which component/feature tested
- Results: Findings and validation status
- Files: <paths from repo root>
- Notes: Hardware requirements, CAN IDs, wiring
```

### Action Types

- `[IMPLEMENT]` - New feature or test added
- `[REFACTOR]` - Code restructuring
- `[FIX]` - Bug fix
- `[TEST]` - Testing and validation
- `[CONFIG]` - Configuration changes
- `[DOCS]` - Documentation updates

---

## Hardware Testing Best Practices

1. **Start Small**: Begin with low values when testing unknown motors
2. **Document Everything**: CAN IDs, wiring, findings
3. **Test Incrementally**: One component at a time
4. **Log Results**: Use DataLog and console output
5. **Safety First**: Ensure secure mounting before testing

---

## Test-Specific Notes

### Telemetry Verbosity

Test projects use **verbose telemetry** for hardware validation. This differs from competition code where telemetry is minimized for performance.

Use enable flags in Constants:
```java
public static final class LoggingConstants {
  public static final boolean ENABLE_LOGGING = true;
  public static final boolean ENABLE_MOTOR_LOGGING = true;  // Motor_Test
  public static final boolean ENABLE_DRIVE_TELEMETRY = true;  // Drive bases
}
```

### Hardware Requirements Documentation

Each test project **must document**:
- Required hardware (motors, sensors, controllers)
- CAN ID assignments
- Wiring requirements
- Expected behavior

See project README.md and NOTES.md for specific details.

---

## Version Notes

**2026 Season:**
- All motor APIs match FRC-2026 STANDARDS.md
- YAGSL for swerve drive (Swerve_Base)
- PathPlanner 2025+ for path following
- PhotonVision for AprilTag tracking

**For current motor API details, see:** [FRC-2026 STANDARDS.md](../FRC-2026/STANDARDS.md#2026-motor-api-reference-breaking-changes)
