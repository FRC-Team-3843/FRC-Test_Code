package frc.lib.motor.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.lib.motor.core.ControllerType;
import org.junit.jupiter.api.Test;

class MotorSupportModelTest {
  @Test
  void victorSpxCanPathUsesLimitedCanRuntime() {
    MotorConfiguration config =
        MotorConfiguration.builder(ControllerFamily.VICTOR_SPX, TransportType.CAN, MotorKind.CIM)
            .feedbackSource(FeedbackSource.INTEGRATED)
            .build();

    assertEquals(ControllerType.VICTOR_SPX, config.controllerType);
    assertTrue(config.capabilities.runtimeSupported);
    assertTrue(config.capabilities.runtimeLimited);
    assertFalse(config.capabilities.supportsSysId);
    assertFalse(config.capabilities.hasCurrentTelemetry);
  }

  @Test
  void pwmSparkFlexPathUsesGenericPwmRuntimeWithSoftwareClosedLoop() {
    MotorConfiguration config =
        MotorConfiguration.builder(ControllerFamily.SPARK_FLEX, TransportType.PWM, MotorKind.NEO)
            .feedbackSource(FeedbackSource.QUADRATURE_DIO)
            .build();

    assertEquals(ControllerType.PWM_MOTOR, config.controllerType);
    assertTrue(config.capabilities.runtimeSupported);
    assertTrue(config.capabilities.usesSoftwareClosedLoop);
    assertTrue(config.capabilities.supportsVelocityClosedLoop);
    assertTrue(config.capabilities.supportsPositionClosedLoop);
    assertTrue(config.capabilities.supportsSysId);
  }

  @Test
  void phoenixSixCanPathKeepsNativeMotionProfileSupport() {
    MotorConfiguration config =
        MotorConfiguration.builder(ControllerFamily.TALON_FX, TransportType.CAN, MotorKind.KRAKEN_X44)
            .feedbackSource(FeedbackSource.INTEGRATED)
            .build();

    assertEquals(ControllerType.TALON_FX, config.controllerType);
    assertTrue(config.capabilities.runtimeSupported);
    assertTrue(config.capabilities.supportsMotionProfile);
    assertTrue(config.capabilities.hasCurrentTelemetry);
    assertTrue(config.capabilities.hasTemperatureTelemetry);
  }
}
