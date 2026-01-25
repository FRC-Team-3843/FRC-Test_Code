package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.auto.ChoreoAutos;
import frc.robot.subsystems.swervedrive.SwerveSubsystem;
import java.io.File;
import swervelib.SwerveInputStream;

public class RobotContainer {
  private final CommandXboxController driverXbox =
      new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);
  private final CommandXboxController operatorXbox =
      new CommandXboxController(Constants.OperatorConstants.OPERATOR_CONTROLLER_PORT);

  private final SwerveSubsystem drivebase =
      new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve"));

  private SendableChooser<Command> autoChooser = new SendableChooser<>();

  private final SwerveInputStream driveAngularVelocity = SwerveInputStream.of(
          drivebase.getSwerveDrive(),
          () -> -driverXbox.getLeftY(),
          () -> -driverXbox.getLeftX())
      .withControllerRotationAxis(() -> -driverXbox.getRightX())
      .deadband(Constants.OperatorConstants.DEADBAND)
      .scaleTranslation(1.0)
      .allianceRelativeControl(true);

  private final SwerveInputStream driveDirectAngle = driveAngularVelocity.copy()
      .withControllerHeadingAxis(driverXbox::getRightX, driverXbox::getRightY)
      .headingWhile(true);

  private final SwerveInputStream driveRobotOriented = driveAngularVelocity.copy()
      .robotRelative(true)
      .allianceRelativeControl(false);

  private final SwerveInputStream driveAngularVelocitySlow = SwerveInputStream.of(
          drivebase.getSwerveDrive(),
          () -> -driverXbox.getLeftY(),
          () -> -driverXbox.getLeftX())
      .withControllerRotationAxis(() -> -driverXbox.getRightX())
      .deadband(Constants.OperatorConstants.DEADBAND)
      .scaleTranslation(0.5)
      .allianceRelativeControl(true);

  public RobotContainer() {
    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);

    setupAutoChooser();
  }

  private void configureBindings() {
    Command driveFieldOrientedAngularVelocity = drivebase.driveFieldOriented(driveAngularVelocity);
    Command driveFieldOrientedDirectAngle = drivebase.driveFieldOriented(driveDirectAngle);

    if (RobotBase.isSimulation()) {
      drivebase.setDefaultCommand(driveFieldOrientedDirectAngle);
    } else {
      drivebase.setDefaultCommand(driveFieldOrientedAngularVelocity);
    }

    driverXbox.start().onTrue(Commands.runOnce(drivebase::zeroGyroWithAlliance));
    driverXbox.povUp().whileTrue(drivebase.lockCommand());
    driverXbox.back().whileTrue(drivebase.centerModulesCommand());
    driverXbox.leftTrigger(0.5).whileTrue(drivebase.driveFieldOriented(driveAngularVelocitySlow));
    driverXbox.rightTrigger(0.5).whileTrue(drivebase.driveFieldOriented(driveRobotOriented));

    if (DriverStation.isTest()) {
      driverXbox.x().whileTrue(drivebase.sysIdDriveMotorCommand());
      driverXbox.y().whileTrue(drivebase.sysIdAngleMotorCommand());
    }
  }

  private void setupAutoChooser() {
    autoChooser = new SendableChooser<>();
    autoChooser.setDefaultOption("None", Commands.none());

    if (!Constants.AutoConstants.ENABLE_AUTO) {
      SmartDashboard.putData("Auto Chooser", autoChooser);
      return;
    }

    switch (Constants.AutoConstants.AUTO_MODE) {
      case PATHPLANNER:
        autoChooser = AutoBuilder.buildAutoChooser();
        break;
      case CHOREO:
        autoChooser.addOption(
            "Choreo - " + Constants.AutoConstants.CHOREO_DEFAULT_AUTO,
            ChoreoAutos.buildAuto(Constants.AutoConstants.CHOREO_DEFAULT_AUTO, drivebase));
        break;
      case NONE:
      default:
        break;
    }

    SmartDashboard.putData("Auto Chooser", autoChooser);
  }

  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }

  public void setMotorBrake(boolean brake) {
    drivebase.setMotorBrake(brake);
  }
}

