package frc.robot.motor;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.motor.UniversalMotor.Mode;

public class PwmServoWrapper implements UniversalMotor {
  private final Servo servo;
  private final MotorKind motorKind;
  private Mode controlMode = Mode.POSITION;
  private double healthScore = 100.0;

  public PwmServoWrapper(MotorConfiguration config) {
    this.motorKind = config.motorKind;
    this.servo = new Servo(config.pwmChannel);
  }

  @Override
  public void setControlMode(Mode mode) { this.controlMode = mode; }
  @Override
  public Mode getControlMode() { return controlMode; }
  
  @Override
  public void set(double value) {
    if (motorKind == MotorKind.CONTINUOUS_SERVO) {
      servo.setSpeed(Math.max(-1, Math.min(1, value)));
    } else {
      servo.setAngle(Math.max(0, Math.min(180, value)));
    }
  }

  @Override
  public void setVoltage(double volts) { set(volts / 12.0); }
  @Override
  public void setVelocityRps(double rps) { set(rps); }
  @Override
  public void setPositionRotations(double rotations) { set(rotations * 360); }
  
  @Override
  public double getVelocityRps() { return 0; }
  @Override
  public double getPositionRotations() { return servo.getAngle() / 360.0; }
  
  @Override
  public void setBrake(boolean brake) {}
  @Override
  public void stop() { servo.setDisabled(); }
  @Override
  public void close() { servo.close(); }

  @Override
  public double getCurrent() { return 0; }
  @Override
  public double getTemperature() { return 0; }
  @Override
  public double getHealthScore() { return healthScore; }
  @Override
  public void setHealthScore(double score) { this.healthScore = score; }
  @Override
  public String getDeviceName() { return "PWM-Servo"; }
  @Override
  public boolean isServo() { return true; }
}