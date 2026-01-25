package frc.robot.drive;

import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import frc.robot.Constants;

public class DriveIOTank implements DriveIO {
  private final MotorGroup left;
  private final MotorGroup right;

  private final double wheelCircumference;
  private final boolean useSensors;
  private final boolean useClosedLoop;

  public DriveIOTank() {
    left = new MotorGroup(Constants.DriveConstants.LEFT_MOTORS);
    right = new MotorGroup(Constants.DriveConstants.RIGHT_MOTORS);

    wheelCircumference = Constants.DriveConstants.WHEEL_CIRCUMFERENCE_METERS;
    useSensors = Constants.DriveConstants.USE_WHEEL_ENCODERS;
    useClosedLoop = Constants.DriveConstants.USE_CLOSED_LOOP;
  }

  @Override
  public void setVoltages(double leftVolts, double rightVolts) {
    left.setVoltage(leftVolts);
    right.setVoltage(rightVolts);
  }

  @Override
  public void setWheelSpeeds(DifferentialDriveWheelSpeeds speeds) {
    if (!useClosedLoop) {
      return;
    }
    double leftRps = speeds.leftMetersPerSecond / wheelCircumference;
    double rightRps = speeds.rightMetersPerSecond / wheelCircumference;
    left.setVelocityRps(leftRps);
    right.setVelocityRps(rightRps);
  }

  @Override
  public DifferentialDriveWheelSpeeds getWheelSpeeds() {
    if (!useSensors) {
      return new DifferentialDriveWheelSpeeds();
    }
    double leftMps = left.getVelocityRps() * wheelCircumference;
    double rightMps = right.getVelocityRps() * wheelCircumference;
    return new DifferentialDriveWheelSpeeds(leftMps, rightMps);
  }

  @Override
  public double[] getWheelPositionsMeters() {
    if (!useSensors) {
      return new double[] {0.0, 0.0};
    }
    double leftMeters = left.getPositionRotations() * wheelCircumference;
    double rightMeters = right.getPositionRotations() * wheelCircumference;
    return new double[] {leftMeters, rightMeters};
  }

  @Override
  public void resetEncoders() {
    if (!useSensors) {
      return;
    }
    left.setPositionRotations(0.0);
    right.setPositionRotations(0.0);
  }

  @Override
  public void stop() {
    left.stop();
    right.stop();
  }

  @Override
  public void setBrake(boolean brake) {
    left.setBrake(brake);
    right.setBrake(brake);
  }
}
