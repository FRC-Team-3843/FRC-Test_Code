package frc.robot.motor;

public final class MotorConfiguration {
  public final ControllerType controllerType;
  public final MotorKind motorKind;
  public final int canId;
  public final String canBus;
  public final int pwmChannel;
  public final double gearRatio;
  public final boolean inverted;
  public final boolean useQuadEncoder;
  public final int quadCpr;
  public final double kP;
  public final double kI;
  public final double kD;
  public final double kV;
  public final double kS;
  public final double kA;
  public final double kG;
  public final MechanismType mechanismType;
  public final boolean brakeMode;
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

  private MotorConfiguration(Builder builder) {
    controllerType = builder.controllerType;
    motorKind = builder.motorKind;
    canId = builder.canId;
    canBus = builder.canBus;
    pwmChannel = builder.pwmChannel;
    gearRatio = builder.gearRatio;
    inverted = builder.inverted;
    useQuadEncoder = builder.useQuadEncoder;
    quadCpr = builder.quadCpr;
    kP = builder.kP;
    kI = builder.kI;
    kD = builder.kD;
    kV = builder.kV;
    kS = builder.kS;
    kA = builder.kA;
    kG = builder.kG;
    mechanismType = builder.mechanismType;
    brakeMode = builder.brakeMode;
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
  }

  public static Builder builder(ControllerType controllerType, MotorKind motorKind) {
    return new Builder(controllerType, motorKind);
  }

  /**
   * Creates a MotorConfiguration from a MotorConfig (JSON-loaded per-motor config).
   *
   * @param mc the motor config
   * @return a fully built MotorConfiguration
   */
  public static MotorConfiguration fromMotorConfig(MotorConfig mc) {
    return builder(mc.getControllerType(), mc.getMotorKind())
        .canId(mc.canId)
        .canBus(mc.canBus)
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
        .build();
  }

  public static final class Builder {
    private final ControllerType controllerType;
    private final MotorKind motorKind;
    private int canId = 1;
    private String canBus = "";
    private int pwmChannel = 0;
    private double gearRatio = 1.0;
    private boolean inverted = false;
    private boolean useQuadEncoder = false;
    private int quadCpr = 4096;
    private double kP = 0.0;
    private double kI = 0.0;
    private double kD = 0.0;
    private double kV = 0.0;
    private double kS = 0.0;
    private double kA = 0.0;
    private double kG = 0.0;
    private MechanismType mechanismType = MechanismType.SIMPLE;
    private boolean brakeMode = true;
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

    private Builder(ControllerType controllerType, MotorKind motorKind) {
      this.controllerType = controllerType;
      this.motorKind = motorKind;
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

    public Builder gearRatio(double gearRatio) {
      this.gearRatio = gearRatio;
      return this;
    }

    public Builder inverted(boolean inverted) {
      this.inverted = inverted;
      return this;
    }

    public Builder useQuadEncoder(boolean useQuadEncoder) {
      this.useQuadEncoder = useQuadEncoder;
      return this;
    }

    public Builder quadCpr(int quadCpr) {
      this.quadCpr = quadCpr;
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

    public Builder mechanismType(MechanismType mechanismType) {
      this.mechanismType = mechanismType;
      return this;
    }

    public Builder brakeMode(boolean brakeMode) {
      this.brakeMode = brakeMode;
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

    public MotorConfiguration build() {
      return new MotorConfiguration(this);
    }
  }
}
