package frc.robot.drive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

  @JsonCreator
  public MotorConfig(
      @JsonProperty("controllerType") MotorControllerType controllerType,
      @JsonProperty("motorKind") MotorKind motorKind,
      @JsonProperty("id") int id,
      @JsonProperty("inverted") boolean inverted,
      @JsonProperty("gearRatio") double gearRatio,
      @JsonProperty("useSensor") boolean useSensor,
      @JsonProperty("currentLimitAmps") Integer currentLimitAmps,
      @JsonProperty("brakeMode") Boolean brakeMode,
      @JsonProperty("kP") Double kP,
      @JsonProperty("kI") Double kI,
      @JsonProperty("kD") Double kD,
      @JsonProperty("kF") Double kF) {
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
