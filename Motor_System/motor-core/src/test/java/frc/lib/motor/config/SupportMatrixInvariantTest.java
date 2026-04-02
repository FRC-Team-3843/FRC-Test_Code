package frc.lib.motor.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.lib.motor.core.ControllerType;
import java.util.List;
import org.junit.jupiter.api.Test;

class SupportMatrixInvariantTest {
  @Test
  void everyControllerFamilyHasExplicitSupportLevelsForCanAndPwm() {
    assertSupportLevels(ControllerFamily.SPARK_MAX, SupportLevel.SUPPORTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.SPARK_FLEX, SupportLevel.SUPPORTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.SPARK, SupportLevel.UNSUPPORTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.TALON_FX, SupportLevel.SUPPORTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.TALON_FXS, SupportLevel.SUPPORTED, SupportLevel.UNSUPPORTED);
    assertSupportLevels(ControllerFamily.TALON_SRX, SupportLevel.SUPPORTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.TALON, SupportLevel.UNSUPPORTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.VICTOR_SPX, SupportLevel.LIMITED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.VICTOR_SP, SupportLevel.UNSUPPORTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.VENOM, SupportLevel.UNIMPLEMENTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.KOORS_40, SupportLevel.UNSUPPORTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.THRIFTY_NOVA, SupportLevel.UNIMPLEMENTED, SupportLevel.UNSUPPORTED);
    assertSupportLevels(ControllerFamily.GENERIC_PWM, SupportLevel.UNSUPPORTED, SupportLevel.SUPPORTED);
    assertSupportLevels(ControllerFamily.SERVO, SupportLevel.UNSUPPORTED, SupportLevel.SUPPORTED);
  }

  @Test
  void supportLevelsDriveControllerTypeAndRuntimeFlagsConsistently() {
    List<FeedbackSource> feedbacks = List.of(
        FeedbackSource.NONE,
        FeedbackSource.INTEGRATED,
        FeedbackSource.QUADRATURE_DIO);

    for (ControllerFamily family : ControllerFamily.values()) {
      for (TransportType transport : TransportType.values()) {
        for (FeedbackSource feedback : feedbacks) {
          MotorConfiguration config = createConfig(family, transport, feedback);
          SupportLevel supportLevel = family.supportLevel(transport);

          assertEquals(supportLevel, config.capabilities.runtimeSupportLevel);
          assertEquals(supportLevel.supportsTransport(), family.supportsTransport(transport));
          assertEquals(supportLevel.hasRuntimeSupport(), family.hasRuntimeSupport(transport));
          assertEquals(supportLevel.hasRuntimeSupport(), config.capabilities.runtimeSupported);

          if (!supportLevel.hasRuntimeSupport()) {
            assertEquals(ControllerType.UNSUPPORTED, config.controllerType);
            continue;
          }

          assertNotNull(config.controllerType);
          assertFalse(config.controllerType == ControllerType.UNSUPPORTED);
          if (transport == TransportType.PWM) {
            ControllerType expected = family == ControllerFamily.SERVO
                ? ControllerType.PWM_SERVO
                : ControllerType.PWM_MOTOR;
            assertEquals(expected, config.controllerType);
          }

          if (feedback == FeedbackSource.NONE) {
            assertFalse(config.capabilities.usesSoftwareClosedLoop);
          }
        }
      }
    }
  }

  @Test
  void limitedAndUnimplementedPathsExposeHumanReadableNotes() {
    for (ControllerFamily family : ControllerFamily.values()) {
      for (TransportType transport : TransportType.values()) {
        SupportLevel level = family.supportLevel(transport);
        if (level == SupportLevel.LIMITED || level == SupportLevel.UNIMPLEMENTED) {
          assertNotNull(family.supportNote(transport));
          assertFalse(family.supportNote(transport).isBlank());
        }
      }
    }
  }

  private static void assertSupportLevels(
      ControllerFamily family,
      SupportLevel expectedCan,
      SupportLevel expectedPwm) {
    assertEquals(expectedCan, family.supportLevel(TransportType.CAN), family.name() + " CAN");
    assertEquals(expectedPwm, family.supportLevel(TransportType.PWM), family.name() + " PWM");
  }

  private static MotorConfiguration createConfig(
      ControllerFamily family,
      TransportType transport,
      FeedbackSource feedback) {
    MotorKind motorKind = family == ControllerFamily.SERVO ? MotorKind.SERVO : MotorKind.CIM;
    MotorConfiguration.Builder builder = MotorConfiguration.builder(family, transport, motorKind)
        .feedbackSource(feedback);

    if (feedback == FeedbackSource.QUADRATURE_DIO) {
      builder.quadratureChannels(0, 1);
    }

    return builder.build();
  }
}
