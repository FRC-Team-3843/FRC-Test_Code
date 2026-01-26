package frc.robot.motor;

import frc.robot.motor.UniversalMotor.Mode;

public class PwmMotorWrapper implements UniversalMotor {
  private final MotorConfiguration config;
  private Mode controlMode = Mode.DUTY_CYCLE;
  private double healthScore = 100.0;

  public PwmMotorWrapper(MotorConfiguration config) {
    this.config = config;
    // ...
  }
  
  // Implementation of all methods ...
  // Returning 0 for all getters
  // ...

  @Override
  public void setControlMode(Mode mode) { this.controlMode = mode; }
  @Override
  public Mode getControlMode() { return controlMode; }
  @Override
  public void setVoltage(double volts) {}
  @Override
  public void setVelocityRps(double rps) {}
  @Override
  public void setPositionRotations(double rotations) {}
  @Override
  public double getVelocityRps() { return 0; }
  @Override
  public double getPositionRotations() { return 0; }
  @Override
  public void setBrake(boolean brake) {}
  @Override
  public void stop() {}
  @Override
  public void set(double value) {}
  @Override
  public double getCurrent() { return 0; }
  @Override
  public double getTemperature() { return 0; }
  @Override
  public double getHealthScore() { return healthScore; }
  @Override
  public void setHealthScore(double score) { this.healthScore = score; }
  @Override
  public String getDeviceName() { return "PWM"; }
  @Override
  public boolean isServo() { return false; }
}
