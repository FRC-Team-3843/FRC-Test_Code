package frc.robot.motor;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix6.configs.Slot0Configs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXSConfiguration;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.hardware.TalonFXS;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.ClosedLoopSlot;
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
  private final PositionVoltage talonPosition = new PositionVoltage(0.0);

  private Mode controlMode = Mode.DUTY_CYCLE;
  private double healthScore = 100.0;

  public CanMotorWrapper(MotorConfiguration config) {
    this.config = config;
    this.controllerType = config.controllerType;
    this.motorKind = config.motorKind;
    this.gearRatio = config.gearRatio;
    this.useSensor = config.useQuadEncoder; // Using useQuadEncoder as general 'use sensor' flag?

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
        talonSrx.setInverted(config.inverted);
        talonSrx.setNeutralMode(NeutralMode.Brake);
        break;
      default:
        throw new IllegalArgumentException("Unsupported CAN motor type: " + controllerType);
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
    baseConfig.idleMode(IdleMode.kBrake);
    baseConfig.encoder.positionConversionFactor(1.0 / gearRatio);
    baseConfig.encoder.velocityConversionFactor(1.0 / gearRatio / 60.0);
    spark.configure(baseConfig, ResetMode.kResetSafeParameters, PersistMode.kNoPersistParameters);        
  }

  private void configureTalonFx(MotorConfiguration config) {
    TalonFXConfiguration fxConfig = new TalonFXConfiguration();
    fxConfig.MotorOutput.Inverted =
        config.inverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
    fxConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    fxConfig.Feedback.SensorToMechanismRatio = gearRatio;

    // Apply PID configuration to Slot0
    fxConfig.Slot0.kP = config.kP;
    fxConfig.Slot0.kI = config.kI;
    fxConfig.Slot0.kD = config.kD;
    fxConfig.Slot0.kV = config.kV;
    fxConfig.Slot0.kS = config.kS;

    talonFx.getConfigurator().apply(fxConfig);
  }

  private void configureTalonFxs(MotorConfiguration config) {
    TalonFXSConfiguration fxsConfig = new TalonFXSConfiguration();
    fxsConfig.MotorOutput.Inverted =
        config.inverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
    fxsConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    fxsConfig.ExternalFeedback.SensorToMechanismRatio = gearRatio;

    // Apply PID configuration to Slot0
    fxsConfig.Slot0.kP = config.kP;
    fxsConfig.Slot0.kI = config.kI;
    fxsConfig.Slot0.kD = config.kD;
    fxsConfig.Slot0.kV = config.kV;
    fxsConfig.Slot0.kS = config.kS;

    talonFxs.getConfigurator().apply(fxsConfig);
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
        sparkClosedLoop.setSetpoint(rotations, SparkBase.ControlType.kPosition, ClosedLoopSlot.kSlot0);  
        break;
      case TALON_FX:
        talonFx.setControl(talonPosition.withPosition(rotations));
        break;
      case TALON_FXS:
        talonFxs.setControl(talonPosition.withPosition(rotations));
        break;
      case TALON_SRX:
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
    // Hot-reload PID configuration for supported motor controllers
    switch (controllerType) {
      case TALON_FX:
        Slot0Configs slot0 = new Slot0Configs();
        slot0.kP = kP;
        slot0.kI = kI;
        slot0.kD = kD;
        slot0.kV = kV;
        slot0.kS = kS;
        talonFx.getConfigurator().apply(slot0);
        break;
      case TALON_FXS:
        Slot0Configs slot0Fxs = new Slot0Configs();
        slot0Fxs.kP = kP;
        slot0Fxs.kI = kI;
        slot0Fxs.kD = kD;
        slot0Fxs.kV = kV;
        slot0Fxs.kS = kS;
        talonFxs.getConfigurator().apply(slot0Fxs);
        break;
      default:
        // Other controller types not supported for hot-reload yet
        break;
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

  private void applySparkIdleMode(boolean brake) {
    SparkBaseConfig config = controllerType == ControllerType.SPARK_FLEX
        ? new SparkFlexConfig()
        : new SparkMaxConfig();
    config.idleMode(brake ? IdleMode.kBrake : IdleMode.kCoast);
    spark.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
  }
}