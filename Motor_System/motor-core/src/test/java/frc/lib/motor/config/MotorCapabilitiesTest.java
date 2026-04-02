package frc.lib.motor.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MotorCapabilitiesTest {
  @Test
  void supportedCanIntegratedPathExposesClosedLoopAndTelemetry() {
    MotorConfiguration config =
        MotorConfiguration.builder(
                ControllerFamily.SPARK_MAX,
                TransportType.CAN,
                MotorKind.NEO)
            .feedbackSource(FeedbackSource.INTEGRATED)
            .build();

    MotorCapabilities capabilities = config.capabilities;

    assertTrue(capabilities.runtimeSupported);
    assertTrue(capabilities.hasPositionTelemetry);
    assertTrue(capabilities.hasVelocityTelemetry);
    assertTrue(capabilities.hasCurrentTelemetry);
    assertTrue(capabilities.hasVoltageTelemetry);
    assertTrue(capabilities.hasTemperatureTelemetry);
    assertTrue(capabilities.hasBatteryVoltageTelemetry);
    assertFalse(capabilities.hasPerChannelCurrentTelemetry);
    assertFalse(capabilities.supportsPowerChannelDetection);
    assertTrue(capabilities.supportsVelocityClosedLoop);
    assertTrue(capabilities.supportsPositionClosedLoop);
    assertTrue(capabilities.supportsMotionProfile);
    assertTrue(capabilities.supportsSysId);
    assertTrue(capabilities.supportsAutomatedTesting);
    assertFalse(capabilities.usesSoftwareClosedLoop);
  }

  @Test
  void limitedVictorSpxCanPathRequiresExternalFeedbackForClosedLoopFeatures() {
    MotorConfiguration config =
        MotorConfiguration.builder(
                ControllerFamily.VICTOR_SPX,
                TransportType.CAN,
                MotorKind.CIM)
            .feedbackSource(FeedbackSource.INTEGRATED)
            .build();

    MotorCapabilities capabilities = config.capabilities;

    assertTrue(capabilities.runtimeSupported);
    assertTrue(capabilities.runtimeLimited);
    assertEquals(SupportLevel.LIMITED, capabilities.runtimeSupportLevel);
    assertFalse(capabilities.hasPositionTelemetry);
    assertFalse(capabilities.hasVelocityTelemetry);
    assertFalse(capabilities.hasCurrentTelemetry);
    assertTrue(capabilities.hasVoltageTelemetry);
    assertFalse(capabilities.hasTemperatureTelemetry);
    assertTrue(capabilities.hasBatteryVoltageTelemetry);
    assertFalse(capabilities.hasPerChannelCurrentTelemetry);
    assertFalse(capabilities.supportsPowerChannelDetection);
    assertFalse(capabilities.supportsVelocityClosedLoop);
    assertFalse(capabilities.supportsPositionClosedLoop);
    assertFalse(capabilities.supportsMotionProfile);
    assertFalse(capabilities.supportsSysId);
    assertFalse(capabilities.supportsAutomatedTesting);
    assertFalse(capabilities.usesSoftwareClosedLoop);
  }

  @Test
  void pwmWithoutFeedbackStaysOpenLoop() {
    MotorConfiguration config =
        MotorConfiguration.builder(
                ControllerFamily.GENERIC_PWM,
                TransportType.PWM,
                MotorKind.CIM)
            .feedbackSource(FeedbackSource.NONE)
            .build();

    MotorCapabilities capabilities = config.capabilities;

    assertTrue(capabilities.runtimeSupported);
    assertFalse(capabilities.runtimeLimited);
    assertFalse(capabilities.hasPositionTelemetry);
    assertFalse(capabilities.hasVelocityTelemetry);
    assertFalse(capabilities.hasCurrentTelemetry);
    assertTrue(capabilities.hasVoltageTelemetry);
    assertTrue(capabilities.hasBatteryVoltageTelemetry);
    assertFalse(capabilities.hasPerChannelCurrentTelemetry);
    assertFalse(capabilities.supportsPowerChannelDetection);
    assertFalse(capabilities.supportsVelocityClosedLoop);
    assertFalse(capabilities.supportsPositionClosedLoop);
    assertFalse(capabilities.supportsSysId);
    assertFalse(capabilities.supportsAutomatedTesting);
    assertFalse(capabilities.usesSoftwareClosedLoop);
  }

  @Test
  void pwmWithExternalFeedbackUsesSoftwareClosedLoop() {
    MotorConfiguration config =
        MotorConfiguration.builder(
                ControllerFamily.TALON_FX,
                TransportType.PWM,
                MotorKind.FALCON)
            .feedbackSource(FeedbackSource.QUADRATURE_DIO)
            .quadratureChannels(0, 1)
            .build();

    MotorCapabilities capabilities = config.capabilities;

    assertTrue(capabilities.runtimeSupported);
    assertTrue(capabilities.hasPositionTelemetry);
    assertTrue(capabilities.hasVelocityTelemetry);
    assertTrue(capabilities.hasBatteryVoltageTelemetry);
    assertFalse(capabilities.hasPerChannelCurrentTelemetry);
    assertFalse(capabilities.supportsPowerChannelDetection);
    assertTrue(capabilities.supportsVelocityClosedLoop);
    assertTrue(capabilities.supportsPositionClosedLoop);
    assertTrue(capabilities.supportsSysId);
    assertTrue(capabilities.supportsAutomatedTesting);
    assertTrue(capabilities.usesSoftwareClosedLoop);
  }

  @Test
  void victorSpxCanWithExternalFeedbackUsesSoftwareClosedLoop() {
    MotorConfiguration config =
        MotorConfiguration.builder(
                ControllerFamily.VICTOR_SPX,
                TransportType.CAN,
                MotorKind.CIM)
            .feedbackSource(FeedbackSource.QUADRATURE_DIO)
            .quadratureChannels(0, 1)
            .build();

    MotorCapabilities capabilities = config.capabilities;

    assertTrue(capabilities.runtimeSupported);
    assertTrue(capabilities.runtimeLimited);
    assertTrue(capabilities.hasPositionTelemetry);
    assertTrue(capabilities.hasVelocityTelemetry);
    assertTrue(capabilities.hasBatteryVoltageTelemetry);
    assertFalse(capabilities.hasPerChannelCurrentTelemetry);
    assertFalse(capabilities.supportsPowerChannelDetection);
    assertTrue(capabilities.supportsVelocityClosedLoop);
    assertTrue(capabilities.supportsPositionClosedLoop);
    assertTrue(capabilities.supportsSysId);
    assertTrue(capabilities.usesSoftwareClosedLoop);
  }

  @Test
  void configuredPowerModuleEnablesPerChannelDetectionCapability() {
    MotorConfiguration config =
        MotorConfiguration.builder(
                ControllerFamily.GENERIC_PWM,
                TransportType.PWM,
                MotorKind.CIM)
            .feedbackSource(FeedbackSource.NONE)
            .powerModule(PowerModuleType.REV_PDH, 1)
            .powerChannel(5)
            .build();

    MotorCapabilities capabilities = config.capabilities;

    assertTrue(capabilities.hasBatteryVoltageTelemetry);
    assertTrue(capabilities.hasPerChannelCurrentTelemetry);
    assertTrue(capabilities.supportsPowerChannelDetection);
    assertTrue(capabilities.hasCurrentTelemetry);
  }
}
