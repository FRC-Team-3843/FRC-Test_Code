package frc.lib.motor.tuning;

import frc.lib.motor.config.ControllerPreset;

/**
 * UI-facing bridge for SysId parameter reads and result publishing.
 *
 * <p>The tuning layer depends only on this interface, not on SmartDashboard or any concrete UI.
 */
public interface SysIdDashboardBridge {
  SysIdDashboardBridge NO_OP = new SysIdDashboardBridge() {
    @Override
    public SysIdParams readParams(ControllerPreset preset) {
      return SysIdParams.defaultsFor(preset);
    }

    @Override
    public void publishStatus(String status) {}

    @Override
    public void publishResult(SysIdAnalyzer.AnalysisResult result) {}
  };

  SysIdParams readParams(ControllerPreset preset);

  void publishStatus(String status);

  void publishResult(SysIdAnalyzer.AnalysisResult result);
}
