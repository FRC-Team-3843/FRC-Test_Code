package frc.robot.motor;

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

/**
 * Top-level system configuration loaded from JSON.
 *
 * <p>Replaces the shooter-specific ShooterConfig with a generic motor system
 * config that works for any mechanism (shooter, elevator, arm, swerve, etc.).
 *
 * <p>Contains a list of motor configs, named setpoints, servo config, and
 * system-level settings. Supports load/save to the deploy directory.
 */
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

  @JsonProperty("buttonBindings")
  public Map<String, String> buttonBindings = new LinkedHashMap<>();

  public MotorSystemConfig() {}

  /**
   * Loads config from a JSON file in the deploy directory.
   *
   * @param filename name of the JSON file (e.g. "motor-config.json")
   * @return parsed config
   * @throws IOException if file cannot be read or parsed
   */
  public static MotorSystemConfig load(String filename) throws IOException {
    File configFile = new File(Filesystem.getDeployDirectory(), filename);
    return mapper.readValue(configFile, MotorSystemConfig.class);
  }

  /**
   * Loads config from an absolute file path. Used at build time (no WPILib runtime).
   *
   * @param path absolute path to the config file
   * @return parsed config
   * @throws IOException if file cannot be read or parsed
   */
  public static MotorSystemConfig load(File path) throws IOException {
    return mapper.readValue(path, MotorSystemConfig.class);
  }

  /**
   * Loads config from a JSON file, returning a default config if loading fails.
   *
   * @param filename name of the JSON file
   * @return parsed config or default
   */
  public static MotorSystemConfig loadOrDefault(String filename) {
    try {
      return load(filename);
    } catch (IOException e) {
      System.err.println("Failed to load config from " + filename + ": " + e.getMessage());
      System.err.println("Using default configuration");
      return createDefaultShooterConfig();
    }
  }

  /**
   * Saves config to a JSON file in the deploy directory.
   *
   * @param filename name of the JSON file
   * @return true if save succeeded
   */
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

  /**
   * Finds a motor config by name (case-insensitive).
   *
   * @param name the motor name to find
   * @return the motor config, or null if not found
   */
  public MotorConfig findMotor(String name) {
    for (MotorConfig mc : motors) {
      if (mc.name.equalsIgnoreCase(name)) {
        return mc;
      }
    }
    return null;
  }

  /**
   * Returns the full dashboard prefix for a motor (e.g. "Shooter/Preshooter").
   *
   * @param motorName the motor's name field
   * @return systemName + "/" + motorName
   */
  public String motorPrefix(String motorName) {
    return systemName + "/" + motorName;
  }

  /** Creates a default config matching the current shooter hardware. */
  private static MotorSystemConfig createDefaultShooterConfig() {
    MotorSystemConfig config = new MotorSystemConfig();
    config.systemName = "Shooter";

    MotorConfig preshooter = new MotorConfig();
    preshooter.name = "Preshooter";
    preshooter.controllerType = "TALON_FX";
    preshooter.motorKind = "KRAKEN_X44";
    preshooter.canId = 20;
    preshooter.brakeMode = false;
    preshooter.kP = 0.2;
    preshooter.kV = 0.116;
    preshooter.kS = 0.25;
    config.motors.add(preshooter);

    MotorConfig mainShooter = new MotorConfig();
    mainShooter.name = "MainShooter";
    mainShooter.controllerType = "TALON_FX";
    mainShooter.motorKind = "KRAKEN";
    mainShooter.canId = 21;
    mainShooter.brakeMode = false;
    mainShooter.kP = 0.2;
    mainShooter.kV = 0.111;
    mainShooter.kS = 0.25;
    config.motors.add(mainShooter);

    config.setpoints.put("setpoint1_Preshooter", 6500.0);
    config.setpoints.put("setpoint1_MainShooter", 5500.0);
    config.setpoints.put("setpoint2_Preshooter", 3250.0);
    config.setpoints.put("setpoint2_MainShooter", 2750.0);

    config.servoChannel = 0;
    config.servoPositions.put("position1", 0.95);
    config.servoPositions.put("position2", 0.45);

    config.velocityToleranceRpm = 50.0;
    config.driverControllerPort = 0;
    config.enableLogging = true;

    config.buttonBindings.put("setpoint1", "A");
    config.buttonBindings.put("setpoint2", "B");
    config.buttonBindings.put("applyPid", "X");
    config.buttonBindings.put("sysId_Preshooter", "Y");
    config.buttonBindings.put("sysId_MainShooter", "BACK");
    config.buttonBindings.put("servoPos1", "LEFT_BUMPER");
    config.buttonBindings.put("servoPos2", "RIGHT_BUMPER");

    return config;
  }
}
