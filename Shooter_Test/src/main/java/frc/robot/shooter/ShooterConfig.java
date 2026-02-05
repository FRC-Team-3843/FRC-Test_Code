package frc.robot.shooter;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration class for shooter system loaded from JSON.
 */
public class ShooterConfig {
  @JsonProperty("preshooterCanId")
  public int preshooterCanId = 20;

  @JsonProperty("preshooterInverted")
  public boolean preshooterInverted = false;

  @JsonProperty("preshooterGearRatio")
  public double preshooterGearRatio = 1.0;

  @JsonProperty("preshooterKp")
  public double preshooterKp = 0.1;

  @JsonProperty("preshooterKi")
  public double preshooterKi = 0.0;

  @JsonProperty("preshooterKd")
  public double preshooterKd = 0.0;

  @JsonProperty("preshooterKv")
  public double preshooterKv = 0.12;

  @JsonProperty("preshooterKs")
  public double preshooterKs = 0.0;

  @JsonProperty("mainShooterCanId")
  public int mainShooterCanId = 21;

  @JsonProperty("mainShooterInverted")
  public boolean mainShooterInverted = false;

  @JsonProperty("mainShooterGearRatio")
  public double mainShooterGearRatio = 1.0;

  @JsonProperty("mainShooterKp")
  public double mainShooterKp = 0.1;

  @JsonProperty("mainShooterKi")
  public double mainShooterKi = 0.0;

  @JsonProperty("mainShooterKd")
  public double mainShooterKd = 0.0;

  @JsonProperty("mainShooterKv")
  public double mainShooterKv = 0.12;

  @JsonProperty("mainShooterKs")
  public double mainShooterKs = 0.0;

  @JsonProperty("servoPwmChannel")
  public int servoPwmChannel = 0;

  @JsonProperty("servoPosition1")
  public double servoPosition1 = 0.5;

  @JsonProperty("servoPosition2")
  public double servoPosition2 = 1.0;

  @JsonProperty("preshooterSetpoint1Rpm")
  public double preshooterSetpoint1Rpm = 6500.0;

  @JsonProperty("preshooterSetpoint2Rpm")
  public double preshooterSetpoint2Rpm = 3250.0;

  @JsonProperty("mainShooterSetpoint1Rpm")
  public double mainShooterSetpoint1Rpm = 5500.0;

  @JsonProperty("mainShooterSetpoint2Rpm")
  public double mainShooterSetpoint2Rpm = 2750.0;

  // Default constructor for Jackson
  public ShooterConfig() {}
}
