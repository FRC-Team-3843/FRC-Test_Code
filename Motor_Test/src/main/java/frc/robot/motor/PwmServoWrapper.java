package frc.robot.motor;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;

public class PwmServoWrapper implements UniversalMotor {
  private final MotorKind motorKind;
  private final Servo servo;
  private Mode controlMode = Mode.POSITION;
  private double healthScore = 0.0;

  private double lastPositionDeg = 0.0;
  private double lastTimestamp = Timer.getFPGATimestamp();
  private double velocityDegPerSec = 0.0;

  public PwmServoWrapper(MotorConfiguration config) {
    motorKind = config.motorKind;
    servo = new Servo(config.pwmChannel);
  }

  @Override
  public void setControlMode(Mode mode) {
    controlMode = mode;
  }

  @Override
  public Mode getControlMode() {
    return controlMode;
  }

  @Override
  public void set(double value) {
    if (motorKind == MotorKind.CONTINUOUS_SERVO) {
      double speed = clamp(value, -1.0, 1.0);
      servo.setSpeed(speed);
    } else {
      double angle = clamp(value, 0.0, 180.0);
      servo.setAngle(angle);
    }
    updateVelocityEstimate();
  }

  @Override
  public double getVelocity() {
    updateVelocityEstimate();
    return velocityDegPerSec;
  }

  @Override
  public double getPosition() {
    return servo.getAngle();
  }

  @Override
  public double getCurrent() {
    return Double.NaN;
  }

  @Override
  public double getTemperature() {
    return Double.NaN;
  }

  @Override
  public double getHealthScore() {
    return healthScore;
  }

  @Override
  public void setHealthScore(double score) {
    healthScore = score;
  }

  @Override
  public String getDeviceName() {
    return "PWM-Servo";
  }

  @Override
  public boolean isServo() {
    return true;
  }

  private void updateVelocityEstimate() {
    double now = Timer.getFPGATimestamp();
    double position = servo.getAngle();
    double dt = now - lastTimestamp;
    if (dt > 1e-3) {
      velocityDegPerSec = (position - lastPositionDeg) / dt;
      lastPositionDeg = position;
      lastTimestamp = now;
    }
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }
}
