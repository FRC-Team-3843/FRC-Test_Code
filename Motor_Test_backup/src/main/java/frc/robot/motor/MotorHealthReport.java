package frc.robot.motor;

import java.time.Instant;
import java.util.Locale;

public class MotorHealthReport {
  public final MotorKind motorKind;
  public final ControllerType controllerType;
  public final double gearRatio;
  public final double peakStartupCurrent;
  public final double steadyStateAvgRps;
  public final double steadyStateMaxRps;
  public final double steadyStateAvgCurrent;
  public final double steadyStateMaxCurrent;
  public final double steadyStateAvgTemp;
  public final double postTestPeakTemp;
  public final double breakawayValue;
  public final double resistanceOhms;
  public final double kvRating;
  public final double coastDownSeconds;
  public final double servoSlewSeconds;
  public final double healthScore;
  public final String grade;

  public MotorHealthReport(
      MotorKind motorKind,
      ControllerType controllerType,
      double gearRatio,
      double peakStartupCurrent,
      double steadyStateAvgRps,
      double steadyStateMaxRps,
      double steadyStateAvgCurrent,
      double steadyStateMaxCurrent,
      double steadyStateAvgTemp,
      double postTestPeakTemp,
      double breakawayValue,
      double resistanceOhms,
      double kvRating,
      double coastDownSeconds,
      double servoSlewSeconds,
      double healthScore,
      String grade) {
    this.motorKind = motorKind;
    this.controllerType = controllerType;
    this.gearRatio = gearRatio;
    this.peakStartupCurrent = peakStartupCurrent;
    this.steadyStateAvgRps = steadyStateAvgRps;
    this.steadyStateMaxRps = steadyStateMaxRps;
    this.steadyStateAvgCurrent = steadyStateAvgCurrent;
    this.steadyStateMaxCurrent = steadyStateMaxCurrent;
    this.steadyStateAvgTemp = steadyStateAvgTemp;
    this.postTestPeakTemp = postTestPeakTemp;
    this.breakawayValue = breakawayValue;
    this.resistanceOhms = resistanceOhms;
    this.kvRating = kvRating;
    this.coastDownSeconds = coastDownSeconds;
    this.servoSlewSeconds = servoSlewSeconds;
    this.healthScore = healthScore;
    this.grade = grade;
  }

  public String generateAiReportJson() {
    return String.format(
        Locale.US,
        "{"
            + "\"timestamp\":\"%s\","
            + "\"controllerType\":\"%s\","
            + "\"motorKind\":\"%s\","
            + "\"gearRatio\":%.3f,"
            + "\"startupPeakCurrent\":%.2f,"
            + "\"steadyStateAvgRps\":%.3f,"
            + "\"steadyStateMaxRps\":%.3f,"
            + "\"steadyStateAvgCurrent\":%.2f,"
            + "\"steadyStateMaxCurrent\":%.2f,"
            + "\"steadyStateAvgTemp\":%.2f,"
            + "\"postTestPeakTemp\":%.2f,"
            + "\"breakawayValue\":%.3f,"
            + "\"resistanceOhms\":%.3f,"
            + "\"kvRating\":%.1f,"
            + "\"coastDownSeconds\":%.3f,"
            + "\"servoSlewSeconds\":%.3f,"
            + "\"healthScore\":%.1f,"
            + "\"grade\":\"%s\""
            + "}",
        Instant.now().toString(),
        controllerType,
        motorKind,
        gearRatio,
        peakStartupCurrent,
        steadyStateAvgRps,
        steadyStateMaxRps,
        steadyStateAvgCurrent,
        steadyStateMaxCurrent,
        steadyStateAvgTemp,
        postTestPeakTemp,
        breakawayValue,
        resistanceOhms,
        kvRating,
        coastDownSeconds,
        servoSlewSeconds,
        healthScore,
        grade);
  }

  public String generateAiReport() {
    return generateAiReportJson();
  }
}
