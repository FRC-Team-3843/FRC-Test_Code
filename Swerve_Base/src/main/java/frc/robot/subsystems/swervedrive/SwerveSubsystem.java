package frc.robot.subsystems.swervedrive;

import static edu.wpi.first.units.Units.Meter;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathfindingCommand;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.path.PathConstraints;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine.Config;
import frc.robot.Constants;
import java.io.File;
import java.util.Arrays;
import java.util.function.Supplier;
import swervelib.SwerveDrive;
import swervelib.SwerveDriveTest;
import swervelib.parser.SwerveParser;
import swervelib.telemetry.SwerveDriveTelemetry;
import swervelib.telemetry.SwerveDriveTelemetry.TelemetryVerbosity;

/**
 * Base swerve subsystem using YAGSL configuration files.
 * Supports PathPlanner autos, optional vision integration, and SysId.
 */
public class SwerveSubsystem extends SubsystemBase {
  private final SwerveDrive swerveDrive;
  private final Vision vision;

  public SwerveSubsystem(File directory) {
    if (Constants.LoggingConstants.ENABLE_SWERVE_TELEMETRY) {
      SwerveDriveTelemetry.verbosity = TelemetryVerbosity.HIGH;
    }

    try {
      swerveDrive = new SwerveParser(directory).createSwerveDrive(
          Constants.DriveConstants.MAX_SPEED,
          new Pose2d(new Translation2d(Meter.of(1), Meter.of(4)), Rotation2d.fromDegrees(0)));
    } catch (Exception e) {
      throw new RuntimeException("Failed to load swerve config", e);
    }

    swerveDrive.setHeadingCorrection(false);
    swerveDrive.setCosineCompensator(true);
    swerveDrive.setAngularVelocityCompensation(true, true, 0.1);
    swerveDrive.setModuleEncoderAutoSynchronize(false, 1);

    vision = Constants.VisionConstants.ENABLE_VISION
        ? new Vision(this::getPose, swerveDrive.field, Constants.VisionConstants.MAX_LATENCY_SECONDS)
        : null;

    if (Constants.AutoConstants.ENABLE_AUTO
        && Constants.AutoConstants.AUTO_MODE == Constants.AutoConstants.AutoMode.PATHPLANNER) {
      setupPathPlanner();
    }
  }

  @Override
  public void periodic() {
    if (vision != null) {
      vision.updatePoseEstimation(swerveDrive);
    }
  }

  private void setupPathPlanner() {
    try {
      RobotConfig config = RobotConfig.fromGUISettings();

      AutoBuilder.configure(
          this::getPose,
          this::resetOdometry,
          this::getRobotVelocity,
          (speedsRobotRelative, moduleFeedForwards) -> {
            swerveDrive.drive(
                speedsRobotRelative,
                swerveDrive.kinematics.toSwerveModuleStates(speedsRobotRelative),
                moduleFeedForwards.linearForces());
          },
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

  public Command lockCommand() {
    return run(() -> swerveDrive.lockPose());
  }

  public Command centerModulesCommand() {
    return run(() -> Arrays.asList(swerveDrive.getModules()).forEach(it -> it.setAngle(0.0)));
  }

  public Command zeroGyroCommand() {
    return Commands.runOnce(this::zeroGyro);
  }

  public Command driveFieldOriented(Supplier<ChassisSpeeds> velocity) {
    return run(() -> swerveDrive.driveFieldOriented(velocity.get()));
  }

  public Command sysIdDriveMotorCommand() {
    return SwerveDriveTest.generateSysIdCommand(
        SwerveDriveTest.setDriveSysIdRoutine(new Config(), this, swerveDrive, 12, true),
        3.0, 5.0, 3.0);
  }

  public Command sysIdAngleMotorCommand() {
    return SwerveDriveTest.generateSysIdCommand(
        SwerveDriveTest.setAngleSysIdRoutine(new Config(), this, swerveDrive),
        3.0, 5.0, 3.0);
  }

  public void driveFieldOriented(ChassisSpeeds velocity) {
    swerveDrive.driveFieldOriented(velocity);
  }

  public void drive(ChassisSpeeds velocity) {
    swerveDrive.drive(velocity);
  }

  public void driveRobotRelative(ChassisSpeeds velocity) {
    swerveDrive.drive(velocity);
  }

  public Pose2d getPose() {
    return swerveDrive.getPose();
  }

  public void resetOdometry(Pose2d pose) {
    swerveDrive.resetOdometry(pose);
  }

  public ChassisSpeeds getRobotVelocity() {
    return swerveDrive.getRobotVelocity();
  }

  public ChassisSpeeds getFieldVelocity() {
    return swerveDrive.getFieldVelocity();
  }

  public SwerveDriveKinematics getKinematics() {
    return swerveDrive.kinematics;
  }

  public Rotation2d getHeading() {
    return getPose().getRotation();
  }

  public SwerveDrive getSwerveDrive() {
    return swerveDrive;
  }

  public void setMotorBrake(boolean brake) {
    swerveDrive.setMotorIdleMode(brake);
  }

  public void zeroGyro() {
    swerveDrive.zeroGyro();
  }

  public void zeroGyroWithAlliance() {
    if (isRedAlliance()) {
      zeroGyro();
      resetOdometry(new Pose2d(getPose().getTranslation(), Rotation2d.fromDegrees(180)));
    } else {
      zeroGyro();
    }
  }

  private boolean isRedAlliance() {
    var alliance = DriverStation.getAlliance();
    return alliance.isPresent() && alliance.get() == DriverStation.Alliance.Red;
  }

  public Command driveToPose(Pose2d pose) {
    PathConstraints constraints = new PathConstraints(
        swerveDrive.getMaximumChassisVelocity(),
        4.0,
        swerveDrive.getMaximumChassisAngularVelocity(),
        Units.degreesToRadians(720));
    return AutoBuilder.pathfindToPose(
        pose,
        constraints,
        edu.wpi.first.units.Units.MetersPerSecond.of(0));
  }

  public void postTrajectory(Trajectory trajectory) {
    swerveDrive.postTrajectory(trajectory);
  }
}

