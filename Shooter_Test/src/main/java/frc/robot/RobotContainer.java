// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.motor.ControllerPreset;
import frc.robot.motor.MechanismType;
import frc.robot.motor.MotorConfig;
import frc.robot.motor.MotorSystemConfig;
import frc.robot.motor.SysIdAnalyzer;
import frc.robot.motor.SysIdParams;
import frc.robot.motor.UnitConverter;
import frc.robot.shooter.ShooterSubsystem;

public class RobotContainer {
  private static final String CONFIG_FILENAME = "motor-config.json";

  private final MotorSystemConfig m_config;
  private final CommandXboxController m_driver;
  private final ShooterSubsystem m_shooter;
  private final boolean[] m_dashboardRunning;  // tracks per-motor dashboard run state

  public RobotContainer(MotorSystemConfig config) {
    m_config = config;
    m_driver = new CommandXboxController(config.driverControllerPort);
    m_shooter = new ShooterSubsystem(config);
    m_dashboardRunning = new boolean[config.motors.size()];

    initDashboard();
    configureBindings();
  }

  /** Initializes all dashboard fields for each motor in the config. */
  private void initDashboard() {
    String sys = m_config.systemName;

    for (int i = 0; i < m_config.motors.size(); i++) {
      MotorConfig mc = m_config.motors.get(i);
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
      SysIdParams.publishDefaults(prefix + "SysId/", preset);

      // Apply toggles
      SmartDashboard.putBoolean(prefix + "SysId/ApplyResults", false);
      SmartDashboard.putBoolean(prefix + "ApplyPID", false);

      // Dashboard run controls
      SmartDashboard.putBoolean(prefix + "Run", false);
      SmartDashboard.putNumber(prefix + "RunTarget", 0.0);
      SmartDashboard.putString(prefix + "ControlMode", "velocity");

      // Position PID fields
      SmartDashboard.putNumber(prefix + "PID/kP_pos", mc.kP_pos);
      SmartDashboard.putNumber(prefix + "PID/kD_pos", mc.kD_pos);

      // Motion profiling fields (editable)
      SmartDashboard.putNumber(prefix + "Motion/CruiseVel", mc.motionCruiseVelocity);
      SmartDashboard.putNumber(prefix + "Motion/Accel", mc.motionAcceleration);
      SmartDashboard.putNumber(prefix + "Motion/Jerk", mc.motionJerk);

      // Soft limits (editable)
      double fwdLimit = mc.forwardLimit == Double.MAX_VALUE ? 0.0 : mc.forwardLimit;
      double revLimit = mc.reverseLimit == -Double.MAX_VALUE ? 0.0 : mc.reverseLimit;
      SmartDashboard.putNumber(prefix + "Limits/Forward", fwdLimit);
      SmartDashboard.putNumber(prefix + "Limits/Reverse", revLimit);

      // Current limit (editable)
      SmartDashboard.putNumber(prefix + "CurrentLimit", mc.currentLimit);

      // Physical estimate placeholders
      SmartDashboard.putNumber(prefix + "SysId/Est_Inertia", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_Friction", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_Mass_kg", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_Efficiency", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_MaxAccel", 0.0);
      SmartDashboard.putNumber(prefix + "SysId/Est_FreeSpeed", 0.0);

      // Info
      SmartDashboard.putString(prefix + "MechanismType", mc.mechanismType);
      SmartDashboard.putString(prefix + "ControllerType", mc.controllerType);

      // Geometry summary
      UnitConverter uc = new UnitConverter(mc.getMechanismType(),
          mc.wheelDiameter, mc.distancePerRotation, mc.armLength, false);
      SmartDashboard.putString(prefix + "Geometry", uc.geometrySummary());

      // Workflow hint
      String hint = getWorkflowHint(mc.getMechanismType());
      SmartDashboard.putString(prefix + "WorkflowHint", hint);
    }

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
        .whileTrue(Commands.run(() -> {
          for (int i = 0; i < m_config.motors.size(); i++) {
            String key = "setpoint1_" + m_config.motors.get(i).name;
            double rpm = SmartDashboard.getNumber(sys + "/Setpoints/" + key,
                m_config.setpoints.getOrDefault(key, 0.0));
            m_shooter.setVelocity(i, rpm);
          }
          Double servoPos = readServoPosition("position1");
          if (servoPos != null) m_shooter.setServoPosition(servoPos);
        }, m_shooter))
        .onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // Setpoint 2
    resolveButton(m_config.buttonBindings.getOrDefault("setpoint2", "B"))
        .whileTrue(Commands.run(() -> {
          for (int i = 0; i < m_config.motors.size(); i++) {
            String key = "setpoint2_" + m_config.motors.get(i).name;
            double rpm = SmartDashboard.getNumber(sys + "/Setpoints/" + key,
                m_config.setpoints.getOrDefault(key, 0.0));
            m_shooter.setVelocity(i, rpm);
          }
          Double servoPos = readServoPosition("position2");
          if (servoPos != null) m_shooter.setServoPosition(servoPos);
        }, m_shooter))
        .onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // Apply PID from dashboard for all motors and save to JSON
    resolveButton(m_config.buttonBindings.getOrDefault("applyPid", "X"))
        .onTrue(Commands.runOnce(() -> {
          applyPidConfig();
          for (int i = 0; i < m_config.motors.size(); i++) {
            SmartDashboard.putBoolean(m_shooter.getMotorName(i) + "/ApplyPID", false);
          }
        }));

    // SysId per motor — bind each motor's SysId to its configured button
    for (int i = 0; i < m_config.motors.size(); i++) {
      String motorName = m_config.motors.get(i).name;
      String buttonKey = "sysId_" + motorName;
      String button = m_config.buttonBindings.get(buttonKey);
      if (button != null) {
        final int idx = i;
        resolveButton(button).whileTrue(m_shooter.sysIdWithAnalysis(idx));
      }
    }

    // Servo positions
    String servoBtn1 = m_config.buttonBindings.getOrDefault("servoPos1", "LEFT_BUMPER");
    resolveButton(servoBtn1).onTrue(Commands.runOnce(() -> {
      Double pos = readServoPosition("position1");
      if (pos != null) m_shooter.setServoPosition(pos);
    }));

    String servoBtn2 = m_config.buttonBindings.getOrDefault("servoPos2", "RIGHT_BUMPER");
    resolveButton(servoBtn2).onTrue(Commands.runOnce(() -> {
      Double pos = readServoPosition("position2");
      if (pos != null) m_shooter.setServoPosition(pos);
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
    for (int i = 0; i < m_config.motors.size(); i++) {
      String motorPrefix = m_shooter.getMotorName(i) + "/";

      // Per-motor Apply PID toggle
      if (SmartDashboard.getBoolean(motorPrefix + "ApplyPID", false)) {
        SmartDashboard.putBoolean(motorPrefix + "ApplyPID", false);
        applyPidForMotor(i);
      }

      // Per-motor Apply SysId toggle
      if (SmartDashboard.getBoolean(motorPrefix + "SysId/ApplyResults", false)) {
        SmartDashboard.putBoolean(motorPrefix + "SysId/ApplyResults", false);
        applySysIdResults(i);
      }

      // Dashboard run toggle — hold true to run motor, stop on release
      boolean runToggle = SmartDashboard.getBoolean(motorPrefix + "Run", false);
      if (runToggle) {
        String mode = SmartDashboard.getString(motorPrefix + "ControlMode", "velocity");
        double target = SmartDashboard.getNumber(motorPrefix + "RunTarget", 0.0);
        switch (mode) {
          case "position":
            m_shooter.setPosition(i, target);
            break;
          case "profile":
            m_shooter.setProfiledPosition(i, target);
            break;
          default: // "velocity"
            m_shooter.setVelocity(i, target);
            break;
        }
        m_dashboardRunning[i] = true;
      } else if (m_dashboardRunning[i]) {
        m_shooter.setVelocity(i, 0.0);
        m_shooter.getMotor(i).stop();
        m_dashboardRunning[i] = false;
      }
    }
  }

  /** Applies SysId-computed values to a motor and saves config. */
  private void applySysIdResults(int motorIndex) {
    SysIdAnalyzer.AnalysisResult result = m_shooter.getSysId(motorIndex).getLastResult();
    String name = m_config.motors.get(motorIndex).name;

    if (result == null || !result.valid) {
      System.out.println("No valid SysId results to apply for " + name);
      return;
    }

    MotorConfig mc = m_config.motors.get(motorIndex);
    String pidPrefix = m_shooter.getMotorName(motorIndex) + "/PID/";

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
    m_shooter.updatePid(motorIndex, result.kP_velocity, kI, 0.0,
        result.kV, result.kS, result.kA, result.kG);

    // Hot-reload position PID (Slot1) if computed
    if (result.kP_position > 0) {
      m_shooter.updatePositionPid(motorIndex, result.kP_position, result.kD_position,
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
    if (m_config.save(CONFIG_FILENAME)) {
      System.out.println("SysId results applied and saved for " + name);
    } else {
      System.out.println("SysId results applied but save failed for " + name);
    }
  }

  /** Reads PID values from dashboard for a single motor, hot-reloads it, saves config. */
  private void applyPidForMotor(int motorIndex) {
    MotorConfig mc = m_config.motors.get(motorIndex);
    String prefix = m_shooter.getMotorName(motorIndex) + "/PID/";

    double kP = SmartDashboard.getNumber(prefix + "kP", mc.kP);
    double kI = SmartDashboard.getNumber(prefix + "kI", mc.kI);
    double kD = SmartDashboard.getNumber(prefix + "kD", mc.kD);
    double kV = SmartDashboard.getNumber(prefix + "kV", mc.kV);
    double kS = SmartDashboard.getNumber(prefix + "kS", mc.kS);
    double kA = SmartDashboard.getNumber(prefix + "kA", mc.kA);
    double kG = SmartDashboard.getNumber(prefix + "kG", mc.kG);

    m_shooter.updatePid(motorIndex, kP, kI, kD, kV, kS, kA, kG);

    // Position PID (Slot1)
    double kP_pos = SmartDashboard.getNumber(prefix + "kP_pos", mc.kP_pos);
    double kD_pos = SmartDashboard.getNumber(prefix + "kD_pos", mc.kD_pos);
    m_shooter.updatePositionPid(motorIndex, kP_pos, kD_pos, kV, kS, kA, kG);

    mc.kP = kP; mc.kI = kI; mc.kD = kD;
    mc.kV = kV; mc.kS = kS; mc.kA = kA; mc.kG = kG;
    mc.kP_pos = kP_pos; mc.kD_pos = kD_pos;

    // Motion profiling from dashboard
    double cruiseVel = SmartDashboard.getNumber(prefix.replace("PID/", "") + "Motion/CruiseVel",
        mc.motionCruiseVelocity);
    double accel = SmartDashboard.getNumber(prefix.replace("PID/", "") + "Motion/Accel",
        mc.motionAcceleration);
    double jerk = SmartDashboard.getNumber(prefix.replace("PID/", "") + "Motion/Jerk",
        mc.motionJerk);
    mc.motionCruiseVelocity = cruiseVel;
    mc.motionAcceleration = accel;
    mc.motionJerk = jerk;
    m_shooter.getMotor(motorIndex).configureMotionProfile(cruiseVel, accel, jerk);

    // Current limit from dashboard
    double currLimit = SmartDashboard.getNumber(prefix.replace("PID/", "") + "CurrentLimit",
        mc.currentLimit);
    mc.currentLimit = currLimit;

    // Also update this motor's setpoints
    String sys = m_config.systemName;
    for (var entry : m_config.setpoints.entrySet()) {
      if (entry.getKey().contains(mc.name)) {
        double val = SmartDashboard.getNumber(
            sys + "/Setpoints/" + entry.getKey(), entry.getValue());
        m_config.setpoints.put(entry.getKey(), val);
      }
    }

    if (m_config.save(CONFIG_FILENAME)) {
      System.out.println("PID applied and saved for " + mc.name);
    } else {
      System.out.println("PID applied but save failed for " + mc.name);
    }
  }

  /** Reads PID values from dashboard for all motors, hot-reloads, saves config. */
  private void applyPidConfig() {
    for (int i = 0; i < m_config.motors.size(); i++) {
      MotorConfig mc = m_config.motors.get(i);
      String prefix = m_shooter.getMotorName(i) + "/PID/";

      double kP = SmartDashboard.getNumber(prefix + "kP", mc.kP);
      double kI = SmartDashboard.getNumber(prefix + "kI", mc.kI);
      double kD = SmartDashboard.getNumber(prefix + "kD", mc.kD);
      double kV = SmartDashboard.getNumber(prefix + "kV", mc.kV);
      double kS = SmartDashboard.getNumber(prefix + "kS", mc.kS);
      double kA = SmartDashboard.getNumber(prefix + "kA", mc.kA);
      double kG = SmartDashboard.getNumber(prefix + "kG", mc.kG);

      m_shooter.updatePid(i, kP, kI, kD, kV, kS, kA, kG);

      mc.kP = kP;
      mc.kI = kI;
      mc.kD = kD;
      mc.kV = kV;
      mc.kS = kS;
      mc.kA = kA;
      mc.kG = kG;
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

    if (m_config.save(CONFIG_FILENAME)) {
      System.out.println("PID configuration applied and saved!");
    } else {
      System.out.println("PID configuration applied but save failed!");
    }
  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
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
