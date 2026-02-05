package frc.robot.motor;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.util.function.BooleanSupplier;

public class MotorTestSubsystem extends SubsystemBase {
  private UniversalMotor motor;
  private MotorConfiguration configuration;
  private MotorHealthReport lastReport;
  private BooleanSupplier enableSupplier = () -> false;

  public MotorTestSubsystem(MotorConfiguration configuration) {
    rebuildMotor(configuration);
  }

  public void setEnableSupplier(BooleanSupplier supplier) {
    enableSupplier = supplier == null ? () -> false : supplier;
  }

  public void rebuildMotor(MotorConfiguration newConfig) {
    // Close old motor first to release hardware resources
    if (motor != null) {
      try {
        motor.close();
      } catch (Exception ex) {
        DriverStation.reportError("Failed to close old motor: " + ex.getMessage(), ex.getStackTrace());
      }
      motor = null;
    }

    configuration = newConfig;
    try {
      motor = MotorFactory.create(newConfig);
    } catch (RuntimeException ex) {
      DriverStation.reportError("Failed to create motor: " + ex.getMessage(), ex.getStackTrace());
      motor = null;
    }
  }

  public UniversalMotor getMotor() {
    return motor;
  }

  public MotorConfiguration getConfiguration() {
    return configuration;
  }

  public void applySetpoint(UniversalMotor.Mode mode, double setpoint) {
    if (motor == null) {
      return;
    }
    motor.setControlMode(mode);
    if (enableSupplier.getAsBoolean()) {
      motor.set(setpoint);
    } else {
      motor.stop();
    }
  }

  public boolean isEnabled() {
    return enableSupplier.getAsBoolean();
  }

  public void stopMotor() {
    if (motor != null) {
      motor.stop();
    }
  }

  public void setLastReport(MotorHealthReport report) {
    lastReport = report;
    if (motor != null) {
      motor.setHealthScore(report.healthScore);
    }
  }

  public MotorHealthReport getLastReport() {
    return lastReport;
  }
}
