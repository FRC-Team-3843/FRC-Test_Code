// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.commands.CharacterizeShooterCommand;
import frc.robot.shooter.ShooterConfig;
import frc.robot.shooter.ShooterConfigLoader;
import frc.robot.shooter.ShooterSubsystem;

public class RobotContainer {
  private static final String CONFIG_FILENAME = "shooter-config.json";

  private final ShooterConfig m_config;
  private final CommandXboxController m_driver;
  private final ShooterSubsystem m_shooter;

  public RobotContainer(ShooterConfig config) {
    m_config = config;
    m_driver = new CommandXboxController(m_config.driverControllerPort);
    m_shooter = new ShooterSubsystem(m_config);

    // Publish initial PID tuning values to SmartDashboard
    SmartDashboard.putNumber("Shooter/Preshooter/PID/kP", m_config.preshooterKp);
    SmartDashboard.putNumber("Shooter/Preshooter/PID/kI", m_config.preshooterKi);
    SmartDashboard.putNumber("Shooter/Preshooter/PID/kD", m_config.preshooterKd);
    SmartDashboard.putNumber("Shooter/Preshooter/PID/kV", m_config.preshooterKv);
    SmartDashboard.putNumber("Shooter/Preshooter/PID/kS", m_config.preshooterKs);

    SmartDashboard.putNumber("Shooter/MainShooter/PID/kP", m_config.mainShooterKp);
    SmartDashboard.putNumber("Shooter/MainShooter/PID/kI", m_config.mainShooterKi);
    SmartDashboard.putNumber("Shooter/MainShooter/PID/kD", m_config.mainShooterKd);
    SmartDashboard.putNumber("Shooter/MainShooter/PID/kV", m_config.mainShooterKv);
    SmartDashboard.putNumber("Shooter/MainShooter/PID/kS", m_config.mainShooterKs);

    // Publish initial setpoint values
    SmartDashboard.putNumber("Shooter/Preshooter/Setpoint1_RPM", m_config.preshooterSetpoint1Rpm);
    SmartDashboard.putNumber("Shooter/Preshooter/Setpoint2_RPM", m_config.preshooterSetpoint2Rpm);
    SmartDashboard.putNumber("Shooter/MainShooter/Setpoint1_RPM", m_config.mainShooterSetpoint1Rpm);
    SmartDashboard.putNumber("Shooter/MainShooter/Setpoint2_RPM", m_config.mainShooterSetpoint2Rpm);

    // Publish servo position values
    SmartDashboard.putNumber("Shooter/Servo/Position1", m_config.servoPosition1);
    SmartDashboard.putNumber("Shooter/Servo/Position2", m_config.servoPosition2);

    // Motor enable/disable toggles (default both enabled)
    SmartDashboard.putBoolean("Shooter/Preshooter/Enabled", true);
    SmartDashboard.putBoolean("Shooter/MainShooter/Enabled", true);

    // Apply PID button indicator
    SmartDashboard.putBoolean("Shooter/ApplyPID", false);

    configureTelemetry();
    configureBindings();
  }

  private void configureTelemetry() {
    SmartDashboard.putString("Shooter/Controls",
        m_config.buttonSetpoint1 + "=SP1 | " +
        m_config.buttonSetpoint2 + "=SP2 | " +
        m_config.buttonApplyPid + "=Apply+Save | " +
        m_config.buttonCharPreshooter + "=TunePre | " +
        m_config.buttonCharMainShooter + "=TuneMain | " +
        m_config.buttonServoPos1 + "=Servo1 | " +
        m_config.buttonServoPos2 + "=Servo2");

    // Characterization status widgets
    SmartDashboard.putString("Shooter/Tuning/Status", "Ready");
    SmartDashboard.putNumber("Shooter/Tuning/Phase", 0);
    SmartDashboard.putNumber("Shooter/Tuning/kS", 0.0);
    SmartDashboard.putNumber("Shooter/Tuning/kV", 0.0);
    SmartDashboard.putNumber("Shooter/Tuning/kP", 0.0);
    SmartDashboard.putNumber("Shooter/Tuning/SteadyStateError_RPM", 0.0);
  }

  private void configureBindings() {
    // Setpoint 1 + servo position 1
    resolveButton(m_config.buttonSetpoint1).whileTrue(Commands.run(() -> {
      double preRpm = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint1_RPM", m_config.preshooterSetpoint1Rpm);
      double mainRpm = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint1_RPM", m_config.mainShooterSetpoint1Rpm);
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position1", m_config.servoPosition1);

      m_shooter.setVelocities(preRpm, mainRpm);
      m_shooter.setServoPosition(servoPos);
    }, m_shooter)).onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // Setpoint 2 + servo position 2
    resolveButton(m_config.buttonSetpoint2).whileTrue(Commands.run(() -> {
      double preRpm = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint2_RPM", m_config.preshooterSetpoint2Rpm);
      double mainRpm = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint2_RPM", m_config.mainShooterSetpoint2Rpm);
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position2", m_config.servoPosition2);

      m_shooter.setVelocities(preRpm, mainRpm);
      m_shooter.setServoPosition(servoPos);
    }, m_shooter)).onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // Apply PID configuration from dashboard and save to JSON
    resolveButton(m_config.buttonApplyPid).onTrue(Commands.runOnce(() -> {
      applyPidConfig();
      SmartDashboard.putBoolean("Shooter/ApplyPID", false);
    }));

    // Characterize preshooter (hold button for ~20 seconds)
    resolveButton(m_config.buttonCharPreshooter).whileTrue(new CharacterizeShooterCommand(m_shooter, true));

    // Characterize main shooter (hold button for ~20 seconds)
    resolveButton(m_config.buttonCharMainShooter).whileTrue(new CharacterizeShooterCommand(m_shooter, false));

    // Servo to position 1 (no motors)
    resolveButton(m_config.buttonServoPos1).onTrue(Commands.runOnce(() -> {
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position1", m_config.servoPosition1);
      m_shooter.setServoPosition(servoPos);
    }));

    // Servo to position 2 (no motors)
    resolveButton(m_config.buttonServoPos2).onTrue(Commands.runOnce(() -> {
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position2", m_config.servoPosition2);
      m_shooter.setServoPosition(servoPos);
    }));
  }

  private void applyPidConfig() {
    // Read PID values from SmartDashboard
    double preKp = SmartDashboard.getNumber("Shooter/Preshooter/PID/kP", m_config.preshooterKp);
    double preKi = SmartDashboard.getNumber("Shooter/Preshooter/PID/kI", m_config.preshooterKi);
    double preKd = SmartDashboard.getNumber("Shooter/Preshooter/PID/kD", m_config.preshooterKd);
    double preKv = SmartDashboard.getNumber("Shooter/Preshooter/PID/kV", m_config.preshooterKv);
    double preKs = SmartDashboard.getNumber("Shooter/Preshooter/PID/kS", m_config.preshooterKs);

    double mainKp = SmartDashboard.getNumber("Shooter/MainShooter/PID/kP", m_config.mainShooterKp);
    double mainKi = SmartDashboard.getNumber("Shooter/MainShooter/PID/kI", m_config.mainShooterKi);
    double mainKd = SmartDashboard.getNumber("Shooter/MainShooter/PID/kD", m_config.mainShooterKd);
    double mainKv = SmartDashboard.getNumber("Shooter/MainShooter/PID/kV", m_config.mainShooterKv);
    double mainKs = SmartDashboard.getNumber("Shooter/MainShooter/PID/kS", m_config.mainShooterKs);

    // Read setpoint and servo values from SmartDashboard
    double preSetpoint1 = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint1_RPM", m_config.preshooterSetpoint1Rpm);
    double preSetpoint2 = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint2_RPM", m_config.preshooterSetpoint2Rpm);
    double mainSetpoint1 = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint1_RPM", m_config.mainShooterSetpoint1Rpm);
    double mainSetpoint2 = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint2_RPM", m_config.mainShooterSetpoint2Rpm);
    double servoPos1 = SmartDashboard.getNumber("Shooter/Servo/Position1", m_config.servoPosition1);
    double servoPos2 = SmartDashboard.getNumber("Shooter/Servo/Position2", m_config.servoPosition2);

    // Hot-reload PID configuration
    m_shooter.updatePreshooterPid(preKp, preKi, preKd, preKv, preKs);
    m_shooter.updateMainShooterPid(mainKp, mainKi, mainKd, mainKv, mainKs);

    // Update config object in memory
    m_config.preshooterKp = preKp;
    m_config.preshooterKi = preKi;
    m_config.preshooterKd = preKd;
    m_config.preshooterKv = preKv;
    m_config.preshooterKs = preKs;

    m_config.mainShooterKp = mainKp;
    m_config.mainShooterKi = mainKi;
    m_config.mainShooterKd = mainKd;
    m_config.mainShooterKv = mainKv;
    m_config.mainShooterKs = mainKs;

    m_config.preshooterSetpoint1Rpm = preSetpoint1;
    m_config.preshooterSetpoint2Rpm = preSetpoint2;
    m_config.mainShooterSetpoint1Rpm = mainSetpoint1;
    m_config.mainShooterSetpoint2Rpm = mainSetpoint2;
    m_config.servoPosition1 = servoPos1;
    m_config.servoPosition2 = servoPos2;

    // Save to JSON on roboRIO
    if (ShooterConfigLoader.saveConfig(CONFIG_FILENAME, m_config)) {
      System.out.println("PID configuration applied and saved!");
    } else {
      System.out.println("PID configuration applied but save failed!");
    }
  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }

  /**
   * Resolves a button name string to the corresponding controller trigger.
   * Supported: A, B, X, Y, LEFT_BUMPER, RIGHT_BUMPER, BACK, START, LEFT_STICK, RIGHT_STICK
   */
  private edu.wpi.first.wpilibj2.command.button.Trigger resolveButton(String buttonName) {
    switch (buttonName.toUpperCase()) {
      case "A": return m_driver.a();
      case "B": return m_driver.b();
      case "X": return m_driver.x();
      case "Y": return m_driver.y();
      case "LEFT_BUMPER": return m_driver.leftBumper();
      case "RIGHT_BUMPER": return m_driver.rightBumper();
      case "BACK": return m_driver.back();
      case "START": return m_driver.start();
      case "LEFT_STICK": return m_driver.leftStick();
      case "RIGHT_STICK": return m_driver.rightStick();
      default:
        System.err.println("Unknown button name: " + buttonName + ", defaulting to A");
        return m_driver.a();
    }
  }
}
