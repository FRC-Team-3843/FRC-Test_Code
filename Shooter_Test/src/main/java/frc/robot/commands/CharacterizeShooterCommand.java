package frc.robot.commands;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.shooter.ShooterSubsystem;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to automatically characterize a shooter motor and calculate optimal PID values.
 *
 * This command runs through three phases:
 * 1. Find kS (static friction) - minimum voltage to overcome friction
 * 2. Find kV (velocity feedforward) - volts per RPS relationship
 * 3. Calculate kP (proportional gain) - based on step response
 *
 * For flywheel velocity control, the priority is:
 * - kV does 90% of the work (feedforward)
 * - kS compensates for static friction
 * - kP handles remaining error (usually small, ~0.1-0.5)
 * - kI and kD are typically NOT needed for velocity control
 */
public class CharacterizeShooterCommand extends Command {
  private enum Phase {
    INIT,
    FIND_KS,
    FIND_KV,
    CALCULATE_KP,
    COMPLETE
  }

  private final ShooterSubsystem m_shooter;
  private final boolean m_usePreshooter;

  private Phase m_phase = Phase.INIT;
  private final Timer m_timer = new Timer();

  // Phase 1: Find kS
  private double m_currentVoltage = 0.0;
  private double m_calculatedKs = 0.0;
  private static final double KS_VOLTAGE_INCREMENT = 0.05; // Volts per step
  private static final double KS_STEP_DURATION = 0.1; // Seconds between steps
  private static final double KS_VELOCITY_THRESHOLD = 10.0 / 60.0; // 10 RPM in RPS

  // Phase 2: Find kV
  private final List<Double> m_kvSamples = new ArrayList<>();
  private int m_kvTestStep = 0;
  private static final double[] KV_TEST_VOLTAGES = {3.0, 6.0, 9.0};
  private static final double KV_SETTLE_TIME = 2.0; // Seconds to reach steady state
  private double m_calculatedKv = 0.0;

  // Phase 3: Calculate kP
  private static final double KP_TEST_RPM = 3000.0;
  private static final double KP_TEST_DURATION = 3.0; // Seconds
  private double m_steadyStateError = 0.0;
  private double m_calculatedKp = 0.1; // Conservative starting point

  /**
   * Creates a characterization command for the shooter.
   *
   * @param shooter The shooter subsystem
   * @param usePreshooter True to characterize preshooter, false for main shooter
   */
  public CharacterizeShooterCommand(ShooterSubsystem shooter, boolean usePreshooter) {
    m_shooter = shooter;
    m_usePreshooter = usePreshooter;
    addRequirements(shooter);
  }

  @Override
  public void initialize() {
    m_phase = Phase.INIT;
    m_timer.reset();
    m_timer.start();

    String motorName = m_usePreshooter ? "Preshooter" : "MainShooter";
    System.out.println("=== Starting Characterization: " + motorName + " ===");
    SmartDashboard.putString("Shooter/Tuning/Status", "Initializing...");
    SmartDashboard.putNumber("Shooter/Tuning/Phase", 0);
  }

  @Override
  public void execute() {
    switch (m_phase) {
      case INIT:
        executeInit();
        break;
      case FIND_KS:
        executeFindKs();
        break;
      case FIND_KV:
        executeFindKv();
        break;
      case CALCULATE_KP:
        executeCalculateKp();
        break;
      case COMPLETE:
        // Do nothing, wait for end
        break;
    }
  }

  private void executeInit() {
    // Brief pause before starting
    if (m_timer.hasElapsed(0.5)) {
      m_phase = Phase.FIND_KS;
      m_timer.reset();
      System.out.println("Phase 1: Finding kS (static friction)...");
      SmartDashboard.putString("Shooter/Tuning/Status", "Phase 1: Finding kS");
      SmartDashboard.putNumber("Shooter/Tuning/Phase", 1);
    }
  }

  private void executeFindKs() {
    // Increment voltage every KS_STEP_DURATION seconds
    if (m_timer.hasElapsed(KS_STEP_DURATION)) {
      m_currentVoltage += KS_VOLTAGE_INCREMENT;
      m_timer.reset();
    }

    // Apply voltage
    setMotorVoltage(m_currentVoltage);

    // Check if motor has started moving
    double velocityRps = getMotorVelocityRps();
    if (Math.abs(velocityRps) > KS_VELOCITY_THRESHOLD) {
      m_calculatedKs = m_currentVoltage - KS_VOLTAGE_INCREMENT; // Use previous voltage
      System.out.println("Found kS = " + String.format("%.4f", m_calculatedKs) + " V");
      SmartDashboard.putNumber("Shooter/Tuning/kS", m_calculatedKs);

      // Move to next phase
      stopMotor();
      m_phase = Phase.FIND_KV;
      m_timer.reset();
      m_kvTestStep = 0;
      System.out.println("Phase 2: Finding kV (velocity feedforward)...");
      SmartDashboard.putString("Shooter/Tuning/Status", "Phase 2: Finding kV");
      SmartDashboard.putNumber("Shooter/Tuning/Phase", 2);
    }

    // Safety timeout
    if (m_currentVoltage > 2.0) {
      System.out.println("WARNING: kS not found within 2V, using 0.5V as default");
      m_calculatedKs = 0.5;
      SmartDashboard.putNumber("Shooter/Tuning/kS", m_calculatedKs);
      stopMotor();
      m_phase = Phase.FIND_KV;
      m_timer.reset();
      m_kvTestStep = 0;
      SmartDashboard.putString("Shooter/Tuning/Status", "Phase 2: Finding kV");
      SmartDashboard.putNumber("Shooter/Tuning/Phase", 2);
    }
  }

  private void executeFindKv() {
    if (m_kvTestStep >= KV_TEST_VOLTAGES.length) {
      // Calculate average kV
      m_calculatedKv = m_kvSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0.12);
      System.out.println("Calculated kV = " + String.format("%.4f", m_calculatedKv) + " V/RPS");
      SmartDashboard.putNumber("Shooter/Tuning/kV", m_calculatedKv);

      // Move to next phase
      stopMotor();
      m_phase = Phase.CALCULATE_KP;
      m_timer.reset();
      System.out.println("Phase 3: Calculating kP from step response...");
      SmartDashboard.putString("Shooter/Tuning/Status", "Phase 3: Calculating kP");
      SmartDashboard.putNumber("Shooter/Tuning/Phase", 3);
      return;
    }

    double testVoltage = KV_TEST_VOLTAGES[m_kvTestStep];
    setMotorVoltage(testVoltage);

    // Wait for steady state
    if (m_timer.hasElapsed(KV_SETTLE_TIME)) {
      double velocityRps = getMotorVelocityRps();
      double kv = testVoltage / velocityRps;
      m_kvSamples.add(kv);

      System.out.println("  Test " + (m_kvTestStep + 1) + ": " +
                         String.format("%.1f", testVoltage) + "V -> " +
                         String.format("%.2f", velocityRps) + " RPS -> kV = " +
                         String.format("%.4f", kv));

      m_kvTestStep++;
      m_timer.reset();
    }
  }

  private void executeCalculateKp() {
    // Apply feedforward-only control to target RPM
    double targetRps = KP_TEST_RPM / 60.0;
    double feedforwardVoltage = m_calculatedKs + (m_calculatedKv * targetRps);
    setMotorVoltage(feedforwardVoltage);

    // After settling, measure steady-state error
    if (m_timer.hasElapsed(KP_TEST_DURATION)) {
      double actualRps = getMotorVelocityRps();
      m_steadyStateError = targetRps - actualRps;

      // Calculate kP: start conservative
      // For velocity control, kP is usually small (0.1 - 0.5)
      // because feedforward does most of the work
      m_calculatedKp = 0.1; // Conservative starting point

      System.out.println("Feedforward test: Target = " +
                         String.format("%.2f", targetRps) + " RPS, Actual = " +
                         String.format("%.2f", actualRps) + " RPS");
      System.out.println("Steady-state error: " +
                         String.format("%.2f", m_steadyStateError * 60.0) + " RPM");
      System.out.println("Recommended kP = " + String.format("%.4f", m_calculatedKp));
      SmartDashboard.putNumber("Shooter/Tuning/kP", m_calculatedKp);
      SmartDashboard.putNumber("Shooter/Tuning/SteadyStateError_RPM", m_steadyStateError * 60.0);

      // Complete
      stopMotor();
      m_phase = Phase.COMPLETE;
      printResults();
      SmartDashboard.putString("Shooter/Tuning/Status", "Complete!");
      SmartDashboard.putNumber("Shooter/Tuning/Phase", 4);
    }
  }

  private void printResults() {
    String motorName = m_usePreshooter ? "Preshooter" : "MainShooter";
    System.out.println("\n=== Characterization Results: " + motorName + " ===");
    System.out.println("kS (static friction): " + String.format("%.4f", m_calculatedKs) + " V");
    System.out.println("kV (velocity FF):     " + String.format("%.4f", m_calculatedKv) + " V/RPS");
    System.out.println("kP (proportional):    " + String.format("%.4f", m_calculatedKp));
    System.out.println("kI (integral):        0.0000 (not needed for velocity)");
    System.out.println("kD (derivative):      0.0000 (not needed for velocity)");
    System.out.println("\nCopy these values to shooter-config.json");
    System.out.println("Then test with A/B buttons and fine-tune kP if needed.");
    System.out.println("=========================================\n");
  }

  @Override
  public void end(boolean interrupted) {
    stopMotor();
    if (interrupted) {
      System.out.println("Characterization interrupted!");
      SmartDashboard.putString("Shooter/Tuning/Status", "Interrupted");
    }
  }

  @Override
  public boolean isFinished() {
    return m_phase == Phase.COMPLETE;
  }

  // Helper methods to work with either motor
  private void setMotorVoltage(double volts) {
    if (m_usePreshooter) {
      m_shooter.setPreshooterVoltage(volts);
    } else {
      m_shooter.setMainShooterVoltage(volts);
    }
  }

  private double getMotorVelocityRps() {
    return m_usePreshooter ?
        m_shooter.getPreshooterVelocityRps() :
        m_shooter.getMainShooterVelocityRps();
  }

  private void stopMotor() {
    m_shooter.stop();
  }
}
