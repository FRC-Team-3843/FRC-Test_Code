package frc.robot.motor.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MotorSystemConfig {
  private static final ObjectMapper mapper = new ObjectMapper();

  @JsonProperty("systemName")
  public String systemName = "System";

  @JsonProperty("motors")
  public List<MotorConfig> motors = new ArrayList<>();

  @JsonProperty("setpoints")
  public Map<String, Double> setpoints = new LinkedHashMap<>();

  @JsonProperty("servoPositions")
  public Map<String, Double> servoPositions = new LinkedHashMap<>();

  @JsonProperty("servoChannel")
  public int servoChannel = -1;

  @JsonProperty("velocityToleranceRpm")
  public double velocityToleranceRpm = 50.0;

  @JsonProperty("driverControllerPort")
  public int driverControllerPort = 0;

  @JsonProperty("enableLogging")
  public boolean enableLogging = true;

  @JsonProperty("powerModuleType")
  public PowerModuleType powerModuleType = PowerModuleType.NONE;

  @JsonProperty("powerModuleId")
  public int powerModuleId = 1;

  @JsonProperty("buttonBindings")
  public Map<String, String> buttonBindings = new LinkedHashMap<>();

  public static MotorSystemConfig load(String filename) throws IOException {
    File configFile = new File(Filesystem.getDeployDirectory(), filename);
    return mapper.readValue(configFile, MotorSystemConfig.class);
  }

  public static MotorSystemConfig load(File path) throws IOException {
    return mapper.readValue(path, MotorSystemConfig.class);
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

  public boolean save(String filename) {
    try {
      File configFile = new File(Filesystem.getDeployDirectory(), filename);
      mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, this);
      System.out.println("Configuration saved to " + configFile.getAbsolutePath());
      return true;
    } catch (IOException e) {
      System.err.println("Failed to save config to " + filename + ": " + e.getMessage());
      return false;
    }
  }

  public MotorConfig findMotor(String name) {
    for (MotorConfig mc : motors) {
      if (mc.name.equalsIgnoreCase(name)) {
        return mc;
      }
    }
    return null;
  }

  public String motorPrefix(String motorName) {
    return systemName + "/" + motorName;
  }

  private static MotorSystemConfig createDefaultMotorSystemConfig() {
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
    motorA.powerChannel = 20;
    config.motors.add(motorA);

    MotorConfig motorB = new MotorConfig();
    motorB.name = "MotorB";
    motorB.controllerFamily = ControllerFamily.TALON_FX.name();
    motorB.transport = TransportType.CAN.name();
    motorB.feedbackSource = FeedbackSource.INTEGRATED.name();
    motorB.motorKind = MotorKind.KRAKEN.name();
    motorB.canId = 21;
    motorB.brakeMode = false;
    motorB.kP = 0.2;
    motorB.kV = 0.111;
    motorB.kS = 0.25;
    motorB.powerChannel = 21;
    config.motors.add(motorB);

    config.setpoints.put("setpoint1_MotorA", 6500.0);
    config.setpoints.put("setpoint1_MotorB", 5500.0);
    config.setpoints.put("setpoint2_MotorA", 3250.0);
    config.setpoints.put("setpoint2_MotorB", 2750.0);

    config.servoChannel = 0;
    config.servoPositions.put("position1", 0.95);
    config.servoPositions.put("position2", 0.45);

    config.buttonBindings.put("setpoint1", "A");
    config.buttonBindings.put("setpoint2", "B");
    config.buttonBindings.put("applyPid", "X");
    config.buttonBindings.put("sysId_MotorA", "Y");
    config.buttonBindings.put("sysId_MotorB", "BACK");
    config.buttonBindings.put("servoPos1", "LEFT_BUMPER");
    config.buttonBindings.put("servoPos2", "RIGHT_BUMPER");

    return config;
  }
}
