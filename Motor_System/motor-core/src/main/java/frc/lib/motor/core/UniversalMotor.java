package frc.lib.motor.core;

public interface UniversalMotor {
  enum Mode {
    DUTY_CYCLE,
    VOLTAGE,
    VELOCITY,
    POSITION,
    CURRENT,
    SMART_MOTION
  }

  void setControlMode(Mode mode);

  // _common methods
  void setVoltage(double volts);
  void setVelocityRps(double rps);
  void setPositionRotations(double rotations);
  double getVelocityRps();
  double getPositionRotations();
  default double getVelocity() { return getVelocityRps(); }
  default double getPosition() { return getPositionRotations(); }
  void setBrake(boolean brake);
  void stop();
  void close(); // Dispose of hardware resources

  // Motor_Test specific methods
  Mode getControlMode();
  void set(double value); // Generic set based on mode?
  double getCurrent();
  double getAppliedVoltage();
  double getTemperature();
  default boolean isFeedbackConnected() { return true; }
  double getHealthScore();
  void setHealthScore(double score);
  String getDeviceName();
  boolean isServo();

  // PID hot-reload support
  default void updatePidConfig(double kP, double kI, double kD, double kV, double kS) {
    // Default no-op implementation for motors that don't support PID tuning
  }

  default void updatePidConfig(double kP, double kI, double kD, double kV, double kS,
                                double kA, double kG) {
    updatePidConfig(kP, kI, kD, kV, kS);
  }

  // Position PID hot-reload (Slot1)
  default void updatePositionPid(double kP, double kD, double kV, double kS,
                                  double kA, double kG) {
    // Default no-op for motors that don't support position PID
  }

  // Motion profiling support
  default void configureMotionProfile(double cruiseVelocityRps, double accelerationRps2,
                                       double jerkRps3) {
    // Default no-op
  }

  default void updateCurrentLimit(double currentLimitAmps) {
    // Default no-op
  }

  default void updateSoftLimits(double forwardLimitRotations, double reverseLimitRotations) {
    // Default no-op
  }

  default void setProfiledPosition(double rotations) {
    setPositionRotations(rotations);
  }

  default boolean isProfileComplete() {
    return true;
  }

  default void periodic() {}
}
