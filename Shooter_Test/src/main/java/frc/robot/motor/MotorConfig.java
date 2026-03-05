package frc.robot.motor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson-serializable per-motor configuration.
 *
 * <p>Each motor in the system gets one of these. Contains everything needed to
 * create a UniversalMotor and configure its PID/feedforward gains.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MotorConfig {
  @JsonProperty("name")
  public String name = "Motor";

  @JsonProperty("controllerType")
  public String controllerType = "TALON_FX";

  @JsonProperty("motorKind")
  public String motorKind = "KRAKEN";

  @JsonProperty("canId")
  public int canId = 1;

  @JsonProperty("canBus")
  public String canBus = "";

  @JsonProperty("mechanismType")
  public String mechanismType = "SIMPLE";

  @JsonProperty("gearRatio")
  public double gearRatio = 1.0;

  @JsonProperty("inverted")
  public boolean inverted = false;

  @JsonProperty("brakeMode")
  public boolean brakeMode = true;

  @JsonProperty("useFeedback")
  public boolean useFeedback = true;

  // PID + Feedforward gains
  @JsonProperty("kP")
  public double kP = 0.0;

  @JsonProperty("kI")
  public double kI = 0.0;

  @JsonProperty("kD")
  public double kD = 0.0;

  @JsonProperty("kV")
  public double kV = 0.0;

  @JsonProperty("kS")
  public double kS = 0.0;

  @JsonProperty("kA")
  public double kA = 0.0;

  @JsonProperty("kG")
  public double kG = 0.0;

  // Position PID gains (Slot1)
  @JsonProperty("kP_pos")
  public double kP_pos = 0.0;

  @JsonProperty("kD_pos")
  public double kD_pos = 0.0;

  // Motor protection
  @JsonProperty("currentLimit")
  public double currentLimit = 40.0;

  // Motion profiling
  @JsonProperty("motionCruiseVelocity")
  public double motionCruiseVelocity = 0.0;

  @JsonProperty("motionAcceleration")
  public double motionAcceleration = 0.0;

  @JsonProperty("motionJerk")
  public double motionJerk = 0.0;

  // Soft position limits (rotations)
  @JsonProperty("forwardLimit")
  public double forwardLimit = Double.MAX_VALUE;

  @JsonProperty("reverseLimit")
  public double reverseLimit = -Double.MAX_VALUE;

  // Mechanism geometry (for unit conversion and physical estimation)
  @JsonProperty("wheelDiameter")
  public double wheelDiameter = 0.0;       // inches, SIMPLE: flywheel/drive wheel diameter

  @JsonProperty("distancePerRotation")
  public double distancePerRotation = 0.0;  // inches, ELEVATOR: linear travel per mechanism rotation

  @JsonProperty("armLength")
  public double armLength = 0.0;            // inches, ARM: pivot-to-tip distance

  @JsonProperty("mass")
  public double mass = 0.0;                 // lbs, mechanism mass for estimation validation

  public MotorConfig() {}

  /** Returns the parsed ControllerType enum. */
  public ControllerType getControllerType() {
    try {
      return ControllerType.valueOf(controllerType);
    } catch (IllegalArgumentException e) {
      System.err.println("Unknown controller type: " + controllerType + ", defaulting to TALON_FX");
      return ControllerType.TALON_FX;
    }
  }

  /** Returns the parsed MotorKind enum. */
  public MotorKind getMotorKind() {
    try {
      return MotorKind.valueOf(motorKind);
    } catch (IllegalArgumentException e) {
      System.err.println("Unknown motor kind: " + motorKind + ", defaulting to KRAKEN");
      return MotorKind.KRAKEN;
    }
  }

  /** Returns the parsed MechanismType enum. */
  public MechanismType getMechanismType() {
    try {
      return MechanismType.valueOf(mechanismType);
    } catch (IllegalArgumentException e) {
      System.err.println("Unknown mechanism type: " + mechanismType + ", defaulting to SIMPLE");
      return MechanismType.SIMPLE;
    }
  }

  /** Returns the ControllerPreset for this motor's controller type. */
  public ControllerPreset getControllerPreset() {
    return ControllerPreset.fromControllerType(getControllerType());
  }
}
