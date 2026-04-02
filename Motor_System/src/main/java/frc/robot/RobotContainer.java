// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.PowerDistribution;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj.DriverStation;
import frc.lib.motor.dashboard.MotorDashboardRuntime;
import frc.lib.motor.config.ControllerPreset;
import frc.lib.motor.config.MechanismType;
import frc.lib.motor.config.MotorConfig;
import frc.lib.motor.config.MotorConfiguration;
import frc.lib.motor.config.MotorMetadataCatalog;
import frc.lib.motor.config.MotorSystemConfig;
import frc.lib.motor.config.MotorSystemConfigLoader;
import frc.lib.motor.core.PowerDistributionRegistry;
import frc.lib.motor.config.PowerModuleType;
import frc.lib.motor.test.PowerChannelDetector;
import frc.lib.motor.tuning.SysIdAnalyzer;
import frc.lib.motor.tuning.UnitConverter;
import frc.lib.motorsystem.MotorSystem;

public class RobotContainer {
  private static final String CONFIG_FILENAME = "motor-config.json";

  private final MotorSystemConfig m_config;
  private final CommandXboxController m_driver;
  private final MotorSystem m_motorSystem;
  private final MotorDashboardRuntime m_dashboardRuntime;
  private final Command[] m_activeBenchCommands;
  private final String[] m_activeBenchOwners;

  public RobotContainer(MotorSystemConfig config) {
    m_config = config;
    m_driver = new CommandXboxController(config.driverControllerPort);
    m_motorSystem = new MotorSystem(config);
    m_dashboardRuntime = new MotorDashboardRuntime(config, m_motorSystem);
    m_activeBenchCommands = new Command[config.motors.size()];
    m_activeBenchOwners = new String[config.motors.size()];

    initDashboard();
    configureBindings();
  }

  /** Initializes all dashboard fields for each motor in the config. */
  private void initDashboard() {
    String sys = m_config.systemName;
    SmartDashboard.putString(sys + "/Metadata/SchemaJson", MotorMetadataCatalog.schemaJson());
    SmartDashboard.putNumber(sys + "/Metadata/MotorCount", m_config.motors.size());

    for (int i = 0; i < m_config.motors.size(); i++) {
      MotorConfig mc = m_config.motors.get(i);
      MotorConfiguration motorConfig = MotorConfiguration.fromMotorConfig(m_config, mc);
      String prefix = sys + "/" + mc.name + "/";

      // PID tuning fields (editable)
      SmartDashboard.putNumber(prefix + "PID/kP", mc.kP);
      SmartDashboard.putNumber(prefix + "PID/kI", mc.kI);
      SmartDashboard.putNumber(prefix + "PID/kD", mc.kD);
      SmartDashboard.putNumber(prefix + "PID/kV", mc.kV);
      SmartDashboard.putNumber(prefix + "PID/kS", mc.kS);
      SmartDashboard.putNumber(prefix + "PID/kA", mc.kA);
      SmartDashboard.putNumber(prefix + "PID/kG", mc.kG);

      // Enable toggle
      SmartDashboard.putBoolean(prefix + "Enabled", true);

      // Bench controls
      SmartDashboard.putBoolean(prefix + "Bench/Armed", false);
      SmartDashboard.putBoolean(prefix + "Bench/StartSysId", false);
      SmartDashboard.putBoolean(prefix + "Bench/StartManualRun", false);
      SmartDashboard.putBoolean(prefix + "Bench/Cancel", false);
      SmartDashboard.putBoolean(prefix + "Bench/DeadmanActive", false);
      SmartDashboard.putString(prefix + "Bench/Status", "Idle");
      SmartDashboard.putString(prefix + "Bench/ActionOwner", "None");
      SmartDashboard.putString(prefix + "Bench/DSMode", "Unknown");

      // SysId result display fields
      SmartDashboard.putString(prefix + "SysId/Status", "Idle");
      SmartDashboard.putNumber(prefix + "SysId/kS", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/kV", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/kA", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/kG", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/R2_Accel", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/R2_SimVel", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/RMSE", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Samples", 0);
      SmartDashboard.putNumber(prefix + "SysId/kP_vel", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/kP_pos", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/kD_pos", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/AutoVelThresh", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/AutoQv", 0.0);

      // SysId analysis parameters (editable)
      ControllerPreset preset = mc.getControllerPreset();
      m_dashboardRuntime.publishSysIdDefaults(i, preset);

      // Apply toggles
      SmartDashboard.putBoolean(prefix + "SysId/ApplyResults", false);
      SmartDashboard.putBoolean(prefix + "ApplyPID", false);

      // Dashboard run controls
      SmartDashboard.putNumber(prefix + "RunTarget", 0.0);

      // Position PID fields
      SmartDashboard.putNumber(prefix + "PID/kP_pos", mc.kP_pos);
      SmartDashboard.putNumber(prefix + "PID/kD_pos", mc.kD_pos);

      // Motion profiling fields (editable)
      SmartDashboard.putNumber(prefix + "Motion/CruiseVel", mc.motionCruiseVelocity);
      SmartDashboard.putNumber(prefix + "Motion/Accel", mc.motionAcceleration);
      SmartDashboard.putNumber(prefix + "Motion/Jerk", mc.motionJerk);

      // Soft limits (editable)
      SmartDashboard.putBoolean(prefix + "Limits/ForwardEnabled", mc.forwardLimit != Double.MAX_VALUE);
      SmartDashboard.putBoolean(prefix + "Limits/ReverseEnabled", mc.reverseLimit != -Double.MAX_VALUE);
      SmartDashboard.putNumber(prefix + "Limits/Forward",
          mc.forwardLimit == Double.MAX_VALUE ? 0.0 : mc.forwardLimit);
      SmartDashboard.putNumber(prefix + "Limits/Reverse",
          mc.reverseLimit == -Double.MAX_VALUE ? 0.0 : mc.reverseLimit);

      // Current limit (editable)
      SmartDashboard.putNumber(prefix + "CurrentLimit", mc.currentLimit);
      SmartDashboard.putBoolean(prefix + "Setup/Apply", false);
      SmartDashboard.putNumber(prefix + "Power/Channel", mc.powerChannel);
      SmartDashboard.putBoolean(prefix + "Power/Detect", false);
      SmartDashboard.putString(prefix + "Power/DetectStatus", powerDetectStatus(motorConfig));
      SmartDashboard.putNumber(prefix + "Feedback/QuadratureA", mc.quadratureChannelA);
      SmartDashboard.putNumber(prefix + "Feedback/QuadratureB", mc.quadratureChannelB);
      SmartDashboard.putNumber(prefix + "Feedback/DutyCycleChannel", mc.dutyCycleChannel);
      SmartDashboard.putNumber(prefix + "Feedback/AnalogChannel", mc.analogChannel);
      SmartDashboard.putNumber(prefix + "Feedback/DistancePerPulseRot",
          mc.feedbackDistancePerPulseRotations);
      SmartDashboard.putNumber(prefix + "Feedback/FullRangeRot", mc.feedbackFullRangeRotations);
      SmartDashboard.putNumber(prefix + "Feedback/OffsetRot", mc.feedbackOffsetRotations);
      SmartDashboard.putBoolean(prefix + "Feedback/Inverted", mc.feedbackInverted);
      SmartDashboard.putBoolean(prefix + "Feedback/ContinuousWrap", mc.feedbackContinuousWrap);
      SmartDashboard.putNumber(prefix + "Feedback/SamplesToAverage", mc.feedbackSamplesToAverage);

      // Physical estimate placeholders
      SmartDashboard.putNumber(prefix + "SysId/Est_Inertia", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_Friction", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_Mass_kg", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_Efficiency", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_MaxAccel", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_FreeSpeed", 0.0);

      // Info
      SmartDashboard.putString(prefix + "ControllerFamily", mc.getControllerFamily().displayName());
      SmartDashboard.putString(prefix + "Transport", mc.transport);
      SmartDashboard.putString(prefix + "MechanismType", mc.mechanismType);
      SmartDashboard.putString(prefix + "ControllerType",
          mc.getControllerFamily().displayName() + " / " + mc.transport);
      SmartDashboard.putNumber(prefix + "GearRatio", mc.gearRatio);
      SmartDashboard.putNumber(prefix + "WheelDiameter", mc.wheelDiameter);
      SmartDashboard.putNumber(prefix + "DistancePerRotation", mc.distancePerRotation);
      SmartDashboard.putNumber(prefix + "ArmLength", mc.armLength);
      SmartDashboard.putNumber(prefix + "Mass", mc.mass);
      SmartDashboard.putString(prefix + "Capabilities/Telemetry", describeTelemetry(motorConfig));
      SmartDashboard.putString(prefix + "Capabilities/Tests", describeTests(motorConfig));
      SmartDashboard.putString(prefix + "Capabilities/Unlock", describeUnlocks(motorConfig));
      SmartDashboard.putString(prefix + "Support/Level", supportLevelLabel(motorConfig));
      SmartDashboard.putString(prefix + "Support/Note", supportNote(motorConfig));
      SmartDashboard.putString(prefix + "Testing/FeedbackSource", mc.feedbackSource);
      SmartDashboard.putBoolean(prefix + "Testing/FeedbackConnected", false);
      SmartDashboard.putString(prefix + "Testing/ClosedLoopSource", "Unknown");
      SmartDashboard.putString(prefix + "Testing/PowerTelemetrySource", "Unknown");
      SmartDashboard.putString(prefix + "Testing/TelemetryHealth", "Waiting for telemetry");
      SmartDashboard.putString(prefix + "Testing/CapabilitySummary", describeTests(motorConfig));
      SmartDashboard.putNumber(prefix + "Testing/MeasuredVelocityRps", 0.0);
      SmartDashboard.putNumber(prefix + "Testing/MeasuredPositionRot", 0.0);
      SmartDashboard.putNumber(prefix + "Testing/MeasuredVoltage", 0.0);
      SmartDashboard.putNumber(prefix + "Testing/MeasuredCurrent", 0.0);
      SmartDashboard.putNumber(prefix + "Testing/MeasuredTemperatureC", 0.0);

      // Geometry summary
      UnitConverter uc = new UnitConverter(mc.getMechanismType(),
          mc.wheelDiameter, mc.distancePerRotation, mc.armLength, false);
      SmartDashboard.putString(prefix + "Geometry", uc.geometrySummary());

      // Workflow hint
      String hint = getWorkflowHint(mc.getMechanismType());
      SmartDashboard.putString(prefix + "WorkflowHint", hint);
    }

    m_dashboardRuntime.publishChoosers();

    // Setpoints (editable)
    for (var entry : m_config.setpoints.entrySet()) {
      SmartDashboard.putNumber(sys + "/Setpoints/" + entry.getKey(), entry.getValue());
    }

    // Servo positions (editable)
    for (var entry : m_config.servoPositions.entrySet()) {
      SmartDashboard.putNumber(sys + "/Servo/" + entry.getKey(), entry.getValue());
    }

    // UseMetric toggle (default: false = imperial)
    SmartDashboard.putBoolean(sys + "/UseMetric", false);

    // Controls help
    StringBuilder controls = new StringBuilder();
    for (var entry : m_config.buttonBindings.entrySet()) {
      if (controls.length() > 0) controls.append(" | ");
      controls.append(entry.getValue()).append("=").append(entry.getKey());
    }
    SmartDashboard.putString(sys + "/Controls", controls.toString());
  }

  private void configureBindings() {
    String sys = m_config.systemName;

    // Setpoint 1: run all motors at setpoint1 values + servo position1
    resolveButton(m_config.buttonBindings.getOrDefault("setpoint1", "A"))
        .and(this::normalControllerControlsEnabled)
        .whileTrue(Commands.run(() -> {
          for (int i = 0; i < m_config.motors.size(); i++) {
            String key = "setpoint1_" + m_config.motors.get(i).name;
            double rpm = SmartDashboard.getNumber(sys + "/Setpoints/" + key,
                m_config.setpoints.getOrDefault(key, 0.0));
            m_motorSystem.setVelocity(i, rpm);
          }
          Double servoPos = readServoPosition("position1");
          if (servoPos != null) m_motorSystem.setServoPosition(servoPos);
        }, m_motorSystem.getAllMotorRequirements()))
        .onFalse(Commands.runOnce(m_motorSystem::stop, m_motorSystem.getAllMotorRequirements()));

    // Setpoint 2
    resolveButton(m_config.buttonBindings.getOrDefault("setpoint2", "B"))
        .and(this::normalControllerControlsEnabled)
        .whileTrue(Commands.run(() -> {
          for (int i = 0; i < m_config.motors.size(); i++) {
            String key = "setpoint2_" + m_config.motors.get(i).name;
            double rpm = SmartDashboard.getNumber(sys + "/Setpoints/" + key,
                m_config.setpoints.getOrDefault(key, 0.0));
            m_motorSystem.setVelocity(i, rpm);
          }
          Double servoPos = readServoPosition("position2");
          if (servoPos != null) m_motorSystem.setServoPosition(servoPos);
        }, m_motorSystem.getAllMotorRequirements()))
        .onFalse(Commands.runOnce(m_motorSystem::stop, m_motorSystem.getAllMotorRequirements()));

    // Apply PID from dashboard for all motors and save to JSON
    resolveButton(m_config.buttonBindings.getOrDefault("applyPid", "X"))
        .and(this::normalControllerControlsEnabled)
        .onTrue(Commands.runOnce(() -> {
          applyPidConfig();
          for (int i = 0; i < m_config.motors.size(); i++) {
            SmartDashboard.putBoolean(m_motorSystem.getMotorName(i) + "/ApplyPID", false);
          }
        }));

    // SysId per motor — bind each motor's SysId to its configured button
    for (int i = 0; i < m_config.motors.size(); i++) {
      String motorName = m_config.motors.get(i).name;
      String buttonKey = "sysId_" + motorName;
      String button = m_config.buttonBindings.get(buttonKey);
      if (button != null) {
        final int idx = i;
        resolveButton(button)
            .and(this::normalControllerControlsEnabled)
            .whileTrue(m_motorSystem.sysIdWithAnalysis(idx));
      }
    }

    // Servo positions
    String servoBtn1 = m_config.buttonBindings.getOrDefault("servoPos1", "LEFT_BUMPER");
    resolveButton(servoBtn1).and(this::normalControllerControlsEnabled).onTrue(Commands.runOnce(() -> {
      Double pos = readServoPosition("position1");
      if (pos != null) m_motorSystem.setServoPosition(pos);
    }));

    String servoBtn2 = m_config.buttonBindings.getOrDefault("servoPos2", "RIGHT_BUMPER");
    resolveButton(servoBtn2).and(this::normalControllerControlsEnabled).onTrue(Commands.runOnce(() -> {
      Double pos = readServoPosition("position2");
      if (pos != null) m_motorSystem.setServoPosition(pos);
    }));
  }

  /** Reads servo position from dashboard, returns null if no servo configured. */
  private Double readServoPosition(String posName) {
    if (m_config.servoChannel < 0) return null;
    String key = m_config.systemName + "/Servo/" + posName;
    double defaultVal = m_config.servoPositions.getOrDefault(posName, 0.5);
    return SmartDashboard.getNumber(key, defaultVal);
  }

  /**
   * Called periodically to check per-motor dashboard toggles and run controls.
   */
  public void checkDashboardToggles() {
    m_dashboardRuntime.syncDashboardState();

    for (int i = 0; i < m_config.motors.size(); i++) {
      String motorPrefix = m_motorSystem.getMotorName(i) + "/";
      String benchPrefix = motorPrefix + "Bench/";
      SmartDashboard.putBoolean(benchPrefix + "DeadmanActive", isDeadmanHeld());
      SmartDashboard.putString(benchPrefix + "DSMode", getDsModeString());

      // Per-motor Apply PID toggle
      if (SmartDashboard.getBoolean(motorPrefix + "ApplyPID", false)) {
        SmartDashboard.putBoolean(motorPrefix + "ApplyPID", false);
        applyPidForMotor(i);
      }

      if (SmartDashboard.getBoolean(motorPrefix + "Setup/Apply", false)) {
        SmartDashboard.putBoolean(motorPrefix + "Setup/Apply", false);
        applySetupForMotor(i);
      }

      // Per-motor Apply SysId toggle
      if (SmartDashboard.getBoolean(motorPrefix + "SysId/ApplyResults", false)) {
        SmartDashboard.putBoolean(motorPrefix + "SysId/ApplyResults", false);
        applySysIdResults(i);
      }

      if (SmartDashboard.getBoolean(benchPrefix + "StartSysId", false)) {
        SmartDashboard.putBoolean(benchPrefix + "StartSysId", false);
        tryStartBenchAction(i, "SysId", createBenchSysIdCommand(i));
      }

      if (SmartDashboard.getBoolean(benchPrefix + "StartManualRun", false)) {
        SmartDashboard.putBoolean(benchPrefix + "StartManualRun", false);
        tryStartBenchAction(i, "ManualRun", createBenchManualRunCommand(i));
      }

      if (SmartDashboard.getBoolean(motorPrefix + "Power/Detect", false)) {
        SmartDashboard.putBoolean(motorPrefix + "Power/Detect", false);
        tryStartBenchAction(i, "DetectPower", createDetectPowerChannelCommand(i));
      }

      if (SmartDashboard.getBoolean(benchPrefix + "Cancel", false)) {
        SmartDashboard.putBoolean(benchPrefix + "Cancel", false);
        cancelBenchAction(i, "Cancelled from dashboard");
      }

      if (isBenchActionActive(i) && !canRunBench(i)) {
        cancelBenchAction(i, "Bench safety released");
      }
    }
  }

  private Command createBenchSysIdCommand(int motorIndex) {
    return m_motorSystem.sysIdWithAnalysis(motorIndex)
        .beforeStarting(() -> setBenchStatus(motorIndex, "Running SysId"))
        .finallyDo(interrupted -> {
          m_motorSystem.getMotor(motorIndex).stop();
          clearBenchAction(motorIndex, interrupted ? "SysId cancelled" : "SysId complete");
        });
  }

  private Command createBenchManualRunCommand(int motorIndex) {
    return Commands.run(() -> runManualBenchOutput(motorIndex), m_motorSystem.getMotorChannel(motorIndex))
        .beforeStarting(() -> setBenchStatus(motorIndex, "Running manual test"))
        .finallyDo(interrupted -> {
          m_motorSystem.getMotor(motorIndex).stop();
          clearBenchAction(motorIndex, interrupted ? "Manual run cancelled" : "Manual run complete");
        });
  }

  private Command createDetectPowerChannelCommand(int motorIndex) {
    MotorConfiguration runtimeConfig =
        MotorConfiguration.fromMotorConfig(m_config, m_config.motors.get(motorIndex));
    if (!runtimeConfig.capabilities.supportsPowerChannelDetection) {
      return Commands.runOnce(() -> {
        SmartDashboard.putString(m_motorSystem.getMotorName(motorIndex) + "/Power/DetectStatus",
            "Auto-detect unavailable - no per-channel current telemetry");
        setBenchStatus(motorIndex, "Power detect unavailable");
      });
    }

    PowerDistribution pd = PowerDistributionRegistry.get(m_config.powerModuleType, m_config.powerModuleId);
    if (pd == null) {
      return Commands.runOnce(() -> {
        SmartDashboard.putString(m_motorSystem.getMotorName(motorIndex) + "/Power/DetectStatus",
            "Power module not available");
        setBenchStatus(motorIndex, "Power detect unavailable");
      });
    }

    final double[] baseline = new double[pd.getNumChannels()];
    final double[] peaks = new double[pd.getNumChannels()];
    final double detectVoltage = 6.0;

    return Commands.sequence(
            Commands.runOnce(() -> m_motorSystem.getMotor(motorIndex).stop(),
                m_motorSystem.getMotorChannel(motorIndex)),
            Commands.waitSeconds(0.2),
            Commands.runOnce(() -> {
              PowerChannelDetector.sampleCurrents(pd, baseline);
              System.arraycopy(baseline, 0, peaks, 0, baseline.length);
              SmartDashboard.putString(
                  m_motorSystem.getMotorName(motorIndex) + "/Power/DetectStatus",
                  "Sampling startup surge...");
            }),
            Commands.runEnd(() -> {
              m_motorSystem.getMotor(motorIndex).setVoltage(detectVoltage);
              PowerChannelDetector.capturePeakCurrents(pd, peaks);
            }, () -> m_motorSystem.getMotor(motorIndex).stop(),
                m_motorSystem.getMotorChannel(motorIndex)).withTimeout(0.2),
            Commands.runOnce(() -> {
              PowerChannelDetector.Result result = PowerChannelDetector.analyze(baseline, peaks);
              SmartDashboard.putString(
                  m_motorSystem.getMotorName(motorIndex) + "/Power/DetectStatus",
                  result.statusText());
              if (result.success()) {
                SmartDashboard.putNumber(
                    m_motorSystem.getMotorName(motorIndex) + "/Power/Channel",
                    result.detectedChannel());
              }
            }))
        .beforeStarting(() -> setBenchStatus(motorIndex, "Detecting power channel"))
        .finallyDo(interrupted -> {
          m_motorSystem.getMotor(motorIndex).stop();
          clearBenchAction(motorIndex, interrupted
              ? "Power detection cancelled"
              : "Power detection complete");
        });
  }

  private void runManualBenchOutput(int motorIndex) {
    String motorPrefix = m_motorSystem.getMotorName(motorIndex) + "/";
    String mode = m_dashboardRuntime.getSelectedControlMode(motorIndex);
    double target = SmartDashboard.getNumber(motorPrefix + "RunTarget", 0.0);
    switch (mode) {
      case "position":
        m_motorSystem.setPosition(motorIndex, target);
        break;
      case "profile":
        m_motorSystem.setProfiledPosition(motorIndex, target);
        break;
      case "voltage":
        m_motorSystem.getMotorChannel(motorIndex).setVoltage(target);
        break;
      case "duty":
        m_motorSystem.getMotorChannel(motorIndex).setDutyCycle(target);
        break;
      default:
        m_motorSystem.setVelocity(motorIndex, target);
        break;
    }
  }

  private void tryStartBenchAction(int motorIndex, String owner, Command command) {
    if (isBenchActionActive(motorIndex)) {
      setBenchStatus(motorIndex, "This motor already has an active bench action");
      return;
    }
    if (!canRunBench(motorIndex)) {
      setBenchStatus(motorIndex, "Requires DS Test + Armed + held A");
      return;
    }

    m_activeBenchCommands[motorIndex] = command;
    m_activeBenchOwners[motorIndex] = owner;
    SmartDashboard.putString(m_motorSystem.getMotorName(motorIndex) + "/Bench/ActionOwner", owner);
    CommandScheduler.getInstance().schedule(command);
  }

  private void cancelBenchAction(int motorIndex, String status) {
    Command command = m_activeBenchCommands[motorIndex];
    if (command != null) {
      CommandScheduler.getInstance().cancel(command);
    }
    clearBenchAction(motorIndex, status);
  }

  private void clearBenchAction(int motorIndex, String status) {
    m_activeBenchCommands[motorIndex] = null;
    m_activeBenchOwners[motorIndex] = null;
    SmartDashboard.putString(m_motorSystem.getMotorName(motorIndex) + "/Bench/ActionOwner", "None");
    setBenchStatus(motorIndex, status);
  }

  private boolean isBenchActionActive(int motorIndex) {
    Command command = m_activeBenchCommands[motorIndex];
    return command != null && CommandScheduler.getInstance().isScheduled(command);
  }

  private boolean canRunBench(int motorIndex) {
    String benchPrefix = m_motorSystem.getMotorName(motorIndex) + "/Bench/";
    return DriverStation.isTest()
        && SmartDashboard.getBoolean(benchPrefix + "Armed", false)
        && isDeadmanHeld();
  }

  private boolean isDeadmanHeld() {
    return m_driver.a().getAsBoolean();
  }

  private boolean normalControllerControlsEnabled() {
    return !DriverStation.isTest() && !anyBenchArmed();
  }

  private boolean anyBenchArmed() {
    for (int i = 0; i < m_config.motors.size(); i++) {
      if (SmartDashboard.getBoolean(m_motorSystem.getMotorName(i) + "/Bench/Armed", false)) {
        return true;
      }
    }
    return false;
  }

  private void setBenchStatus(int motorIndex, String status) {
    SmartDashboard.putString(m_motorSystem.getMotorName(motorIndex) + "/Bench/Status", status);
  }

  private static String getDsModeString() {
    if (DriverStation.isTest()) return "TEST";
    if (DriverStation.isAutonomous()) return "AUTO";
    if (DriverStation.isTeleop()) return "TELEOP";
    if (DriverStation.isDisabled()) return "DISABLED";
    return "UNKNOWN";
  }

  private static String describeTelemetry(MotorConfiguration config) {
    if (!config.capabilities.runtimeSupported) {
      return "Unavailable";
    }
    StringBuilder sb = new StringBuilder();
    if (config.capabilities.runtimeLimited) sb.append("Limited ");
    if (config.capabilities.hasPositionTelemetry) sb.append("Pos ");
    if (config.capabilities.hasVelocityTelemetry) sb.append("Vel ");
    if (config.capabilities.hasCurrentTelemetry) sb.append("Current ");
    if (config.capabilities.hasVoltageTelemetry) sb.append("Voltage ");
    if (config.capabilities.hasTemperatureTelemetry) sb.append("Temp ");
    return sb.length() == 0 ? "None" : sb.toString().trim();
  }

  private static String describeTests(MotorConfiguration config) {
    if (!config.capabilities.runtimeSupported) {
      return "Unsupported runtime";
    }
    StringBuilder sb = new StringBuilder();
    if (config.capabilities.runtimeLimited) sb.append("Limited ");
    if (config.capabilities.supportsSysId) sb.append("SysId ");
    if (config.capabilities.supportsVelocityClosedLoop) sb.append("Velocity ");
    if (config.capabilities.supportsPositionClosedLoop) sb.append("Position ");
    if (config.capabilities.supportsMotionProfile) sb.append("Profile ");
    return sb.length() == 0 ? "Manual only" : sb.toString().trim();
  }

  private static String describeUnlocks(MotorConfiguration config) {
    StringBuilder sb = new StringBuilder();
    if (!config.capabilities.runtimeSupported) {
      return "Runtime support not implemented for "
          + config.controllerFamily.displayName()
          + " over "
          + config.transport
          + ". "
          + config.capabilities.runtimeSupportNote;
    }
    if (config.capabilities.runtimeLimited && !config.capabilities.runtimeSupportNote.isBlank()) {
      sb.append(config.capabilities.runtimeSupportNote).append(' ');
    }
    if (!config.capabilities.supportsSysId) {
      sb.append("Add feedback for SysId. ");
    }
    if (!config.capabilities.hasCurrentTelemetry) {
      sb.append("Add CAN telemetry or PD channel for current. ");
    }
    if (!config.capabilities.supportsPowerChannelDetection) {
      sb.append("Power-channel auto-detect requires per-channel current telemetry. ");
    }
    if (!config.capabilities.hasTemperatureTelemetry) {
      sb.append("Temp telemetry unavailable on this path. ");
    }
    return sb.length() == 0 ? "All configured features unlocked" : sb.toString().trim();
  }

  /** Applies SysId-computed values to a motor and saves config. */
  private void applySysIdResults(int motorIndex) {
    SysIdAnalyzer.AnalysisResult result = m_motorSystem.getSysId(motorIndex).getLastResult();
    String name = m_config.motors.get(motorIndex).name;

    if (result == null || !result.valid) {
      System.out.println("No valid SysId results to apply for " + name);
      return;
    }

    MotorConfig mc = m_config.motors.get(motorIndex);
    String pidPrefix = m_motorSystem.getMotorName(motorIndex) + "/PID/";

    // Read kI from dashboard (not changed by SysId)
    double kI = SmartDashboard.getNumber(pidPrefix + "kI", mc.kI);

    // Update dashboard PID fields with SysId results
    SmartDashboard.putNumber(pidPrefix + "kP", result.kP_velocity);
    SmartDashboard.putNumber(pidPrefix + "kD", 0.0);
    SmartDashboard.putNumber(pidPrefix + "kV", result.kV);
    SmartDashboard.putNumber(pidPrefix + "kS", result.kS);
    SmartDashboard.putNumber(pidPrefix + "kA", result.kA);
    SmartDashboard.putNumber(pidPrefix + "kG", result.kG);

    // Hot-reload velocity PID (Slot0)
    m_motorSystem.updatePid(motorIndex, result.kP_velocity, kI, 0.0,
        result.kV, result.kS, result.kA, result.kG);

    // Hot-reload position PID (Slot1) if computed
    if (result.kP_position > 0) {
      m_motorSystem.updatePositionPid(motorIndex, result.kP_position, result.kD_position,
          result.kV, result.kS, result.kA, result.kG);
      mc.kP_pos = result.kP_position;
      mc.kD_pos = result.kD_position;
      SmartDashboard.putNumber(pidPrefix + "kP_pos", result.kP_position);
      SmartDashboard.putNumber(pidPrefix + "kD_pos", result.kD_position);
    }

    // Update config object
    mc.kP = result.kP_velocity;
    mc.kD = 0.0;
    mc.kV = result.kV;
    mc.kS = result.kS;
    mc.kA = result.kA;
    mc.kG = result.kG;

    System.out.printf("[Apply %s] kP=%.6f kV=%.6f kS=%.6f kA=%.6f kG=%.6f%n",
        name, result.kP_velocity, result.kV, result.kS, result.kA, result.kG);

    // Save to JSON
    if (MotorSystemConfigLoader.save(CONFIG_FILENAME, m_config)) {
      System.out.println("SysId results applied and saved for " + name);
    } else {
      System.out.println("SysId results applied but save failed for " + name);
    }
  }

  /** Reads PID values from dashboard for a single motor, hot-reloads it, saves config. */
  private void applyPidForMotor(int motorIndex) {
    MotorConfig mc = m_config.motors.get(motorIndex);
    applyDashboardSettingsToMotor(motorIndex);

    if (MotorSystemConfigLoader.save(CONFIG_FILENAME, m_config)) {
      System.out.println("PID applied and saved for " + mc.name);
    } else {
      System.out.println("PID applied but save failed for " + mc.name);
    }
  }

  /** Reads PID values from dashboard for all motors, hot-reloads, saves config. */
  private void applyPidConfig() {
    for (int i = 0; i < m_config.motors.size(); i++) {
      applyDashboardSettingsToMotor(i);
    }

    // Update setpoints in config
    String sys = m_config.systemName;
    for (var entry : m_config.setpoints.entrySet()) {
      double val = SmartDashboard.getNumber(sys + "/Setpoints/" + entry.getKey(), entry.getValue());
      m_config.setpoints.put(entry.getKey(), val);
    }

    // Update servo positions in config
    for (var entry : m_config.servoPositions.entrySet()) {
      double val = SmartDashboard.getNumber(sys + "/Servo/" + entry.getKey(), entry.getValue());
      m_config.servoPositions.put(entry.getKey(), val);
    }

    if (MotorSystemConfigLoader.save(CONFIG_FILENAME, m_config)) {
      System.out.println("PID configuration applied and saved!");
    } else {
      System.out.println("PID configuration applied but save failed!");
    }
  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }

  private void applyDashboardSettingsToMotor(int motorIndex) {
    MotorConfig mc = m_config.motors.get(motorIndex);
    String motorPrefix = m_motorSystem.getMotorName(motorIndex) + "/";
    String pidPrefix = motorPrefix + "PID/";

    double kP = SmartDashboard.getNumber(pidPrefix + "kP", mc.kP);
    double kI = SmartDashboard.getNumber(pidPrefix + "kI", mc.kI);
    double kD = SmartDashboard.getNumber(pidPrefix + "kD", mc.kD);
    double kV = SmartDashboard.getNumber(pidPrefix + "kV", mc.kV);
    double kS = SmartDashboard.getNumber(pidPrefix + "kS", mc.kS);
    double kA = SmartDashboard.getNumber(pidPrefix + "kA", mc.kA);
    double kG = SmartDashboard.getNumber(pidPrefix + "kG", mc.kG);
    double kPPos = SmartDashboard.getNumber(pidPrefix + "kP_pos", mc.kP_pos);
    double kDPos = SmartDashboard.getNumber(pidPrefix + "kD_pos", mc.kD_pos);
    double cruiseVel = SmartDashboard.getNumber(motorPrefix + "Motion/CruiseVel",
        mc.motionCruiseVelocity);
    double accel = SmartDashboard.getNumber(motorPrefix + "Motion/Accel",
        mc.motionAcceleration);
    double jerk = SmartDashboard.getNumber(motorPrefix + "Motion/Jerk",
        mc.motionJerk);
    double currLimit = SmartDashboard.getNumber(motorPrefix + "CurrentLimit", mc.currentLimit);
    double gearRatio = SmartDashboard.getNumber(motorPrefix + "GearRatio", mc.gearRatio);
    boolean fwdLimitEnabled = SmartDashboard.getBoolean(motorPrefix + "Limits/ForwardEnabled",
        mc.forwardLimit != Double.MAX_VALUE);
    boolean revLimitEnabled = SmartDashboard.getBoolean(motorPrefix + "Limits/ReverseEnabled",
        mc.reverseLimit != -Double.MAX_VALUE);
    double fwdLimit = SmartDashboard.getNumber(motorPrefix + "Limits/Forward", 0.0);
    double revLimit = SmartDashboard.getNumber(motorPrefix + "Limits/Reverse", 0.0);
    int powerChannel = (int) Math.round(
        SmartDashboard.getNumber(motorPrefix + "Power/Channel", mc.powerChannel));
    int quadratureA = (int) Math.round(
        SmartDashboard.getNumber(motorPrefix + "Feedback/QuadratureA", mc.quadratureChannelA));
    int quadratureB = (int) Math.round(
        SmartDashboard.getNumber(motorPrefix + "Feedback/QuadratureB", mc.quadratureChannelB));
    int dutyCycleChannel = (int) Math.round(
        SmartDashboard.getNumber(motorPrefix + "Feedback/DutyCycleChannel", mc.dutyCycleChannel));
    int analogChannel = (int) Math.round(
        SmartDashboard.getNumber(motorPrefix + "Feedback/AnalogChannel", mc.analogChannel));
    double distancePerPulseRot = SmartDashboard.getNumber(
        motorPrefix + "Feedback/DistancePerPulseRot", mc.feedbackDistancePerPulseRotations);
    double fullRangeRot = SmartDashboard.getNumber(
        motorPrefix + "Feedback/FullRangeRot", mc.feedbackFullRangeRotations);
    double offsetRot = SmartDashboard.getNumber(
        motorPrefix + "Feedback/OffsetRot", mc.feedbackOffsetRotations);
    boolean feedbackInverted = SmartDashboard.getBoolean(
        motorPrefix + "Feedback/Inverted", mc.feedbackInverted);
    boolean continuousWrap = SmartDashboard.getBoolean(
        motorPrefix + "Feedback/ContinuousWrap", mc.feedbackContinuousWrap);
    int samplesToAverage = (int) Math.round(
        SmartDashboard.getNumber(motorPrefix + "Feedback/SamplesToAverage", mc.feedbackSamplesToAverage));
    double wheelDiameter = SmartDashboard.getNumber(motorPrefix + "WheelDiameter", mc.wheelDiameter);
    double distancePerRotation = SmartDashboard.getNumber(
        motorPrefix + "DistancePerRotation", mc.distancePerRotation);
    double armLength = SmartDashboard.getNumber(motorPrefix + "ArmLength", mc.armLength);
    double mass = SmartDashboard.getNumber(motorPrefix + "Mass", mc.mass);

    mc.gearRatio = gearRatio;
    mc.kP = kP;
    mc.kI = kI;
    mc.kD = kD;
    mc.kV = kV;
    mc.kS = kS;
    mc.kA = kA;
    mc.kG = kG;
    mc.kP_pos = kPPos;
    mc.kD_pos = kDPos;
    mc.motionCruiseVelocity = cruiseVel;
    mc.motionAcceleration = accel;
    mc.motionJerk = jerk;
    mc.currentLimit = currLimit;
    mc.forwardLimit = fwdLimitEnabled ? fwdLimit : Double.MAX_VALUE;
    mc.reverseLimit = revLimitEnabled ? revLimit : -Double.MAX_VALUE;
    mc.powerChannel = powerChannel;
    mc.quadratureChannelA = quadratureA;
    mc.quadratureChannelB = quadratureB;
    mc.dutyCycleChannel = dutyCycleChannel;
    mc.analogChannel = analogChannel;
    mc.feedbackDistancePerPulseRotations = distancePerPulseRot;
    mc.feedbackFullRangeRotations = fullRangeRot;
    mc.feedbackOffsetRotations = offsetRot;
    mc.feedbackInverted = feedbackInverted;
    mc.feedbackContinuousWrap = continuousWrap;
    mc.feedbackSamplesToAverage = samplesToAverage;
    mc.wheelDiameter = wheelDiameter;
    mc.distancePerRotation = distancePerRotation;
    mc.armLength = armLength;
    mc.mass = mass;

    m_motorSystem.updatePid(motorIndex, kP, kI, kD, kV, kS, kA, kG);
    m_motorSystem.updatePositionPid(motorIndex, kPPos, kDPos, kV, kS, kA, kG);
    m_motorSystem.getMotor(motorIndex).configureMotionProfile(cruiseVel, accel, jerk);
    m_motorSystem.getMotor(motorIndex).updateCurrentLimit(currLimit);
    m_motorSystem.getMotor(motorIndex).updateSoftLimits(mc.forwardLimit, mc.reverseLimit);

    String sys = m_config.systemName;
    for (var entry : m_config.setpoints.entrySet()) {
      if (entry.getKey().contains(mc.name)) {
        double val = SmartDashboard.getNumber(
            sys + "/Setpoints/" + entry.getKey(), entry.getValue());
        m_config.setpoints.put(entry.getKey(), val);
      }
    }

    MotorConfiguration updatedConfig = MotorConfiguration.fromMotorConfig(m_config, mc);
    UnitConverter updatedGeometry = new UnitConverter(
        mc.getMechanismType(),
        mc.wheelDiameter,
        mc.distancePerRotation,
        mc.armLength,
        false);
    SmartDashboard.putString(motorPrefix + "Geometry", updatedGeometry.geometrySummary());
    SmartDashboard.putString(motorPrefix + "Capabilities/Telemetry", describeTelemetry(updatedConfig));
    SmartDashboard.putString(motorPrefix + "Capabilities/Tests", describeTests(updatedConfig));
    SmartDashboard.putString(motorPrefix + "Capabilities/Unlock", describeUnlocks(updatedConfig));
    SmartDashboard.putString(motorPrefix + "Support/Level", supportLevelLabel(updatedConfig));
    SmartDashboard.putString(motorPrefix + "Support/Note", supportNote(updatedConfig));
  }

  private void applySetupForMotor(int motorIndex) {
    MotorConfig mc = m_config.motors.get(motorIndex);
    applyDashboardSettingsToMotor(motorIndex);

    if (MotorSystemConfigLoader.save(CONFIG_FILENAME, m_config)) {
      setBenchStatus(motorIndex, "Setup applied and saved");
      System.out.println("Setup applied and saved for " + mc.name);
    } else {
      setBenchStatus(motorIndex, "Setup applied but save failed");
      System.out.println("Setup applied but save failed for " + mc.name);
    }
  }

  /** Returns a tuning workflow hint string based on mechanism type. */
  private static String getWorkflowHint(MechanismType mechType) {
    switch (mechType) {
      case SIMPLE:
        return "Tuning: 1.SysId -> 2.Apply -> 3.Test velocity setpoints";
      case ELEVATOR:
        return "Tuning: 1.Set limits -> 2.SysId -> 3.Apply -> 4.Set cruise/accel -> 5.Test profile";
      case ARM:
        return "Tuning: 1.Set limits -> 2.SysId -> 3.Apply -> 4.Set cruise/accel -> 5.Test profile";
      default:
        return "Tuning: 1.SysId -> 2.Apply -> 3.Test";
    }
  }

  private String powerDetectStatus(MotorConfiguration config) {
    if (!config.capabilities.supportsPowerChannelDetection) {
      return "Auto-detect unavailable - no per-channel current telemetry";
    }
    return "Idle - auto-detect then confirm before saving";
  }

  private static String supportLevelLabel(MotorConfiguration config) {
    return switch (config.capabilities.runtimeSupportLevel) {
      case SUPPORTED -> "Supported";
      case LIMITED -> "Limited";
      case UNIMPLEMENTED -> "Unimplemented";
      case UNSUPPORTED -> "Unsupported";
    };
  }

  private static String supportNote(MotorConfiguration config) {
    return config.capabilities.runtimeSupportNote == null
        || config.capabilities.runtimeSupportNote.isBlank()
        ? "Full runtime path available"
        : config.capabilities.runtimeSupportNote;
  }

  /** Resolves a button name string to the corresponding controller trigger. */
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
        System.err.println("Unknown button: " + buttonName + ", defaulting to A");
        return m_driver.a();
    }
  }
}
