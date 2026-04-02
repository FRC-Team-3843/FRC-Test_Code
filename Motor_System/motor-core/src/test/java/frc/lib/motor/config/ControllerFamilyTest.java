package frc.lib.motor.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ControllerFamilyTest {
  @Test
  void sparkMaxSupportsCanAndPwmAtRuntime() {
    assertTrue(ControllerFamily.SPARK_MAX.supportsTransport(TransportType.CAN));
    assertTrue(ControllerFamily.SPARK_MAX.supportsTransport(TransportType.PWM));
    assertTrue(ControllerFamily.SPARK_MAX.hasRuntimeSupport(TransportType.CAN));
    assertTrue(ControllerFamily.SPARK_MAX.hasRuntimeSupport(TransportType.PWM));
  }

  @Test
  void talonFxsIsCanOnlyInCurrentRuntimeModel() {
    assertTrue(ControllerFamily.TALON_FXS.supportsTransport(TransportType.CAN));
    assertTrue(ControllerFamily.TALON_FXS.hasRuntimeSupport(TransportType.CAN));
    assertFalse(ControllerFamily.TALON_FXS.supportsTransport(TransportType.PWM));
    assertFalse(ControllerFamily.TALON_FXS.hasRuntimeSupport(TransportType.PWM));
  }

  @Test
  void victorSpxIsRecognizedButCanSupportIsNotImplemented() {
    assertTrue(ControllerFamily.VICTOR_SPX.supportsTransport(TransportType.CAN));
    assertTrue(ControllerFamily.VICTOR_SPX.hasRuntimeSupport(TransportType.CAN));
    assertEquals(SupportLevel.LIMITED, ControllerFamily.VICTOR_SPX.supportLevel(TransportType.CAN));
    assertTrue(ControllerFamily.VICTOR_SPX.supportsTransport(TransportType.PWM));
    assertTrue(ControllerFamily.VICTOR_SPX.hasRuntimeSupport(TransportType.PWM));
  }

  @Test
  void thriftyNovaIsRecognizedWithoutCurrentRuntimePath() {
    assertTrue(ControllerFamily.THRIFTY_NOVA.supportsTransport(TransportType.CAN));
    assertFalse(ControllerFamily.THRIFTY_NOVA.hasRuntimeSupport(TransportType.CAN));
    assertEquals(SupportLevel.UNIMPLEMENTED, ControllerFamily.THRIFTY_NOVA.supportLevel(TransportType.CAN));
    assertFalse(ControllerFamily.THRIFTY_NOVA.supportsTransport(TransportType.PWM));
    assertFalse(ControllerFamily.THRIFTY_NOVA.hasRuntimeSupport(TransportType.PWM));
  }
}
