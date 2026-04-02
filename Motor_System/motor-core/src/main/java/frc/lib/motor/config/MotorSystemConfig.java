package frc.lib.motor.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MotorSystemConfig {
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
}
