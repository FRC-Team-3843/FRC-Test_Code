package frc.robot.motor.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import frc.robot.motor.tuning.ControllerPreset;

/**
 * Jackson-serializable per-motor configuration using explicit controller family,
 * transport, and feedback source.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MotorConfig {
  @JsonProperty("name")
  public String name = "Motor";

  @JsonProperty("controllerFamily")
  public String controllerFamily = "TALON_FX";

  @JsonProperty("transport")
  public String transport = "CAN";

  @JsonProperty("feedbackSource")
  public String feedbackSource = "INTEGRATED";

  @JsonProperty("motorKind")
  public String motorKind = "KRAKEN";

  @JsonProperty("mechanismType")
  public String mechanismType = "SIMPLE";

  @JsonProperty("canId")
  public int canId = 1;

  @JsonProperty("canBus")
  public String canBus = "";

  @JsonProperty("pwmChannel")
  public int pwmChannel = 0;

  @JsonProperty("powerChannel")
  public int powerChannel = -1;

  @JsonProperty("gearRatio")
  public double gearRatio = 1.0;

  @JsonProperty("inverted")
  public boolean inverted = false;

  @JsonProperty("brakeMode")
  public boolean brakeMode = true;

  // External feedback wiring
  @JsonProperty("quadratureChannelA")
  public int quadratureChannelA = -1;

  @JsonProperty("quadratureChannelB")
  public int quadratureChannelB = -1;

  @JsonProperty("dutyCycleChannel")
  public int dutyCycleChannel = -1;

  @JsonProperty("analogChannel")
  public int analogChannel = -1;

  @JsonProperty("feedbackDistancePerPulseRotations")
  public double feedbackDistancePerPulseRotations = 1.0 / 4096.0;

  @JsonProperty("feedbackFullRangeRotations")
  public double feedbackFullRangeRotations = 1.0;

  @JsonProperty("feedbackOffsetRotations")
  public double feedbackOffsetRotations = 0.0;

  @JsonProperty("feedbackInverted")
  public boolean feedbackInverted = false;

  @JsonProperty("feedbackContinuousWrap")
  public boolean feedbackContinuousWrap = true;

  @JsonProperty("feedbackSamplesToAverage")
  public int feedbackSamplesToAverage = 5;

  // PID + feedforward gains
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

  @JsonProperty("kP_pos")
  public double kP_pos = 0.0;

  @JsonProperty("kD_pos")
  public double kD_pos = 0.0;

  @JsonProperty("currentLimit")
  public double currentLimit = 40.0;

  @JsonProperty("motionCruiseVelocity")
  public double motionCruiseVelocity = 0.0;

  @JsonProperty("motionAcceleration")
  public double motionAcceleration = 0.0;

  @JsonProperty("motionJerk")
  public double motionJerk = 0.0;

  @JsonProperty("forwardLimit")
  public double forwardLimit = Double.MAX_VALUE;

  @JsonProperty("reverseLimit")
  public double reverseLimit = -Double.MAX_VALUE;

  @JsonProperty("wheelDiameter")
  public double wheelDiameter = 0.0;

  @JsonProperty("distancePerRotation")
  public double distancePerRotation = 0.0;

  @JsonProperty("armLength")
  public double armLength = 0.0;

  @JsonProperty("mass")
  public double mass = 0.0;

  public ControllerFamily getControllerFamily() {
    try {
      return ControllerFamily.valueOf(controllerFamily);
    } catch (IllegalArgumentException e) {
      System.err.println("Unknown controller family: " + controllerFamily + ", defaulting to TALON_FX");
      return ControllerFamily.TALON_FX;
    }
  }

  public TransportType getTransportType() {
    try {
      return TransportType.valueOf(transport);
    } catch (IllegalArgumentException e) {
      System.err.println("Unknown transport: " + transport + ", defaulting to CAN");
      return TransportType.CAN;
    }
  }

  public FeedbackSource getFeedbackSource() {
    try {
      return FeedbackSource.valueOf(feedbackSource);
    } catch (IllegalArgumentException e) {
      System.err.println("Unknown feedback source: " + feedbackSource + ", defaulting to INTEGRATED");
      return FeedbackSource.INTEGRATED;
    }
  }

  public MotorKind getMotorKind() {
    try {
      return MotorKind.valueOf(motorKind);
    } catch (IllegalArgumentException e) {
      System.err.println("Unknown motor kind: " + motorKind + ", defaulting to KRAKEN");
      return MotorKind.KRAKEN;
    }
  }

  public MechanismType getMechanismType() {
    try {
      return MechanismType.valueOf(mechanismType);
    } catch (IllegalArgumentException e) {
      System.err.println("Unknown mechanism type: " + mechanismType + ", defaulting to SIMPLE");
      return MechanismType.SIMPLE;
    }
  }

  public ControllerPreset getControllerPreset() {
    return ControllerPreset.from(this);
  }
}
