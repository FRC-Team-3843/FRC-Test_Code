package frc.robot.drive;

import edu.wpi.first.math.kinematics.MecanumDriveWheelSpeeds;
import frc.robot.Constants;

public class DriveIOMecanum implements DriveIO {
  private final UniversalMotor frontLeft;
  private final UniversalMotor rearLeft;
  private final UniversalMotor frontRight;
  private final UniversalMotor rearRight;

  private final double wheelCircumference;
  private final boolean useSensors;
  private final boolean useClosedLoop;

  public DriveIOMecanum() {
    frontLeft = MotorFactory.createMotor(Constants.DriveConstants.FRONT_LEFT);
    rearLeft = MotorFactory.createMotor(Constants.DriveConstants.REAR_LEFT);
    frontRight = MotorFactory.createMotor(Constants.DriveConstants.FRONT_RIGHT);
    rearRight = MotorFactory.createMotor(Constants.DriveConstants.REAR_RIGHT);

    wheelCircumference = Constants.DriveConstants.WHEEL_CIRCUMFERENCE_METERS;
    useSensors = Constants.DriveConstants.USE_WHEEL_ENCODERS;
    useClosedLoop = Constants.DriveConstants.USE_CLOSED_LOOP;
  }

  @Override
  public void setVoltages(double frontLeftVolts, double rearLeftVolts, double frontRightVolts, double rearRightVolts) {
    frontLeft.setVoltage(frontLeftVolts);
    rearLeft.setVoltage(rearLeftVolts);
    frontRight.setVoltage(frontRightVolts);
    rearRight.setVoltage(rearRightVolts);
  }

  @Override
  public void setWheelSpeeds(MecanumDriveWheelSpeeds speeds) {
    if (!useClosedLoop) {
      return;
    }
    double flRps = speeds.frontLeftMetersPerSecond / wheelCircumference;
    double rlRps = speeds.rearLeftMetersPerSecond / wheelCircumference;
    double frRps = speeds.frontRightMetersPerSecond / wheelCircumference;
    double rrRps = speeds.rearRightMetersPerSecond / wheelCircumference;

    frontLeft.setVelocityRps(flRps);
    rearLeft.setVelocityRps(rlRps);
    frontRight.setVelocityRps(frRps);
    rearRight.setVelocityRps(rrRps);
  }

  @Override
  public MecanumDriveWheelSpeeds getWheelSpeeds() {
    if (!useSensors) {
      return new MecanumDriveWheelSpeeds();
    }
    double fl = frontLeft.getVelocityRps() * wheelCircumference;
    double rl = rearLeft.getVelocityRps() * wheelCircumference;
    double fr = frontRight.getVelocityRps() * wheelCircumference;
    double rr = rearRight.getVelocityRps() * wheelCircumference;
    return new MecanumDriveWheelSpeeds(fl, fr, rl, rr);
  }

  @Override
  public double[] getWheelPositionsMeters() {
    if (!useSensors) {
      return new double[] {0.0, 0.0, 0.0, 0.0};
    }
    double fl = frontLeft.getPositionRotations() * wheelCircumference;
    double rl = rearLeft.getPositionRotations() * wheelCircumference;
    double fr = frontRight.getPositionRotations() * wheelCircumference;
    double rr = rearRight.getPositionRotations() * wheelCircumference;
    return new double[] {fl, rl, fr, rr};
  }

  @Override
  public void resetEncoders() {
    // Not all controllers support resetting encoders; zero via setPosition.
    if (!useSensors) {
      return;
    }
    frontLeft.setPositionRotations(0.0);
    rearLeft.setPositionRotations(0.0);
    frontRight.setPositionRotations(0.0);
    rearRight.setPositionRotations(0.0);
  }

  @Override
  public void stop() {
    frontLeft.stop();
    rearLeft.stop();
    frontRight.stop();
    rearRight.stop();
  }

  @Override
  public void setBrake(boolean brake) {
    frontLeft.setBrake(brake);
    rearLeft.setBrake(brake);
    frontRight.setBrake(brake);
    rearRight.setBrake(brake);
  }
}
