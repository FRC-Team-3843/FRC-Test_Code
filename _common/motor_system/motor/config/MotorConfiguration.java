package frc.robot.motor.config;

import frc.robot.motor.core.ControllerType;

public final class MotorConfiguration {
  public final ControllerFamily controllerFamily;
  public final TransportType transport;
  public final FeedbackSource feedbackSource;
  public final ControllerType controllerType;
  public final MotorKind motorKind;
  public final int canId;
  public final String canBus;
  public final int pwmChannel;
  public final int powerChannel;
  public final double gearRatio;
  public final boolean inverted;
  public final boolean brakeMode;
  public final double kP;
  public final double kI;
  public final double kD;
  public final double kV;
  public final double kS;
  public final double kA;
  public final double kG;
  public final double kP_pos;
  public final double kD_pos;
  public final double currentLimit;
  public final double motionCruiseVelocity;
  public final double motionAcceleration;
  public final double motionJerk;
  public final double forwardLimit;
  public final double reverseLimit;
  public final double wheelDiameter;
  public final double distancePerRotation;
  public final double armLength;
  public final double mass;
  public final MechanismType mechanismType;
  public final PowerModuleType powerModuleType;
  public final int powerModuleId;
  public final int quadratureChannelA;
  public final int quadratureChannelB;
  public final int dutyCycleChannel;
  public final int analogChannel;
  public final double feedbackDistancePerPulseRotations;
  public final double feedbackFullRangeRotations;
  public final double feedbackOffsetRotations;
  public final boolean feedbackInverted;
  public final boolean feedbackContinuousWrap;
  public final int feedbackSamplesToAverage;
  public final MotorCapabilities capabilities;

  private MotorConfiguration(Builder builder) {
    controllerFamily = builder.controllerFamily;
    transport = builder.transport;
    feedbackSource = builder.feedbackSource;
    controllerType = deriveControllerType(builder.controllerFamily, builder.transport);
    motorKind = builder.motorKind;
    canId = builder.canId;
    canBus = builder.canBus;
    pwmChannel = builder.pwmChannel;
    powerChannel = builder.powerChannel;
    gearRatio = builder.gearRatio;
    inverted = builder.inverted;
    brakeMode = builder.brakeMode;
    kP = builder.kP;
    kI = builder.kI;
    kD = builder.kD;
    kV = builder.kV;
    kS = builder.kS;
    kA = builder.kA;
    kG = builder.kG;
    kP_pos = builder.kP_pos;
    kD_pos = builder.kD_pos;
    currentLimit = builder.currentLimit;
    motionCruiseVelocity = builder.motionCruiseVelocity;
    motionAcceleration = builder.motionAcceleration;
    motionJerk = builder.motionJerk;
    forwardLimit = builder.forwardLimit;
    reverseLimit = builder.reverseLimit;
    wheelDiameter = builder.wheelDiameter;
    distancePerRotation = builder.distancePerRotation;
    armLength = builder.armLength;
    mass = builder.mass;
    mechanismType = builder.mechanismType;
    powerModuleType = builder.powerModuleType;
    powerModuleId = builder.powerModuleId;
    quadratureChannelA = builder.quadratureChannelA;
    quadratureChannelB = builder.quadratureChannelB;
    dutyCycleChannel = builder.dutyCycleChannel;
    analogChannel = builder.analogChannel;
    feedbackDistancePerPulseRotations = builder.feedbackDistancePerPulseRotations;
    feedbackFullRangeRotations = builder.feedbackFullRangeRotations;
    feedbackOffsetRotations = builder.feedbackOffsetRotations;
    feedbackInverted = builder.feedbackInverted;
    feedbackContinuousWrap = builder.feedbackContinuousWrap;
    feedbackSamplesToAverage = builder.feedbackSamplesToAverage;
    capabilities = MotorCapabilities.resolve(this);
  }

  public static Builder builder(ControllerFamily controllerFamily, TransportType transport, MotorKind motorKind) {
    return new Builder(controllerFamily, transport, motorKind);
  }

  public static MotorConfiguration fromMotorConfig(MotorSystemConfig systemConfig, MotorConfig mc) {
    return builder(mc.getControllerFamily(), mc.getTransportType(), mc.getMotorKind())
        .feedbackSource(mc.getFeedbackSource())
        .canId(mc.canId)
        .canBus(mc.canBus)
        .pwmChannel(mc.pwmChannel)
        .powerChannel(mc.powerChannel)
        .powerModule(systemConfig.powerModuleType, systemConfig.powerModuleId)
        .gearRatio(mc.gearRatio)
        .inverted(mc.inverted)
        .brakeMode(mc.brakeMode)
        .kP(mc.kP)
        .kI(mc.kI)
        .kD(mc.kD)
        .kV(mc.kV)
        .kS(mc.kS)
        .kA(mc.kA)
        .kG(mc.kG)
        .mechanismType(mc.getMechanismType())
        .kP_pos(mc.kP_pos)
        .kD_pos(mc.kD_pos)
        .currentLimit(mc.currentLimit)
        .motionCruiseVelocity(mc.motionCruiseVelocity)
        .motionAcceleration(mc.motionAcceleration)
        .motionJerk(mc.motionJerk)
        .forwardLimit(mc.forwardLimit)
        .reverseLimit(mc.reverseLimit)
        .wheelDiameter(mc.wheelDiameter)
        .distancePerRotation(mc.distancePerRotation)
        .armLength(mc.armLength)
        .mass(mc.mass)
        .quadratureChannels(mc.quadratureChannelA, mc.quadratureChannelB)
        .dutyCycleChannel(mc.dutyCycleChannel)
        .analogChannel(mc.analogChannel)
        .feedbackDistancePerPulseRotations(mc.feedbackDistancePerPulseRotations)
        .feedbackFullRangeRotations(mc.feedbackFullRangeRotations)
        .feedbackOffsetRotations(mc.feedbackOffsetRotations)
        .feedbackInverted(mc.feedbackInverted)
        .feedbackContinuousWrap(mc.feedbackContinuousWrap)
        .feedbackSamplesToAverage(mc.feedbackSamplesToAverage)
        .build();
  }

  private static ControllerType deriveControllerType(
      ControllerFamily controllerFamily,
      TransportType transport) {
    if (transport == TransportType.PWM) {
      return controllerFamily == ControllerFamily.SERVO
          ? ControllerType.PWM_SERVO
          : ControllerType.PWM_MOTOR;
    }

    switch (controllerFamily) {
      case SPARK_MAX:
        return ControllerType.SPARK_MAX;
      case SPARK_FLEX:
        return ControllerType.SPARK_FLEX;
      case TALON_SRX:
        return ControllerType.TALON_SRX;
      case TALON_FX:
        return ControllerType.TALON_FX;
      case TALON_FXS:
        return ControllerType.TALON_FXS;
      default:
        return ControllerType.PWM_MOTOR;
    }
  }

  public static final class Builder {
    private final ControllerFamily controllerFamily;
    private final TransportType transport;
    private final MotorKind motorKind;
    private FeedbackSource feedbackSource = FeedbackSource.INTEGRATED;
    private int canId = 1;
    private String canBus = "";
    private int pwmChannel = 0;
    private int powerChannel = -1;
    private double gearRatio = 1.0;
    private boolean inverted = false;
    private boolean brakeMode = true;
    private double kP = 0.0;
    private double kI = 0.0;
    private double kD = 0.0;
    private double kV = 0.0;
    private double kS = 0.0;
    private double kA = 0.0;
    private double kG = 0.0;
    private double kP_pos = 0.0;
    private double kD_pos = 0.0;
    private double currentLimit = 40.0;
    private double motionCruiseVelocity = 0.0;
    private double motionAcceleration = 0.0;
    private double motionJerk = 0.0;
    private double forwardLimit = Double.MAX_VALUE;
    private double reverseLimit = -Double.MAX_VALUE;
    private double wheelDiameter = 0.0;
    private double distancePerRotation = 0.0;
    private double armLength = 0.0;
    private double mass = 0.0;
    private MechanismType mechanismType = MechanismType.SIMPLE;
    private PowerModuleType powerModuleType = PowerModuleType.NONE;
    private int powerModuleId = 1;
    private int quadratureChannelA = -1;
    private int quadratureChannelB = -1;
    private int dutyCycleChannel = -1;
    private int analogChannel = -1;
    private double feedbackDistancePerPulseRotations = 1.0 / 4096.0;
    private double feedbackFullRangeRotations = 1.0;
    private double feedbackOffsetRotations = 0.0;
    private boolean feedbackInverted = false;
    private boolean feedbackContinuousWrap = true;
    private int feedbackSamplesToAverage = 5;

    private Builder(ControllerFamily controllerFamily, TransportType transport, MotorKind motorKind) {
      this.controllerFamily = controllerFamily;
      this.transport = transport;
      this.motorKind = motorKind;
    }

    public Builder feedbackSource(FeedbackSource feedbackSource) {
      this.feedbackSource = feedbackSource;
      return this;
    }

    public Builder canId(int canId) {
      this.canId = canId;
      return this;
    }

    public Builder canBus(String canBus) {
      this.canBus = canBus == null ? "" : canBus;
      return this;
    }

    public Builder pwmChannel(int pwmChannel) {
      this.pwmChannel = pwmChannel;
      return this;
    }

    public Builder powerModule(PowerModuleType moduleType, int moduleId) {
      powerModuleType = moduleType == null ? PowerModuleType.NONE : moduleType;
      powerModuleId = moduleId;
      return this;
    }

    public Builder powerChannel(int powerChannel) {
      this.powerChannel = powerChannel;
      return this;
    }

    public Builder gearRatio(double gearRatio) {
      this.gearRatio = gearRatio;
      return this;
    }

    public Builder inverted(boolean inverted) {
      this.inverted = inverted;
      return this;
    }

    public Builder brakeMode(boolean brakeMode) {
      this.brakeMode = brakeMode;
      return this;
    }

    public Builder kP(double kP) {
      this.kP = kP;
      return this;
    }

    public Builder kI(double kI) {
      this.kI = kI;
      return this;
    }

    public Builder kD(double kD) {
      this.kD = kD;
      return this;
    }

    public Builder kV(double kV) {
      this.kV = kV;
      return this;
    }

    public Builder kS(double kS) {
      this.kS = kS;
      return this;
    }

    public Builder kA(double kA) {
      this.kA = kA;
      return this;
    }

    public Builder kG(double kG) {
      this.kG = kG;
      return this;
    }

    public Builder kP_pos(double kP_pos) {
      this.kP_pos = kP_pos;
      return this;
    }

    public Builder kD_pos(double kD_pos) {
      this.kD_pos = kD_pos;
      return this;
    }

    public Builder currentLimit(double currentLimit) {
      this.currentLimit = currentLimit;
      return this;
    }

    public Builder motionCruiseVelocity(double motionCruiseVelocity) {
      this.motionCruiseVelocity = motionCruiseVelocity;
      return this;
    }

    public Builder motionAcceleration(double motionAcceleration) {
      this.motionAcceleration = motionAcceleration;
      return this;
    }

    public Builder motionJerk(double motionJerk) {
      this.motionJerk = motionJerk;
      return this;
    }

    public Builder forwardLimit(double forwardLimit) {
      this.forwardLimit = forwardLimit;
      return this;
    }

    public Builder reverseLimit(double reverseLimit) {
      this.reverseLimit = reverseLimit;
      return this;
    }

    public Builder wheelDiameter(double wheelDiameter) {
      this.wheelDiameter = wheelDiameter;
      return this;
    }

    public Builder distancePerRotation(double distancePerRotation) {
      this.distancePerRotation = distancePerRotation;
      return this;
    }

    public Builder armLength(double armLength) {
      this.armLength = armLength;
      return this;
    }

    public Builder mass(double mass) {
      this.mass = mass;
      return this;
    }

    public Builder mechanismType(MechanismType mechanismType) {
      this.mechanismType = mechanismType;
      return this;
    }

    public Builder quadratureChannels(int channelA, int channelB) {
      quadratureChannelA = channelA;
      quadratureChannelB = channelB;
      return this;
    }

    public Builder dutyCycleChannel(int dutyCycleChannel) {
      this.dutyCycleChannel = dutyCycleChannel;
      return this;
    }

    public Builder analogChannel(int analogChannel) {
      this.analogChannel = analogChannel;
      return this;
    }

    public Builder feedbackDistancePerPulseRotations(double value) {
      feedbackDistancePerPulseRotations = value;
      return this;
    }

    public Builder feedbackFullRangeRotations(double value) {
      feedbackFullRangeRotations = value;
      return this;
    }

    public Builder feedbackOffsetRotations(double value) {
      feedbackOffsetRotations = value;
      return this;
    }

    public Builder feedbackInverted(boolean value) {
      feedbackInverted = value;
      return this;
    }

    public Builder feedbackContinuousWrap(boolean value) {
      feedbackContinuousWrap = value;
      return this;
    }

    public Builder feedbackSamplesToAverage(int value) {
      feedbackSamplesToAverage = value;
      return this;
    }

    public MotorConfiguration build() {
      return new MotorConfiguration(this);
    }
  }
}
