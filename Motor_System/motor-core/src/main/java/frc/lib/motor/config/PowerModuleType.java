package frc.lib.motor.config;

import edu.wpi.first.wpilibj.PowerDistribution;

public enum PowerModuleType {
  NONE,
  AUTO,
  CTRE_PDP,
  REV_PDH;

  public boolean hasBatteryVoltageTelemetry() {
    return true;
  }

  public boolean hasPerChannelCurrentTelemetry() {
    return this != NONE;
  }

  public PowerDistribution.ModuleType toWpilibType() {
    switch (this) {
      case CTRE_PDP:
        return PowerDistribution.ModuleType.kCTRE;
      case REV_PDH:
      case AUTO:
      default:
        return PowerDistribution.ModuleType.kRev;
    }
  }
}
