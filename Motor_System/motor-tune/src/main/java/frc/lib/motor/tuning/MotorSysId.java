package frc.lib.motor.tuning;

import static edu.wpi.first.units.Units.*;

import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;
import frc.lib.motor.config.ControllerPreset;
import frc.lib.motor.config.MechanismType;
import frc.lib.motor.config.MotorConfig;
import frc.lib.motor.core.UniversalMotor;
import java.util.ArrayList;
import java.util.List;

/**
 * Reusable SysId characterization wrapper for any UniversalMotor.
 *
 * <p>Creates a WPILib {@link SysIdRoutine} that drives the motor with voltage and logs
 * voltage, angular velocity (RPS), and angular position (rotations). The resulting log
 * can be opened in the WPILib SysId tool to compute kS, kV, and kA.
 *
 * <p>Also collects data in-memory during tests and runs WPILib-matching OLS regression
 * on-robot to produce instant feedforward and feedback gains on the Elastic dashboard.
 *
 * <p>Usage:
 * <pre>
 * MotorSysId sysId = new MotorSysId(motor, "MotorSystem/MotorA", subsystem,
 *     MechanismType.SIMPLE, ControllerPreset.PHOENIX6);
 * button.whileTrue(sysId.fullRoutineWithAnalysis());
 * </pre>
 */
public class MotorSysId {
  private final SysIdRoutine m_routine;
  private final UniversalMotor m_motor;
  private final String m_motorName;
  private final MechanismType m_mechanismType;
  private final ControllerPreset m_preset;
  private final DCMotor m_dcMotor;
  private final double m_gearRatio;
  private final double m_distPerRot;
  private final double m_armLength;
  private SysIdDashboardBridge m_dashboardBridge = SysIdDashboardBridge.NO_OP;

  private final List<SysIdAnalyzer.DataPoint> m_collectedData = new ArrayList<>();
  private boolean m_collecting = false;
  private SysIdAnalyzer.AnalysisResult m_lastResult = null;
  private int m_runCount = 0;

  /**
   * Creates a SysId routine with default config and SIMPLE mechanism.
   */
  public MotorSysId(UniversalMotor motor, String motorName, SubsystemBase subsystem) {
    this(motor, motorName, subsystem, MechanismType.SIMPLE, ControllerPreset.PHOENIX6,
        new SysIdRoutine.Config(), null, 1.0, 0.0, 0.0);
  }

  /**
   * Creates a SysId routine with mechanism type and controller preset.
   */
  public MotorSysId(UniversalMotor motor, String motorName, SubsystemBase subsystem,
                    MechanismType mechanismType, ControllerPreset preset) {
    this(motor, motorName, subsystem, mechanismType, preset, new SysIdRoutine.Config(),
        null, 1.0, 0.0, 0.0);
  }

  /**
   * Creates a SysId routine with mechanism type, preset, and motor config for physical estimation.
   */
  public MotorSysId(UniversalMotor motor, String motorName, SubsystemBase subsystem,
                    MechanismType mechanismType, ControllerPreset preset,
                    MotorConfig mc) {
    this(motor, motorName, subsystem, mechanismType, preset, new SysIdRoutine.Config(),
        mc.getMotorKind().getDCMotor(), mc.gearRatio, mc.distancePerRotation, mc.armLength);
  }

  /**
   * Creates a SysId routine with full configuration.
   */
  public MotorSysId(UniversalMotor motor, String motorName, SubsystemBase subsystem,
                    MechanismType mechanismType, ControllerPreset preset,
                    SysIdRoutine.Config config,
                    DCMotor dcMotor, double gearRatio, double distPerRot, double armLength) {
    m_motor = motor;
    m_motorName = motorName;
    m_mechanismType = mechanismType;
    m_preset = preset;
    m_dcMotor = dcMotor;
    m_gearRatio = gearRatio;
    m_distPerRot = distPerRot;
    m_armLength = armLength;

    m_routine = new SysIdRoutine(
        config,
        new SysIdRoutine.Mechanism(
            voltage -> motor.setVoltage(voltage.in(Volts)),
            log -> {
              log.motor(motorName)
                  .voltage(Volts.of(motor.getAppliedVoltage()))
                  .angularVelocity(RotationsPerSecond.of(motor.getVelocityRps()))
                  .angularPosition(Rotations.of(motor.getPositionRotations()));

              if (m_collecting) {
                m_collectedData.add(new SysIdAnalyzer.DataPoint(
                    Timer.getFPGATimestamp(),
                    motor.getAppliedVoltage(),
                    motor.getVelocityRps(),
                    motor.getPositionRotations()));
              }
            },
            subsystem));
  }

  /** Quasistatic test, forward direction. */
  public Command quasistaticForward() {
    return m_routine.quasistatic(SysIdRoutine.Direction.kForward);
  }

  /** Quasistatic test, reverse direction. */
  public Command quasistaticReverse() {
    return m_routine.quasistatic(SysIdRoutine.Direction.kReverse);
  }

  /** Dynamic test, forward direction. */
  public Command dynamicForward() {
    return m_routine.dynamic(SysIdRoutine.Direction.kForward);
  }

  /** Dynamic test, reverse direction. */
  public Command dynamicReverse() {
    return m_routine.dynamic(SysIdRoutine.Direction.kReverse);
  }

  /**
   * Returns a sequential command that runs all 4 SysId tests in order:
   * quasistatic forward, quasistatic reverse, dynamic forward, dynamic reverse.
   *
   * @param pauseSeconds seconds to pause between tests
   * @return command that runs the full SysId routine
   */
  public Command fullRoutine(double pauseSeconds) {
    return Commands.sequence(
        quasistaticForward().withTimeout(10),
        Commands.waitSeconds(pauseSeconds),
        quasistaticReverse().withTimeout(10),
        Commands.waitSeconds(pauseSeconds),
        dynamicForward().withTimeout(10),
        Commands.waitSeconds(pauseSeconds),
        dynamicReverse().withTimeout(10));
  }

  /** Convenience overload with 1-second pauses between tests. */
  public Command fullRoutine() {
    return fullRoutine(1.0);
  }

  /**
   * Returns a command that runs the full SysId routine with on-robot analysis.
   * Collects data in-memory during tests, then runs WPILib-matching OLS regression
   * and publishes results to the Elastic dashboard.
   */
  public Command fullRoutineWithAnalysis() {
    return fullRoutineWithAnalysis(1.0);
  }

  /**
   * Returns a command that runs the full SysId routine with on-robot analysis.
   *
   * @param pauseSeconds seconds to pause between tests
   * @return command that runs tests then analyzes
   */
  public Command fullRoutineWithAnalysis(double pauseSeconds) {
    return Commands.sequence(
        Commands.runOnce(this::startCollection),
        fullRoutine(pauseSeconds),
        Commands.runOnce(this::stopCollectionAndAnalyze))
        .finallyDo(interrupted -> {
          m_collecting = false;
          m_motor.stop();
          if (interrupted) {
            publishStatus("Cancelled");
          }
        });
  }

  public void setDashboardBridge(SysIdDashboardBridge dashboardBridge) {
    m_dashboardBridge = dashboardBridge == null ? SysIdDashboardBridge.NO_OP : dashboardBridge;
  }

  /** Starts in-memory data collection. Clears any previous data. */
  private void startCollection() {
    m_runCount++;
    m_collectedData.clear();
    m_collecting = true;
    m_lastResult = null;
    publishStatus("Collecting... (run #" + m_runCount + ")");
    System.out.println("[SysId:" + m_motorName + "] Run #" + m_runCount + " started");
  }

  /** Stops collection and runs analysis using SysIdParams from dashboard. */
  private void stopCollectionAndAnalyze() {
    m_collecting = false;
    int pointCount = m_collectedData.size();
    publishStatus("Analyzing " + pointCount + " points... (run #" + m_runCount + ")");
    System.out.println("[SysId:" + m_motorName + "] Run #" + m_runCount
        + " stopped, collected " + pointCount + " data points");

    SysIdParams sysIdParams = m_dashboardBridge.readParams(m_preset);

    SysIdAnalyzer analyzer = new SysIdAnalyzer(m_mechanismType, sysIdParams,
        m_dcMotor, m_gearRatio, m_distPerRot, m_armLength);
    m_lastResult = analyzer.analyze(m_collectedData);

    if (m_lastResult.valid) {
      publishStatus("Run #" + m_runCount + " Complete! (" + m_lastResult.sampleCount
          + " samples, R²accel=" + String.format("%.4f", m_lastResult.rSquaredAccel)
          + " R²sim=" + String.format("%.4f", m_lastResult.rSquaredSimVel) + ")");
      publishResults(m_lastResult);
      System.out.printf("[SysId:%s] Run #%d: kS=%.6f kV=%.6f kA=%.6f kG=%.6f adjR²=%.6f simR²=%.6f n=%d%n",
          m_motorName, m_runCount, m_lastResult.kS, m_lastResult.kV,
          m_lastResult.kA, m_lastResult.kG, m_lastResult.rSquaredAccel,
          m_lastResult.rSquaredSimVel, m_lastResult.sampleCount);
    } else {
      publishStatus("Run #" + m_runCount + " Error: " + m_lastResult.errorMessage);
      System.out.println("[SysId:" + m_motorName + "] Run #" + m_runCount
          + " FAILED: " + m_lastResult.errorMessage);
    }
  }

  /** Publishes analysis results through the configured UI bridge. */
  private void publishResults(SysIdAnalyzer.AnalysisResult result) {
    m_dashboardBridge.publishResult(result);

    System.out.printf("[SysId:%s] Feedback: kP_vel=%.6f kP_pos=%.6f kD_pos=%.6f (factor=%.4f)%n",
        m_motorName, result.kP_velocity, result.kP_position, result.kD_position,
        m_preset.outputConversionFactor);
  }

  private static double round6(double value) {
    return Math.round(value * 1e6) / 1e6;
  }

  /** Publishes status through the configured UI bridge. */
  private void publishStatus(String status) {
    m_dashboardBridge.publishStatus(status);
  }

  /** Returns the last analysis result, or null if analysis hasn't been run. */
  public SysIdAnalyzer.AnalysisResult getLastResult() {
    return m_lastResult;
  }

  /** Returns the mechanism type used for analysis. */
  public MechanismType getMechanismType() {
    return m_mechanismType;
  }

  /** Returns the controller preset used for LQR computation. */
  public ControllerPreset getControllerPreset() {
    return m_preset;
  }
}
