package frc.lib.motor.core;

import edu.wpi.first.wpilibj.PowerDistribution;
import frc.lib.motor.config.PowerModuleType;
import java.util.HashMap;
import java.util.Map;

public final class PowerDistributionRegistry {
  private static final Map<String, PowerDistribution> INSTANCES = new HashMap<>();

  private PowerDistributionRegistry() {}

  public static synchronized PowerDistribution get(PowerModuleType moduleType, int moduleId) {
    if (moduleType == PowerModuleType.NONE) {
      return null;
    }
    if (moduleType == PowerModuleType.AUTO) {
      return INSTANCES.computeIfAbsent("AUTO", k -> new PowerDistribution());
    }

    String key = moduleType.name() + ":" + moduleId;
    return INSTANCES.computeIfAbsent(key,
        k -> new PowerDistribution(moduleId, moduleType.toWpilibType()));
  }
}
