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

    public MotorConfiguration build() {
      return new MotorConfiguration(this);
    }
  }
}
