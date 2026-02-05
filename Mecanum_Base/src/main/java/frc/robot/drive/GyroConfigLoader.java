package frc.robot.drive;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.File;
import java.io.IOException;

/**
 * Utility class for loading gyro configuration from JSON files.
 *
 * <p>Expected JSON format in motor-config.json or separate gyro-config.json:
 * <pre>
 * {
 *   "gyro": {
 *     "type": "PIGEON2",
 *     "canId": 9,
 *     "inverted": false
 *   }
 * }
 * </pre>
 */
public class GyroConfigLoader {
  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * Loads gyro configuration from a JSON file in the deploy directory.
   *
   * @param filename Name of the JSON file (e.g., "motor-config.json")
   * @return GyroConfig object, or null if gyro section not found or type is NONE
   * @throws IOException if file cannot be read or parsed
   */
  public static GyroConfig loadFromFile(String filename) throws IOException {
    File configFile = new File(Filesystem.getDeployDirectory(), filename);
    JsonNode root = mapper.readTree(configFile);

    // Check if gyro section exists
    if (!root.has("gyro")) {
      return null;
    }

    JsonNode gyroNode = root.get("gyro");
    String typeStr = gyroNode.get("type").asText();

    // If type is NONE, return null
    if (typeStr.equalsIgnoreCase("NONE")) {
      return null;
    }

    GyroConfig.GyroType type = GyroConfig.GyroType.valueOf(typeStr.toUpperCase());
    int canId = gyroNode.has("canId") ? gyroNode.get("canId").asInt() : 0;
    boolean inverted = gyroNode.has("inverted") && gyroNode.get("inverted").asBoolean();

    return new GyroConfig(type, canId, inverted);
  }

  /**
   * Creates a GyroIO instance based on the configuration.
   * Requires GyroIO interfaces to be available in your project.
   *
   * @param config GyroConfig object, or null for no gyro
   * @return GyroIO implementation based on config
   */
  public static GyroIO createGyroIO(GyroConfig config) {
    if (config == null) {
      return new GyroIONone();
    }

    // Note: inverted parameter not yet supported by GyroIO implementations
    // TODO: Add inverted support to GyroIO classes in future
    switch (config.type) {
      case ADIS16470:
        return new GyroIOAdis16470();
      case ADXRS450:
        return new GyroIOAdxrs450();
      case PIGEON2:
        return new GyroIOPigeon2(config.canId);
      case NONE:
      default:
        return new GyroIONone();
    }
  }
}
