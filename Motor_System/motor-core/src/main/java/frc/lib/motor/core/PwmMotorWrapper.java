package frc.lib.motor.core;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.motorcontrol.Koors40;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkFlex;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonFX;
import edu.wpi.first.wpilibj.motorcontrol.PWMTalonSRX;
import edu.wpi.first.wpilibj.motorcontrol.PWMVenom;
import edu.wpi.first.wpilibj.motorcontrol.PWMVictorSPX;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.motorcontrol.Talon;
import edu.wpi.first.wpilibj.motorcontrol.VictorSP;
import frc.lib.motor.config.FeedbackSource;
import frc.lib.motor.config.MotorConfiguration;
import frc.lib.motor.config.PowerModuleType;
import frc.lib.motor.feedback.FeedbackSensor;
import frc.lib.motor.feedback.FeedbackSensorFactory;
import frc.lib.motor.core.UniversalMotor.Mode;

public class PwmMotorWrapper implements UniversalMotor {
  private final MotorConfiguration m_config;
  private final MotorController m_motor;
  private final FeedbackSensor m_feedbackSensor;
  private final PIDController m_velocityController;
  private final PIDController m_positionController;
  private Mode m_controlMode = Mode.DUTY_CYCLE;
  private double m_healthScore = 100.0;
  private double m_targetVelocityRps = 0.0;
  private double m_targetPositionRotations = 0.0;
  private double m_lastAppliedVoltage = 0.0;
  private double m_kP = 0.0;
  private double m_kI = 0.0;
  private double m_kD = 0.0;
  private double m_kV = 0.0;
  private double m_kS = 0.0;
  private double m_kA = 0.0;
  private double m_kG = 0.0;
  private double m_kPPos = 0.0;
  private double m_kDPos = 0.0;

  public PwmMotorWrapper(MotorConfiguration config) {
    m_config = config;
    m_motor = createMotorController(config);
    m_motor.setInverted(config.inverted);
    m_feedbackSensor = FeedbackSensorFactory.create(config);

    m_velocityController = new PIDController(config.kP, config.kI, config.kD);
    m_positionController = new PIDController(config.kP_pos, 0.0, config.kD_pos);

    m_kP = config.kP;
    m_kI = config.kI;
    m_kD = config.kD;
    m_kV = config.kV;
    m_kS = config.kS;
    m_kA = config.kA;
    m_kG = config.kG;
    m_kPPos = config.kP_pos;
    m_kDPos = config.kD_pos;
  }

  private static MotorController createMotorController(MotorConfiguration config) {
    switch (config.controllerFamily) {
      case KOORS_40:
        return new Koors40(config.pwmChannel);
      case SPARK:
        return new Spark(config.pwmChannel);
      case SPARK_MAX:
        return new PWMSparkMax(config.pwmChannel);
      case SPARK_FLEX:
        return new PWMSparkFlex(config.pwmChannel);
      case TALON:
        return new Talon(config.pwmChannel);
      case TALON_FX:
        return new PWMTalonFX(config.pwmChannel);
      case TALON_SRX:
        return new PWMTalonSRX(config.pwmChannel);
      case VENOM:
        return new PWMVenom(config.pwmChannel);
      case VICTOR_SP:
        return new VictorSP(config.pwmChannel);
      case VICTOR_SPX:
      case GENERIC_PWM:
        return new PWMVictorSPX(config.pwmChannel);
      default:
        throw new IllegalArgumentException(
            "Unsupported PWM controller family: " + config.controllerFamily);
    }
  }

  @Override
  public void setControlMode(Mode mode) {
    m_controlMode = mode;
  }

  @Override
  public Mode getControlMode() {
    return m_controlMode;
  }

  @Override
  public void setVoltage(double volts) {
    m_controlMode = Mode.VOLTAGE;
    m_lastAppliedVoltage = volts;
    m_motor.setVoltage(volts);
  }

  @Override
  public void setVelocityRps(double rps) {
    m_controlMode = Mode.VELOCITY;
    m_targetVelocityRps = rps;
  }

  @Override
  public void setPositionRotations(double rotations) {
    m_controlMode = Mode.POSITION;
    m_targetPositionRotations = rotations;
  }

  @Override
  public double getVelocityRps() {
    return m_feedbackSensor != null ? m_feedbackSensor.getVelocityRps() : 0.0;
  }

  @Override
  public double getPositionRotations() {
    return m_feedbackSensor != null ? m_feedbackSensor.getPositionRotations() : 0.0;
  }

  @Override
  public void setBrake(boolean brake) {}

  @Override
  public void stop() {
    m_motor.stopMotor();
    m_lastAppliedVoltage = 0.0;
    m_velocityController.reset();
    m_positionController.reset();
  }

  @Override
  public void close() {
    if (m_feedbackSensor != null) {
      m_feedbackSensor.close();
    }
  }

  @Override
  public void set(double value) {
    switch (m_controlMode) {
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
        m_lastAppliedVoltage = value * RobotController.getBatteryVoltage();
        m_motor.set(value);
        break;
    }
  }

  @Override
  public double getCurrent() {
    if (m_config.powerChannel < 0 || m_config.powerModuleType == PowerModuleType.NONE) {
      return 0.0;
    }

    var pd = PowerDistributionRegistry.get(m_config.powerModuleType, m_config.powerModuleId);
    return pd != null ? pd.getCurrent(m_config.powerChannel) : 0.0;
  }

  @Override
  public double getAppliedVoltage() {
    return m_lastAppliedVoltage;
  }

  @Override
  public double getTemperature() {
    return 0.0;
  }

  @Override
  public boolean isFeedbackConnected() {
    return m_config.feedbackSource != FeedbackSource.NONE
        && m_feedbackSensor != null
        && m_feedbackSensor.isConnected();
  }

  @Override
  public double getHealthScore() {
    return m_healthScore;
  }

  @Override
  public void setHealthScore(double score) {
    m_healthScore = score;
  }

  @Override
  public String getDeviceName() {
    return "PWM-" + m_config.pwmChannel;
  }

  @Override
  public boolean isServo() {
    return false;
  }

  @Override
  public void updatePidConfig(double kP, double kI, double kD, double kV, double kS,
                              double kA, double kG) {
    m_kP = kP;
    m_kI = kI;
    m_kD = kD;
    m_kV = kV;
    m_kS = kS;
    m_kA = kA;
    m_kG = kG;
    m_velocityController.setPID(kP, kI, kD);
  }

  @Override
  public void updatePositionPid(double kP, double kD, double kV, double kS,
                                double kA, double kG) {
    m_kPPos = kP;
    m_kDPos = kD;
    m_kV = kV;
    m_kS = kS;
    m_kA = kA;
    m_kG = kG;
    m_positionController.setPID(kP, 0.0, kD);
  }

  @Override
  public void periodic() {
    if (m_feedbackSensor == null) {
      return;
    }

    switch (m_controlMode) {
      case VELOCITY:
        applySoftwareVelocity();
        break;
      case POSITION:
        applySoftwarePosition();
        break;
      default:
        break;
    }
  }

  private void applySoftwareVelocity() {
    double measuredRps = getVelocityRps();
    double pidVolts = m_velocityController.calculate(measuredRps, m_targetVelocityRps);
    double ffVolts = Math.signum(m_targetVelocityRps) * m_kS + m_targetVelocityRps * m_kV
        + gravityFeedforward();
    double volts = MathUtil.clamp(pidVolts + ffVolts, -12.0, 12.0);
    m_lastAppliedVoltage = volts;
    m_motor.setVoltage(volts);
  }

  private void applySoftwarePosition() {
    double measuredRot = getPositionRotations();
    double pidVolts = m_positionController.calculate(measuredRot, m_targetPositionRotations);
    double volts = MathUtil.clamp(pidVolts + gravityFeedforward(), -12.0, 12.0);
    m_lastAppliedVoltage = volts;
    m_motor.setVoltage(volts);
  }

  private double gravityFeedforward() {
    switch (m_config.mechanismType) {
      case ARM:
        return m_kG * Math.cos(getPositionRotations() * 2.0 * Math.PI);
      case ELEVATOR:
        return m_kG;
      default:
        return 0.0;
    }
  }
}
