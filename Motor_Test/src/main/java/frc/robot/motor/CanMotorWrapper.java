package frc.robot.motor;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.TalonSRXFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix6.configs.MotionMagicConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.configs.TalonFXSConfiguration;
import com.ctre.phoenix6.controls.DutyCycleOut;
import com.ctre.phoenix6.controls.MotionMagicVoltage;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.hardware.TalonFXS;
import com.ctre.phoenix6.signals.InvertedValue;
import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkBase;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.ClosedLoopConfig;
import com.revrobotics.spark.config.MAXMotionConfig;
import com.revrobotics.spark.config.SparkBaseConfig;
import com.revrobotics.spark.config.SparkFlexConfig;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj.DriverStation;

public class CanMotorWrapper implements UniversalMotor {
  private final ControllerType controllerType;
  private final MotorKind motorKind;
  private final double gearRatio;
  private final int quadCpr;

  private SparkBase spark;
  private SparkClosedLoopController sparkClosedLoop;
  private RelativeEncoder sparkEncoder;

  private TalonFX talonFx;
  private TalonFXS talonFxs;
  private WPI_TalonSRX talonSrx;

  private final DutyCycleOut talonFxDuty = new DutyCycleOut(0.0);
  private final VoltageOut talonFxVoltage = new VoltageOut(0.0);
  private final VelocityVoltage talonFxVelocity = new VelocityVoltage(0.0);
  private final PositionVoltage talonFxPosition = new PositionVoltage(0.0);
  private final MotionMagicVoltage talonFxMotionMagic = new MotionMagicVoltage(0.0);
  private boolean warnedCurrentNoPro = false;

  private Mode controlMode = Mode.DUTY_CYCLE;
  private double healthScore = 0.0;

  public CanMotorWrapper(MotorConfiguration config) {
    controllerType = config.controllerType;
    motorKind = config.motorKind;
    gearRatio = config.gearRatio <= 0.0 ? 1.0 : config.gearRatio;
    quadCpr = config.quadCpr > 0 ? config.quadCpr : 4096;

    switch (controllerType) {
      case SPARK_MAX:
        spark = new SparkMax(config.canId, sparkMotorType(config.motorKind));
        configureSpark(config, new SparkMaxConfig());
        break;
      case SPARK_FLEX:
        spark = new SparkFlex(config.canId, sparkMotorType(config.motorKind));
        configureSpark(config, new SparkFlexConfig());
        break;
      case TALON_FX:
        talonFx = new TalonFX(config.canId);
        configureTalonFx(config);
        break;
      case TALON_FXS:
        talonFxs = new TalonFXS(config.canId);
        configureTalonFx(config);
        break;
      case TALON_SRX:
        talonSrx = new WPI_TalonSRX(config.canId);
        talonSrx.setInverted(config.inverted);
        if (config.useQuadEncoder) {
          talonSrx.configSelectedFeedbackSensor(TalonSRXFeedbackDevice.QuadEncoder, 0, 100);
        }
        break;
      default:
        throw new IllegalArgumentException("Controller type not supported for CAN wrapper");
    }
  }

  private SparkBase.MotorType sparkMotorType(MotorKind kind) {
    return kind.isBrushed() ? SparkBase.MotorType.kBrushed : SparkBase.MotorType.kBrushless;
  }

  private void configureSpark(MotorConfiguration config, SparkBaseConfig sparkConfig) {
    spark.setInverted(config.inverted);
    sparkClosedLoop = spark.getClosedLoopController();
    sparkEncoder = spark.getEncoder();

    sparkConfig.encoder.positionConversionFactor(1.0 / gearRatio);
    sparkConfig.encoder.velocityConversionFactor(1.0 / gearRatio / 60.0);

    ClosedLoopConfig closedLoop = sparkConfig.closedLoop;
    closedLoop.maxMotion.apply(
        new MAXMotionConfig()
            .positionMode(MAXMotionConfig.MAXMotionPositionMode.kMAXMotionTrapezoidal)
            .cruiseVelocity(20.0)
            .maxAcceleration(40.0));

    try {
      spark.configure(sparkConfig, ResetMode.kResetSafeParameters, PersistMode.kNoPersistParameters);
    } catch (IllegalStateException ex) {
      DriverStation.reportError("Spark configuration failed: " + ex.getMessage(), ex.getStackTrace());
    }
  }

  private void configureTalonFx(MotorConfiguration config) {
    if (talonFx != null) {
      TalonFXConfiguration fxConfig = new TalonFXConfiguration();
      fxConfig.MotorOutput.Inverted =
          config.inverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
      fxConfig.Feedback.SensorToMechanismRatio = gearRatio;
      MotionMagicConfigs motionMagic = fxConfig.MotionMagic;
      motionMagic.MotionMagicCruiseVelocity = 20.0;
      motionMagic.MotionMagicAcceleration = 40.0;
      talonFx.getConfigurator().apply(fxConfig);
    }
    if (talonFxs != null) {
      TalonFXSConfiguration fxsConfig = new TalonFXSConfiguration();
      fxsConfig.MotorOutput.Inverted =
          config.inverted ? InvertedValue.Clockwise_Positive : InvertedValue.CounterClockwise_Positive;
      fxsConfig.ExternalFeedback.SensorToMechanismRatio = gearRatio;
      MotionMagicConfigs motionMagic = fxsConfig.MotionMagic;
      motionMagic.MotionMagicCruiseVelocity = 20.0;
      motionMagic.MotionMagicAcceleration = 40.0;
      talonFxs.getConfigurator().apply(fxsConfig);
    }
  }

  @Override
  public void setControlMode(Mode mode) {
    controlMode = mode;
  }

  @Override
  public Mode getControlMode() {
    return controlMode;
  }

  @Override
  public void set(double value) {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        setSpark(value);
        break;
      case TALON_FX:
        setTalonFx(value, talonFx);
        break;
      case TALON_FXS:
        setTalonFx(value, talonFxs);
        break;
      case TALON_SRX:
        setTalonSrx(value);
        break;
      default:
        break;
    }
  }

  private void setSpark(double value) {
    if (spark == null) {
      return;
    }
    switch (controlMode) {
      case DUTY_CYCLE:
        spark.set(value);
        break;
      case VOLTAGE:
        spark.setVoltage(value);
        break;
      case VELOCITY:
        if (sparkClosedLoop != null) {
          sparkClosedLoop.setSetpoint(value, SparkBase.ControlType.kVelocity);
        }
        break;
      case POSITION:
        if (sparkClosedLoop != null) {
          sparkClosedLoop.setSetpoint(value, SparkBase.ControlType.kPosition);
        }
        break;
      case CURRENT:
        if (sparkClosedLoop != null) {
          sparkClosedLoop.setSetpoint(value, SparkBase.ControlType.kCurrent);
        }
        break;
      case SMART_MOTION:
        if (sparkClosedLoop != null) {
          sparkClosedLoop.setSetpoint(value, SparkBase.ControlType.kMAXMotionPositionControl);
        }
        break;
      default:
        break;
    }
  }

  private void setTalonFx(double value, TalonFX talon) {
    if (talon == null) {
      return;
    }
    switch (controlMode) {
      case DUTY_CYCLE:
        talon.setControl(talonFxDuty.withOutput(value));
        break;
      case VOLTAGE:
        talon.setControl(talonFxVoltage.withOutput(value));
        break;
      case VELOCITY:
        talon.setControl(talonFxVelocity.withVelocity(value));
        break;
      case POSITION:
        talon.setControl(talonFxPosition.withPosition(value));
        break;
      case CURRENT:
        warnCurrentNotSupported();
        talon.setControl(talonFxDuty.withOutput(0.0));
        break;
      case SMART_MOTION:
        talon.setControl(talonFxMotionMagic.withPosition(value));
        break;
      default:
        break;
    }
  }

  private void setTalonFx(double value, TalonFXS talon) {
    if (talon == null) {
      return;
    }
    switch (controlMode) {
      case DUTY_CYCLE:
        talon.setControl(talonFxDuty.withOutput(value));
        break;
      case VOLTAGE:
        talon.setControl(talonFxVoltage.withOutput(value));
        break;
      case VELOCITY:
        talon.setControl(talonFxVelocity.withVelocity(value));
        break;
      case POSITION:
        talon.setControl(talonFxPosition.withPosition(value));
        break;
      case CURRENT:
        warnCurrentNotSupported();
        talon.setControl(talonFxDuty.withOutput(0.0));
        break;
      case SMART_MOTION:
        talon.setControl(talonFxMotionMagic.withPosition(value));
        break;
      default:
        break;
    }
  }

  private void setTalonSrx(double value) {
    if (talonSrx == null) {
      return;
    }
    switch (controlMode) {
      case DUTY_CYCLE:
        talonSrx.set(ControlMode.PercentOutput, value);
        break;
      case VOLTAGE:
        talonSrx.setVoltage(value);
        break;
      case VELOCITY:
        talonSrx.set(ControlMode.Velocity, rpsToTalonSrxUnits(value));
        break;
      case POSITION:
        talonSrx.set(ControlMode.Position, rotationsToTalonSrxUnits(value));
        break;
      case CURRENT:
        talonSrx.set(ControlMode.Current, value);
        break;
      case SMART_MOTION:
        talonSrx.set(ControlMode.MotionMagic, rotationsToTalonSrxUnits(value));
        break;
      default:
        break;
    }
  }

  private double rpsToTalonSrxUnits(double rps) {
    return (rps * gearRatio) * (configQuadCpr() / 10.0);
  }

  private double rotationsToTalonSrxUnits(double rotations) {
    return rotations * gearRatio * configQuadCpr();
  }

  private int configQuadCpr() {
    return quadCpr;
  }

  private void warnCurrentNotSupported() {
    if (!warnedCurrentNoPro) {
      DriverStation.reportWarning(
          "Phoenix Pro not available: TalonFX/FXS current control disabled. Use Voltage or Duty Cycle.",
          false);
      warnedCurrentNoPro = true;
    }
  }

  @Override
  public double getVelocity() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        return sparkEncoder != null ? sparkEncoder.getVelocity() : 0.0;
      case TALON_FX:
        return talonFx != null ? talonFx.getVelocity().getValueAsDouble() : 0.0;
      case TALON_FXS:
        return talonFxs != null ? talonFxs.getVelocity().getValueAsDouble() : 0.0;
      case TALON_SRX:
        return talonSrx != null ? talonSrx.getSelectedSensorVelocity() / configQuadCpr() * 10.0 / gearRatio : 0.0;
      default:
        return 0.0;
    }
  }

  @Override
  public double getPosition() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        return sparkEncoder != null ? sparkEncoder.getPosition() : 0.0;
      case TALON_FX:
        return talonFx != null ? talonFx.getPosition().getValueAsDouble() : 0.0;
      case TALON_FXS:
        return talonFxs != null ? talonFxs.getPosition().getValueAsDouble() : 0.0;
      case TALON_SRX:
        return talonSrx != null ? talonSrx.getSelectedSensorPosition() / configQuadCpr() / gearRatio : 0.0;
      default:
        return 0.0;
    }
  }

  @Override
  public double getCurrent() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        return spark != null ? spark.getOutputCurrent() : 0.0;
      case TALON_FX:
        return talonFx != null ? talonFx.getStatorCurrent().getValueAsDouble() : 0.0;
      case TALON_FXS:
        return talonFxs != null ? talonFxs.getStatorCurrent().getValueAsDouble() : 0.0;
      case TALON_SRX:
        return talonSrx != null ? talonSrx.getStatorCurrent() : 0.0;
      default:
        return 0.0;
    }
  }

  @Override
  public double getTemperature() {
    switch (controllerType) {
      case SPARK_MAX:
      case SPARK_FLEX:
        return spark != null ? spark.getMotorTemperature() : 0.0;
      case TALON_FX:
        return talonFx != null ? talonFx.getDeviceTemp().getValueAsDouble() : 0.0;
      case TALON_FXS:
        return talonFxs != null ? talonFxs.getDeviceTemp().getValueAsDouble() : 0.0;
      case TALON_SRX:
        return talonSrx != null ? talonSrx.getTemperature() : 0.0;
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
    healthScore = score;
  }

  @Override
  public String getDeviceName() {
    return controllerType + "-" + motorKind;
  }

  @Override
  public boolean isServo() {
    return motorKind.isServo();
  }
}
