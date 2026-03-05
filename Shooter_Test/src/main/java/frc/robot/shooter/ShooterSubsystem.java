package frc.robot.shooter;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.motor.ControllerPreset;
import frc.robot.motor.MotorConfig;
import frc.robot.motor.MotorConfiguration;
import frc.robot.motor.MotorFactory;
import frc.robot.motor.MotorSysId;
import frc.robot.motor.MotorSystemConfig;
import frc.robot.motor.UnitConverter;
import frc.robot.motor.UniversalMotor;

/**
 * Generic motor system subsystem driven by MotorSystemConfig.
 *
 * <p>Creates motors dynamically from config. Each motor gets its own SysId routine.
 * Supports any number of motors and an optional servo.
 */
public class ShooterSubsystem extends SubsystemBase {
  private final MotorSystemConfig m_config;
  private final UniversalMotor[] m_motors;
  private final MotorSysId[] m_sysIds;
  private final ControllerPreset[] m_presets;
  private final String[] m_motorNames;
  private final UnitConverter[] m_converters;
  @SuppressWarnings("unchecked")
  private final SendableChooser<String>[] m_chartChoosers;
  private final Servo m_servo;
  private final double m_velocityToleranceRpm;

  private final double[] m_setpointsRpm;
  private final boolean[] m_enabled;

  public ShooterSubsystem(MotorSystemConfig config) {
    m_config = config;
    int count = config.motors.size();
    m_motors = new UniversalMotor[count];
    m_sysIds = new MotorSysId[count];
    m_presets = new ControllerPreset[count];
    m_motorNames = new String[count];
    m_converters = new UnitConverter[count];
    m_chartChoosers = new SendableChooser[count];
    m_setpointsRpm = new double[count];
    m_enabled = new boolean[count];

    for (int i = 0; i < count; i++) {
      MotorConfig mc = config.motors.get(i);
      m_motorNames[i] = config.motorPrefix(mc.name);
      m_presets[i] = mc.getControllerPreset();
      m_enabled[i] = true;

      MotorConfiguration motorConfig = MotorConfiguration.fromMotorConfig(mc);
      m_motors[i] = MotorFactory.create(motorConfig);

      m_sysIds[i] = new MotorSysId(
          m_motors[i], m_motorNames[i], this,
          mc.getMechanismType(), m_presets[i], mc);

      m_converters[i] = new UnitConverter(mc.getMechanismType(),
          mc.wheelDiameter, mc.distancePerRotation, mc.armLength, false);

      // Switchable chart chooser
      SendableChooser<String> chooser = new SendableChooser<>();
      chooser.setDefaultOption("RPM", "rpm");
      chooser.addOption("Current (A)", "current");
      chooser.addOption("Voltage (V)", "voltage");
      chooser.addOption("Position (rot)", "position");
      chooser.addOption("Temperature (C)", "temperature");
      if (m_converters[i].hasGeometry()) {
        chooser.addOption("Real Speed", "realspeed");
      }
      m_chartChoosers[i] = chooser;
      SmartDashboard.putData(m_motorNames[i] + "/ChartType", chooser);
    }

    // Optional servo
    if (config.servoChannel >= 0) {
      m_servo = new Servo(config.servoChannel);
    } else {
      m_servo = null;
    }

    m_velocityToleranceRpm = config.velocityToleranceRpm;
  }

  /** Returns the number of motors in this system. */
  public int getMotorCount() {
    return m_motors.length;
  }

  /** Returns the motor at the given index. */
  public UniversalMotor getMotor(int index) {
    return m_motors[index];
  }

  /** Returns the dashboard name for the motor at the given index. */
  public String getMotorName(int index) {
    return m_motorNames[index];
  }

  /** Returns the MotorSysId for the motor at the given index. */
  public MotorSysId getSysId(int index) {
    return m_sysIds[index];
  }

  /** Returns the ControllerPreset for the motor at the given index. */
  public ControllerPreset getPreset(int index) {
    return m_presets[index];
  }

  /** Returns the config that created this subsystem. */
  public MotorSystemConfig getConfig() {
    return m_config;
  }

  /**
   * Sets velocity for a specific motor by index.
   *
   * @param index motor index
   * @param rpm target velocity in RPM
   */
  public void setVelocity(int index, double rpm) {
    m_setpointsRpm[index] = rpm;
    if (m_enabled[index]) {
      m_motors[index].setVelocityRps(rpm / 60.0);
    }
  }

  /**
   * Sets velocities for all motors at once. Array length must match motor count.
   */
  public void setVelocities(double... rpms) {
    for (int i = 0; i < Math.min(rpms.length, m_motors.length); i++) {
      setVelocity(i, rpms[i]);
    }
  }

  /** Enables or disables a motor by index. */
  public void setEnabled(int index, boolean enabled) {
    m_enabled[index] = enabled;
    if (!enabled) {
      m_motors[index].stop();
      m_setpointsRpm[index] = 0.0;
    }
  }

  /** Returns whether the motor at the given index is enabled. */
  public boolean isEnabled(int index) {
    return m_enabled[index];
  }

  /** Returns the current velocity in RPM for a motor by index. */
  public double getVelocityRpm(int index) {
    return m_motors[index].getVelocityRps() * 60.0;
  }

  /** Returns whether a motor is within tolerance of its setpoint. */
  public boolean isAtSetpoint(int index) {
    if (m_setpointsRpm[index] == 0.0) return false;
    return Math.abs(getVelocityRpm(index) - m_setpointsRpm[index]) < m_velocityToleranceRpm;
  }

  /** Returns the current draw for a motor by index. */
  public double getCurrent(int index) {
    return m_motors[index].getCurrent();
  }

  /**
   * Sets position for a specific motor by index.
   *
   * @param index motor index
   * @param rotations target position in rotations
   */
  public void setPosition(int index, double rotations) {
    if (m_enabled[index]) {
      m_motors[index].setPositionRotations(rotations);
    }
  }

  /**
   * Sets profiled (trapezoidal) position for a specific motor by index.
   *
   * @param index motor index
   * @param rotations target position in rotations
   */
  public void setProfiledPosition(int index, double rotations) {
    if (m_enabled[index]) {
      m_motors[index].setProfiledPosition(rotations);
    }
  }

  /**
   * Updates velocity PID (Slot0) for a motor by index (hot-reload).
   */
  public void updatePid(int index, double kP, double kI, double kD,
                         double kV, double kS, double kA, double kG) {
    m_motors[index].updatePidConfig(kP, kI, kD, kV, kS, kA, kG);
  }

  /**
   * Updates position PID (Slot1) for a motor by index (hot-reload).
   */
  public void updatePositionPid(int index, double kP, double kD,
                                 double kV, double kS, double kA, double kG) {
    m_motors[index].updatePositionPid(kP, kD, kV, kS, kA, kG);
  }

  /** Sets servo position (0.0 to 1.0). No-op if no servo configured. */
  public void setServoPosition(double position) {
    if (m_servo != null) {
      m_servo.set(position);
    }
  }

  /** Stops all motors. */
  public void stop() {
    for (int i = 0; i < m_motors.length; i++) {
      m_motors[i].stop();
      m_setpointsRpm[i] = 0.0;
    }
  }

  /** Returns the SysId routine with analysis command for a motor by index. */
  public Command sysIdWithAnalysis(int index) {
    return m_sysIds[index].fullRoutineWithAnalysis();
  }

  /** Returns the SysId routine (without analysis) for a motor by index. */
  public Command sysId(int index) {
    return m_sysIds[index].fullRoutine();
  }

  // ─── Convenience methods for 2-motor shooter ─────────────────────

  /** Sets both shooter motor velocities (convenience for 2-motor setup). */
  public void setShooterVelocities(double preshooterRpm, double mainShooterRpm) {
    if (m_motors.length >= 2) {
      setVelocity(0, preshooterRpm);
      setVelocity(1, mainShooterRpm);
    }
  }

  @Override
  public void periodic() {
    boolean useMetric = SmartDashboard.getBoolean(
        m_config.systemName + "/UseMetric", false);

    for (int i = 0; i < m_motors.length; i++) {
      String prefix = m_motorNames[i] + "/";
      boolean en = m_enabled[i];

      // Read enable toggles from dashboard
      m_enabled[i] = SmartDashboard.getBoolean(prefix + "Enabled", true);
      en = m_enabled[i];

      // Core telemetry (always published for text displays)
      double rpm = en ? getVelocityRpm(i) : 0.0;
      SmartDashboard.putNumber(prefix + "ActualRPM", rpm);
      SmartDashboard.putBoolean(prefix + "AtSetpoint", en && isAtSetpoint(i));
      SmartDashboard.putNumber(prefix + "SetpointRPM", m_setpointsRpm[i]);
      SmartDashboard.putNumber(prefix + "CurrentAmps", en ? getCurrent(i) : 0.0);

      // Real-world unit telemetry
      m_converters[i].setUseMetric(useMetric);
      if (m_converters[i].hasGeometry()) {
        double rps = en ? m_motors[i].getVelocityRps() : 0.0;
        double rot = en ? m_motors[i].getPositionRotations() : 0.0;
        SmartDashboard.putNumber(prefix + "RealSpeed",
            m_converters[i].convertVelocity(rps));
        SmartDashboard.putString(prefix + "SpeedUnit",
            m_converters[i].velocityUnit());
        SmartDashboard.putNumber(prefix + "RealPosition",
            m_converters[i].convertPosition(rot));
        SmartDashboard.putString(prefix + "PosUnit",
            m_converters[i].positionUnit());
      }

      // Switchable chart: read chooser selection, publish to single graph topic
      String chartType = m_chartChoosers[i].getSelected();
      if (chartType == null) chartType = "rpm";
      double chartValue;
      String chartLabel;
      switch (chartType) {
        case "current":
          chartValue = en ? getCurrent(i) : 0.0;
          chartLabel = "Current (A)";
          break;
        case "voltage":
          chartValue = en ? m_motors[i].getAppliedVoltage() : 0.0;
          chartLabel = "Voltage (V)";
          break;
        case "position":
          chartValue = en ? m_motors[i].getPositionRotations() : 0.0;
          chartLabel = "Position (rot)";
          break;
        case "temperature":
          chartValue = en ? m_motors[i].getTemperature() : 0.0;
          chartLabel = "Temperature (C)";
          break;
        case "realspeed":
          double rps = en ? m_motors[i].getVelocityRps() : 0.0;
          chartValue = m_converters[i].convertVelocity(rps);
          chartLabel = "Speed (" + m_converters[i].velocityUnit() + ")";
          break;
        default: // "rpm"
          chartValue = rpm;
          chartLabel = "RPM";
          break;
      }
      SmartDashboard.putNumber(prefix + "ChartValue", chartValue);
      SmartDashboard.putString(prefix + "ChartLabel", chartLabel);
    }
  }

  /** Closes and releases all hardware resources. */
  public void close() {
    for (UniversalMotor motor : m_motors) {
      motor.close();
    }
    if (m_servo != null) {
      m_servo.close();
    }
  }
}
