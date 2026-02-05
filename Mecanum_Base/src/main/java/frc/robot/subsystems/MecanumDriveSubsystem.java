package frc.robot.subsystems;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.estimator.MecanumDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.MecanumDriveKinematics;
import edu.wpi.first.math.kinematics.MecanumDriveWheelPositions;
import edu.wpi.first.math.kinematics.MecanumDriveWheelSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.drive.DriveIO;
import frc.robot.drive.DriveIOMecanum;
import frc.robot.drive.GyroIO;
import frc.robot.drive.GyroIOAdis16470;
import frc.robot.drive.GyroIOAdxrs450;
import frc.robot.drive.GyroIONone;
import frc.robot.drive.GyroIOPigeon2;
import frc.robot.vision.Vision;
import java.util.function.DoubleSupplier;

public class MecanumDriveSubsystem extends SubsystemBase {
  private final DriveIO driveIO = new DriveIOMecanum();
  private final GyroIO gyro = createGyro();

  private final MecanumDriveKinematics kinematics = new MecanumDriveKinematics(
      new Translation2d(Constants.DriveConstants.WHEELBASE_METERS / 2.0,
                        Constants.DriveConstants.TRACK_WIDTH_METERS / 2.0),
      new Translation2d(Constants.DriveConstants.WHEELBASE_METERS / 2.0,
                        -Constants.DriveConstants.TRACK_WIDTH_METERS / 2.0),
      new Translation2d(-Constants.DriveConstants.WHEELBASE_METERS / 2.0,
                        Constants.DriveConstants.TRACK_WIDTH_METERS / 2.0),
      new Translation2d(-Constants.DriveConstants.WHEELBASE_METERS / 2.0,
                        -Constants.DriveConstants.TRACK_WIDTH_METERS / 2.0));

  private final MecanumDrivePoseEstimator poseEstimator =
      Constants.DriveConstants.USE_WHEEL_ENCODERS && Constants.DriveConstants.USE_GYRO
          ? new MecanumDrivePoseEstimator(
              kinematics,
              gyro.getRotation(),
              getWheelPositions(),
              new Pose2d())
          : null;

  private final Vision vision = Constants.VisionConstants.ENABLE_VISION && poseEstimator != null
      ? new Vision(Constants.VisionConstants.MAX_LATENCY_SECONDS)
      : null;

  private boolean fieldCentric = Constants.DriveConstants.FIELD_CENTRIC_DEFAULT;

  public MecanumDriveSubsystem() {
    if (Constants.AutoConstants.ENABLE_AUTO
        && Constants.AutoConstants.AUTO_MODE == Constants.AutoConstants.AutoMode.PATHPLANNER) {
      setupPathPlanner();
    }
  }

  public void drive(double forward, double strafe, double rotation) {
    forward = MathUtil.applyDeadband(forward, Constants.OperatorConstants.DEADBAND);
    strafe = MathUtil.applyDeadband(strafe, Constants.OperatorConstants.DEADBAND);
    rotation = MathUtil.applyDeadband(rotation, Constants.OperatorConstants.DEADBAND);

    ChassisSpeeds speeds = fieldCentric && Constants.DriveConstants.USE_GYRO
        ? ChassisSpeeds.fromFieldRelativeSpeeds(forward, strafe, rotation, gyro.getRotation())
        : new ChassisSpeeds(forward, strafe, rotation);

    driveRobotRelative(speeds);
  }

  public void driveRobotRelative(ChassisSpeeds speeds) {
    MecanumDriveWheelSpeeds wheelSpeeds = kinematics.toWheelSpeeds(speeds);
    wheelSpeeds.desaturate(Constants.DriveConstants.MAX_WHEEL_SPEED_MPS);

    if (Constants.DriveConstants.USE_CLOSED_LOOP) {
      driveIO.setWheelSpeeds(wheelSpeeds);
    } else {
      double fl = wheelSpeeds.frontLeftMetersPerSecond / Constants.DriveConstants.MAX_WHEEL_SPEED_MPS;
      double fr = wheelSpeeds.frontRightMetersPerSecond / Constants.DriveConstants.MAX_WHEEL_SPEED_MPS;
      double rl = wheelSpeeds.rearLeftMetersPerSecond / Constants.DriveConstants.MAX_WHEEL_SPEED_MPS;
      double rr = wheelSpeeds.rearRightMetersPerSecond / Constants.DriveConstants.MAX_WHEEL_SPEED_MPS;
      driveIO.setVoltages(fl * 12.0, rl * 12.0, fr * 12.0, rr * 12.0);
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
      poseEstimator.resetPosition(gyro.getRotation(), getWheelPositions(), pose);
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

  public void toggleFieldCentric() {
    fieldCentric = !fieldCentric;
  }

  public boolean isFieldCentric() {
    return fieldCentric;
  }

  public void setBrake(boolean brake) {
    driveIO.setBrake(brake);
  }

  public MecanumDriveKinematics getKinematics() {
    return kinematics;
  }

  public MecanumDriveWheelSpeeds getWheelSpeeds() {
    return driveIO.getWheelSpeeds();
  }

  public MecanumDriveWheelPositions getWheelPositions() {
    double[] positions = driveIO.getWheelPositionsMeters();
    return new MecanumDriveWheelPositions(
        positions[0],
        positions[2],
        positions[1],
        positions[3]);
  }

  @Override
  public void periodic() {
    if (poseEstimator != null) {
      poseEstimator.update(gyro.getRotation(), getWheelPositions());
      if (vision != null) {
        vision.addVisionMeasurements(poseEstimator);
      }
    }

    if (Constants.LoggingConstants.ENABLE_DRIVE_TELEMETRY) {
      Pose2d pose = getPose();
      SmartDashboard.putNumber("Drive/PoseX", pose.getX());
      SmartDashboard.putNumber("Drive/PoseY", pose.getY());
      SmartDashboard.putNumber("Drive/HeadingDeg", getHeading().getDegrees());
      MecanumDriveWheelSpeeds speeds = getWheelSpeeds();
      SmartDashboard.putNumber("Drive/FL Speed", speeds.frontLeftMetersPerSecond);
      SmartDashboard.putNumber("Drive/FR Speed", speeds.frontRightMetersPerSecond);
      SmartDashboard.putNumber("Drive/RL Speed", speeds.rearLeftMetersPerSecond);
      SmartDashboard.putNumber("Drive/RR Speed", speeds.rearRightMetersPerSecond);
    }
  }

  public Command driveCommand(DoubleSupplier forward, DoubleSupplier strafe, DoubleSupplier rotation) {
    return run(() -> drive(forward.getAsDouble(), strafe.getAsDouble(), rotation.getAsDouble()))
        .withName("Drive");
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

    // Try to load gyro configuration from JSON
    try {
      frc.robot.drive.GyroConfig config = frc.robot.drive.GyroConfigLoader.loadFromFile("motor-config.json");
      if (config != null) {
        return frc.robot.drive.GyroConfigLoader.createGyroIO(config);
      }
    } catch (Exception e) {
      System.err.println("Failed to load gyro config from JSON, falling back to Constants");
      e.printStackTrace();
    }

    // Fallback to Constants if JSON loading fails
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
