package frc.robot.drive;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
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
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;
import com.revrobotics.spark.config.SparkMaxConfig;
import frc.robot.Constants.DriveConstants.MotorControllerType;
import frc.robot.Constants.DriveConstants.MotorKind;

public class CanMotorWrapper implements UniversalMotor {
  private static final int TALON_SRX_CPR = 4096;

  private final MotorControllerType controllerType;
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

  public CanMotorWrapper(MotorConfig config) {
    controllerType = config.controllerType;
    motorKind = config.motorKind;
    gearRatio = config.gearRatio;
    useSensor = config.useSensor;

    switch (controllerType) {
      case SPARK_MAX:
        spark = new SparkMax(config.id, sparkMotorType());
        configureSpark(new SparkMaxConfig(), config);
        break;
      case SPARK_FLEX:
        spark = new SparkFlex(config.id, sparkMotorType());
        configureSpark(new SparkFlexConfig(), config);
        break;
      case TALON_FX:
        talonFx = new TalonFX(config.id);
        configureTalonFx(config);
        break;
      case TALON_FXS:
        talonFxs = new TalonFXS(config.id);
        configureTalonFxs(config);
        break;
      case TALON_SRX:
        talonSrx = new WPI_TalonSRX(config.id);
        talonSrx.setInverted(config.inverted);
        talonSrx.setNeutralMode(NeutralMode.Brake);
        break;
      default:
        throw new IllegalArgumentException("Unsupported CAN motor type: " + controllerType);
    }
  }

  private SparkBase.MotorType sparkMotorType() {
    return motorKind == MotorKind.BRUSHED
        ? SparkBase.MotorType.kBrushed
        : SparkBase.MotorType.kBrushless;
  }

  private void configureSpark(SparkBaseConfig baseConfig, MotorConfig config) {
    spark.setInverted(config.inverted);
    sparkClosedLoop = spark.getClosedLoopController();
    sparkEncoder = spark.getEncoder();

    baseConfig.idleMode(IdleMode.kBrake);
    baseConfig.encoder.positionConversionFactor(1.0 / gearRatio);
    baseConfig.encoder.velocityConversionFactor(1.0 / gearRatio / 60.0);
    spark.configure(baseConfig, ResetMode.kResetSafeParameters, PersistMode.kNoPersistParameters);
  }

  private void configureTalonFx(MotorConfig config) {
    TalonFXConfiguration fxConfig = new TalonFXConfiguration();
    fxConfig.MotorOutput.Inverted =
        config.inverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
    fxConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    fxConfig.Feedback.SensorToMechanismRatio = gearRatio;
    talonFx.getConfigurator().apply(fxConfig);
  }

  private void configureTalonFxs(MotorConfig config) {
    TalonFXSConfiguration fxsConfig = new TalonFXSConfiguration();
    fxsConfig.MotorOutput.Inverted =
        config.inverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
    fxsConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    fxsConfig.ExternalFeedback.SensorToMechanismRatio = gearRatio;
    talonFxs.getConfigurator().apply(fxsConfig);
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
    if (!useSensor) {
      setVoltage(12.0 * Math.signum(rps));
      return;
    }
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        sparkClosedLoop.setSetpoint(rps, SparkBase.ControlType.kVelocity);
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
    if (!useSensor) {
      return;
    }
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        sparkClosedLoop.setSetpoint(rotations, SparkBase.ControlType.kPosition);
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
    if (!useSensor) {
      return 0.0;
    }
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
    if (!useSensor) {
      return 0.0;
    }
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

  private void applySparkIdleMode(boolean brake) {
    SparkBaseConfig config = controllerType == MotorControllerType.SPARK_FLEX
        ? new SparkFlexConfig()
        : new SparkMaxConfig();
    config.idleMode(brake ? IdleMode.kBrake : IdleMode.kCoast);
    spark.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
  }

  private double rpsToTalonSrxUnits(double rps) {
    return (rps * gearRatio) * (TALON_SRX_CPR / 10.0);
  }

  private double rotationsToTalonSrxUnits(double rotations) {
    return rotations * gearRatio * TALON_SRX_CPR;
  }
}
