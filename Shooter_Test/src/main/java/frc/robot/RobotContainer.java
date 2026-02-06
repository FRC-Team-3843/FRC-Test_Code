// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.shooter.ShooterConfig;
import frc.robot.shooter.ShooterConfigLoader;
import frc.robot.shooter.ShooterSubsystem;

public class RobotContainer {
  private final CommandXboxController m_driver =
      new CommandXboxController(Constants.OperatorConstants.DRIVER_CONTROLLER_PORT);

  // Load shooter configuration from JSON
  private final ShooterConfig m_config = ShooterConfigLoader.loadConfigOrDefault("shooter-config.json");
  private final ShooterSubsystem m_shooter = new ShooterSubsystem(m_config);

  public RobotContainer() {
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

    // Apply PID button indicator
    SmartDashboard.putBoolean("Shooter/ApplyPID", false);

    configureTelemetry();
    configureBindings();
  }

  private void configureTelemetry() {
    // Note: Telemetry is continuously updated in Robot.java's robotPeriodic() or subsystem periodic()
    // Initial placeholder values
    SmartDashboard.putNumber("Shooter/Preshooter/ActualRPM", 0);
    SmartDashboard.putNumber("Shooter/MainShooter/ActualRPM", 0);
    SmartDashboard.putBoolean("Shooter/Preshooter/AtSetpoint", false);
    SmartDashboard.putBoolean("Shooter/MainShooter/AtSetpoint", false);
    SmartDashboard.putString("Shooter/Controls", "A = Setpoint1 | B = Setpoint2 | X = Apply PID");
  }

  private void configureBindings() {
    // A button: Setpoint 1 + servo position 1
    m_driver.a().whileTrue(Commands.run(() -> {
      double preRpm = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint1_RPM", m_config.preshooterSetpoint1Rpm);
      double mainRpm = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint1_RPM", m_config.mainShooterSetpoint1Rpm);
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position1", m_config.servoPosition1);

      m_shooter.setVelocities(preRpm, mainRpm);
      m_shooter.setServoPosition(servoPos);
    }, m_shooter)).onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // B button: Setpoint 2 + servo position 2
    m_driver.b().whileTrue(Commands.run(() -> {
      double preRpm = SmartDashboard.getNumber("Shooter/Preshooter/Setpoint2_RPM", m_config.preshooterSetpoint2Rpm);
      double mainRpm = SmartDashboard.getNumber("Shooter/MainShooter/Setpoint2_RPM", m_config.mainShooterSetpoint2Rpm);
      double servoPos = SmartDashboard.getNumber("Shooter/Servo/Position2", m_config.servoPosition2);

      m_shooter.setVelocities(preRpm, mainRpm);
      m_shooter.setServoPosition(servoPos);
    }, m_shooter)).onFalse(Commands.runOnce(m_shooter::stop, m_shooter));

    // X button: Apply PID configuration from dashboard
    m_driver.x().onTrue(Commands.runOnce(() -> {
      applyPidConfig();
      SmartDashboard.putBoolean("Shooter/ApplyPID", false);
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

    // Hot-reload PID configuration
    m_shooter.updatePreshooterPid(preKp, preKi, preKd, preKv, preKs);
    m_shooter.updateMainShooterPid(mainKp, mainKi, mainKd, mainKv, mainKs);

    System.out.println("PID configuration applied!");
  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
}
