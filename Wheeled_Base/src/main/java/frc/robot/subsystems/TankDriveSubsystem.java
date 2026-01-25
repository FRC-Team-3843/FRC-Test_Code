package frc.robot.subsystems;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.estimator.DifferentialDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelPositions;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.drive.DriveIO;
import frc.robot.drive.DriveIOTank;
import frc.robot.drive.GyroIO;
import frc.robot.drive.GyroIOAdis16470;
import frc.robot.drive.GyroIOAdxrs450;
import frc.robot.drive.GyroIONone;
import frc.robot.drive.GyroIOPigeon2;
import frc.robot.vision.Vision;
import java.util.function.DoubleSupplier;

public class TankDriveSubsystem extends SubsystemBase {
  private final DriveIO driveIO = new DriveIOTank();
  private final GyroIO gyro = createGyro();

  private final DifferentialDriveKinematics kinematics =
      new DifferentialDriveKinematics(Constants.DriveConstants.TRACK_WIDTH_METERS);

  private final DifferentialDrivePoseEstimator poseEstimator =
      Constants.DriveConstants.USE_WHEEL_ENCODERS && Constants.DriveConstants.USE_GYRO
          ? new DifferentialDrivePoseEstimator(
              kinematics,
              gyro.getRotation(),
              getWheelPositions().leftMeters,
              getWheelPositions().rightMeters,
              new Pose2d())
          : null;

  private final Vision vision = Constants.VisionConstants.ENABLE_VISION && poseEstimator != null
      ? new Vision(Constants.VisionConstants.MAX_LATENCY_SECONDS)
      : null;

  public TankDriveSubsystem() {
    if (Constants.AutoConstants.ENABLE_AUTO
        && Constants.AutoConstants.AUTO_MODE == Constants.AutoConstants.AutoMode.PATHPLANNER) {
      setupPathPlanner();
    }
  }

  public void driveArcade(double forward, double rotation) {
    forward = MathUtil.applyDeadband(forward, Constants.OperatorConstants.DEADBAND);
    rotation = MathUtil.applyDeadband(rotation, Constants.OperatorConstants.DEADBAND);

    double left = forward + rotation;
    double right = forward - rotation;
    driveTank(left, right);
  }

  public void driveTank(double left, double right) {
    left = MathUtil.applyDeadband(left, Constants.OperatorConstants.DEADBAND);
    right = MathUtil.applyDeadband(right, Constants.OperatorConstants.DEADBAND);

    double max = Math.max(Math.abs(left), Math.abs(right));
    if (max > 1.0) {
      left /= max;
      right /= max;
    }

    double leftMps = left * Constants.DriveConstants.MAX_WHEEL_SPEED_MPS;
    double rightMps = right * Constants.DriveConstants.MAX_WHEEL_SPEED_MPS;
    driveWheelSpeeds(new DifferentialDriveWheelSpeeds(leftMps, rightMps));
  }

  public void driveRobotRelative(ChassisSpeeds speeds) {
    DifferentialDriveWheelSpeeds wheelSpeeds = kinematics.toWheelSpeeds(speeds);
    wheelSpeeds.desaturate(Constants.DriveConstants.MAX_WHEEL_SPEED_MPS);
    driveWheelSpeeds(wheelSpeeds);
  }

  private void driveWheelSpeeds(DifferentialDriveWheelSpeeds wheelSpeeds) {
    if (Constants.DriveConstants.USE_CLOSED_LOOP) {
      driveIO.setWheelSpeeds(wheelSpeeds);
    } else {
      double left = wheelSpeeds.leftMetersPerSecond / Constants.DriveConstants.MAX_WHEEL_SPEED_MPS;
      double right = wheelSpeeds.rightMetersPerSecond / Constants.DriveConstants.MAX_WHEEL_SPEED_MPS;
      driveIO.setVoltages(left * 12.0, right * 12.0);
    }
  }

  public void stop() {
    driveIO.stop();
  }

  public void resetEncoders() {
    driveIO.resetEncoders();
  }

  public void resetPose(Pose2d pose) {
    if (poseEstimator != null) {
      DifferentialDriveWheelPositions positions = getWheelPositions();
      poseEstimator.resetPosition(gyro.getRotation(), positions.leftMeters, positions.rightMeters, pose);
    }
  }

  public Pose2d getPose() {
    return poseEstimator != null ? poseEstimator.getEstimatedPosition() : new Pose2d();
  }

  public Rotation2d getHeading() {
    return gyro.getRotation();
  }

  public void zeroGyro() {
    gyro.reset();
  }

  public DifferentialDriveWheelSpeeds getWheelSpeeds() {
    return driveIO.getWheelSpeeds();
  }

  public DifferentialDriveWheelPositions getWheelPositions() {
    double[] positions = driveIO.getWheelPositionsMeters();
    return new DifferentialDriveWheelPositions(positions[0], positions[1]);
  }

  public DifferentialDriveKinematics getKinematics() {
    return kinematics;
  }

  public void setBrake(boolean brake) {
    driveIO.setBrake(brake);
  }

  @Override
  public void periodic() {
    if (poseEstimator != null) {
      DifferentialDriveWheelPositions positions = getWheelPositions();
      poseEstimator.update(gyro.getRotation(), positions.leftMeters, positions.rightMeters);
      if (vision != null) {
        vision.addVisionMeasurements(poseEstimator);
      }
    }

    if (Constants.LoggingConstants.ENABLE_DRIVE_TELEMETRY) {
      Pose2d pose = getPose();
      SmartDashboard.putNumber("Drive/PoseX", pose.getX());
      SmartDashboard.putNumber("Drive/PoseY", pose.getY());
      SmartDashboard.putNumber("Drive/HeadingDeg", getHeading().getDegrees());
      DifferentialDriveWheelSpeeds speeds = getWheelSpeeds();
      SmartDashboard.putNumber("Drive/LeftSpeed", speeds.leftMetersPerSecond);
      SmartDashboard.putNumber("Drive/RightSpeed", speeds.rightMetersPerSecond);
    }
  }

  public Command driveArcadeCommand(DoubleSupplier forward, DoubleSupplier rotation) {
    return run(() -> driveArcade(forward.getAsDouble(), rotation.getAsDouble()))
        .withName("DriveArcade");
  }

  public Command driveTankCommand(DoubleSupplier left, DoubleSupplier right) {
    return run(() -> driveTank(left.getAsDouble(), right.getAsDouble()))
        .withName("DriveTank");
  }

  private void setupPathPlanner() {
    try {
      RobotConfig config = RobotConfig.fromGUISettings();

      AutoBuilder.configure(
          this::getPose,
          this::resetPose,
          this::getRobotRelativeSpeeds,
          (speeds, feedforwards) -> driveRobotRelative(speeds),
          new PPHolonomicDriveController(
              new PIDConstants(Constants.AutoConstants.TRANSLATION_P,
                               Constants.AutoConstants.TRANSLATION_I,
                               Constants.AutoConstants.TRANSLATION_D),
              new PIDConstants(Constants.AutoConstants.ROTATION_P,
                               Constants.AutoConstants.ROTATION_I,
                               Constants.AutoConstants.ROTATION_D)),
          config,
          () -> {
            var alliance = DriverStation.getAlliance();
            return alliance.isPresent() && alliance.get() == DriverStation.Alliance.Red;
          },
          this);
    } catch (Exception e) {
      DriverStation.reportError("Failed to configure PathPlanner: " + e.getMessage(), e.getStackTrace());
    }

    PathfindingCommand.warmupCommand().schedule();
  }

  private ChassisSpeeds getRobotRelativeSpeeds() {
    return kinematics.toChassisSpeeds(getWheelSpeeds());
  }

  private GyroIO createGyro() {
    if (!Constants.DriveConstants.USE_GYRO) {
      return new GyroIONone();
    }
    switch (Constants.DriveConstants.GYRO_TYPE) {
      case ADXRS450:
        return new GyroIOAdxrs450();
      case PIGEON2:
        return new GyroIOPigeon2(Constants.DriveConstants.GYRO_CAN_ID);
      case ADIS16470:
      default:
        return new GyroIOAdis16470();
    }
  }
}
