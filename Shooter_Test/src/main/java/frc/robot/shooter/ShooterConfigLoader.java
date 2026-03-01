package frc.robot.shooter;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for loading shooter configuration from JSON files.
 */
public class ShooterConfigLoader {
  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Loads shooter configuration from a JSON file in the deploy directory.
   *
   * @param filename Name of the JSON file (e.g., "shooter-config.json")
   * @return ShooterConfig object loaded from the file
   * @throws IOException if the file cannot be read or parsed
   */
  public static ShooterConfig loadConfig(String filename) throws IOException {
    File configFile = new File(Filesystem.getDeployDirectory(), filename);
    return mapper.readValue(configFile, ShooterConfig.class);
  }

  /**
   * Loads shooter configuration from a JSON file, returning a default config if loading fails.
   *
   * @param filename Name of the JSON file (e.g., "shooter-config.json")
   * @return ShooterConfig object loaded from the file, or default config if loading fails
   */
  public static ShooterConfig loadConfigOrDefault(String filename) {
    try {
      return loadConfig(filename);
    } catch (IOException e) {
      System.err.println("Failed to load shooter config from " + filename + ": " + e.getMessage());
      System.err.println("Using default configuration");
      return new ShooterConfig();
    }
  }

  /**
   * Saves shooter configuration to a JSON file in the deploy directory.
   * Used to persist tuned values on the roboRIO across reboots.
   *
   * @param filename Name of the JSON file (e.g., "shooter-config.json")
   * @param config The ShooterConfig object to save
   * @return true if save succeeded, false otherwise
   */
  public static boolean saveConfig(String filename, ShooterConfig config) {
    try {
      File configFile = new File(Filesystem.getDeployDirectory(), filename);
      mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
      System.out.println("Configuration saved to " + configFile.getAbsolutePath());
      return true;
    } catch (IOException e) {
      System.err.println("Failed to save shooter config to " + filename + ": " + e.getMessage());
      return false;
    }
  }
}
