package frc.lib.motor.feedback;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.AnalogEncoder;
import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import frc.lib.motor.config.MotorConfiguration;

public final class FeedbackSensorFactory {
  private FeedbackSensorFactory() {}

  public static FeedbackSensor create(MotorConfiguration config) {
    switch (config.feedbackSource) {
      case QUADRATURE_DIO:
        return new QuadratureFeedbackSensor(config);
      case DUTY_CYCLE:
        return new DutyCycleFeedbackSensor(config);
      case ANALOG:
        return new AnalogFeedbackSensor(config);
      default:
        return null;
    }
  }

  private abstract static class DerivedVelocitySensor implements FeedbackSensor {
    private final double m_fullRangeRotations;
    private final boolean m_continuousWrap;
    private final boolean m_inverted;
    private boolean m_initialized = false;
    private double m_lastTimestamp;
    private double m_lastRawPosition;
    private double m_unwrappedPosition;
    private double m_velocityRps;

    DerivedVelocitySensor(double fullRangeRotations, boolean continuousWrap, boolean inverted) {
      m_fullRangeRotations = fullRangeRotations;
      m_continuousWrap = continuousWrap;
      m_inverted = inverted;
    }

    protected abstract double readRawPositionRotations();

    protected abstract boolean sensorConnected();

    @Override
    public final double getPositionRotations() {
      updateState();
      return m_continuousWrap ? m_unwrappedPosition : m_lastRawPosition;
    }

    @Override
    public final double getVelocityRps() {
      updateState();
      return m_velocityRps;
    }

    @Override
    public final boolean isConnected() {
      return sensorConnected();
    }

    @Override
    public void reset() {
      m_initialized = false;
      m_velocityRps = 0.0;
    }

    private void updateState() {
      double timestamp = Timer.getFPGATimestamp();
      double rawPosition = readRawPositionRotations();
      if (m_inverted) {
        rawPosition = -rawPosition;
      }

      if (!m_initialized) {
        m_initialized = true;
        m_lastTimestamp = timestamp;
        m_lastRawPosition = rawPosition;
        m_unwrappedPosition = rawPosition;
        m_velocityRps = 0.0;
        return;
      }

      double delta = rawPosition - m_lastRawPosition;
      if (m_continuousWrap && m_fullRangeRotations > 0.0) {
        double halfRange = m_fullRangeRotations / 2.0;
        if (delta > halfRange) {
          delta -= m_fullRangeRotations;
        } else if (delta < -halfRange) {
          delta += m_fullRangeRotations;
        }
      }

      double dt = Math.max(1e-3, timestamp - m_lastTimestamp);
      m_unwrappedPosition += delta;
      m_velocityRps = MathUtil.clamp(delta / dt, -1e5, 1e5);
      m_lastRawPosition = rawPosition;
      m_lastTimestamp = timestamp;
    }
  }

  private static final class QuadratureFeedbackSensor implements FeedbackSensor {
    private final Encoder m_encoder;

    QuadratureFeedbackSensor(MotorConfiguration config) {
      m_encoder = new Encoder(config.quadratureChannelA, config.quadratureChannelB, config.feedbackInverted);
      m_encoder.setDistancePerPulse(config.feedbackDistancePerPulseRotations);
      m_encoder.setSamplesToAverage(Math.max(1, config.feedbackSamplesToAverage));
    }

    @Override
    public double getPositionRotations() {
      return m_encoder.getDistance();
    }

    @Override
    public double getVelocityRps() {
      return m_encoder.getRate();
    }

    @Override
    public boolean isConnected() {
      return true;
    }

    @Override
    public void reset() {
      m_encoder.reset();
    }

    @Override
    public void close() {
      m_encoder.close();
    }
  }

  private static final class DutyCycleFeedbackSensor extends DerivedVelocitySensor {
    private final DutyCycleEncoder m_encoder;

    DutyCycleFeedbackSensor(MotorConfiguration config) {
      super(config.feedbackFullRangeRotations, config.feedbackContinuousWrap, config.feedbackInverted);
      m_encoder = new DutyCycleEncoder(
          config.dutyCycleChannel,
          config.feedbackFullRangeRotations,
          config.feedbackOffsetRotations);
    }

    @Override
    protected double readRawPositionRotations() {
      return m_encoder.get();
    }

    @Override
    protected boolean sensorConnected() {
      return m_encoder.isConnected();
    }

    @Override
    public void close() {
      m_encoder.close();
    }
  }

  private static final class AnalogFeedbackSensor extends DerivedVelocitySensor {
    private final AnalogEncoder m_encoder;

    AnalogFeedbackSensor(MotorConfiguration config) {
      super(config.feedbackFullRangeRotations, config.feedbackContinuousWrap, config.feedbackInverted);
      m_encoder = new AnalogEncoder(
          config.analogChannel,
          config.feedbackFullRangeRotations,
          config.feedbackOffsetRotations);
    }

    @Override
    protected double readRawPositionRotations() {
      return m_encoder.get();
    }

    @Override
    protected boolean sensorConnected() {
      return true;
    }

    @Override
    public void close() {
      m_encoder.close();
    }
  }
}
