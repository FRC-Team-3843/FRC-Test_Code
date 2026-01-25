package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.auto.ChoreoAutos;
import frc.robot.subsystems.MecanumDriveSubsystem;

public class RobotContainer {
  private final CommandXboxController driverXbox =
      new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);

  private final MecanumDriveSubsystem drive = new MecanumDriveSubsystem();

  private SendableChooser<Command> autoChooser = new SendableChooser<>();

  public RobotContainer() {
    configureBindings();
    DriverStation.silenceJoystickConnectionWarning(true);
    setupAutoChooser();
  }

  private void configureBindings() {
    drive.setDefaultCommand(
        drive.driveCommand(
            () -> -driverXbox.getLeftY(),
            () -> -driverXbox.getLeftX(),
            () -> -driverXbox.getRightX()));

    driverXbox.start().onTrue(Commands.runOnce(drive::zeroGyro));
    driverXbox.back().onTrue(Commands.runOnce(drive::toggleFieldCentric));
    driverXbox.x().onTrue(Commands.runOnce(drive::resetEncoders));
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
            ChoreoAutos.buildAuto(Constants.AutoConstants.CHOREO_DEFAULT_AUTO, drive));
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
    drive.setBrake(brake);
  }
}
