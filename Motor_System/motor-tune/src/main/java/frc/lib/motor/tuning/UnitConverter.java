package frc.lib.motor.tuning;

import frc.lib.motor.config.MechanismType;

/**
 * Mechanism-aware unit conversion for real-world telemetry display.
 *
 * <p>Converts motor units (RPS, rotations) to real-world units (ft/s, inches, degrees)
 * based on mechanism geometry. Supports imperial/metric toggle.
 */
public class UnitConverter {
  private final MechanismType mechType;
  private final double wheelDiameter;     // inches
  private final double distPerRot;        // inches
  private final double armLength;         // inches
  private boolean useMetric;

  public UnitConverter(MechanismType mechType, double wheelDiameter,
                       double distPerRot, double armLength, boolean useMetric) {
    this.mechType = mechType;
    this.wheelDiameter = wheelDiameter;
    this.distPerRot = distPerRot;
    this.armLength = armLength;
    this.useMetric = useMetric;
  }

  /** Returns true if any geometry is configured for unit conversion. */
  public boolean hasGeometry() {
    switch (mechType) {
      case SIMPLE:   return wheelDiameter > 0;
      case ELEVATOR: return distPerRot > 0;
      case ARM:      return armLength > 0;
      default:       return false;
    }
  }

  public void setUseMetric(boolean useMetric) {
    this.useMetric = useMetric;
  }

  /** Converts velocity from RPS to real-world speed. */
  public double convertVelocity(double rps) {
    switch (mechType) {
      case SIMPLE: {
        if (wheelDiameter <= 0) return rps * 60; // fallback RPM
        double ips = rps * Math.PI * wheelDiameter;
        return useMetric ? ips * 0.0254 : ips / 12.0; // m/s or ft/s
      }
      case ELEVATOR: {
        if (distPerRot <= 0) return rps * 60;
        double ips = rps * distPerRot;
        return useMetric ? ips * 0.0254 : ips / 12.0;
      }
      case ARM: {
        if (armLength <= 0) return rps * 60;
        // Convert RPS to deg/s or rad/s
        return useMetric ? rps * 2 * Math.PI : rps * 360.0;
      }
      default:
        return rps * 60; // RPM fallback
    }
  }

  /** Converts position from rotations to real-world position. */
  public double convertPosition(double rotations) {
    switch (mechType) {
      case SIMPLE: {
        if (wheelDiameter <= 0) return rotations;
        double inches = rotations * Math.PI * wheelDiameter;
        return useMetric ? inches * 0.0254 : inches;
      }
      case ELEVATOR: {
        if (distPerRot <= 0) return rotations;
        double inches = rotations * distPerRot;
        return useMetric ? inches * 0.0254 : inches;
      }
      case ARM: {
        if (armLength <= 0) return rotations;
        return useMetric ? rotations * 2 * Math.PI : rotations * 360.0;
      }
      default:
        return rotations;
    }
  }

  public String velocityUnit() {
    if (!hasGeometry()) return "RPM";
    if (mechType == MechanismType.ARM) return useMetric ? "rad/s" : "deg/s";
    return useMetric ? "m/s" : "ft/s";
  }

  public String positionUnit() {
    if (!hasGeometry()) return "rot";
    if (mechType == MechanismType.ARM) return useMetric ? "rad" : "deg";
    return useMetric ? "m" : "in";
  }

  public String massUnit() {
    return useMetric ? "kg" : "lbs";
  }

  /** Converts mass from lbs to display unit. */
  public double convertMass(double lbs) {
    return useMetric ? lbs * 0.4536 : lbs;
  }

  /** Returns a geometry summary string for display. */
  public String geometrySummary() {
    switch (mechType) {
      case SIMPLE:
        return wheelDiameter > 0 ? String.format("Wheel: %.1f in", wheelDiameter) : "";
      case ELEVATOR:
        return distPerRot > 0 ? String.format("Dist/rot: %.2f in", distPerRot) : "";
      case ARM:
        return armLength > 0 ? String.format("Length: %.1f in", armLength) : "";
      default:
        return "";
    }
  }
}
