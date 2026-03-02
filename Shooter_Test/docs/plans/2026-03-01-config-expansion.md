# Shooter Config Expansion Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Expand shooter-config.json to expose all hardcoded values (per-motor brake mode, velocity tolerance, CAN bus, motor/controller types, controller port, logging toggle, button bindings) and add save-back capability so tuned values persist on the roboRIO across reboots.

**Architecture:** All configuration lives in `ShooterConfig.java` (Jackson-annotated POJO) loaded from `shooter-config.json` in the deploy directory. New fields get added to the POJO and JSON, consumers updated to read from config instead of hardcoded values. A new `saveConfig()` method in `ShooterConfigLoader` writes the config back to the deploy directory on the roboRIO. Button bindings use string-based enum mapping (e.g. `"A"`, `"LEFT_BUMPER"`) resolved to `CommandXboxController` trigger methods at startup.

**Tech Stack:** Java 17, WPILib 2026, Jackson (already a dependency), FRC command-based framework.

**Note on testing:** This is an FRC hardware test project with no unit test infrastructure. Steps that would normally be TDD are replaced with "build and verify" steps using `gradlew build`. The project must be set to use the WPILib JDK: `JAVA_HOME="C:/Users/Public/wpilib/2026/jdk"`.

**Build command (used throughout):**
```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && JAVA_HOME="C:/Users/Public/wpilib/2026/jdk" ./gradlew build
```

---

### Task 1: Add new fields to ShooterConfig.java

**Files:**
- Modify: `src/main/java/frc/robot/shooter/ShooterConfig.java`

**Step 1: Replace shared `brakeMode` with per-motor brake modes**

Replace the single `brakeMode` field (line 57-58) with two separate fields:

```java
  @JsonProperty("preshooterBrakeMode")
  public boolean preshooterBrakeMode = false;

  @JsonProperty("mainShooterBrakeMode")
  public boolean mainShooterBrakeMode = false;
```

**Step 2: Add velocity tolerance field**

After the brake mode fields, add:

```java
  @JsonProperty("velocityToleranceRpm")
  public double velocityToleranceRpm = 50.0;
```

**Step 3: Add CAN bus fields**

```java
  @JsonProperty("preshooterCanBus")
  public String preshooterCanBus = "";

  @JsonProperty("mainShooterCanBus")
  public String mainShooterCanBus = "";
```

**Step 4: Add motor kind and controller type fields**

```java
  @JsonProperty("preshooterMotorKind")
  public String preshooterMotorKind = "KRAKEN_X44";

  @JsonProperty("preshooterControllerType")
  public String preshooterControllerType = "TALON_FX";

  @JsonProperty("mainShooterMotorKind")
  public String mainShooterMotorKind = "KRAKEN";

  @JsonProperty("mainShooterControllerType")
  public String mainShooterControllerType = "TALON_FX";
```

**Step 5: Add general settings fields**

```java
  @JsonProperty("driverControllerPort")
  public int driverControllerPort = 0;

  @JsonProperty("enableMotorLogging")
  public boolean enableMotorLogging = true;
```

**Step 6: Add button binding fields**

```java
  @JsonProperty("buttonSetpoint1")
  public String buttonSetpoint1 = "A";

  @JsonProperty("buttonSetpoint2")
  public String buttonSetpoint2 = "B";

  @JsonProperty("buttonApplyPid")
  public String buttonApplyPid = "X";

  @JsonProperty("buttonCharPreshooter")
  public String buttonCharPreshooter = "Y";

  @JsonProperty("buttonCharMainShooter")
  public String buttonCharMainShooter = "BACK";

  @JsonProperty("buttonServoPos1")
  public String buttonServoPos1 = "LEFT_BUMPER";

  @JsonProperty("buttonServoPos2")
  public String buttonServoPos2 = "RIGHT_BUMPER";
```

**Step 7: Build to verify**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && JAVA_HOME="C:/Users/Public/wpilib/2026/jdk" ./gradlew build
```

Expected: BUILD SUCCESSFUL (Jackson ignores unknown JSON fields by default, and old JSON won't have new fields so defaults apply).

**Step 8: Commit**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && git add src/main/java/frc/robot/shooter/ShooterConfig.java
git commit -m "feat: add new config fields for brake modes, tolerance, CAN bus, motor types, buttons"
```

---

### Task 2: Update shooter-config.json with new fields

**Files:**
- Modify: `src/main/deploy/shooter-config.json`

**Step 1: Rewrite the JSON file with all new fields**

Replace the entire file with:

```json
{
  "preshooterCanId": 20,
  "preshooterInverted": false,
  "preshooterGearRatio": 1.0,
  "preshooterKp": 0.2,
  "preshooterKi": 0.0,
  "preshooterKd": 0.0,
  "preshooterKv": 0.116,
  "preshooterKs": 0.25,
  "preshooterCanBus": "",
  "preshooterMotorKind": "KRAKEN_X44",
  "preshooterControllerType": "TALON_FX",
  "preshooterBrakeMode": false,

  "mainShooterCanId": 21,
  "mainShooterInverted": false,
  "mainShooterGearRatio": 1.0,
  "mainShooterKp": 0.2,
  "mainShooterKi": 0.0,
  "mainShooterKd": 0.0,
  "mainShooterKv": 0.111,
  "mainShooterKs": 0.25,
  "mainShooterCanBus": "",
  "mainShooterMotorKind": "KRAKEN",
  "mainShooterControllerType": "TALON_FX",
  "mainShooterBrakeMode": false,

  "velocityToleranceRpm": 50.0,

  "servoPwmChannel": 0,
  "servoPosition1": 0.95,
  "servoPosition2": 0.45,

  "preshooterSetpoint1Rpm": 6500.0,
  "preshooterSetpoint2Rpm": 3250.0,
  "mainShooterSetpoint1Rpm": 5500.0,
  "mainShooterSetpoint2Rpm": 2750.0,

  "driverControllerPort": 0,
  "enableMotorLogging": true,

  "buttonSetpoint1": "A",
  "buttonSetpoint2": "B",
  "buttonApplyPid": "X",
  "buttonCharPreshooter": "Y",
  "buttonCharMainShooter": "BACK",
  "buttonServoPos1": "LEFT_BUMPER",
  "buttonServoPos2": "RIGHT_BUMPER"
}
```

**Step 2: Build to verify JSON parses correctly**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && JAVA_HOME="C:/Users/Public/wpilib/2026/jdk" ./gradlew build
```

Expected: BUILD SUCCESSFUL.

**Step 3: Commit**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && git add src/main/deploy/shooter-config.json
git commit -m "feat: update shooter-config.json with all new configurable fields"
```

---

### Task 3: Add save-back capability to ShooterConfigLoader

**Files:**
- Modify: `src/main/java/frc/robot/shooter/ShooterConfigLoader.java`

**Step 1: Add saveConfig method**

Add the following method after the existing `loadConfigOrDefault` method (after line 40):

```java
  /**
   * Saves shooter configuration to a JSON file in the deploy directory.
   * Used to persist tuned values on the roboRIO across reboots.
   *
   * @param filename Name of the JSON file (e.g., "shooter-config.json")
   * @param config The ShooterConfig object to save
   * @return true if save succeeded, false otherwise
   */
  public static boolean saveConfig(String filename, ShooterConfig config) {
    try {
      File configFile = new File(Filesystem.getDeployDirectory(), filename);
      mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
      System.out.println("Configuration saved to " + configFile.getAbsolutePath());
      return true;
    } catch (IOException e) {
      System.err.println("Failed to save shooter config to " + filename + ": " + e.getMessage());
      return false;
    }
  }
```

**Step 2: Build to verify**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && JAVA_HOME="C:/Users/Public/wpilib/2026/jdk" ./gradlew build
```

Expected: BUILD SUCCESSFUL.

**Step 3: Commit**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && git add src/main/java/frc/robot/shooter/ShooterConfigLoader.java
git commit -m "feat: add saveConfig method to persist tuned values to JSON on roboRIO"
```

---

### Task 4: Update ShooterSubsystem to use new config fields

**Files:**
- Modify: `src/main/java/frc/robot/shooter/ShooterSubsystem.java`

**Step 1: Use motor/controller types from config instead of hardcoded enums**

Replace the constructor (lines 23-56) with:

```java
  public ShooterSubsystem(ShooterConfig config) {
    // Create preshooter motor from config
    m_preshooter = new CanMotorWrapper(
        MotorConfiguration.builder(
                ControllerType.valueOf(config.preshooterControllerType),
                MotorKind.valueOf(config.preshooterMotorKind))
            .canId(config.preshooterCanId)
            .canBus(config.preshooterCanBus)
            .inverted(config.preshooterInverted)
            .gearRatio(config.preshooterGearRatio)
            .kP(config.preshooterKp)
            .kI(config.preshooterKi)
            .kD(config.preshooterKd)
            .kV(config.preshooterKv)
            .kS(config.preshooterKs)
            .brakeMode(config.preshooterBrakeMode)
            .build());

    // Create main shooter motor from config
    m_mainShooter = new CanMotorWrapper(
        MotorConfiguration.builder(
                ControllerType.valueOf(config.mainShooterControllerType),
                MotorKind.valueOf(config.mainShooterMotorKind))
            .canId(config.mainShooterCanId)
            .canBus(config.mainShooterCanBus)
            .inverted(config.mainShooterInverted)
            .gearRatio(config.mainShooterGearRatio)
            .kP(config.mainShooterKp)
            .kI(config.mainShooterKi)
            .kD(config.mainShooterKd)
            .kV(config.mainShooterKv)
            .kS(config.mainShooterKs)
            .brakeMode(config.mainShooterBrakeMode)
            .build());

    // Create servo
    m_servo = new Servo(config.servoPwmChannel);

    m_velocityToleranceRpm = config.velocityToleranceRpm;
  }
```

**Step 2: Build to verify**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && JAVA_HOME="C:/Users/Public/wpilib/2026/jdk" ./gradlew build
```

Expected: BUILD SUCCESSFUL.

**Step 3: Commit**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && git add src/main/java/frc/robot/shooter/ShooterSubsystem.java
git commit -m "feat: use motor/controller types, per-motor brake, CAN bus, tolerance from config"
```

---

### Task 5: Update RobotContainer for button bindings, save-back, and config-driven port

**Files:**
- Modify: `src/main/java/frc/robot/RobotContainer.java`

This is the largest change. The controller port, button bindings, and save-back logic all live here.

**Step 1: Add button resolver helper method and update field initialization**

Replace the class header and field declarations (lines 16-22) with:

```java
public class RobotContainer {
  private static final String CONFIG_FILENAME = "shooter-config.json";

  // Load shooter configuration from JSON
  private final ShooterConfig m_config = ShooterConfigLoader.loadConfigOrDefault(CONFIG_FILENAME);
  private final CommandXboxController m_driver =
      new CommandXboxController(m_config.driverControllerPort);
  private final ShooterSubsystem m_shooter = new ShooterSubsystem(m_config);
```

Add a button resolver method at the bottom of the class (before the closing brace):

```java
  /**
   * Resolves a button name string to the corresponding controller trigger.
   * Supported: A, B, X, Y, LEFT_BUMPER, RIGHT_BUMPER, BACK, START, LEFT_STICK, RIGHT_STICK
   */
  private edu.wpi.first.wpilibj2.command.button.Trigger resolveButton(String buttonName) {
    switch (buttonName.toUpperCase()) {
      case "A": return m_driver.a();
      case "B": return m_driver.b();
      case "X": return m_driver.x();
      case "Y": return m_driver.y();
      case "LEFT_BUMPER": return m_driver.leftBumper();
      case "RIGHT_BUMPER": return m_driver.rightBumper();
      case "BACK": return m_driver.back();
      case "START": return m_driver.start();
      case "LEFT_STICK": return m_driver.leftStick();
      case "RIGHT_STICK": return m_driver.rightStick();
      default:
        System.err.println("Unknown button name: " + buttonName + ", defaulting to A");
        return m_driver.a();
    }
  }
```

**Step 2: Update configureBindings to use button resolver**

Replace the entire `configureBindings()` method (lines 71-115) with:

```java
  private void configureBindings() {
    // Setpoint 1 + servo position 1
    resolveButton(m_config.buttonSetpoint1).whileTrue(Commands.run(() -> {
      double preRpm = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint1_RPM", m_config.preshooterSetpoint1Rpm);
      double mainRpm = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint1_RPM", m_config.mainShooterSetpoint1Rpm);
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position1", m_config.servoPosition1);

      m_shooter.setVelocities(preRpm, mainRpm);
      m_shooter.setServoPosition(servoPos);
    }, m_shooter)).onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // Setpoint 2 + servo position 2
    resolveButton(m_config.buttonSetpoint2).whileTrue(Commands.run(() -> {
      double preRpm = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint2_RPM", m_config.preshooterSetpoint2Rpm);
      double mainRpm = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint2_RPM", m_config.mainShooterSetpoint2Rpm);
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position2", m_config.servoPosition2);

      m_shooter.setVelocities(preRpm, mainRpm);
      m_shooter.setServoPosition(servoPos);
    }, m_shooter)).onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // Apply PID configuration from dashboard and save to JSON
    resolveButton(m_config.buttonApplyPid).onTrue(Commands.runOnce(() -> {
      applyPidConfig();
      SmartDashboard.putBoolean("Shooter/ApplyPID", false);
    }));

    // Characterize preshooter (hold button for ~20 seconds)
    resolveButton(m_config.buttonCharPreshooter).whileTrue(new CharacterizeShooterCommand(m_shooter, true));

    // Characterize main shooter (hold button for ~20 seconds)
    resolveButton(m_config.buttonCharMainShooter).whileTrue(new CharacterizeShooterCommand(m_shooter, false));

    // Servo to position 1 (no motors)
    resolveButton(m_config.buttonServoPos1).onTrue(Commands.runOnce(() -> {
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position1", m_config.servoPosition1);
      m_shooter.setServoPosition(servoPos);
    }));

    // Servo to position 2 (no motors)
    resolveButton(m_config.buttonServoPos2).onTrue(Commands.runOnce(() -> {
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position2", m_config.servoPosition2);
      m_shooter.setServoPosition(servoPos);
    }));
  }
```

**Step 3: Update applyPidConfig to save back to JSON**

Replace the `applyPidConfig()` method (lines 117-136) with:

```java
  private void applyPidConfig() {
    // Read PID values from SmartDashboard
    double preKp = SmartDashboard.getNumber("Shooter/Preshooter/PID/kP", m_config.preshooterKp);
    double preKi = SmartDashboard.getNumber("Shooter/Preshooter/PID/kI", m_config.preshooterKi);
    double preKd = SmartDashboard.getNumber("Shooter/Preshooter/PID/kD", m_config.preshooterKd);
    double preKv = SmartDashboard.getNumber("Shooter/Preshooter/PID/kV", m_config.preshooterKv);
    double preKs = SmartDashboard.getNumber("Shooter/Preshooter/PID/kS", m_config.preshooterKs);

    double mainKp = SmartDashboard.getNumber("Shooter/MainShooter/PID/kP", m_config.mainShooterKp);
    double mainKi = SmartDashboard.getNumber("Shooter/MainShooter/PID/kI", m_config.mainShooterKi);
    double mainKd = SmartDashboard.getNumber("Shooter/MainShooter/PID/kD", m_config.mainShooterKd);
    double mainKv = SmartDashboard.getNumber("Shooter/MainShooter/PID/kV", m_config.mainShooterKv);
    double mainKs = SmartDashboard.getNumber("Shooter/MainShooter/PID/kS", m_config.mainShooterKs);

    // Read setpoint and servo values from SmartDashboard
    double preSetpoint1 = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint1_RPM", m_config.preshooterSetpoint1Rpm);
    double preSetpoint2 = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint2_RPM", m_config.preshooterSetpoint2Rpm);
    double mainSetpoint1 = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint1_RPM", m_config.mainShooterSetpoint1Rpm);
    double mainSetpoint2 = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint2_RPM", m_config.mainShooterSetpoint2Rpm);
    double servoPos1 = SmartDashboard.getNumber("Shooter/Servo/Position1", m_config.servoPosition1);
    double servoPos2 = SmartDashboard.getNumber("Shooter/Servo/Position2", m_config.servoPosition2);

    // Hot-reload PID configuration
    m_shooter.updatePreshooterPid(preKp, preKi, preKd, preKv, preKs);
    m_shooter.updateMainShooterPid(mainKp, mainKi, mainKd, mainKv, mainKs);

    // Update config object in memory
    m_config.preshooterKp = preKp;
    m_config.preshooterKi = preKi;
    m_config.preshooterKd = preKd;
    m_config.preshooterKv = preKv;
    m_config.preshooterKs = preKs;

    m_config.mainShooterKp = mainKp;
    m_config.mainShooterKi = mainKi;
    m_config.mainShooterKd = mainKd;
    m_config.mainShooterKv = mainKv;
    m_config.mainShooterKs = mainKs;

    m_config.preshooterSetpoint1Rpm = preSetpoint1;
    m_config.preshooterSetpoint2Rpm = preSetpoint2;
    m_config.mainShooterSetpoint1Rpm = mainSetpoint1;
    m_config.mainShooterSetpoint2Rpm = mainSetpoint2;
    m_config.servoPosition1 = servoPos1;
    m_config.servoPosition2 = servoPos2;

    // Save to JSON on roboRIO
    if (ShooterConfigLoader.saveConfig(CONFIG_FILENAME, m_config)) {
      System.out.println("PID configuration applied and saved!");
    } else {
      System.out.println("PID configuration applied but save failed!");
    }
  }
```

**Step 4: Update configureTelemetry controls string to reflect configurable buttons**

Replace the controls string in `configureTelemetry()` (line 60) with:

```java
    SmartDashboard.putString("Shooter/Controls",
        m_config.buttonSetpoint1 + "=SP1 | " +
        m_config.buttonSetpoint2 + "=SP2 | " +
        m_config.buttonApplyPid + "=Apply+Save | " +
        m_config.buttonCharPreshooter + "=TunePre | " +
        m_config.buttonCharMainShooter + "=TuneMain | " +
        m_config.buttonServoPos1 + "=Servo1 | " +
        m_config.buttonServoPos2 + "=Servo2");
```

**Step 5: Build to verify**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && JAVA_HOME="C:/Users/Public/wpilib/2026/jdk" ./gradlew build
```

Expected: BUILD SUCCESSFUL.

**Step 6: Commit**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && git add src/main/java/frc/robot/RobotContainer.java
git commit -m "feat: configurable button bindings, save-back to JSON on Apply PID, config-driven port"
```

---

### Task 6: Update Robot.java and Constants.java to use config

**Files:**
- Modify: `src/main/java/frc/robot/Robot.java`
- Modify: `src/main/java/frc/robot/Constants.java`

**Step 1: Update Robot.java to read logging toggle from config**

The challenge: `Robot` creates `RobotContainer` which owns the config. We need the config loaded before `RobotContainer` is created. Simplest approach: load the config directly in `Robot` and pass it to `RobotContainer`.

Replace `Robot.java` entirely:

```java
package frc.robot;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.net.WebServer;
import frc.robot.shooter.ShooterConfig;
import frc.robot.shooter.ShooterConfigLoader;

public class Robot extends TimedRobot {
  private Command m_autonomousCommand;
  private final RobotContainer m_robotContainer;

  public Robot() {
    ShooterConfig config = ShooterConfigLoader.loadConfigOrDefault("shooter-config.json");

    if (config.enableMotorLogging) {
      DataLogManager.start();
      DriverStation.startDataLog(DataLogManager.getLog());
    }

    // Serve deploy directory on port 5800 for Elastic remote layout loading
    WebServer.start(5800, Filesystem.getDeployDirectory().getPath());

    m_robotContainer = new RobotContainer(config);
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
  }

  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();
    if (m_autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(m_autonomousCommand);
    }
  }

  @Override
  public void teleopInit() {
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }
}
```

**Step 2: Update RobotContainer to accept config via constructor**

In `RobotContainer.java`, change the config loading line and add a constructor parameter.

Replace:
```java
  // Load shooter configuration from JSON
  private final ShooterConfig m_config = ShooterConfigLoader.loadConfigOrDefault(CONFIG_FILENAME);
```

With:
```java
  private final ShooterConfig m_config;
```

And change the constructor signature from `public RobotContainer()` to:
```java
  public RobotContainer(ShooterConfig config) {
    m_config = config;
```

Note: The `m_driver` and `m_shooter` fields that reference `m_config` must be initialized inside the constructor body instead of as field initializers, since `m_config` is now set in the constructor. Move them:

Replace the field declarations:
```java
  private final CommandXboxController m_driver =
      new CommandXboxController(m_config.driverControllerPort);
  private final ShooterSubsystem m_shooter = new ShooterSubsystem(m_config);
```

With fields that are assigned in the constructor:
```java
  private final CommandXboxController m_driver;
  private final ShooterSubsystem m_shooter;
```

And at the start of the constructor body:
```java
  public RobotContainer(ShooterConfig config) {
    m_config = config;
    m_driver = new CommandXboxController(m_config.driverControllerPort);
    m_shooter = new ShooterSubsystem(m_config);

    // ... rest of constructor unchanged
```

Also remove the `ShooterConfigLoader` import from `RobotContainer.java` since it no longer loads directly.

**Step 3: Clean up Constants.java**

Remove the values that moved to JSON config. Replace entire file:

```java
package frc.robot;

public final class Constants {
  private Constants() {}
}
```

`OperatorConstants.DRIVER_CONTROLLER_PORT` and `LoggingConstants.ENABLE_MOTOR_LOGGING` are now in the JSON config.

**Step 4: Build to verify**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && JAVA_HOME="C:/Users/Public/wpilib/2026/jdk" ./gradlew build
```

Expected: BUILD SUCCESSFUL.

**Step 5: Commit**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && git add src/main/java/frc/robot/Robot.java src/main/java/frc/robot/RobotContainer.java src/main/java/frc/robot/Constants.java
git commit -m "feat: move logging toggle and controller port to JSON config, pass config from Robot to RobotContainer"
```

---

### Task 7: Final build verification and cleanup

**Files:**
- All modified files (full project build)

**Step 1: Clean build**

```bash
cd C:/GitHub/FRC-Test_Code/Shooter_Test && JAVA_HOME="C:/Users/Public/wpilib/2026/jdk" ./gradlew clean build
```

Expected: BUILD SUCCESSFUL.

**Step 2: Verify JSON is valid**

Read `src/main/deploy/shooter-config.json` and confirm all fields are present with correct defaults.

**Step 3: Verify no remaining references to old Constants fields**

Search for `Constants.OperatorConstants` and `Constants.LoggingConstants` across the project. Should find zero matches.

**Step 4: Commit any cleanup if needed, then final summary**

Review all changes across the branch.
