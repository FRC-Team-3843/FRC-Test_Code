package frc.robot.motor;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * All configurable SysId analysis parameters in one place.
 *
 * <p>These can be read from the Elastic dashboard before each SysId run,
 * allowing real-time tuning of analysis parameters without redeploying.
 */
public class SysIdParams {
  // LQR parameters
  public double maxControlEffort = 7.0;     // volts
  public double maxVelocityError = 0.0;     // rot/s (0 = auto-compute from kS/kV)
  public double maxPositionError = 0.5;     // rotations

  // Preprocessing
  public int medianWindowSize = 3;          // WPILib default median filter window
  public double velocityThreshold = 0.0;    // 0 = auto-compute from noise floor
  public double testDurationSeconds = 10.0; // per-test duration

  // Controller timing (auto-set from ControllerPreset)
  public double measurementDelay = 0.0;     // seconds
  public double controllerPeriod = 0.001;   // seconds
  public double outputConversionFactor = 1.0;

  public SysIdParams() {}

  /**
   * Creates SysIdParams by reading from SmartDashboard, with controller preset defaults.
   *
   * @param prefix SmartDashboard key prefix (e.g. "Shooter/Preshooter/SysId/")
   * @param preset controller timing preset
   * @return populated SysIdParams
   */
  public static SysIdParams fromDashboard(String prefix, ControllerPreset preset) {
    SysIdParams params = new SysIdParams();

    // LQR params (user-editable on dashboard)
    params.maxControlEffort = SmartDashboard.getNumber(prefix + "MaxEffort", 7.0);
    params.maxVelocityError = SmartDashboard.getNumber(prefix + "MaxVelErr", 0.0);
    params.maxPositionError = SmartDashboard.getNumber(prefix + "MaxPosErr", 0.5);

    // Preprocessing params (user-editable)
    params.medianWindowSize = (int) SmartDashboard.getNumber(prefix + "MedianWindow", 3);
    params.velocityThreshold = SmartDashboard.getNumber(prefix + "VelThreshold", 0.0);
    params.testDurationSeconds = SmartDashboard.getNumber(prefix + "TestDuration", 10.0);

    // Controller timing (from preset, not user-editable but published for visibility)
    params.measurementDelay = preset.measurementDelaySeconds;
    params.controllerPeriod = preset.periodSeconds;
    params.outputConversionFactor = preset.outputConversionFactor;

    return params;
  }

  /**
   * Publishes default values to SmartDashboard so they appear on the Elastic dashboard.
   * Call once during initialization.
   *
   * @param prefix SmartDashboard key prefix
   * @param preset controller timing preset
   */
  public static void publishDefaults(String prefix, ControllerPreset preset) {
    SmartDashboard.putNumber(prefix + "MaxEffort", 7.0);
    SmartDashboard.putNumber(prefix + "MaxVelErr", 0.0);
    SmartDashboard.putNumber(prefix + "MaxPosErr", 0.5);
    SmartDashboard.putNumber(prefix + "MedianWindow", 3);
    SmartDashboard.putNumber(prefix + "VelThreshold", 0.0);
    SmartDashboard.putNumber(prefix + "TestDuration", 10.0);

    // Read-only timing info
    SmartDashboard.putNumber(prefix + "MeasDelay(ms)", preset.measurementDelaySeconds * 1000.0);
    SmartDashboard.putNumber(prefix + "CtrlPeriod(ms)", preset.periodSeconds * 1000.0);
    SmartDashboard.putNumber(prefix + "OutputFactor", preset.outputConversionFactor);
  }
}
