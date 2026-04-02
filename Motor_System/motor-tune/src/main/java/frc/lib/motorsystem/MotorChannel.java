package frc.lib.motorsystem;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.lib.motor.config.ControllerPreset;
import frc.lib.motor.config.FeedbackSource;
import frc.lib.motor.config.MotorCapabilities;
import frc.lib.motor.config.MotorConfig;
import frc.lib.motor.config.MotorConfiguration;
import frc.lib.motor.config.MotorSystemConfig;
import frc.lib.motor.config.PowerModuleType;
import frc.lib.motor.config.TransportType;
import frc.lib.motor.core.MotorFactory;
import frc.lib.motor.core.UniversalMotor;
import frc.lib.motor.tuning.MotorSysId;
import frc.lib.motor.tuning.UnitConverter;
import java.util.ArrayList;
import java.util.List;

/** Per-motor bench subsystem that owns one motor and its tuning/runtime behavior. */
public class MotorChannel extends SubsystemBase {
  private final MotorSystemConfig m_systemConfig;
  private final MotorConfig m_motorConfig;
  private final MotorConfiguration m_runtimeConfig;
  private final UniversalMotor m_motor;
  private final MotorSysId m_sysId;
  private final ControllerPreset m_preset;
  private final String m_motorName;
  private final UnitConverter m_converter;
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
  }

  public record ChartOption(String key, String label) {}

  public record TelemetrySnapshot(
      double actualRpm,
      boolean atSetpoint,
      double setpointRpm,
      double currentAmps,
      double voltage,
      double positionRot,
      double velocityRps,
      double temperatureC,
      boolean hasRealUnits,
      double realSpeed,
      String speedUnit,
      double realPosition,
      String positionUnit,
      double chartValue,
      String chartLabel,
      String feedbackSource,
      boolean feedbackConnected,
      String closedLoopSource,
      String powerTelemetrySource,
      String telemetryHealth,
      String capabilitySummary,
      double measuredVelocityRps,
      double measuredPositionRot,
      double measuredVoltage,
      double measuredCurrent,
      double measuredTemperatureC) {}

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

  public List<String> getAvailableControlModes() {
    List<String> modes = new ArrayList<>();
    modes.add("velocity");
    if (m_runtimeConfig.capabilities.supportsPositionClosedLoop) {
      modes.add("position");
    }
    if (m_runtimeConfig.capabilities.supportsMotionProfile) {
      modes.add("profile");
    }
    modes.add("voltage");
    modes.add("duty");
    return List.copyOf(modes);
  }

  public List<ChartOption> getAvailableChartOptions() {
    List<ChartOption> options = new ArrayList<>();
    options.add(new ChartOption("rpm", "RPM"));
    options.add(new ChartOption("current", "Current (A)"));
    options.add(new ChartOption("voltage", "Voltage (V)"));
    options.add(new ChartOption("position", "Position (rot)"));
    options.add(new ChartOption("temperature", "Temperature (C)"));
    if (m_converter.hasGeometry()) {
      options.add(new ChartOption("realspeed", "Real Speed"));
    }
    return List.copyOf(options);
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
    m_motor.periodic();
  }

  public TelemetrySnapshot createTelemetrySnapshot(boolean useMetric, String chartType) {
    double actualRpm = m_enabled ? getVelocityRpm() : 0.0;
    double currentAmps = m_enabled ? getCurrent() : 0.0;
    double voltage = m_enabled ? getAppliedVoltage() : 0.0;
    double positionRot = m_enabled ? m_motor.getPositionRotations() : 0.0;
    double velocityRps = m_enabled ? m_motor.getVelocityRps() : 0.0;
    double temperatureC = m_enabled ? getTemperature() : 0.0;

    m_converter.setUseMetric(useMetric);
    boolean hasRealUnits = m_converter.hasGeometry();
    double realSpeed = hasRealUnits ? m_converter.convertVelocity(velocityRps) : 0.0;
    double realPosition = hasRealUnits ? m_converter.convertPosition(positionRot) : 0.0;

    double chartValue;
    String chartLabel;
    switch (chartType == null ? "rpm" : chartType) {
      case "current":
        chartValue = currentAmps;
        chartLabel = "Current (A)";
        break;
      case "voltage":
        chartValue = voltage;
        chartLabel = "Voltage (V)";
        break;
      case "position":
        chartValue = positionRot;
        chartLabel = "Position (rot)";
        break;
      case "temperature":
        chartValue = temperatureC;
        chartLabel = "Temperature (C)";
        break;
      case "realspeed":
        chartValue = realSpeed;
        chartLabel = "Speed (" + m_converter.velocityUnit() + ")";
        break;
      default:
        chartValue = actualRpm;
        chartLabel = "RPM";
        break;
    }

    MotorCapabilities capabilities = m_runtimeConfig.capabilities;

    return new TelemetrySnapshot(
        actualRpm,
        m_enabled && isAtSetpoint(),
        m_setpointRpm,
        currentAmps,
        voltage,
        positionRot,
        velocityRps,
        temperatureC,
        hasRealUnits,
        realSpeed,
        m_converter.velocityUnit(),
        realPosition,
        m_converter.positionUnit(),
        chartValue,
        chartLabel,
        m_motorConfig.feedbackSource,
        m_motor.isFeedbackConnected(),
        controlPathLabel(capabilities),
        powerTelemetryLabel(),
        telemetryHealth(capabilities),
        capabilitySummary(capabilities),
        m_motor.getVelocityRps(),
        m_motor.getPositionRotations(),
        m_motor.getAppliedVoltage(),
        m_motor.getCurrent(),
        m_motor.getTemperature());
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
