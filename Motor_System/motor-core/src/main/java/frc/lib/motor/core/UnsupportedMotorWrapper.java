package frc.lib.motor.core;

import frc.lib.motor.config.MotorConfiguration;

/**
 * No-op motor used when a controller/transport path is recognized but not implemented.
 * This keeps the app running so the dashboard can surface the unsupported state cleanly.
 */
public class UnsupportedMotorWrapper implements UniversalMotor {
  private final MotorConfiguration m_config;
  private Mode m_controlMode = Mode.DUTY_CYCLE;
  private double m_healthScore = 0.0;

  public UnsupportedMotorWrapper(MotorConfiguration config) {
    m_config = config;
  }

  @Override
  public void setControlMode(Mode mode) {
    m_controlMode = mode;
  }

  @Override
  public void setVoltage(double volts) {}

  @Override
  public void setVelocityRps(double rps) {}

  @Override
  public void setPositionRotations(double rotations) {}

  @Override
  public double getVelocityRps() {
    return 0.0;
  }

  @Override
  public double getPositionRotations() {
    return 0.0;
  }

  @Override
  public void setBrake(boolean brake) {}

  @Override
  public void stop() {}

  @Override
  public void close() {}

  @Override
  public Mode getControlMode() {
    return m_controlMode;
  }

  @Override
  public void set(double value) {}

  @Override
  public double getCurrent() {
    return 0.0;
  }

  @Override
  public double getAppliedVoltage() {
    return 0.0;
  }

  @Override
  public double getTemperature() {
    return 0.0;
  }

  @Override
  public boolean isFeedbackConnected() {
    return false;
  }

  @Override
  public double getHealthScore() {
    return m_healthScore;
  }

  @Override
  public void setHealthScore(double score) {
    m_healthScore = score;
  }

  @Override
  public String getDeviceName() {
    return "UNSUPPORTED-" + m_config.controllerFamily + "-" + m_config.transport;
  }

  @Override
  public boolean isServo() {
    return false;
  }
}
