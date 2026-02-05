// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.BuiltInWidgets;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.shooter.ShooterConfig;
import frc.robot.shooter.ShooterConfigLoader;
import frc.robot.shooter.ShooterSubsystem;

public class RobotContainer {
  private final ShuffleboardTab m_tab = Shuffleboard.getTab("Shooter Test");
  private final CommandXboxController m_driver =
      new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);

  // Load shooter configuration from JSON
  private final ShooterConfig m_config = ShooterConfigLoader.loadConfigOrDefault("shooter-config.json");
  private final ShooterSubsystem m_shooter = new ShooterSubsystem(m_config);

  // PID Tuning entries - Preshooter
  private final GenericEntry m_preshooterKpEntry;
  private final GenericEntry m_preshooterKiEntry;
  private final GenericEntry m_preshooterKdEntry;
  private final GenericEntry m_preshooterKvEntry;
  private final GenericEntry m_preshooterKsEntry;

  // PID Tuning entries - Main Shooter
  private final GenericEntry m_mainShooterKpEntry;
  private final GenericEntry m_mainShooterKiEntry;
  private final GenericEntry m_mainShooterKdEntry;
  private final GenericEntry m_mainShooterKvEntry;
  private final GenericEntry m_mainShooterKsEntry;

  // Setpoint entries
  private final GenericEntry m_preshooterSetpoint1Entry;
  private final GenericEntry m_preshooterSetpoint2Entry;
  private final GenericEntry m_mainShooterSetpoint1Entry;
  private final GenericEntry m_mainShooterSetpoint2Entry;

  // Servo position entries
  private final GenericEntry m_servoPosition1Entry;
  private final GenericEntry m_servoPosition2Entry;

  // Apply PID button
  private final GenericEntry m_applyPidEntry;

  public RobotContainer() {
    // Create PID tuning entries
    m_preshooterKpEntry = m_tab.add("Pre kP", m_config.preshooterKp).getEntry();
    m_preshooterKiEntry = m_tab.add("Pre kI", m_config.preshooterKi).getEntry();
    m_preshooterKdEntry = m_tab.add("Pre kD", m_config.preshooterKd).getEntry();
    m_preshooterKvEntry = m_tab.add("Pre kV", m_config.preshooterKv).getEntry();
    m_preshooterKsEntry = m_tab.add("Pre kS", m_config.preshooterKs).getEntry();

    m_mainShooterKpEntry = m_tab.add("Main kP", m_config.mainShooterKp).getEntry();
    m_mainShooterKiEntry = m_tab.add("Main kI", m_config.mainShooterKi).getEntry();
    m_mainShooterKdEntry = m_tab.add("Main kD", m_config.mainShooterKd).getEntry();
    m_mainShooterKvEntry = m_tab.add("Main kV", m_config.mainShooterKv).getEntry();
    m_mainShooterKsEntry = m_tab.add("Main kS", m_config.mainShooterKs).getEntry();

    // Create setpoint entries
    m_preshooterSetpoint1Entry = m_tab.add("Pre SP1 (RPM)", m_config.preshooterSetpoint1Rpm).getEntry();
    m_preshooterSetpoint2Entry = m_tab.add("Pre SP2 (RPM)", m_config.preshooterSetpoint2Rpm).getEntry();
    m_mainShooterSetpoint1Entry = m_tab.add("Main SP1 (RPM)", m_config.mainShooterSetpoint1Rpm).getEntry();
    m_mainShooterSetpoint2Entry = m_tab.add("Main SP2 (RPM)", m_config.mainShooterSetpoint2Rpm).getEntry();

    // Create servo position entries
    m_servoPosition1Entry = m_tab.add("Servo Pos 1", m_config.servoPosition1).getEntry();
    m_servoPosition2Entry = m_tab.add("Servo Pos 2", m_config.servoPosition2).getEntry();

    // Apply PID button
    m_applyPidEntry = m_tab.add("Apply PID", false).withWidget(BuiltInWidgets.kToggleButton).getEntry();

    configureTelemetry();
    configureBindings();
  }

  private void configureTelemetry() {
    // Actual RPM telemetry
    m_tab.addNumber("Preshooter RPM", m_shooter::getPreshooterVelocityRpm);
    m_tab.addNumber("Main Shooter RPM", m_shooter::getMainShooterVelocityRpm);

    // At-setpoint indicators
    m_tab.addBoolean("Pre At SP?", m_shooter::isPreshooterAtSetpoint)
        .withWidget(BuiltInWidgets.kBooleanBox);
    m_tab.addBoolean("Main At SP?", m_shooter::isMainShooterAtSetpoint)
        .withWidget(BuiltInWidgets.kBooleanBox);

    // Controls info
    m_tab.add("Controls", "A = SP1 | B = SP2").withWidget(BuiltInWidgets.kTextView);
  }

  private void configureBindings() {
    // A button: Setpoint 1 + servo position 1
    m_driver.a().whileTrue(Commands.run(() -> {
      double preRpm = m_preshooterSetpoint1Entry.getDouble(m_config.preshooterSetpoint1Rpm);
      double mainRpm = m_mainShooterSetpoint1Entry.getDouble(m_config.mainShooterSetpoint1Rpm);
      double servoPos = m_servoPosition1Entry.getDouble(m_config.servoPosition1);

      m_shooter.setVelocities(preRpm, mainRpm);
      m_shooter.setServoPosition(servoPos);
    }, m_shooter)).onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // B button: Setpoint 2 + servo position 2
    m_driver.b().whileTrue(Commands.run(() -> {
      double preRpm = m_preshooterSetpoint2Entry.getDouble(m_config.preshooterSetpoint2Rpm);
      double mainRpm = m_mainShooterSetpoint2Entry.getDouble(m_config.mainShooterSetpoint2Rpm);
      double servoPos = m_servoPosition2Entry.getDouble(m_config.servoPosition2);

      m_shooter.setVelocities(preRpm, mainRpm);
      m_shooter.setServoPosition(servoPos);
    }, m_shooter)).onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // Apply PID button trigger
    m_driver.x().onTrue(Commands.runOnce(() -> {
      applyPidConfig();
      m_applyPidEntry.setBoolean(false);
    }));
  }

  private void applyPidConfig() {
    // Read PID values from dashboard
    double preKp = m_preshooterKpEntry.getDouble(m_config.preshooterKp);
    double preKi = m_preshooterKiEntry.getDouble(m_config.preshooterKi);
    double preKd = m_preshooterKdEntry.getDouble(m_config.preshooterKd);
    double preKv = m_preshooterKvEntry.getDouble(m_config.preshooterKv);
    double preKs = m_preshooterKsEntry.getDouble(m_config.preshooterKs);

    double mainKp = m_mainShooterKpEntry.getDouble(m_config.mainShooterKp);
    double mainKi = m_mainShooterKiEntry.getDouble(m_config.mainShooterKi);
    double mainKd = m_mainShooterKdEntry.getDouble(m_config.mainShooterKd);
    double mainKv = m_mainShooterKvEntry.getDouble(m_config.mainShooterKv);
    double mainKs = m_mainShooterKsEntry.getDouble(m_config.mainShooterKs);

    // Hot-reload PID configuration
    m_shooter.updatePreshooterPid(preKp, preKi, preKd, preKv, preKs);
    m_shooter.updateMainShooterPid(mainKp, mainKi, mainKd, mainKv, mainKs);

    System.out.println("PID configuration applied!");
  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
}
