package frc.robot.shooter;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.motor.CanMotorWrapper;
import frc.robot.motor.ControllerType;
import frc.robot.motor.MotorConfiguration;
import frc.robot.motor.MotorKind;

public class ShooterSubsystem extends SubsystemBase {
  private final CanMotorWrapper m_preshooter;
  private final CanMotorWrapper m_mainShooter;
  private final Servo m_servo;
  private final double m_velocityToleranceRpm;

  private double m_preshooterSetpointRpm = 0.0;
  private double m_mainShooterSetpointRpm = 0.0;

  private boolean m_preshooterEnabled = true;
  private boolean m_mainShooterEnabled = true;

  public ShooterSubsystem(ShooterConfig config) {
    // Create preshooter motor (Kraken X44)
    m_preshooter = new CanMotorWrapper(
        MotorConfiguration.builder(ControllerType.TALON_FX, MotorKind.KRAKEN_X44)
            .canId(config.preshooterCanId)
            .inverted(config.preshooterInverted)
            .gearRatio(config.preshooterGearRatio)
            .kP(config.preshooterKp)
            .kI(config.preshooterKi)
            .kD(config.preshooterKd)
            .kV(config.preshooterKv)
            .kS(config.preshooterKs)
            .brakeMode(config.brakeMode)
            .build());

    // Create main shooter motor (Kraken X60)
    m_mainShooter = new CanMotorWrapper(
        MotorConfiguration.builder(ControllerType.TALON_FX, MotorKind.KRAKEN)
            .canId(config.mainShooterCanId)
            .inverted(config.mainShooterInverted)
            .gearRatio(config.mainShooterGearRatio)
            .kP(config.mainShooterKp)
            .kI(config.mainShooterKi)
            .kD(config.mainShooterKd)
            .kV(config.mainShooterKv)
            .kS(config.mainShooterKs)
            .brakeMode(config.brakeMode)
            .build());

    // Create servo
    m_servo = new Servo(config.servoPwmChannel);

    m_velocityToleranceRpm = 50.0; // Default tolerance
  }

  /**
   * Sets the velocity setpoints for both shooter motors in RPM.
   *
   * @param preshooterRpm Preshooter target velocity in RPM
   * @param mainShooterRpm Main shooter target velocity in RPM
   */
  public void setVelocities(double preshooterRpm, double mainShooterRpm) {
    m_preshooterSetpointRpm = preshooterRpm;
    m_mainShooterSetpointRpm = mainShooterRpm;

    // Convert RPM to RPS for motor control, only if enabled
    if (m_preshooterEnabled) {
      m_preshooter.setVelocityRps(preshooterRpm / 60.0);
    }
    if (m_mainShooterEnabled) {
      m_mainShooter.setVelocityRps(mainShooterRpm / 60.0);
    }
  }

  /**
   * Enables or disables the preshooter motor.
   *
   * @param enabled True to enable, false to disable
   */
  public void setPreshooterEnabled(boolean enabled) {
    m_preshooterEnabled = enabled;
    if (!enabled) {
      m_preshooter.stop();
      m_preshooterSetpointRpm = 0.0;
    }
  }

  /**
   * Enables or disables the main shooter motor.
   *
   * @param enabled True to enable, false to disable
   */
  public void setMainShooterEnabled(boolean enabled) {
    m_mainShooterEnabled = enabled;
    if (!enabled) {
      m_mainShooter.stop();
      m_mainShooterSetpointRpm = 0.0;
    }
  }

  /**
   * Checks if preshooter is enabled.
   *
   * @return True if enabled
   */
  public boolean isPreshooterEnabled() {
    return m_preshooterEnabled;
  }

  /**
   * Checks if main shooter is enabled.
   *
   * @return True if enabled
   */
  public boolean isMainShooterEnabled() {
    return m_mainShooterEnabled;
  }

  /**
   * Sets the servo position (0.0 to 1.0).
   *
   * @param position Servo position
   */
  public void setServoPosition(double position) {
    m_servo.set(position);
  }

  /**
   * Stops both shooter motors.
   */
  public void stop() {
    m_preshooter.stop();
    m_mainShooter.stop();
    m_preshooterSetpointRpm = 0.0;
    m_mainShooterSetpointRpm = 0.0;
  }

  /**
   * Gets the current preshooter velocity in RPM.
   *
   * @return Preshooter velocity in RPM
   */
  public double getPreshooterVelocityRpm() {
    return m_preshooter.getVelocityRps() * 60.0;
  }

  /**
   * Gets the current main shooter velocity in RPM.
   *
   * @return Main shooter velocity in RPM
   */
  public double getMainShooterVelocityRpm() {
    return m_mainShooter.getVelocityRps() * 60.0;
  }

  /**
   * Checks if preshooter is within tolerance of setpoint.
   *
   * @return True if preshooter is at setpoint
   */
  public boolean isPreshooterAtSetpoint() {
    if (m_preshooterSetpointRpm == 0.0) {
      return false;
    }
    double error = Math.abs(getPreshooterVelocityRpm() - m_preshooterSetpointRpm);
    return error < m_velocityToleranceRpm;
  }

  /**
   * Checks if main shooter is within tolerance of setpoint.
   *
   * @return True if main shooter is at setpoint
   */
  public boolean isMainShooterAtSetpoint() {
    if (m_mainShooterSetpointRpm == 0.0) {
      return false;
    }
    double error = Math.abs(getMainShooterVelocityRpm() - m_mainShooterSetpointRpm);
    return error < m_velocityToleranceRpm;
  }

  /**
   * Updates preshooter PID configuration (hot-reload).
   *
   * @param kP Proportional gain
   * @param kI Integral gain
   * @param kD Derivative gain
   * @param kV Velocity feedforward
   * @param kS Static friction feedforward
   */
  public void updatePreshooterPid(double kP, double kI, double kD, double kV, double kS) {
    m_preshooter.updatePidConfig(kP, kI, kD, kV, kS);
  }

  /**
   * Updates main shooter PID configuration (hot-reload).
   *
   * @param kP Proportional gain
   * @param kI Integral gain
   * @param kD Derivative gain
   * @param kV Velocity feedforward
   * @param kS Static friction feedforward
   */
  public void updateMainShooterPid(double kP, double kI, double kD, double kV, double kS) {
    m_mainShooter.updatePidConfig(kP, kI, kD, kV, kS);
  }

  /**
   * Sets raw voltage to preshooter motor (for characterization).
   *
   * @param volts Voltage to apply
   */
  public void setPreshooterVoltage(double volts) {
    if (m_preshooterEnabled) {
      m_preshooter.setVoltage(volts);
    }
  }

  /**
   * Sets raw voltage to main shooter motor (for characterization).
   *
   * @param volts Voltage to apply
   */
  public void setMainShooterVoltage(double volts) {
    if (m_mainShooterEnabled) {
      m_mainShooter.setVoltage(volts);
    }
  }

  /**
   * Gets applied voltage for preshooter (for characterization).
   *
   * @return Applied voltage in volts
   */
  public double getPreshooterVoltage() {
    return m_preshooter.getAppliedVoltage();
  }

  /**
   * Gets applied voltage for main shooter (for characterization).
   *
   * @return Applied voltage in volts
   */
  public double getMainShooterVoltage() {
    return m_mainShooter.getAppliedVoltage();
  }

  /**
   * Gets preshooter current (for characterization).
   *
   * @return Current in amps
   */
  public double getPreshooterCurrent() {
    return m_preshooter.getCurrent();
  }

  /**
   * Gets main shooter current (for characterization).
   *
   * @return Current in amps
   */
  public double getMainShooterCurrent() {
    return m_mainShooter.getCurrent();
  }

  /**
   * Gets preshooter velocity in RPS (for characterization).
   *
   * @return Velocity in rotations per second
   */
  public double getPreshooterVelocityRps() {
    return m_preshooter.getVelocityRps();
  }

  /**
   * Gets main shooter velocity in RPS (for characterization).
   *
   * @return Velocity in rotations per second
   */
  public double getMainShooterVelocityRps() {
    return m_mainShooter.getVelocityRps();
  }

  @Override
  public void periodic() {
    // Read enable toggles from dashboard
    m_preshooterEnabled = SmartDashboard.getBoolean("Shooter/Preshooter/Enabled", true);
    m_mainShooterEnabled = SmartDashboard.getBoolean("Shooter/MainShooter/Enabled", true);

    // Publish telemetry to SmartDashboard using hierarchical paths for Elastic Dashboard
    SmartDashboard.putNumber("Shooter/Preshooter/ActualRPM", m_preshooterEnabled ? getPreshooterVelocityRpm() : 0.0);
    SmartDashboard.putNumber("Shooter/MainShooter/ActualRPM", m_mainShooterEnabled ? getMainShooterVelocityRpm() : 0.0);
    SmartDashboard.putBoolean("Shooter/Preshooter/AtSetpoint", m_preshooterEnabled && isPreshooterAtSetpoint());
    SmartDashboard.putBoolean("Shooter/MainShooter/AtSetpoint", m_mainShooterEnabled && isMainShooterAtSetpoint());
    SmartDashboard.putNumber("Shooter/Preshooter/SetpointRPM", m_preshooterSetpointRpm);
    SmartDashboard.putNumber("Shooter/MainShooter/SetpointRPM", m_mainShooterSetpointRpm);
    SmartDashboard.putNumber("Shooter/Preshooter/CurrentAmps", m_preshooterEnabled ? getPreshooterCurrent() : 0.0);
    SmartDashboard.putNumber("Shooter/MainShooter/CurrentAmps", m_mainShooterEnabled ? getMainShooterCurrent() : 0.0);
  }

  /**
   * Closes and releases all hardware resources.
   */
  public void close() {
    m_preshooter.close();
    m_mainShooter.close();
    m_servo.close();
  }
}
