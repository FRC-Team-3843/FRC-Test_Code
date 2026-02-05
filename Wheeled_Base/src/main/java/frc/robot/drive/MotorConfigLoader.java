package frc.robot.drive;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public final class MotorConfigLoader {
  private MotorConfigLoader() {}

  public static Map<String, MotorConfig> loadConfigs(String fileName) {
    File deployDirectory = Filesystem.getDeployDirectory();
    File configFile = new File(deployDirectory, fileName);
    ObjectMapper mapper = new ObjectMapper();

    try {
      return mapper.readValue(configFile, new TypeReference<Map<String, MotorConfig>>() {});
    } catch (IOException e) {
      e.printStackTrace();
      return Collections.emptyMap();
    }
  }
}
