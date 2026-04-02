# FRC-Test_Code Common Classes

> **Documentation Guide:**
> - **This file (README):** Common snapshot purpose and usage
> - **STANDARDS.md:** Coding standards (see C:\GitHub\FRC-Test_Code\STANDARDS.md)

## Purpose

This folder contains **copyable snapshots** of shared code. It is no longer the preferred integration path for the motor stack now that `Motor_System` has internal library modules.

## Workflow

### When Using Common Classes

1. Copy the files you need from `_common\` into your project.
2. Place them in the appropriate package structure in your project.
3. Update package declarations to match your project structure.

For the motor stack specifically, prefer depending on the `Motor_System` internal libraries instead of copying `_common/motor_system`.

## Directory Structure

- `_common/motor/`: legacy/simple motor abstraction snapshot
- `_common/motor_system/`: legacy snapshot of the extracted generic motor system

## Change Log

### 2026-01-25
- Initial creation of `_common` structure
- Added motor abstraction classes (UniversalMotor, MotorFactory, etc.)

### 2026-03-07
- Added `_common/motor_system` as the copy snapshot for the extracted generic motor project

### 2026-03-08
- `Motor_System` moved to an internal multi-library structure (`motor-core`, `motor-tune`, `motor-test`, `motor-dashboard`)
- `_common/motor_system` is now a legacy snapshot and should not be treated as the preferred update path

## Important Notes

- Do not import directly from `_common`
- Keep snapshots intentional; they can drift if not refreshed
- Prefer shared library reuse where available
