package frc.lib.motor.config;

/**
 * Capability summary derived from controller family, transport, feedback source,
 * and optional power-distribution telemetry.
 */
public final class MotorCapabilities {
  public final SupportLevel runtimeSupportLevel;
  public final String runtimeSupportNote;
  public final boolean runtimeSupported;
  public final boolean runtimeLimited;
  public final boolean hasPositionTelemetry;
  public final boolean hasVelocityTelemetry;
  public final boolean hasCurrentTelemetry;
  public final boolean hasVoltageTelemetry;
  public final boolean hasTemperatureTelemetry;
  public final boolean hasBatteryVoltageTelemetry;
  public final boolean hasPerChannelCurrentTelemetry;
  public final boolean supportsPowerChannelDetection;
  public final boolean supportsVelocityClosedLoop;
  public final boolean supportsPositionClosedLoop;
  public final boolean supportsMotionProfile;
  public final boolean supportsSysId;
  public final boolean supportsAutomatedTesting;
  public final boolean usesSoftwareClosedLoop;

  private MotorCapabilities(
      SupportLevel runtimeSupportLevel,
      String runtimeSupportNote,
      boolean runtimeSupported,
      boolean runtimeLimited,
      boolean hasPositionTelemetry,
      boolean hasVelocityTelemetry,
      boolean hasCurrentTelemetry,
      boolean hasVoltageTelemetry,
      boolean hasTemperatureTelemetry,
      boolean hasBatteryVoltageTelemetry,
      boolean hasPerChannelCurrentTelemetry,
      boolean supportsPowerChannelDetection,
      boolean supportsVelocityClosedLoop,
      boolean supportsPositionClosedLoop,
      boolean supportsMotionProfile,
      boolean supportsSysId,
      boolean supportsAutomatedTesting,
      boolean usesSoftwareClosedLoop) {
    this.runtimeSupportLevel = runtimeSupportLevel;
    this.runtimeSupportNote = runtimeSupportNote;
    this.runtimeSupported = runtimeSupported;
    this.runtimeLimited = runtimeLimited;
    this.hasPositionTelemetry = hasPositionTelemetry;
    this.hasVelocityTelemetry = hasVelocityTelemetry;
    this.hasCurrentTelemetry = hasCurrentTelemetry;
    this.hasVoltageTelemetry = hasVoltageTelemetry;
    this.hasTemperatureTelemetry = hasTemperatureTelemetry;
    this.hasBatteryVoltageTelemetry = hasBatteryVoltageTelemetry;
    this.hasPerChannelCurrentTelemetry = hasPerChannelCurrentTelemetry;
    this.supportsPowerChannelDetection = supportsPowerChannelDetection;
    this.supportsVelocityClosedLoop = supportsVelocityClosedLoop;
    this.supportsPositionClosedLoop = supportsPositionClosedLoop;
    this.supportsMotionProfile = supportsMotionProfile;
    this.supportsSysId = supportsSysId;
    this.supportsAutomatedTesting = supportsAutomatedTesting;
    this.usesSoftwareClosedLoop = usesSoftwareClosedLoop;
  }

  public static MotorCapabilities resolve(MotorConfiguration config) {
    SupportLevel supportLevel = config.controllerFamily.supportLevel(config.transport);
    String supportNote = config.controllerFamily.supportNote(config.transport);
    boolean runtimeSupported = supportLevel.hasRuntimeSupport();
    if (!runtimeSupported) {
      return new MotorCapabilities(
          supportLevel,
          supportNote,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false,
          false);
    }

    boolean nativeFeedback = hasNativeFeedback(config.controllerFamily, config.transport);
    boolean integratedFeedback = config.feedbackSource == FeedbackSource.INTEGRATED && nativeFeedback;
    boolean externalFeedback =
        config.feedbackSource != FeedbackSource.NONE && config.feedbackSource != FeedbackSource.INTEGRATED;

    boolean hasPosition = integratedFeedback || externalFeedback;
    boolean hasVelocity = integratedFeedback || externalFeedback;

    boolean nativeCurrentTelemetry = hasNativeCurrentTelemetry(config.controllerFamily, config.transport);
    boolean nativeVoltageTelemetry = hasNativeVoltageTelemetry(config.controllerFamily, config.transport);
    boolean nativeTemperatureTelemetry =
        hasNativeTemperatureTelemetry(config.controllerFamily, config.transport);
    boolean pdCurrent = config.powerChannel >= 0 && config.powerModuleType != PowerModuleType.NONE;
    boolean batteryVoltageTelemetry = config.powerModuleType.hasBatteryVoltageTelemetry();
    boolean perChannelCurrentTelemetry = config.powerModuleType.hasPerChannelCurrentTelemetry();

    boolean current = nativeCurrentTelemetry || pdCurrent;
    boolean voltage = nativeVoltageTelemetry || config.transport == TransportType.PWM;
    boolean temperature = nativeTemperatureTelemetry;

    boolean softwareClosedLoop = externalFeedback
        || (config.transport == TransportType.PWM && config.feedbackSource != FeedbackSource.NONE);

    boolean velocityClosedLoop = integratedFeedback || softwareClosedLoop;
    boolean positionClosedLoop = integratedFeedback || softwareClosedLoop;

    boolean motionProfile = integratedFeedback && supportsNativeMotionProfile(
        config.controllerFamily, config.transport);

    boolean sysId = hasPosition || hasVelocity;
    boolean automatedTesting = hasVelocity || current || temperature;

    return new MotorCapabilities(
        supportLevel,
        supportNote,
        runtimeSupported,
        supportLevel == SupportLevel.LIMITED,
        hasPosition,
        hasVelocity,
        current,
        voltage,
        temperature,
        batteryVoltageTelemetry,
        perChannelCurrentTelemetry,
        perChannelCurrentTelemetry,
        velocityClosedLoop,
        positionClosedLoop,
        motionProfile,
        sysId,
        automatedTesting,
        softwareClosedLoop);
  }

  private static boolean hasNativeFeedback(ControllerFamily family, TransportType transport) {
    return transport == TransportType.CAN
        && switch (family) {
          case SPARK_MAX, SPARK_FLEX, TALON_FX, TALON_FXS, TALON_SRX -> true;
          default -> false;
        };
  }

  private static boolean hasNativeCurrentTelemetry(ControllerFamily family, TransportType transport) {
    if (transport != TransportType.CAN) {
      return false;
    }
    return switch (family) {
      case SPARK_MAX, SPARK_FLEX, TALON_FX, TALON_FXS, TALON_SRX -> true;
      default -> false;
    };
  }

  private static boolean hasNativeVoltageTelemetry(ControllerFamily family, TransportType transport) {
    if (transport == TransportType.PWM) {
      return false;
    }
    return switch (family) {
      case GENERIC_PWM, SERVO -> false;
      default -> true;
    };
  }

  private static boolean hasNativeTemperatureTelemetry(ControllerFamily family, TransportType transport) {
    if (transport != TransportType.CAN) {
      return false;
    }
    return switch (family) {
      case SPARK_MAX, SPARK_FLEX, TALON_FX, TALON_FXS, TALON_SRX -> true;
      default -> false;
    };
  }

  private static boolean supportsNativeMotionProfile(ControllerFamily family, TransportType transport) {
    return transport == TransportType.CAN
        && switch (family) {
          case TALON_FX, TALON_FXS, TALON_SRX, SPARK_MAX, SPARK_FLEX -> true;
          default -> false;
        };
  }
}
