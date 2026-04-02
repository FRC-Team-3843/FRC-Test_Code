package frc.lib.motor.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MotorSystemConfigLoaderTest {
  @Test
  void defaultConfigIsSingleMotorBaseline() {
    MotorSystemConfig config = MotorSystemConfigLoader.createDefaultMotorSystemConfig();

    assertEquals("MotorSystem", config.systemName);
    assertEquals(1, config.motors.size());
    assertEquals("MotorA", config.motors.get(0).name);
    assertEquals(ControllerFamily.TALON_FX.name(), config.motors.get(0).controllerFamily);
    assertEquals(TransportType.CAN.name(), config.motors.get(0).transport);
    assertTrue(config.setpoints.containsKey("setpoint1_MotorA"));
    assertFalse(config.setpoints.containsKey("setpoint1_MotorB"));
  }

  @Test
  void loadReadsSchemaFromExplicitFile() throws IOException {
    Path tempFile = Files.createTempFile("motor-system-config", ".json");
    Files.writeString(tempFile, """
        {
          "systemName": "BenchRig",
          "motors": [
            {
              "name": "ArmMotor",
              "controllerFamily": "SPARK_MAX",
              "transport": "CAN",
              "feedbackSource": "INTEGRATED",
              "motorKind": "NEO"
            }
          ],
          "velocityToleranceRpm": 42.0
        }
        """);

    MotorSystemConfig config = MotorSystemConfigLoader.load(tempFile.toFile());

    assertEquals("BenchRig", config.systemName);
    assertEquals(1, config.motors.size());
    assertEquals("ArmMotor", config.motors.get(0).name);
    assertEquals("SPARK_MAX", config.motors.get(0).controllerFamily);
    assertEquals(42.0, config.velocityToleranceRpm);
    assertNotNull(config.setpoints);

    Files.deleteIfExists(tempFile);
  }
}
