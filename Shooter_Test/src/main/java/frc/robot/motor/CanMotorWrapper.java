package frc.robot.motor;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.Slot1Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXSConfiguration;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.hardware.TalonFXS;
import com.ctre.phoenix6.signals.GravityTypeValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.ClosedLoopSlot;
import com.revrobotics.spark.config.MAXMotionConfig.MAXMotionPositionMode;
import com.revrobotics.spark.config.SparkBaseConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkFlexConfig;
import com.revrobotics.spark.config.SparkMaxConfig;
import frc.robot.motor.UniversalMotor.Mode;

public class CanMotorWrapper implements UniversalMotor {
  private static final int TALON_SRX_CPR = 4096;

  private final MotorConfiguration config;
  private final ControllerType controllerType;
  private final MotorKind motorKind;
  private final MechanismType mechanismType;
  private final double gearRatio;
  private final boolean useSensor;

  private SparkBase spark;
  private SparkClosedLoopController sparkClosedLoop;
  private RelativeEncoder sparkEncoder;

  private TalonFX talonFx;
  private TalonFXS talonFxs;
  private WPI_TalonSRX talonSrx;

  private final VoltageOut talonVoltage = new VoltageOut(0.0);
  private final VelocityVoltage talonVelocity = new VelocityVoltage(0.0);
  private final PositionVoltage talonPosition = new PositionVoltage(0.0).withSlot(1);
  private final MotionMagicVoltage talonMotionMagic = new MotionMagicVoltage(0.0).withSlot(1);

  private Mode controlMode = Mode.DUTY_CYCLE;
  private double healthScore = 100.0;

  public CanMotorWrapper(MotorConfiguration config) {
    this.config = config;
    this.controllerType = config.controllerType;
    this.motorKind = config.motorKind;
    this.mechanismType = config.mechanismType;
    this.gearRatio = config.gearRatio;
    this.useSensor = config.useQuadEncoder;

    switch (controllerType) {
      case SPARK_MAX:
        spark = new SparkMax(config.canId, sparkMotorType());
        configureSpark(new SparkMaxConfig(), config);
        break;
      case SPARK_FLEX:
        spark = new SparkFlex(config.canId, sparkMotorType());
        configureSpark(new SparkFlexConfig(), config);
        break;
      case TALON_FX:
        talonFx = new TalonFX(config.canId);
        configureTalonFx(config);
        break;
      case TALON_FXS:
        talonFxs = new TalonFXS(config.canId);
        configureTalonFxs(config);
        break;
      case TALON_SRX:
        talonSrx = new WPI_TalonSRX(config.canId);
        configureTalonSrx(config);
        break;
      default:
        throw new IllegalArgumentException("Unsupported CAN motor type: " + controllerType);
    }

    if (config.motionCruiseVelocity > 0) {
      configureMotionProfile(config.motionCruiseVelocity, config.motionAcceleration,
          config.motionJerk);
    }
  }

  private SparkBase.MotorType sparkMotorType() {
    return motorKind.isBrushed()
        ? SparkBase.MotorType.kBrushed
        : SparkBase.MotorType.kBrushless;
  }

  private void configureSpark(SparkBaseConfig baseConfig, MotorConfiguration config) {
    sparkClosedLoop = spark.getClosedLoopController();
    sparkEncoder = spark.getEncoder();

    baseConfig.inverted(config.inverted);
    baseConfig.idleMode(config.brakeMode ? IdleMode.kBrake : IdleMode.kCoast);
    baseConfig.encoder.positionConversionFactor(1.0 / gearRatio);
    baseConfig.encoder.velocityConversionFactor(1.0 / gearRatio / 60.0);

    // Current limiting
    if (config.currentLimit > 0) {
      baseConfig.smartCurrentLimit((int) config.currentLimit);
    }

    // Slot1: position PID
    baseConfig.closedLoop
        .p(config.kP_pos, ClosedLoopSlot.kSlot1)
        .d(config.kD_pos, ClosedLoopSlot.kSlot1);

    // Soft position limits
    if (config.forwardLimit != Double.MAX_VALUE) {
      baseConfig.softLimit.forwardSoftLimit(config.forwardLimit).forwardSoftLimitEnabled(true);
    }
    if (config.reverseLimit != -Double.MAX_VALUE) {
      baseConfig.softLimit.reverseSoftLimit(config.reverseLimit).reverseSoftLimitEnabled(true);
    }

    // MaxMotion constraints
    if (config.motionCruiseVelocity > 0) {
      baseConfig.closedLoop.maxMotion
          .cruiseVelocity(config.motionCruiseVelocity)
          .maxAcceleration(config.motionAcceleration)
          .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal);
    }

    spark.configure(baseConfig, ResetMode.kResetSafeParameters, PersistMode.kNoPersistParameters);
  }

  private void configureTalonFx(MotorConfiguration config) {
    TalonFXConfiguration fxConfig = new TalonFXConfiguration();
    fxConfig.MotorOutput.Inverted =
        config.inverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
    fxConfig.MotorOutput.NeutralMode = config.brakeMode ? NeutralModeValue.Brake : NeutralModeValue.Coast;
    fxConfig.Feedback.SensorToMechanismRatio = gearRatio;

    // Slot0: velocity PID
    GravityTypeValue gravType = gravityTypeFromMechanism(config.mechanismType);
    fxConfig.Slot0.kP = config.kP;
    fxConfig.Slot0.kI = config.kI;
    fxConfig.Slot0.kD = config.kD;
    fxConfig.Slot0.kV = config.kV;
    fxConfig.Slot0.kS = config.kS;
    fxConfig.Slot0.kA = config.kA;
    fxConfig.Slot0.kG = config.kG;
    fxConfig.Slot0.GravityType = gravType;

    // Slot1: position PID (shares feedforward with velocity)
    fxConfig.Slot1.kP = config.kP_pos;
    fxConfig.Slot1.kD = config.kD_pos;
    fxConfig.Slot1.kV = config.kV;
    fxConfig.Slot1.kS = config.kS;
    fxConfig.Slot1.kA = config.kA;
    fxConfig.Slot1.kG = config.kG;
    fxConfig.Slot1.GravityType = gravType;

    // Current limits
    if (config.currentLimit > 0) {
      fxConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
      fxConfig.CurrentLimits.SupplyCurrentLimit = config.currentLimit;
      fxConfig.CurrentLimits.StatorCurrentLimitEnable = true;
      fxConfig.CurrentLimits.StatorCurrentLimit = config.currentLimit * 2;
    }

    // Soft position limits
    if (config.forwardLimit != Double.MAX_VALUE) {
      fxConfig.SoftwareLimitSwitch.ForwardSoftLimitEnable = true;
      fxConfig.SoftwareLimitSwitch.ForwardSoftLimitThreshold = config.forwardLimit;
    }
    if (config.reverseLimit != -Double.MAX_VALUE) {
      fxConfig.SoftwareLimitSwitch.ReverseSoftLimitEnable = true;
      fxConfig.SoftwareLimitSwitch.ReverseSoftLimitThreshold = config.reverseLimit;
    }

    // Motion Magic constraints
    if (config.motionCruiseVelocity > 0) {
      fxConfig.MotionMagic.MotionMagicCruiseVelocity = config.motionCruiseVelocity;
      fxConfig.MotionMagic.MotionMagicAcceleration = config.motionAcceleration;
      fxConfig.MotionMagic.MotionMagicJerk = config.motionJerk;
    }

    talonFx.getConfigurator().apply(fxConfig);
  }

  private void configureTalonFxs(MotorConfiguration config) {
    TalonFXSConfiguration fxsConfig = new TalonFXSConfiguration();
    fxsConfig.MotorOutput.Inverted =
        config.inverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
    fxsConfig.MotorOutput.NeutralMode = config.brakeMode ? NeutralModeValue.Brake : NeutralModeValue.Coast;
    fxsConfig.ExternalFeedback.SensorToMechanismRatio = gearRatio;

    // Slot0: velocity PID
    GravityTypeValue gravType = gravityTypeFromMechanism(config.mechanismType);
    fxsConfig.Slot0.kP = config.kP;
    fxsConfig.Slot0.kI = config.kI;
    fxsConfig.Slot0.kD = config.kD;
    fxsConfig.Slot0.kV = config.kV;
    fxsConfig.Slot0.kS = config.kS;
    fxsConfig.Slot0.kA = config.kA;
    fxsConfig.Slot0.kG = config.kG;
    fxsConfig.Slot0.GravityType = gravType;

    // Slot1: position PID
    fxsConfig.Slot1.kP = config.kP_pos;
    fxsConfig.Slot1.kD = config.kD_pos;
    fxsConfig.Slot1.kV = config.kV;
    fxsConfig.Slot1.kS = config.kS;
    fxsConfig.Slot1.kA = config.kA;
    fxsConfig.Slot1.kG = config.kG;
    fxsConfig.Slot1.GravityType = gravType;

    // Current limits
    if (config.currentLimit > 0) {
      fxsConfig.CurrentLimits.SupplyCurrentLimitEnable = true;
      fxsConfig.CurrentLimits.SupplyCurrentLimit = config.currentLimit;
      fxsConfig.CurrentLimits.StatorCurrentLimitEnable = true;
      fxsConfig.CurrentLimits.StatorCurrentLimit = config.currentLimit * 2;
    }

    // Soft position limits
    if (config.forwardLimit != Double.MAX_VALUE) {
      fxsConfig.SoftwareLimitSwitch.ForwardSoftLimitEnable = true;
      fxsConfig.SoftwareLimitSwitch.ForwardSoftLimitThreshold = config.forwardLimit;
    }
    if (config.reverseLimit != -Double.MAX_VALUE) {
      fxsConfig.SoftwareLimitSwitch.ReverseSoftLimitEnable = true;
      fxsConfig.SoftwareLimitSwitch.ReverseSoftLimitThreshold = config.reverseLimit;
    }

    // Motion Magic constraints
    if (config.motionCruiseVelocity > 0) {
      fxsConfig.MotionMagic.MotionMagicCruiseVelocity = config.motionCruiseVelocity;
      fxsConfig.MotionMagic.MotionMagicAcceleration = config.motionAcceleration;
      fxsConfig.MotionMagic.MotionMagicJerk = config.motionJerk;
    }

    talonFxs.getConfigurator().apply(fxsConfig);
  }

  private void configureTalonSrx(MotorConfiguration config) {
    talonSrx.setInverted(config.inverted);
    talonSrx.setNeutralMode(config.brakeMode ? NeutralMode.Brake : NeutralMode.Coast);

    // Slot0: velocity PID
    talonSrx.config_kP(0, config.kP);
    talonSrx.config_kI(0, config.kI);
    talonSrx.config_kD(0, config.kD);
    talonSrx.config_kF(0, config.kV);

    // Slot1: position PID
    talonSrx.config_kP(1, config.kP_pos);
    talonSrx.config_kD(1, config.kD_pos);
    talonSrx.config_kF(1, config.kV);

    // Current limiting
    if (config.currentLimit > 0) {
      talonSrx.configSupplyCurrentLimit(new SupplyCurrentLimitConfiguration(
          true, config.currentLimit, config.currentLimit * 1.25, 1.0));
    }

    // Soft position limits
    if (config.forwardLimit != Double.MAX_VALUE) {
      talonSrx.configForwardSoftLimitEnable(true);
      talonSrx.configForwardSoftLimitThreshold(rotationsToTalonSrxUnits(config.forwardLimit));
    }
    if (config.reverseLimit != -Double.MAX_VALUE) {
      talonSrx.configReverseSoftLimitEnable(true);
      talonSrx.configReverseSoftLimitThreshold(rotationsToTalonSrxUnits(config.reverseLimit));
    }

    // Motion Magic constraints
    if (config.motionCruiseVelocity > 0) {
      talonSrx.configMotionCruiseVelocity(rpsToTalonSrxUnits(config.motionCruiseVelocity));
      talonSrx.configMotionAcceleration(rpsToTalonSrxUnits(config.motionAcceleration));
      talonSrx.configMotionSCurveStrength((int) Math.min(config.motionJerk, 8));
    }
  }

  @Override
  public void setControlMode(Mode mode) {
    this.controlMode = mode;
  }

  @Override
  public Mode getControlMode() {
    return controlMode;
  }

  @Override
  public void set(double value) {
    switch (controlMode) {
      case VOLTAGE:
        setVoltage(value);
        break;
      case VELOCITY:
        setVelocityRps(value);
        break;
      case POSITION:
        setPositionRotations(value);
        break;
      case DUTY_CYCLE:
      default:
        setVoltage(value * 12.0);
        break;
    }
  }

  @Override
  public void setVoltage(double volts) {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        spark.setVoltage(volts);
        break;
      case TALON_FX:
        talonFx.setControl(talonVoltage.withOutput(volts));
        break;
      case TALON_FXS:
        talonFxs.setControl(talonVoltage.withOutput(volts));
        break;
      case TALON_SRX:
        talonSrx.setVoltage(volts);
        break;
      default:
        break;
    }
  }

  @Override
  public void setVelocityRps(double rps) {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        sparkClosedLoop.setSetpoint(rps, SparkBase.ControlType.kVelocity, ClosedLoopSlot.kSlot0);        
        break;
      case TALON_FX:
        talonFx.setControl(talonVelocity.withVelocity(rps));
        break;
      case TALON_FXS:
        talonFxs.setControl(talonVelocity.withVelocity(rps));
        break;
      case TALON_SRX:
        talonSrx.set(ControlMode.Velocity, rpsToTalonSrxUnits(rps));
        break;
      default:
        break;
    }
  }

  @Override
  public void setPositionRotations(double rotations) {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        sparkClosedLoop.setSetpoint(rotations, SparkBase.ControlType.kPosition,
            ClosedLoopSlot.kSlot1);
        break;
      case TALON_FX:
        talonFx.setControl(talonPosition.withPosition(rotations));
        break;
      case TALON_FXS:
        talonFxs.setControl(talonPosition.withPosition(rotations));
        break;
      case TALON_SRX:
        talonSrx.selectProfileSlot(1, 0);
        talonSrx.set(ControlMode.Position, rotationsToTalonSrxUnits(rotations));
        break;
      default:
        break;
    }
  }

  @Override
  public double getVelocityRps() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        return sparkEncoder != null ? sparkEncoder.getVelocity() : 0.0;
      case TALON_FX:
        return talonFx.getVelocity().getValueAsDouble();
      case TALON_FXS:
        return talonFxs.getVelocity().getValueAsDouble();
      case TALON_SRX:
        return talonSrx.getSelectedSensorVelocity() / TALON_SRX_CPR * 10.0 / gearRatio;
      default:
        return 0.0;
    }
  }

  @Override
  public double getPositionRotations() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        return sparkEncoder != null ? sparkEncoder.getPosition() : 0.0;
      case TALON_FX:
        return talonFx.getPosition().getValueAsDouble();
      case TALON_FXS:
        return talonFxs.getPosition().getValueAsDouble();
      case TALON_SRX:
        return talonSrx.getSelectedSensorPosition() / TALON_SRX_CPR / gearRatio;
      default:
        return 0.0;
    }
  }

  @Override
  public void setBrake(boolean brake) {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        applySparkIdleMode(brake);
        break;
      case TALON_FX:
        TalonFXConfiguration fxConfig = new TalonFXConfiguration();
        fxConfig.MotorOutput.NeutralMode = brake ? NeutralModeValue.Brake : NeutralModeValue.Coast;       
        talonFx.getConfigurator().apply(fxConfig);
        break;
      case TALON_FXS:
        TalonFXSConfiguration fxsConfig = new TalonFXSConfiguration();
        fxsConfig.MotorOutput.NeutralMode = brake ? NeutralModeValue.Brake : NeutralModeValue.Coast;      
        talonFxs.getConfigurator().apply(fxsConfig);
        break;
      case TALON_SRX:
        talonSrx.setNeutralMode(brake ? NeutralMode.Brake : NeutralMode.Coast);
        break;
      default:
        break;
    }
  }

  @Override
  public void stop() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        spark.stopMotor();
        break;
      case TALON_FX:
        talonFx.stopMotor();
        break;
      case TALON_FXS:
        talonFxs.stopMotor();
        break;
      case TALON_SRX:
        talonSrx.stopMotor();
        break;
      default:
        break;
    }
  }

  @Override
  public double getCurrent() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        return spark.getOutputCurrent();
      case TALON_FX:
        return talonFx.getTorqueCurrent().getValueAsDouble();
      case TALON_FXS:
        return talonFxs.getTorqueCurrent().getValueAsDouble();
      case TALON_SRX:
        return talonSrx.getStatorCurrent();
      default:
        return 0.0;
    }
  }

  @Override
  public double getAppliedVoltage() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        return spark.getAppliedOutput() * spark.getBusVoltage();
      case TALON_FX:
        return talonFx.getMotorVoltage().getValueAsDouble();
      case TALON_FXS:
        return talonFxs.getMotorVoltage().getValueAsDouble();
      case TALON_SRX:
        return talonSrx.getMotorOutputVoltage();
      default:
        return 0.0;
    }
  }

  @Override
  public double getTemperature() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        return spark.getMotorTemperature();
      case TALON_FX:
        return talonFx.getDeviceTemp().getValueAsDouble();
      case TALON_FXS:
        return talonFxs.getDeviceTemp().getValueAsDouble();
      case TALON_SRX:
        return talonSrx.getTemperature();
      default:
        return 0.0;
    }
  }

  @Override
  public double getHealthScore() {
    return healthScore;
  }

  @Override
  public void setHealthScore(double score) {
    this.healthScore = score;
  }

  @Override
  public String getDeviceName() {
    return "CAN-" + config.canId;
  }

  @Override
  public boolean isServo() {
    return false;
  }

  @Override
  public void updatePidConfig(double kP, double kI, double kD, double kV, double kS) {
    updatePidConfig(kP, kI, kD, kV, kS, 0.0, 0.0);
  }

  @Override
  public void updatePidConfig(double kP, double kI, double kD, double kV, double kS,
                                double kA, double kG) {
    switch (controllerType) {
      case TALON_FX:
        Slot0Configs slot0 = new Slot0Configs();
        slot0.kP = kP;
        slot0.kI = kI;
        slot0.kD = kD;
        slot0.kV = kV;
        slot0.kS = kS;
        slot0.kA = kA;
        slot0.kG = kG;
        slot0.GravityType = gravityTypeFromMechanism(mechanismType);
        talonFx.getConfigurator().apply(slot0);
        break;
      case TALON_FXS:
        Slot0Configs slot0Fxs = new Slot0Configs();
        slot0Fxs.kP = kP;
        slot0Fxs.kI = kI;
        slot0Fxs.kD = kD;
        slot0Fxs.kV = kV;
        slot0Fxs.kS = kS;
        slot0Fxs.kA = kA;
        slot0Fxs.kG = kG;
        slot0Fxs.GravityType = gravityTypeFromMechanism(mechanismType);
        talonFxs.getConfigurator().apply(slot0Fxs);
        break;
      default:
        break;
    }
  }

  @Override
  public void updatePositionPid(double kP, double kD, double kV, double kS,
                                  double kA, double kG) {
    switch (controllerType) {
      case TALON_FX:
        Slot1Configs slot1 = new Slot1Configs();
        slot1.kP = kP;
        slot1.kD = kD;
        slot1.kV = kV;
        slot1.kS = kS;
        slot1.kA = kA;
        slot1.kG = kG;
        slot1.GravityType = gravityTypeFromMechanism(mechanismType);
        talonFx.getConfigurator().apply(slot1);
        break;
      case TALON_FXS:
        Slot1Configs slot1Fxs = new Slot1Configs();
        slot1Fxs.kP = kP;
        slot1Fxs.kD = kD;
        slot1Fxs.kV = kV;
        slot1Fxs.kS = kS;
        slot1Fxs.kA = kA;
        slot1Fxs.kG = kG;
        slot1Fxs.GravityType = gravityTypeFromMechanism(mechanismType);
        talonFxs.getConfigurator().apply(slot1Fxs);
        break;
      case TALON_SRX:
        talonSrx.config_kP(1, kP);
        talonSrx.config_kD(1, kD);
        talonSrx.config_kF(1, kV);
        break;
      default:
        break;
    }
  }

  @Override
  public void configureMotionProfile(double cruiseVelocityRps, double accelerationRps2,
                                       double jerkRps3) {
    switch (controllerType) {
      case TALON_FX:
        MotionMagicConfigs mm = new MotionMagicConfigs();
        mm.MotionMagicCruiseVelocity = cruiseVelocityRps;
        mm.MotionMagicAcceleration = accelerationRps2;
        mm.MotionMagicJerk = jerkRps3;
        talonFx.getConfigurator().apply(mm);
        break;
      case TALON_FXS:
        MotionMagicConfigs mmFxs = new MotionMagicConfigs();
        mmFxs.MotionMagicCruiseVelocity = cruiseVelocityRps;
        mmFxs.MotionMagicAcceleration = accelerationRps2;
        mmFxs.MotionMagicJerk = jerkRps3;
        talonFxs.getConfigurator().apply(mmFxs);
        break;
      case SPARK_MAX:
      case SPARK_FLEX:
        SparkBaseConfig mmConfig = controllerType == ControllerType.SPARK_FLEX
            ? new SparkFlexConfig() : new SparkMaxConfig();
        mmConfig.closedLoop.maxMotion
            .cruiseVelocity(cruiseVelocityRps)
            .maxAcceleration(accelerationRps2)
            .positionMode(MAXMotionPositionMode.kMAXMotionTrapezoidal);
        spark.configure(mmConfig, ResetMode.kNoResetSafeParameters,
            PersistMode.kNoPersistParameters);
        break;
      case TALON_SRX:
        talonSrx.configMotionCruiseVelocity(rpsToTalonSrxUnits(cruiseVelocityRps));
        talonSrx.configMotionAcceleration(rpsToTalonSrxUnits(accelerationRps2));
        talonSrx.configMotionSCurveStrength((int) Math.min(jerkRps3, 8));
        break;
      default:
        break;
    }
  }

  @Override
  public void setProfiledPosition(double rotations) {
    switch (controllerType) {
      case TALON_FX:
        talonFx.setControl(talonMotionMagic.withPosition(rotations));
        break;
      case TALON_FXS:
        talonFxs.setControl(talonMotionMagic.withPosition(rotations));
        break;
      case SPARK_MAX:
      case SPARK_FLEX:
        sparkClosedLoop.setSetpoint(rotations,
            SparkBase.ControlType.kMAXMotionPositionControl, ClosedLoopSlot.kSlot1);
        break;
      case TALON_SRX:
        talonSrx.selectProfileSlot(1, 0);
        talonSrx.set(ControlMode.MotionMagic, rotationsToTalonSrxUnits(rotations));
        break;
      default:
        break;
    }
  }

  @Override
  public boolean isProfileComplete() {
    switch (controllerType) {
      case TALON_FX:
        return Math.abs(talonFx.getClosedLoopError().getValueAsDouble()) < 0.5;
      case TALON_FXS:
        return Math.abs(talonFxs.getClosedLoopError().getValueAsDouble()) < 0.5;
      case SPARK_MAX:
      case SPARK_FLEX:
        return Math.abs(sparkEncoder.getPosition() - talonPosition.Position) < 0.5;
      case TALON_SRX:
        return Math.abs(talonSrx.getClosedLoopError()) < rotationsToTalonSrxUnits(0.5);
      default:
        return true;
    }
  }

  @Override
  public void close() {
    // Properly dispose of hardware resources
    if (spark != null) {
      spark.close();
      spark = null;
      sparkClosedLoop = null;
      sparkEncoder = null;
    }
    if (talonFx != null) {
      talonFx.close();
      talonFx = null;
    }
    if (talonFxs != null) {
      talonFxs.close();
      talonFxs = null;
    }
    if (talonSrx != null) {
      talonSrx.close();
      talonSrx = null;
    }
  }

  private double rpsToTalonSrxUnits(double rps) {
    return (rps * gearRatio) * (TALON_SRX_CPR / 10.0);
  }

  private double rotationsToTalonSrxUnits(double rotations) {
    return rotations * gearRatio * TALON_SRX_CPR;
  }

  private static GravityTypeValue gravityTypeFromMechanism(MechanismType type) {
    switch (type) {
      case ARM:
        return GravityTypeValue.Arm_Cosine;
      case ELEVATOR:
        return GravityTypeValue.Elevator_Static;
      default:
        return GravityTypeValue.Elevator_Static;
    }
  }

  private void applySparkIdleMode(boolean brake) {
    SparkBaseConfig config = controllerType == ControllerType.SPARK_FLEX
        ? new SparkFlexConfig()
        : new SparkMaxConfig();
    config.idleMode(brake ? IdleMode.kBrake : IdleMode.kCoast);
    spark.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
  }
}