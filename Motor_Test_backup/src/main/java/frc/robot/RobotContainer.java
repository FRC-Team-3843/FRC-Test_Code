// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.motor.ControllerType;
import frc.robot.motor.MotorConfiguration;
import frc.robot.motor.MotorHealthTest;
import frc.robot.motor.MotorKind;
import frc.robot.motor.MotorTestSubsystem;
import frc.robot.motor.UniversalMotor;

public class RobotContainer {
  private final ShuffleboardTab tab = Shuffleboard.getTab("Motor Test");

  private final SendableChooser<ControllerType> controllerChooser = new SendableChooser<>();
  private final SendableChooser<MotorKind> motorChooser = new SendableChooser<>();
  private final SendableChooser<UniversalMotor.Mode> modeChooser = new SendableChooser<>();

  private final GenericEntry enableEntry =
      tab.add("Enable Output (Hold)", false).withWidget(BuiltInWidgets.kToggleButton).getEntry();
  private final GenericEntry setpointEntry =
      tab.add("Setpoint", 0.0).withWidget(BuiltInWidgets.kNumberSlider).getEntry();
  private final GenericEntry canIdEntry = tab.add("CAN ID", Constants.MotorConstants.DEFAULT_CAN_ID).getEntry();
  private final GenericEntry pwmChannelEntry =
      tab.add("PWM Channel", Constants.MotorConstants.DEFAULT_PWM_CHANNEL).getEntry();
  private final GenericEntry gearRatioEntry =
      tab.add("Gear Ratio", Constants.MotorConstants.DEFAULT_GEAR_RATIO).getEntry();
  private final GenericEntry quadCprEntry = tab.add("Quad CPR", Constants.MotorConstants.DEFAULT_QUAD_CPR).getEntry();
  private final GenericEntry invertedEntry = tab.add("Invert", false).getEntry();
  private final GenericEntry applyConfigEntry =
      tab.add("Apply Config", false).withWidget(BuiltInWidgets.kToggleButton).getEntry();

  private final CommandXboxController driver = new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);

  private final MotorTestSubsystem subsystem = new MotorTestSubsystem(defaultConfiguration());

  private final MotorHealthTest healthTestCommand = new MotorHealthTest(subsystem);

  public RobotContainer() {
    configureChoosers();
    configureDashboard();
    configureBindings();
  }

  private void configureChoosers() {
    controllerChooser.setDefaultOption("Spark Max", ControllerType.SPARK_MAX);
    controllerChooser.addOption("Spark Flex", ControllerType.SPARK_FLEX);
    controllerChooser.addOption("Talon SRX", ControllerType.TALON_SRX);
    controllerChooser.addOption("Talon FX", ControllerType.TALON_FX);
    controllerChooser.addOption("Talon FXS", ControllerType.TALON_FXS);
    controllerChooser.addOption("PWM Servo", ControllerType.PWM_SERVO);

    motorChooser.setDefaultOption("CIM", MotorKind.CIM);
    motorChooser.addOption("NEO", MotorKind.NEO);
    motorChooser.addOption("NEO 550", MotorKind.NEO_550);
    motorChooser.addOption("Kraken", MotorKind.KRAKEN);
    motorChooser.addOption("Servo", MotorKind.SERVO);
    motorChooser.addOption("Continuous Servo", MotorKind.CONTINUOUS_SERVO);

    modeChooser.setDefaultOption("Duty Cycle", UniversalMotor.Mode.DUTY_CYCLE);
    modeChooser.addOption("Voltage", UniversalMotor.Mode.VOLTAGE);
    modeChooser.addOption("Velocity", UniversalMotor.Mode.VELOCITY);
    modeChooser.addOption("Position", UniversalMotor.Mode.POSITION);
    modeChooser.addOption("Current (Spark/SRX only)", UniversalMotor.Mode.CURRENT);
    modeChooser.addOption("Smart Motion", UniversalMotor.Mode.SMART_MOTION);
  }

  private void configureDashboard() {
    tab.add("Controller Type", controllerChooser);
    tab.add("Motor Type", motorChooser);
    tab.add("Control Mode", modeChooser);
    tab.addBoolean("Health OK", () -> subsystem.getLastReport() != null && subsystem.getLastReport().healthScore >= 70.0)
        .withWidget(BuiltInWidgets.kBooleanBox);
    tab.addNumber("Health Score", () -> subsystem.getLastReport() != null ? subsystem.getLastReport().healthScore : 0.0);
    tab.addString("Grade", () -> subsystem.getLastReport() != null ? subsystem.getLastReport().grade : "N/A");
    tab.addNumber("Temp C", () -> subsystem.getMotor() != null ? subsystem.getMotor().getTemperature() : 0.0);
    tab.addNumber("Post Test Peak Temp", () -> subsystem.getLastReport() != null ? subsystem.getLastReport().postTestPeakTemp : 0.0);
    tab.addNumber("Breakaway V", () -> subsystem.getLastReport() != null ? subsystem.getLastReport().breakawayValue : 0.0);
    tab.addNumber("Resistance Ohms", () -> subsystem.getLastReport() != null ? subsystem.getLastReport().resistanceOhms : 0.0);
    tab.addNumber("Kv Rating", () -> subsystem.getLastReport() != null ? subsystem.getLastReport().kvRating : 0.0);
    tab.addNumber("Current A", () -> subsystem.getMotor() != null ? subsystem.getMotor().getCurrent() : 0.0);
    tab.addNumber("Velocity RPS", () -> subsystem.getMotor() != null ? subsystem.getMotor().getVelocity() : 0.0);
    tab.addNumber("Position", () -> subsystem.getMotor() != null ? subsystem.getMotor().getPosition() : 0.0);
  }

  private void configureBindings() {
    subsystem.setEnableSupplier(
        () -> enableEntry.getBoolean(false) || driver.getHID().getAButton());

    subsystem.setDefaultCommand(
        new RunCommand(
            () -> {
              UniversalMotor.Mode mode = modeChooser.getSelected();
              double setpoint = setpointEntry.getDouble(0.0);
              if (mode == null) {
                mode = UniversalMotor.Mode.DUTY_CYCLE;
              }
              subsystem.applySetpoint(mode, setpoint);
            },
            subsystem));

    new Trigger(() -> applyConfigEntry.getBoolean(false))
        .onTrue(new InstantCommand(this::applyConfiguration, subsystem));

    driver.b().whileTrue(healthTestCommand);
  }

  private MotorConfiguration defaultConfiguration() {
    return MotorConfiguration.builder(ControllerType.SPARK_MAX, MotorKind.CIM)
        .canId(Constants.MotorConstants.DEFAULT_CAN_ID)
        .pwmChannel(Constants.MotorConstants.DEFAULT_PWM_CHANNEL)
        .gearRatio(Constants.MotorConstants.DEFAULT_GEAR_RATIO)
        .quadCpr(Constants.MotorConstants.DEFAULT_QUAD_CPR)
        .build();
  }

  private void applyConfiguration() {
    ControllerType controllerType =
        controllerChooser.getSelected() != null ? controllerChooser.getSelected() : ControllerType.SPARK_MAX;
    MotorKind motorKind = motorChooser.getSelected() != null ? motorChooser.getSelected() : MotorKind.CIM;
    MotorConfiguration config =
        MotorConfiguration.builder(controllerType, motorKind)
            .canId((int) canIdEntry.getInteger(Constants.MotorConstants.DEFAULT_CAN_ID))
            .pwmChannel((int) pwmChannelEntry.getInteger(Constants.MotorConstants.DEFAULT_PWM_CHANNEL))
            .gearRatio(gearRatioEntry.getDouble(Constants.MotorConstants.DEFAULT_GEAR_RATIO))
            .quadCpr((int) quadCprEntry.getInteger(Constants.MotorConstants.DEFAULT_QUAD_CPR))
            .inverted(invertedEntry.getBoolean(false))
            .useQuadEncoder(controllerType == ControllerType.TALON_SRX)
            .build();
    subsystem.rebuildMotor(config);
    applyConfigEntry.setBoolean(false);
  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
}
