package frc.robot.drive;

import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX;
import edu.wpi.first.wpilibj.motorcontrol.PWMVictorSPX;
import frc.robot.Constants.DriveConstants.MotorControllerType;

public class PwmMotorWrapper implements UniversalMotor {
  private final MotorController controller;

  public PwmMotorWrapper(MotorConfig config) {
    MotorControllerType type = config.controllerType;
    switch (type) {
      case PWM_SPARKMAX:
        controller = new PWMSparkMax(config.id);
        break;
      case PWM_TALONSRX:
        controller = new PWMTalonSRX(config.id);
        break;
      case PWM_VICTORSPX:
        controller = new PWMVictorSPX(config.id);
        break;
      default:
        throw new IllegalArgumentException("Unsupported PWM motor type: " + type);
    }
    controller.setInverted(config.inverted);
  }

  @Override
  public void setVoltage(double volts) {
    controller.setVoltage(volts);
  }

  @Override
  public void setVelocityRps(double rps) {
    controller.set(Math.signum(rps));
  }

  @Override
  public void setPositionRotations(double rotations) {
  }

  @Override
  public double getVelocityRps() {
    return 0.0;
  }

  @Override
  public double getPositionRotations() {
    return 0.0;
  }

  @Override
  public void setBrake(boolean brake) {
  }

  @Override
  public void stop() {
    controller.stopMotor();
  }
}
