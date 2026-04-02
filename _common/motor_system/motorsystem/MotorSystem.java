package frc.robot.motorsystem;

import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.motor.tuning.ControllerPreset;
import frc.robot.motor.config.MotorSystemConfig;
import frc.robot.motor.tuning.MotorSysId;
import frc.robot.motor.core.UniversalMotor;

/** Aggregates independent motor channels plus the optional servo for this test rig. */
public class MotorSystem {
  private final MotorSystemConfig m_config;
  private final MotorChannel[] m_motorChannels;
  private final Servo m_servo;

  public MotorSystem(MotorSystemConfig config) {
    m_config = config;
    m_motorChannels = new MotorChannel[config.motors.size()];
    for (int i = 0; i < config.motors.size(); i++) {
      m_motorChannels[i] = new MotorChannel(config, config.motors.get(i));
    }

    if (config.servoChannel >= 0) {
      m_servo = new Servo(config.servoChannel);
    } else {
      m_servo = null;
    }
  }

  public int getMotorCount() {
    return m_motorChannels.length;
  }

  public MotorChannel getMotorChannel(int index) {
    return m_motorChannels[index];
  }

  public Subsystem[] getAllMotorRequirements() {
    return m_motorChannels;
  }

  public UniversalMotor getMotor(int index) {
    return m_motorChannels[index].getMotor();
  }

  public String getMotorName(int index) {
    return m_motorChannels[index].getMotorName();
  }

  public MotorSysId getSysId(int index) {
    return m_motorChannels[index].getSysId();
  }

  public ControllerPreset getPreset(int index) {
    return m_motorChannels[index].getPreset();
  }

  public MotorSystemConfig getConfig() {
    return m_config;
  }

  public void setVelocity(int index, double rpm) {
    m_motorChannels[index].setVelocity(rpm);
  }

  public void setVelocities(double... rpms) {
    for (int i = 0; i < Math.min(rpms.length, m_motorChannels.length); i++) {
      setVelocity(i, rpms[i]);
    }
  }

  public void setEnabled(int index, boolean enabled) {
    m_motorChannels[index].setEnabled(enabled);
  }

  public boolean isEnabled(int index) {
    return m_motorChannels[index].isEnabled();
  }

  public double getVelocityRpm(int index) {
    return m_motorChannels[index].getVelocityRpm();
  }

  public boolean isAtSetpoint(int index) {
    return m_motorChannels[index].isAtSetpoint();
  }

  public double getCurrent(int index) {
    return m_motorChannels[index].getCurrent();
  }

  public String getSelectedControlMode(int index) {
    return m_motorChannels[index].getSelectedControlMode();
  }

  public void setPosition(int index, double rotations) {
    m_motorChannels[index].setPosition(rotations);
  }

  public void setProfiledPosition(int index, double rotations) {
    m_motorChannels[index].setProfiledPosition(rotations);
  }

  public void updatePid(int index, double kP, double kI, double kD,
                        double kV, double kS, double kA, double kG) {
    m_motorChannels[index].updatePid(kP, kI, kD, kV, kS, kA, kG);
  }

  public void updatePositionPid(int index, double kP, double kD,
                                double kV, double kS, double kA, double kG) {
    m_motorChannels[index].updatePositionPid(kP, kD, kV, kS, kA, kG);
  }

  public void setServoPosition(double position) {
    if (m_servo != null) {
      m_servo.set(position);
    }
  }

  public void stop() {
    for (MotorChannel channel : m_motorChannels) {
      channel.stop();
    }
  }

  public Command sysIdWithAnalysis(int index) {
    return m_motorChannels[index].sysIdWithAnalysis();
  }

  public Command sysId(int index) {
    return m_motorChannels[index].sysId();
  }

  public void close() {
    for (MotorChannel channel : m_motorChannels) {
      channel.close();
    }
    if (m_servo != null) {
      m_servo.close();
    }
  }
}
