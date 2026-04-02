package frc.robot.motor.config;

/**
 * Capability summary derived from controller family, transport, feedback source,
 * and optional power-distribution telemetry.
 */
public final class MotorCapabilities {
  public final boolean hasPositionTelemetry;
  public final boolean hasVelocityTelemetry;
  public final boolean hasCurrentTelemetry;
  public final boolean hasVoltageTelemetry;
  public final boolean hasTemperatureTelemetry;
  public final boolean supportsVelocityClosedLoop;
  public final boolean supportsPositionClosedLoop;
  public final boolean supportsMotionProfile;
  public final boolean supportsSysId;
  public final boolean supportsAutomatedTesting;
  public final boolean usesSoftwareClosedLoop;

  private MotorCapabilities(
      boolean hasPositionTelemetry,
      boolean hasVelocityTelemetry,
      boolean hasCurrentTelemetry,
      boolean hasVoltageTelemetry,
      boolean hasTemperatureTelemetry,
      boolean supportsVelocityClosedLoop,
      boolean supportsPositionClosedLoop,
      boolean supportsMotionProfile,
      boolean supportsSysId,
      boolean supportsAutomatedTesting,
      boolean usesSoftwareClosedLoop) {
    this.hasPositionTelemetry = hasPositionTelemetry;
    this.hasVelocityTelemetry = hasVelocityTelemetry;
    this.hasCurrentTelemetry = hasCurrentTelemetry;
    this.hasVoltageTelemetry = hasVoltageTelemetry;
    this.hasTemperatureTelemetry = hasTemperatureTelemetry;
    this.supportsVelocityClosedLoop = supportsVelocityClosedLoop;
    this.supportsPositionClosedLoop = supportsPositionClosedLoop;
    this.supportsMotionProfile = supportsMotionProfile;
    this.supportsSysId = supportsSysId;
    this.supportsAutomatedTesting = supportsAutomatedTesting;
    this.usesSoftwareClosedLoop = usesSoftwareClosedLoop;
  }

  public static MotorCapabilities resolve(MotorConfiguration config) {
    boolean integratedFeedback =
        config.feedbackSource == FeedbackSource.INTEGRATED && config.transport == TransportType.CAN;
    boolean externalFeedback =
        config.feedbackSource != FeedbackSource.NONE && config.feedbackSource != FeedbackSource.INTEGRATED;

    boolean hasPosition = integratedFeedback || externalFeedback;
    boolean hasVelocity = integratedFeedback || externalFeedback;

    boolean canTelemetry = config.transport == TransportType.CAN
        && config.controllerFamily != ControllerFamily.GENERIC_PWM
        && config.controllerFamily != ControllerFamily.SERVO;
    boolean pdCurrent = config.powerChannel >= 0 && config.powerModuleType != PowerModuleType.NONE;

    boolean current = canTelemetry || pdCurrent;
    boolean voltage = true;
    boolean temperature = canTelemetry;

    boolean softwareClosedLoop = externalFeedback
        || (config.transport == TransportType.PWM && config.feedbackSource != FeedbackSource.NONE);

    boolean velocityClosedLoop = integratedFeedback || softwareClosedLoop;
    boolean positionClosedLoop = integratedFeedback || softwareClosedLoop;

    boolean motionProfile = integratedFeedback && config.transport == TransportType.CAN
        && (config.controllerFamily == ControllerFamily.TALON_FX
            || config.controllerFamily == ControllerFamily.TALON_FXS
            || config.controllerFamily == ControllerFamily.TALON_SRX
            || config.controllerFamily == ControllerFamily.SPARK_MAX
            || config.controllerFamily == ControllerFamily.SPARK_FLEX);

    boolean sysId = hasPosition || hasVelocity;
    boolean automatedTesting = hasVelocity || current || temperature;

    return new MotorCapabilities(
        hasPosition,
        hasVelocity,
        current,
        voltage,
        temperature,
        velocityClosedLoop,
        positionClosedLoop,
        motionProfile,
        sysId,
        automatedTesting,
        softwareClosedLoop);
  }
}
