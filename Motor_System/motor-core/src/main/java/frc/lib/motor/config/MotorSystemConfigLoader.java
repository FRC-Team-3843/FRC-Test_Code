package frc.lib.motor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.File;
import java.io.IOException;

/** File I/O and default factory for {@link MotorSystemConfig}. */
public final class MotorSystemConfigLoader {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private MotorSystemConfigLoader() {}

  public static MotorSystemConfig load(String filename) throws IOException {
    File configFile = new File(Filesystem.getDeployDirectory(), filename);
    return load(configFile);
  }

  public static MotorSystemConfig load(File path) throws IOException {
    return MAPPER.readValue(path, MotorSystemConfig.class);
  }

  public static MotorSystemConfig loadOrDefault(String filename) {
    try {
      return load(filename);
    } catch (IOException e) {
      System.err.println("Failed to load config from " + filename + ": " + e.getMessage());
      System.err.println("Using default configuration");
      return createDefaultMotorSystemConfig();
    }
  }

  public static boolean save(String filename, MotorSystemConfig config) {
    try {
      File configFile = new File(Filesystem.getDeployDirectory(), filename);
      MAPPER.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
      System.out.println("Configuration saved to " + configFile.getAbsolutePath());
      return true;
    } catch (IOException e) {
      System.err.println("Failed to save config to " + filename + ": " + e.getMessage());
      return false;
    }
  }

  public static MotorSystemConfig createDefaultMotorSystemConfig() {
    MotorSystemConfig config = new MotorSystemConfig();
    config.systemName = "MotorSystem";
    config.powerModuleType = PowerModuleType.NONE;
    config.powerModuleId = 1;

    MotorConfig motorA = new MotorConfig();
    motorA.name = "MotorA";
    motorA.controllerFamily = ControllerFamily.TALON_FX.name();
    motorA.transport = TransportType.CAN.name();
    motorA.feedbackSource = FeedbackSource.INTEGRATED.name();
    motorA.motorKind = MotorKind.KRAKEN_X44.name();
    motorA.canId = 20;
    motorA.brakeMode = false;
    motorA.kP = 0.2;
    motorA.kV = 0.116;
    motorA.kS = 0.25;
    motorA.powerChannel = -1;
    config.motors.add(motorA);

    config.setpoints.put("setpoint1_MotorA", 6500.0);
    config.setpoints.put("setpoint2_MotorA", 3250.0);
    config.servoChannel = -1;

    config.buttonBindings.put("setpoint1", "A");
    config.buttonBindings.put("setpoint2", "B");
    config.buttonBindings.put("applyPid", "X");
    config.buttonBindings.put("sysId_MotorA", "Y");

    return config;
  }
}
