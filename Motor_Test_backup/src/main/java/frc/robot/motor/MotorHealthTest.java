package frc.robot.motor;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;

public class MotorHealthTest extends Command {
  private enum Phase {
    BREAKAWAY,
    STARTUP,
    STEADY_STATE,
    COAST_DOWN,
    SERVO_HOME,
    SERVO_SWEEP,
    TEMP_MONITOR,
    DONE
  }

  private final MotorTestSubsystem subsystem;
  private UniversalMotor motor;
  private MotorConfiguration configuration;
  private final Timer timer = new Timer();

  private Phase phase = Phase.STARTUP;
  private double startupPeakCurrent = 0.0;
  private double steadySumRps = 0.0;
  private double steadySumCurrent = 0.0;
  private double steadySumTemp = 0.0;
  private double steadyMaxRps = 0.0;
  private double steadyMaxCurrent = 0.0;
  private double postTestPeakTemp = 0.0;
  private double breakawayValue = 0.0;
  private double resistanceOhms = 0.0;
  private double kvRating = 0.0;
  private double breakawayRamp = 0.0;
  private int steadySamples = 0;
  private double coastDownSeconds = 0.0;
  private double servoSlewSeconds = 0.0;
  private double coastStartTime = 0.0;

  private final DoubleLogEntry logVelocity;
  private final DoubleLogEntry logCurrent;
  private final DoubleLogEntry logTemp;

  public MotorHealthTest(MotorTestSubsystem subsystem) {
    this.subsystem = subsystem;
    DataLog log = DataLogManager.getLog();
    logVelocity = new DoubleLogEntry(log, "/motorHealth/velocityRps");
    logCurrent = new DoubleLogEntry(log, "/motorHealth/currentA");
    logTemp = new DoubleLogEntry(log, "/motorHealth/tempC");
    addRequirements(subsystem);
  }

  @Override
  public void initialize() {
    motor = subsystem.getMotor();
    configuration = subsystem.getConfiguration();
    timer.reset();
    timer.start();
    startupPeakCurrent = 0.0;
    steadySumRps = 0.0;
    steadySumCurrent = 0.0;
    steadySumTemp = 0.0;
    steadyMaxRps = 0.0;
    steadyMaxCurrent = 0.0;
    postTestPeakTemp = 0.0;
    breakawayValue = 0.0;
    resistanceOhms = 0.0;
    kvRating = 0.0;
    breakawayRamp = 0.0;
    steadySamples = 0;
    coastDownSeconds = 0.0;
    servoSlewSeconds = 0.0;
    coastStartTime = 0.0;
    phase = motor != null && motor.isServo() ? Phase.SERVO_HOME : Phase.BREAKAWAY;
  }

  @Override
  public void execute() {
    if (motor == null) {
      phase = Phase.DONE;
      return;
    }
    if (!subsystem.isEnabled()) {
      motor.stop();
      return;
    }

    logTelemetry();

    switch (phase) {
      case BREAKAWAY:
        motor.setControlMode(UniversalMotor.Mode.DUTY_CYCLE);
        breakawayRamp += 0.25 * 0.02 / 3.0; // Ramp to 0.25 over 3s (assuming 20ms loop)
        motor.set(breakawayRamp);
        if (Math.abs(motor.getVelocity()) > 0.5 || breakawayRamp >= 0.25) {
            if (Math.abs(motor.getVelocity()) > 0.5 && breakawayValue == 0.0) {
                breakawayValue = breakawayRamp;
            }
            if (timer.hasElapsed(3.0) || breakawayValue > 0.0) {
                motor.stop();
                timer.reset();
                phase = Phase.STARTUP;
            }
        }
        break;
      case STARTUP:
        motor.setControlMode(UniversalMotor.Mode.DUTY_CYCLE);
        motor.set(0.2);
        double current = motor.getCurrent();
        startupPeakCurrent = Math.max(startupPeakCurrent, current);
        
        // Estimate Resistance (R = V/I) during early startup
        // Only valid if not moving much yet and we have current
        if (timer.get() < 0.1 && current > 1.0) {
            double busVoltage = 12.0; // Assume 12V or read from RobotController if available
            double appliedVolts = busVoltage * 0.2;
            resistanceOhms = appliedVolts / current;
        }

        if (timer.hasElapsed(0.6)) {
          timer.reset();
          phase = Phase.STEADY_STATE;
        }
        break;
      case STEADY_STATE:
        motor.setControlMode(UniversalMotor.Mode.DUTY_CYCLE);
        motor.set(1.0);
        sampleSteadyState();
        if (timer.hasElapsed(10.0)) {
          motor.stop();
          timer.reset();
          coastStartTime = Timer.getFPGATimestamp();
          phase = Phase.COAST_DOWN;
        }
        break;
      case COAST_DOWN:
        motor.stop();
        double rps = Math.abs(motor.getVelocity());
        if ((rps < 0.1 || timer.hasElapsed(15.0)) && coastDownSeconds <= 0.0) {
          coastDownSeconds = Timer.getFPGATimestamp() - coastStartTime;
          phase = Phase.TEMP_MONITOR;
          timer.reset();
          System.out.println("Coast complete. Monitoring temp for 45s (Release button to finish early)...");
        }
        break;
      case SERVO_HOME:
        motor.setControlMode(UniversalMotor.Mode.POSITION);
        motor.set(0.0);
        if (timer.hasElapsed(0.3)) {
          timer.reset();
          phase = Phase.SERVO_SWEEP;
        }
        break;
      case SERVO_SWEEP:
        motor.setControlMode(UniversalMotor.Mode.POSITION);
        motor.set(180.0);
        if (motor.getPosition() >= 175.0 || timer.hasElapsed(5.0)) {
          servoSlewSeconds = timer.get();
          phase = Phase.TEMP_MONITOR;
          timer.reset();
          System.out.println("Servo sweep complete. Monitoring temp for 45s (Release button to finish early)...");
        }
        break;
      case TEMP_MONITOR:
        motor.stop();
        double temp = motor.getTemperature();
        if (Double.isFinite(temp)) {
          postTestPeakTemp = Math.max(postTestPeakTemp, temp);
        }
        if (timer.hasElapsed(45.0)) {
          phase = Phase.DONE;
        }
        break;
      case DONE:
      default:
        break;
    }
  }

  @Override
  public boolean isFinished() {
    return phase == Phase.DONE;
  }

  @Override
  public void end(boolean interrupted) {
    if (motor != null) {
      motor.stop();
    }
    // Only skip report if interrupted BEFORE we finished the main mechanical tests
    if (interrupted && phase != Phase.TEMP_MONITOR && phase != Phase.DONE) {
      return;
    }
    MotorHealthReport report = buildReport();
    if (report != null) {
      subsystem.setLastReport(report);
      System.out.println(report.generateAiReport());
    }
  }

  private void sampleSteadyState() {
    double rps = motor.getVelocity();
    double current = motor.getCurrent();
    double temp = motor.getTemperature();
    steadySamples++;
    steadySumRps += rps;
    steadySumCurrent += current;
    steadySumTemp += Double.isFinite(temp) ? temp : 0.0;
    steadyMaxRps = Math.max(steadyMaxRps, rps);
    steadyMaxCurrent = Math.max(steadyMaxCurrent, current);
  }

  private void logTelemetry() {
    logVelocity.append(motor.getVelocity());
    logCurrent.append(motor.getCurrent());
    logTemp.append(motor.getTemperature());
  }

  private MotorHealthReport buildReport() {
    if (motor == null) {
      return null;
    }
    double avgRps = steadySamples > 0 ? steadySumRps / steadySamples : 0.0;
    double avgCurrent = steadySamples > 0 ? steadySumCurrent / steadySamples : 0.0;
    double avgTemp = steadySamples > 0 ? steadySumTemp / steadySamples : Double.NaN;
    
    // Calculate Kv (RPM/Volt)
    // steadyState was at 1.0 duty cycle. Assuming ~12V.
    double estVolts = 12.0; 
    kvRating = estVolts > 0 ? (avgRps * 60.0) / estVolts : 0.0;

    double healthScore;
    String grade;
    if (motor.isServo()) {
      double slew = servoSlewSeconds <= 0.0 ? 5.0 : servoSlewSeconds;
      healthScore = Math.max(0.0, 100.0 - (slew * 20.0));
      grade = gradeFromServoSlew(slew);
    } else {
      double expected = MotorSpecs.expectedFreeSpeedRps(configuration.motorKind, configuration.gearRatio);
      double expectedCurrent = MotorSpecs.expectedFreeCurrent(configuration.motorKind);
      
      // 1. Speed Ratio (Target 1.0)
      double speedRatio = expected > 0.0 ? (steadyMaxRps / expected) : 0.0;
      speedRatio = Math.min(speedRatio, 1.2);
      healthScore = speedRatio * 100.0;

      // 2. Current Check (Demagnetization or Friction)
      if (avgCurrent > 0.0) {
        // High current at no load suggests friction or short
        if (expectedCurrent > 0.0 && avgCurrent > expectedCurrent * 2.0) {
            healthScore -= 15.0;
        }
        // Very high startup current check
        if (startupPeakCurrent > avgCurrent * 5.0) { 
             // Normal for DC motors to have high inrush, but excessive might be short
        }
      }

      // 3. Breakaway Friction Check
      if (breakawayValue > 0.15) { // Needs >15% power to start moving -> Friction
          healthScore -= (breakawayValue - 0.15) * 200.0;
      }
      
      // 4. Resistance Check (Weak connection)
      // Hard to grade without baseline, but extremely high R is bad.
      
      // 5. Temperature Checks
      if (Double.isFinite(avgTemp) && avgTemp > 70.0) {
        healthScore -= (avgTemp - 70.0) * 0.5;
      }
      if (postTestPeakTemp > 70.0) {
        healthScore -= (postTestPeakTemp - 70.0) * 1.0; 
      }
      
      healthScore = Math.max(0.0, Math.min(healthScore, 100.0));
      grade = gradeFromScore(healthScore);
    }

    return new MotorHealthReport(
        configuration.motorKind,
        configuration.controllerType,
        configuration.gearRatio,
        startupPeakCurrent,
        avgRps,
        steadyMaxRps,
        avgCurrent,
        steadyMaxCurrent,
        avgTemp,
        postTestPeakTemp,
        breakawayValue,
        resistanceOhms,
        kvRating,
        coastDownSeconds,
        servoSlewSeconds,
        healthScore,
        grade);
  }

  private static String gradeFromScore(double score) {
    if (score >= 90.0) {
      return "A";
    }
    if (score >= 80.0) {
      return "B";
    }
    if (score >= 70.0) {
      return "C";
    }
    if (score >= 60.0) {
      return "D";
    }
    return "F";
  }

  private static String gradeFromServoSlew(double seconds) {
    if (seconds <= 0.5) {
      return "A";
    }
    if (seconds <= 0.8) {
      return "B";
    }
    if (seconds <= 1.2) {
      return "C";
    }
    if (seconds <= 1.6) {
      return "D";
    }
    return "F";
  }

}
