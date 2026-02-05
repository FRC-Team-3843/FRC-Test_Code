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
  }

  public static Builder builder(ControllerType controllerType, MotorKind motorKind) {
    return new Builder(controllerType, motorKind);
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

    public MotorConfiguration build() {
      return new MotorConfiguration(this);
    }
  }
}
