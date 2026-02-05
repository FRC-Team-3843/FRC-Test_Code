# Common Gyro Abstraction

This directory contains gyro configuration utilities for JSON-based gyro setup.

## Files

- `GyroConfig.java` - Configuration record for gyro sensors
- `GyroConfigLoader.java` - Utility to load gyro config from JSON

## Usage

### 1. Copy Files to Your Project

Copy these files to your project and update package declarations:
```java
package frc.robot.drive;  // or frc.robot.gyro, etc.
```

### 2. Add Gyro Section to JSON Config

Add a gyro section to your `motor-config.json` or create a separate `gyro-config.json`:

```json
{
  "gyro": {
    "type": "PIGEON2",
    "canId": 9,
    "inverted": false
  }
}
```

### 3. Load Configuration in Your Subsystem

```java
// In your drive subsystem constructor:
GyroConfig gyroConfig = GyroConfigLoader.loadFromFile("motor-config.json");
GyroIO gyro = GyroConfigLoader.createGyroIO(gyroConfig);
```

## Supported Gyro Types

- `NONE` - No gyro present
- `ADIS16470` - Analog Devices ADIS16470 IMU (SPI)
- `ADXRS450` - Analog Devices ADXRS450 Gyro (SPI)
- `PIGEON2` - CTRE Pigeon 2.0 IMU (CAN)

## JSON Fields

- `type` (required) - Gyro type from list above
- `canId` (optional) - CAN ID for CAN-based gyros like Pigeon2 (default: 0)
- `inverted` (optional) - Invert gyro readings (default: false)

## Note

This is a **copy-based** system, not a library. Copy files to each project and update package declarations. This ensures each project can customize the code as needed.
