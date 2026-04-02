// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.net.WebServer;
import frc.lib.motor.config.MotorSystemConfig;
import frc.lib.motor.config.MotorSystemConfigLoader;

public class Robot extends TimedRobot {
  private Command m_autonomousCommand;
  private final RobotContainer m_robotContainer;

  public Robot() {
    MotorSystemConfig config = MotorSystemConfigLoader.loadOrDefault("motor-config.json");

    if (config.enableLogging) {
      DataLogManager.start();
      DriverStation.startDataLog(DataLogManager.getLog());
    }

    // Serve deploy directory on port 5800 for Elastic remote layout loading
    // elastic-layout.json is generated at build time by DashboardGenerator (Gradle task)
    WebServer.start(5800, Filesystem.getDeployDirectory().getPath());

    m_robotContainer = new RobotContainer(config);
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    m_robotContainer.checkDashboardToggles();
  }

  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();
    if (m_autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(m_autonomousCommand);
    }
  }

  @Override
  public void teleopInit() {
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }
}
