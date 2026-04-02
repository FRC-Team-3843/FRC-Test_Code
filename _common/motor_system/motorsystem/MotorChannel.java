package frc.robot.motorsystem;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.motor.tuning.ControllerPreset;
import frc.robot.motor.config.FeedbackSource;
import frc.robot.motor.config.MotorCapabilities;
import frc.robot.motor.config.MotorConfig;
import frc.robot.motor.config.MotorConfiguration;
import frc.robot.motor.core.MotorFactory;
import frc.robot.motor.tuning.MotorSysId;
import frc.robot.motor.config.MotorSystemConfig;
import frc.robot.motor.config.PowerModuleType;
import frc.robot.motor.config.TransportType;
import frc.robot.motor.tuning.UnitConverter;
import frc.robot.motor.core.UniversalMotor;

/** Per-motor bench subsystem that owns one motor, its SysId flow, and dashboard telemetry. */
public class MotorChannel extends SubsystemBase {
  private final MotorSystemConfig m_systemConfig;
  private final MotorConfig m_motorConfig;
  private final MotorConfiguration m_runtimeConfig;
  private final UniversalMotor m_motor;
  private final MotorSysId m_sysId;
  private final ControllerPreset m_preset;
  private final String m_motorName;
  private final UnitConverter m_converter;
  private final SendableChooser<String> m_chartChooser;
  private final SendableChooser<String> m_controlModeChooser;
  private final double m_velocityToleranceRpm;

  private boolean m_enabled = true;
  private double m_setpointRpm = 0.0;

  public MotorChannel(MotorSystemConfig systemConfig, MotorConfig motorConfig) {
    m_systemConfig = systemConfig;
    m_motorConfig = motorConfig;
    m_runtimeConfig = MotorConfiguration.fromMotorConfig(systemConfig, motorConfig);
    m_motorName = systemConfig.motorPrefix(motorConfig.name);
    m_preset = motorConfig.getControllerPreset();
    m_motor = MotorFactory.create(m_runtimeConfig);
    m_sysId = new MotorSysId(
        m_motor, m_motorName, this,
        motorConfig.getMechanismType(), m_preset, motorConfig);
    m_converter = new UnitConverter(motorConfig.getMechanismType(),
        motorConfig.wheelDiameter, motorConfig.distancePerRotation, motorConfig.armLength, false);
    m_velocityToleranceRpm = systemConfig.velocityToleranceRpm;

    m_chartChooser = new SendableChooser<>();
    m_chartChooser.setDefaultOption("RPM", "rpm");
    m_chartChooser.addOption("Current (A)", "current");
    m_chartChooser.addOption("Voltage (V)", "voltage");
    m_chartChooser.addOption("Position (rot)", "position");
    m_chartChooser.addOption("Temperature (C)", "temperature");
    if (m_converter.hasGeometry()) {
      m_chartChooser.addOption("Real Speed", "realspeed");
    }
    SmartDashboard.putData(m_motorName + "/ChartType", m_chartChooser);

    m_controlModeChooser = new SendableChooser<>();
    m_controlModeChooser.setDefaultOption("Velocity", "velocity");
    if (m_runtimeConfig.capabilities.supportsPositionClosedLoop) {
      m_controlModeChooser.addOption("Position", "position");
    }
    if (m_runtimeConfig.capabilities.supportsMotionProfile) {
      m_controlModeChooser.addOption("Profile", "profile");
    }
    m_controlModeChooser.addOption("Voltage", "voltage");
    m_controlModeChooser.addOption("Duty", "duty");
    SmartDashboard.putData(m_motorName + "/ControlModeChooser", m_controlModeChooser);
  }

  public UniversalMotor getMotor() {
    return m_motor;
  }

  public MotorSysId getSysId() {
    return m_sysId;
  }

  public MotorConfig getMotorConfig() {
    return m_motorConfig;
  }

  public MotorConfiguration getRuntimeConfig() {
    return m_runtimeConfig;
  }

  public String getMotorName() {
    return m_motorName;
  }

  public ControllerPreset getPreset() {
    return m_preset;
  }

  public void setVelocity(double rpm) {
    m_setpointRpm = rpm;
    if (m_enabled) {
      m_motor.setVelocityRps(rpm / 60.0);
    }
  }

  public void setPosition(double rotations) {
    if (m_enabled) {
      m_motor.setPositionRotations(rotations);
    }
  }

  public void setProfiledPosition(double rotations) {
    if (m_enabled) {
      m_motor.setProfiledPosition(rotations);
    }
  }

  public void setVoltage(double volts) {
    if (m_enabled) {
      m_motor.setVoltage(volts);
    }
  }

  public void setDutyCycle(double dutyCycle) {
    if (m_enabled) {
      m_motor.setControlMode(UniversalMotor.Mode.DUTY_CYCLE);
      m_motor.set(dutyCycle);
    }
  }

  public void setEnabled(boolean enabled) {
    m_enabled = enabled;
    if (!enabled) {
      stop();
    }
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  public double getVelocityRpm() {
    return m_motor.getVelocityRps() * 60.0;
  }

  public boolean isAtSetpoint() {
    if (m_setpointRpm == 0.0) {
      return false;
    }
    return Math.abs(getVelocityRpm() - m_setpointRpm) < m_velocityToleranceRpm;
  }

  public double getCurrent() {
    return m_motor.getCurrent();
  }

  public double getAppliedVoltage() {
    return m_motor.getAppliedVoltage();
  }

  public double getTemperature() {
    return m_motor.getTemperature();
  }

  public String getSelectedControlMode() {
    String selected = m_controlModeChooser.getSelected();
    return selected == null ? "velocity" : selected;
  }

  public void updatePid(double kP, double kI, double kD,
                        double kV, double kS, double kA, double kG) {
    m_motor.updatePidConfig(kP, kI, kD, kV, kS, kA, kG);
  }

  public void updatePositionPid(double kP, double kD,
                                double kV, double kS, double kA, double kG) {
    m_motor.updatePositionPid(kP, kD, kV, kS, kA, kG);
  }

  public void configureMotionProfile(double cruiseVelocityRps, double accelerationRps2,
                                     double jerkRps3) {
    m_motor.configureMotionProfile(cruiseVelocityRps, accelerationRps2, jerkRps3);
  }

  public void updateCurrentLimit(double currentLimitAmps) {
    m_motor.updateCurrentLimit(currentLimitAmps);
  }

  public void updateSoftLimits(double forwardLimitRotations, double reverseLimitRotations) {
    m_motor.updateSoftLimits(forwardLimitRotations, reverseLimitRotations);
  }

  public Command sysIdWithAnalysis() {
    return m_sysId.fullRoutineWithAnalysis();
  }

  public Command sysId() {
    return m_sysId.fullRoutine();
  }

  public void stop() {
    m_motor.stop();
    m_setpointRpm = 0.0;
  }

  public void close() {
    m_motor.close();
  }

  @Override
  public void periodic() {
    String prefix = m_motorName + "/";
    boolean useMetric = SmartDashboard.getBoolean(m_systemConfig.systemName + "/UseMetric", false);

    m_enabled = SmartDashboard.getBoolean(prefix + "Enabled", true);
    m_motor.periodic();

    double rpm = m_enabled ? getVelocityRpm() : 0.0;
    SmartDashboard.putNumber(prefix + "ActualRPM", rpm);
    SmartDashboard.putBoolean(prefix + "AtSetpoint", m_enabled && isAtSetpoint());
    SmartDashboard.putNumber(prefix + "SetpointRPM", m_setpointRpm);
    SmartDashboard.putNumber(prefix + "CurrentAmps", m_enabled ? getCurrent() : 0.0);
    SmartDashboard.putNumber(prefix + "Voltage", m_enabled ? getAppliedVoltage() : 0.0);
    SmartDashboard.putNumber(prefix + "PositionRot", m_enabled ? m_motor.getPositionRotations() : 0.0);
    SmartDashboard.putNumber(prefix + "VelocityRps", m_enabled ? m_motor.getVelocityRps() : 0.0);
    SmartDashboard.putNumber(prefix + "TemperatureC", m_enabled ? getTemperature() : 0.0);

    m_converter.setUseMetric(useMetric);
    if (m_converter.hasGeometry()) {
      double rps = m_enabled ? m_motor.getVelocityRps() : 0.0;
      double rot = m_enabled ? m_motor.getPositionRotations() : 0.0;
      SmartDashboard.putNumber(prefix + "RealSpeed", m_converter.convertVelocity(rps));
      SmartDashboard.putString(prefix + "SpeedUnit", m_converter.velocityUnit());
      SmartDashboard.putNumber(prefix + "RealPosition", m_converter.convertPosition(rot));
      SmartDashboard.putString(prefix + "PosUnit", m_converter.positionUnit());
    }

    String chartType = m_chartChooser.getSelected();
    if (chartType == null) {
      chartType = "rpm";
    }
    double chartValue;
    String chartLabel;
    switch (chartType) {
      case "current":
        chartValue = m_enabled ? getCurrent() : 0.0;
        chartLabel = "Current (A)";
        break;
      case "voltage":
        chartValue = m_enabled ? getAppliedVoltage() : 0.0;
        chartLabel = "Voltage (V)";
        break;
      case "position":
        chartValue = m_enabled ? m_motor.getPositionRotations() : 0.0;
        chartLabel = "Position (rot)";
        break;
      case "temperature":
        chartValue = m_enabled ? getTemperature() : 0.0;
        chartLabel = "Temperature (C)";
        break;
      case "realspeed":
        double rps = m_enabled ? m_motor.getVelocityRps() : 0.0;
        chartValue = m_converter.convertVelocity(rps);
        chartLabel = "Speed (" + m_converter.velocityUnit() + ")";
        break;
      default:
        chartValue = rpm;
        chartLabel = "RPM";
        break;
    }
    SmartDashboard.putNumber(prefix + "ChartValue", chartValue);
    SmartDashboard.putString(prefix + "ChartLabel", chartLabel);

    publishTestingStatus(prefix);
  }

  private void publishTestingStatus(String prefix) {
    MotorCapabilities capabilities = m_runtimeConfig.capabilities;

    SmartDashboard.putString(prefix + "Testing/FeedbackSource", m_motorConfig.feedbackSource);
    SmartDashboard.putBoolean(prefix + "Testing/FeedbackConnected", m_motor.isFeedbackConnected());
    SmartDashboard.putString(prefix + "Testing/ClosedLoopSource", controlPathLabel(capabilities));
    SmartDashboard.putString(prefix + "Testing/PowerTelemetrySource", powerTelemetryLabel());
    SmartDashboard.putString(prefix + "Testing/TelemetryHealth", telemetryHealth(capabilities));
    SmartDashboard.putString(prefix + "Testing/CapabilitySummary", capabilitySummary(capabilities));
    SmartDashboard.putNumber(prefix + "Testing/MeasuredVelocityRps", m_motor.getVelocityRps());
    SmartDashboard.putNumber(prefix + "Testing/MeasuredPositionRot", m_motor.getPositionRotations());
    SmartDashboard.putNumber(prefix + "Testing/MeasuredVoltage", m_motor.getAppliedVoltage());
    SmartDashboard.putNumber(prefix + "Testing/MeasuredCurrent", m_motor.getCurrent());
    SmartDashboard.putNumber(prefix + "Testing/MeasuredTemperatureC", m_motor.getTemperature());
  }

  private String powerTelemetryLabel() {
    if (m_motorConfig.powerChannel >= 0 && m_systemConfig.powerModuleType != PowerModuleType.NONE) {
      return m_systemConfig.powerModuleType + " ch " + m_motorConfig.powerChannel;
    }
    if (m_runtimeConfig.capabilities.hasCurrentTelemetry
        && m_runtimeConfig.transport == TransportType.CAN) {
      return "Controller CAN telemetry";
    }
    return "None";
  }

  private static String controlPathLabel(MotorCapabilities capabilities) {
    if (capabilities.usesSoftwareClosedLoop) {
      return "Software closed-loop";
    }
    if (capabilities.supportsVelocityClosedLoop || capabilities.supportsPositionClosedLoop) {
      return "Controller closed-loop";
    }
    return "Open-loop only";
  }

  private String telemetryHealth(MotorCapabilities capabilities) {
    if ((m_runtimeConfig.feedbackSource != FeedbackSource.NONE)
        && !m_motor.isFeedbackConnected()) {
      return "Configured feedback not connected";
    }
    if (!capabilities.hasCurrentTelemetry && !capabilities.hasTemperatureTelemetry
        && !capabilities.hasVelocityTelemetry) {
      return "Minimal telemetry";
    }
    return "Telemetry available";
  }

  private static String capabilitySummary(MotorCapabilities capabilities) {
    StringBuilder sb = new StringBuilder();
    if (capabilities.supportsSysId) {
      sb.append("SysId ");
    }
    if (capabilities.supportsVelocityClosedLoop) {
      sb.append("Velocity ");
    }
    if (capabilities.supportsPositionClosedLoop) {
      sb.append("Position ");
    }
    if (capabilities.supportsMotionProfile) {
      sb.append("Profile ");
    }
    return sb.length() == 0 ? "Manual only" : sb.toString().trim();
  }
}
