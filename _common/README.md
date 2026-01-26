# FRC-Test_Code Common Classes

## Purpose

This folder contains **canonical implementations** of shared code that is **copied** (not imported) into test projects.

## Why Copy Instead of Import?

- **Competition Reliability**: At competitions, internet access may be unavailable. Copied code works without external dependencies.
- **Project Independence**: Each test project remains self-contained and deployable without build system complexities.
- **Simplicity**: No need to configure vendor dependencies or manage shared libraries during time-critical hardware testing.

## Workflow

### When Using Common Classes

1. **Copy** the files you need from `_common\` into your project
2. Place them in the appropriate package structure in your project
3. Update package declarations to match your project structure

### When Modifying Common Classes

1. **Update this folder FIRST** with your improvements
2. **Document the change** in this README (see Change Log below)
3. **Copy the updated version** to all projects that use it
4. **Test in all affected projects** before committing

## Directory Structure

```
_common\
├── README.md (this file)
└── motor\              ← Motor abstraction layer
    ├── UniversalMotor.java      ← Common interface for all motor types
    ├── MotorFactory.java        ← Factory for creating motor instances
    ├── CanMotorWrapper.java     ← CAN motor wrapper (SparkMax, TalonFX, etc.)
    ├── PwmMotorWrapper.java     ← PWM motor wrapper
    └── MotorConfig.java         ← Motor configuration builder
```

## Motor Abstraction

### Purpose

Provides a unified interface for controlling different motor controller types (CAN: SparkMax, TalonFX, etc.; PWM: Talon SRX, Victor SPX, etc.).

### Key Features

- Vendor-agnostic motor control interface
- Consistent velocity and position control API
- Built-in configuration validation
- Support for both CAN and PWM motor controllers

### Usage Example

```java
// Create a motor using the factory
UniversalMotor motor = MotorFactory.create(
    MotorConfig.builder()
        .canId(1)
        .motorType(MotorConfig.MotorType.SPARK_MAX_BRUSHLESS)
        .inverted(false)
        .brakeMode(true)
        .currentLimit(40)
        .build()
);

// Use the motor
motor.setVoltage(6.0);
motor.setVelocity(100.0); // RPM
motor.stop();
```

### Projects Using Motor Abstraction

- **Motor_Test**: Full abstraction in `frc.robot.motor` package
- **Mecanum_Base**: Motor abstraction in `frc.robot.drive` package
- **Wheeled_Base**: Motor abstraction in `frc.robot.drive` package

## Change Log

### 2026-01-25
- Initial creation of `_common` structure
- Added motor abstraction classes (UniversalMotor, MotorFactory, etc.)
- Consolidated implementations from Motor_Test, Mecanum_Base, Wheeled_Base
- **UPDATE:** Fixed `CanMotorWrapper` to use 2026 REVLib `SparkMaxConfig` and `setReference` API.
- **NEW:** Added `utils/Alert.java` for persistent dashboard alerts.

## Future Considerations

When the team is comfortable with WPILib vendor dependency workflows, consider converting this to a vendor dependency format for easier updates across projects.

## Important Notes

- **DO NOT import from _common**: Copy files into your project
- **Keep implementations IDENTICAL**: When you improve code, update _common first, then propagate
- **Test thoroughly**: After copying updates, verify functionality in affected projects
- **Document changes**: Update this README's change log when modifying common classes
