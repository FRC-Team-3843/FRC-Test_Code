// COPY THIS FILE: Update package declaration when copying to your project
// Example packages: frc.robot.drive, frc.robot.motor
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

  // Optional configuration fields
  public final Integer currentLimitAmps;  // Current limit in amps (null = use default)
  public final Boolean brakeMode;         // Brake mode (null = use default, usually brake)
  public final Double kP;                 // Proportional gain for PID (null = use default)
  public final Double kI;                 // Integral gain for PID (null = use default)
  public final Double kD;                 // Derivative gain for PID (null = use default)
  public final Double kF;                 // Feedforward gain for PID (null = use default)

  public MotorConfig(
      MotorControllerType controllerType,
      MotorKind motorKind,
      int id,
      boolean inverted,
      double gearRatio,
      boolean useSensor) {
    this(controllerType, motorKind, id, inverted, gearRatio, useSensor, null, null, null, null, null, null);
  }

  public MotorConfig(
      MotorControllerType controllerType,
      MotorKind motorKind,
      int id,
      boolean inverted,
      double gearRatio,
      boolean useSensor,
      Integer currentLimitAmps,
      Boolean brakeMode,
      Double kP,
      Double kI,
      Double kD,
      Double kF) {
    this.controllerType = controllerType;
    this.motorKind = motorKind;
    this.id = id;
    this.inverted = inverted;
    this.gearRatio = gearRatio;
    this.useSensor = useSensor;
    this.currentLimitAmps = currentLimitAmps;
    this.brakeMode = brakeMode;
    this.kP = kP;
    this.kI = kI;
    this.kD = kD;
    this.kF = kF;
  }
}
