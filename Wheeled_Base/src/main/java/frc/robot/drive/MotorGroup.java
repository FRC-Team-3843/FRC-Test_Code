package frc.robot.drive;

import java.util.ArrayList;
import java.util.List;

public class MotorGroup {
  private final List<UniversalMotor> motors = new ArrayList<>();
  private final UniversalMotor leader;
  private final boolean useSensor;

  public MotorGroup(MotorConfig[] configs) {
    if (configs.length == 0) {
      throw new IllegalArgumentException("MotorGroup requires at least one motor");
    }
    for (MotorConfig config : configs) {
      motors.add(MotorFactory.createMotor(config));
    }
    leader = motors.get(0);
    useSensor = configs[0].useSensor;
  }

  public void setVoltage(double volts) {
    for (UniversalMotor motor : motors) {
      motor.setVoltage(volts);
    }
  }

  public void setVelocityRps(double rps) {
    for (UniversalMotor motor : motors) {
      motor.setVelocityRps(rps);
    }
  }

  public void setPositionRotations(double rotations) {
    for (UniversalMotor motor : motors) {
      motor.setPositionRotations(rotations);
    }
  }

  public double getVelocityRps() {
    return useSensor ? leader.getVelocityRps() : 0.0;
  }

  public double getPositionRotations() {
    return useSensor ? leader.getPositionRotations() : 0.0;
  }

  public void setBrake(boolean brake) {
    for (UniversalMotor motor : motors) {
      motor.setBrake(brake);
    }
  }

  public void stop() {
    for (UniversalMotor motor : motors) {
      motor.stop();
    }
  }
}
