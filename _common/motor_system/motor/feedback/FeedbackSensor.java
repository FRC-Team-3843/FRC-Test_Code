package frc.robot.motor.feedback;

public interface FeedbackSensor extends AutoCloseable {
  double getPositionRotations();

  double getVelocityRps();

  boolean isConnected();

  default void reset() {}

  @Override
  void close();
}
