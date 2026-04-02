package frc.lib.motor.dashboard;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.lib.motor.config.ControllerPreset;
import frc.lib.motor.config.MotorConfig;
import frc.lib.motor.config.MotorSystemConfig;
import frc.lib.motor.tuning.SysIdAnalyzer;
import frc.lib.motor.tuning.SysIdDashboardBridge;
import frc.lib.motor.tuning.SysIdParams;
import frc.lib.motorsystem.MotorChannel;
import frc.lib.motorsystem.MotorSystem;
import java.util.List;

/**
 * Owns the SmartDashboard/Elastic runtime contract for motor tuning UIs.
 * This keeps chooser state and telemetry publishing out of the motor runtime layer.
 */
public class MotorDashboardRuntime {
  private final MotorSystemConfig m_config;
  private final MotorSystem m_motorSystem;
  private final SendableChooser<String>[] m_chartChoosers;
  private final SendableChooser<String>[] m_controlModeChoosers;

  @SuppressWarnings("unchecked")
  public MotorDashboardRuntime(MotorSystemConfig config, MotorSystem motorSystem) {
    m_config = config;
    m_motorSystem = motorSystem;
    m_chartChoosers = new SendableChooser[config.motors.size()];
    m_controlModeChoosers = new SendableChooser[config.motors.size()];
  }

  public void publishChoosers() {
    for (int i = 0; i < m_config.motors.size(); i++) {
      MotorChannel channel = m_motorSystem.getMotorChannel(i);
      String prefix = channel.getMotorName() + "/";

      SendableChooser<String> controlModeChooser = new SendableChooser<>();
      addChooserOptions(controlModeChooser, channel.getAvailableControlModes(), "velocity");
      SmartDashboard.putData(prefix + "ControlModeChooser", controlModeChooser);
      m_controlModeChoosers[i] = controlModeChooser;

      SendableChooser<String> chartChooser = new SendableChooser<>();
      List<MotorChannel.ChartOption> chartOptions = channel.getAvailableChartOptions();
      for (int optionIndex = 0; optionIndex < chartOptions.size(); optionIndex++) {
        MotorChannel.ChartOption option = chartOptions.get(optionIndex);
        if (optionIndex == 0) {
          chartChooser.setDefaultOption(option.label(), option.key());
        } else {
          chartChooser.addOption(option.label(), option.key());
        }
      }
      SmartDashboard.putData(prefix + "ChartType", chartChooser);
      m_chartChoosers[i] = chartChooser;

      bindSysIdBridge(i);
    }
  }

  public void publishSysIdDefaults(int motorIndex, ControllerPreset preset) {
    String prefix = m_motorSystem.getMotorName(motorIndex) + "/SysId/";
    SysIdParams defaults = SysIdParams.defaultsFor(preset);

    SmartDashboard.putNumber(prefix + "MaxEffort", defaults.maxControlEffort);
    SmartDashboard.putNumber(prefix + "MaxVelErr", defaults.maxVelocityError);
    SmartDashboard.putNumber(prefix + "MaxPosErr", defaults.maxPositionError);
    SmartDashboard.putNumber(prefix + "MedianWindow", defaults.medianWindowSize);
    SmartDashboard.putNumber(prefix + "VelThreshold", defaults.velocityThreshold);
    SmartDashboard.putNumber(prefix + "TestDuration", defaults.testDurationSeconds);
    SmartDashboard.putNumber(prefix + "MeasDelay(ms)", defaults.measurementDelay * 1000.0);
    SmartDashboard.putNumber(prefix + "CtrlPeriod(ms)", defaults.controllerPeriod * 1000.0);
    SmartDashboard.putNumber(prefix + "OutputFactor", defaults.outputConversionFactor);
  }

  public void syncDashboardState() {
    boolean useMetric = SmartDashboard.getBoolean(m_config.systemName + "/UseMetric", false);

    for (int i = 0; i < m_config.motors.size(); i++) {
      MotorConfig motorConfig = m_config.motors.get(i);
      MotorChannel channel = m_motorSystem.getMotorChannel(i);
      String prefix = m_config.motorPrefix(motorConfig.name) + "/";

      boolean enabled = SmartDashboard.getBoolean(prefix + "Enabled", channel.isEnabled());
      channel.setEnabled(enabled);

      MotorChannel.TelemetrySnapshot snapshot =
          channel.createTelemetrySnapshot(useMetric, selectedChartType(i));
      publishSnapshot(prefix, snapshot);
    }
  }

  public String getSelectedControlMode(int motorIndex) {
    SendableChooser<String> chooser = m_controlModeChoosers[motorIndex];
    String selected = chooser == null ? null : chooser.getSelected();
    return selected == null ? "velocity" : selected;
  }

  private static void addChooserOptions(
      SendableChooser<String> chooser,
      List<String> options,
      String defaultOption) {
    boolean defaultSet = false;
    for (String option : options) {
      String label = titleCase(option);
      if (!defaultSet && option.equals(defaultOption)) {
        chooser.setDefaultOption(label, option);
        defaultSet = true;
      } else {
        chooser.addOption(label, option);
      }
    }
    if (!defaultSet && !options.isEmpty()) {
      chooser.setDefaultOption(titleCase(options.get(0)), options.get(0));
    }
  }

  private String selectedChartType(int motorIndex) {
    SendableChooser<String> chooser = m_chartChoosers[motorIndex];
    String selected = chooser == null ? null : chooser.getSelected();
    return selected == null ? "rpm" : selected;
  }

  private static void publishSnapshot(String prefix, MotorChannel.TelemetrySnapshot snapshot) {
    SmartDashboard.putNumber(prefix + "ActualRPM", snapshot.actualRpm());
    SmartDashboard.putBoolean(prefix + "AtSetpoint", snapshot.atSetpoint());
    SmartDashboard.putNumber(prefix + "SetpointRPM", snapshot.setpointRpm());
    SmartDashboard.putNumber(prefix + "CurrentAmps", snapshot.currentAmps());
    SmartDashboard.putNumber(prefix + "Voltage", snapshot.voltage());
    SmartDashboard.putNumber(prefix + "PositionRot", snapshot.positionRot());
    SmartDashboard.putNumber(prefix + "VelocityRps", snapshot.velocityRps());
    SmartDashboard.putNumber(prefix + "TemperatureC", snapshot.temperatureC());
    SmartDashboard.putNumber(prefix + "ChartValue", snapshot.chartValue());
    SmartDashboard.putString(prefix + "ChartLabel", snapshot.chartLabel());

    if (snapshot.hasRealUnits()) {
      SmartDashboard.putNumber(prefix + "RealSpeed", snapshot.realSpeed());
      SmartDashboard.putString(prefix + "SpeedUnit", snapshot.speedUnit());
      SmartDashboard.putNumber(prefix + "RealPosition", snapshot.realPosition());
      SmartDashboard.putString(prefix + "PosUnit", snapshot.positionUnit());
    } else {
      SmartDashboard.putNumber(prefix + "RealSpeed", 0.0);
      SmartDashboard.putString(prefix + "SpeedUnit", "");
      SmartDashboard.putNumber(prefix + "RealPosition", 0.0);
      SmartDashboard.putString(prefix + "PosUnit", "");
    }

    SmartDashboard.putString(prefix + "Testing/FeedbackSource", snapshot.feedbackSource());
    SmartDashboard.putBoolean(prefix + "Testing/FeedbackConnected", snapshot.feedbackConnected());
    SmartDashboard.putString(prefix + "Testing/ClosedLoopSource", snapshot.closedLoopSource());
    SmartDashboard.putString(prefix + "Testing/PowerTelemetrySource", snapshot.powerTelemetrySource());
    SmartDashboard.putString(prefix + "Testing/TelemetryHealth", snapshot.telemetryHealth());
    SmartDashboard.putString(prefix + "Testing/CapabilitySummary", snapshot.capabilitySummary());
    SmartDashboard.putNumber(prefix + "Testing/MeasuredVelocityRps", snapshot.measuredVelocityRps());
    SmartDashboard.putNumber(prefix + "Testing/MeasuredPositionRot", snapshot.measuredPositionRot());
    SmartDashboard.putNumber(prefix + "Testing/MeasuredVoltage", snapshot.measuredVoltage());
    SmartDashboard.putNumber(prefix + "Testing/MeasuredCurrent", snapshot.measuredCurrent());
    SmartDashboard.putNumber(prefix + "Testing/MeasuredTemperatureC", snapshot.measuredTemperatureC());
  }

  private void bindSysIdBridge(int motorIndex) {
    String prefix = m_motorSystem.getMotorName(motorIndex) + "/SysId/";
    m_motorSystem.getSysId(motorIndex).setDashboardBridge(new DashboardSysIdBridge(prefix));
  }

  private static double round6(double value) {
    return Math.round(value * 1e6) / 1e6;
  }

  private static final class DashboardSysIdBridge implements SysIdDashboardBridge {
    private final String m_prefix;

    private DashboardSysIdBridge(String prefix) {
      m_prefix = prefix;
    }

    @Override
    public SysIdParams readParams(ControllerPreset preset) {
      SysIdParams params = SysIdParams.defaultsFor(preset);
      params.maxControlEffort = SmartDashboard.getNumber(m_prefix + "MaxEffort", params.maxControlEffort);
      params.maxVelocityError = SmartDashboard.getNumber(m_prefix + "MaxVelErr", params.maxVelocityError);
      params.maxPositionError = SmartDashboard.getNumber(m_prefix + "MaxPosErr", params.maxPositionError);
      params.medianWindowSize = (int) SmartDashboard.getNumber(m_prefix + "MedianWindow", params.medianWindowSize);
      params.velocityThreshold = SmartDashboard.getNumber(m_prefix + "VelThreshold", params.velocityThreshold);
      params.testDurationSeconds = SmartDashboard.getNumber(m_prefix + "TestDuration", params.testDurationSeconds);
      return params;
    }

    @Override
    public void publishStatus(String status) {
      SmartDashboard.putString(m_prefix + "Status", status);
    }

    @Override
    public void publishResult(SysIdAnalyzer.AnalysisResult result) {
      SmartDashboard.putNumber(m_prefix + "kS", round6(result.kS));
      SmartDashboard.putNumber(m_prefix + "kV", round6(result.kV));
      SmartDashboard.putNumber(m_prefix + "kA", round6(result.kA));
      SmartDashboard.putNumber(m_prefix + "kG", round6(result.kG));
      SmartDashboard.putNumber(m_prefix + "R2_Accel", round6(result.rSquaredAccel));
      SmartDashboard.putNumber(m_prefix + "R2_SimVel", round6(result.rSquaredSimVel));
      SmartDashboard.putNumber(m_prefix + "RMSE", round6(result.rmse));
      SmartDashboard.putNumber(m_prefix + "Samples", result.sampleCount);
      SmartDashboard.putNumber(m_prefix + "kP_vel", round6(result.kP_velocity));
      SmartDashboard.putNumber(m_prefix + "kP_pos", round6(result.kP_position));
      SmartDashboard.putNumber(m_prefix + "kD_pos", round6(result.kD_position));
      SmartDashboard.putNumber(m_prefix + "AutoVelThresh", round6(result.autoVelocityThreshold));
      SmartDashboard.putNumber(m_prefix + "AutoQv", round6(result.autoQv));
      SmartDashboard.putNumber(m_prefix + "MaxEffort", result.maxControlEffort);
      SmartDashboard.putNumber(m_prefix + "MaxVelErr", result.maxVelocityError);
      SmartDashboard.putNumber(m_prefix + "MaxPosErr", result.maxPositionError);

      SysIdAnalyzer.PhysicalEstimate est = result.physicalEstimate;
      if (est != null && est.valid) {
        SmartDashboard.putNumber(m_prefix + "Est_Inertia", round6(est.momentOfInertia));
        SmartDashboard.putNumber(m_prefix + "Est_Friction", round6(est.frictionTorque));
        SmartDashboard.putNumber(m_prefix + "Est_Mass_kg", round6(est.estimatedMass));
        SmartDashboard.putNumber(m_prefix + "Est_Efficiency", round6(est.efficiency));
        SmartDashboard.putNumber(m_prefix + "Est_MaxAccel", round6(est.maxAcceleration));
        SmartDashboard.putNumber(m_prefix + "Est_FreeSpeed", round6(est.freeSpeed));
      }
    }
  }

  private static String titleCase(String option) {
    if (option == null || option.isBlank()) {
      return "";
    }
    return Character.toUpperCase(option.charAt(0)) + option.substring(1);
  }
}
