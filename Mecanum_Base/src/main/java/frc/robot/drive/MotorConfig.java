package frc.robot.drive;

import frc.robot.Constants.DriveConstants.MotorControllerType;
import frc.robot.Constants.DriveConstants.MotorKind;

public final class MotorConfig {
  public final MotorControllerType controllerType;
  public final MotorKind motorKind;
  public final int id;
  public final boolean inverted;
  public final double gearRatio;
  public final boolean useSensor;

  public MotorConfig(
      MotorControllerType controllerType,
      MotorKind motorKind,
      int id,
      boolean inverted,
      double gearRatio,
      boolean useSensor) {
    this.controllerType = controllerType;
    this.motorKind = motorKind;
    this.id = id;
    this.inverted = inverted;
    this.gearRatio = gearRatio;
    this.useSensor = useSensor;
  }
}
