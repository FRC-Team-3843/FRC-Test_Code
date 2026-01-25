package frc.robot.motor;

import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;

public class MotorHealthTest extends Command {
  private enum Phase {
    STARTUP,
    STEADY_STATE,
    COAST_DOWN,
    SERVO_HOME,
    SERVO_SWEEP,
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
    steadySamples = 0;
    coastDownSeconds = 0.0;
    servoSlewSeconds = 0.0;
    coastStartTime = 0.0;
    phase = motor != null && motor.isServo() ? Phase.SERVO_HOME : Phase.STARTUP;
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
      case STARTUP:
        motor.setControlMode(UniversalMotor.Mode.DUTY_CYCLE);
        motor.set(0.2);
        startupPeakCurrent = Math.max(startupPeakCurrent, motor.getCurrent());
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
        if (rps < 0.1 && coastDownSeconds <= 0.0) {
          coastDownSeconds = Timer.getFPGATimestamp() - coastStartTime;
          phase = Phase.DONE;
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
    if (interrupted) {
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

    double healthScore;
    String grade;
    if (motor.isServo()) {
      double slew = servoSlewSeconds <= 0.0 ? 5.0 : servoSlewSeconds;
      healthScore = Math.max(0.0, 100.0 - (slew * 20.0));
      grade = gradeFromServoSlew(slew);
    } else {
      double expected = MotorSpecs.expectedFreeSpeedRps(configuration.motorKind, configuration.gearRatio);
      double ratio = expected > 0.0 ? (steadyMaxRps / expected) : 0.0;
      ratio = Math.min(ratio, 1.2);
      healthScore = ratio * 100.0;
      if (avgCurrent > 0.0 && startupPeakCurrent > avgCurrent * 2.0) {
        healthScore -= 10.0;
      }
      if (Double.isFinite(avgTemp) && avgTemp > 70.0) {
        healthScore -= (avgTemp - 70.0) * 0.5;
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
