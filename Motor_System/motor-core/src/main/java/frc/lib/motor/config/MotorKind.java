package frc.lib.motor.config;

import edu.wpi.first.math.system.plant.DCMotor;

public enum MotorKind {
  CIM("CIM", DCMotor.getCIM(1), false, true),
  MINI_CIM("Mini CIM", DCMotor.getMiniCIM(1), false, true),
  BAG("BAG", DCMotor.getBag(1), false, true),
  RS775_PRO("RS775 Pro", DCMotor.getVex775Pro(1), false, true),
  RS775_125("RS775-125", DCMotor.getAndymarkRs775_125(1), false, true),
  ANDYMARK_9015("AndyMark 9015", DCMotor.getAndymark9015(1), false, true),
  BANEBOTS_RS775("Banebots RS775", DCMotor.getBanebotsRs775(1), false, true),
  BANEBOTS_RS550("Banebots RS550", DCMotor.getBanebotsRs550(1), false, true),
  MINION("Minion", DCMotor.getMinion(1), false, false),
  NEO("NEO", DCMotor.getNEO(1), false, false),
  NEO_550("NEO 550", DCMotor.getNeo550(1), false, false),
  NEO_VORTEX("NEO Vortex", DCMotor.getNeoVortex(1), false, false),
  KRAKEN("Kraken X60", DCMotor.getKrakenX60(1), false, false),
  KRAKEN_X44("Kraken X44", DCMotor.getKrakenX44(1), false, false),
  FALCON("Falcon 500", DCMotor.getFalcon500(1), false, false),
  REDLINE_A("RedLine A", null, false, true),
  PULSAR_775("Pulsar 775", null, false, true),
  VENOM("Venom", null, false, false),
  NEVEREST("NeveRest", null, false, true),
  PG("PG", null, false, true),
  AUTOMOTIVE_WINDOW("Automotive Window Motor", null, false, true),
  AUTOMOTIVE_WIPER("Automotive Wiper Motor", null, false, true),
  SNOW_BLOWER("Snow Blower Motor", null, false, true),
  SERVO("Servo", null, true, false),
  CONTINUOUS_SERVO("Continuous Servo", null, true, false),
  CUSTOM_BRUSHED("Custom Brushed", null, false, true),
  CUSTOM_BRUSHLESS("Custom Brushless", null, false, false);

  private final String m_displayName;
  private final DCMotor m_model;
  private final boolean m_servo;
  private final boolean m_brushed;

  MotorKind(String displayName, DCMotor model, boolean servo, boolean brushed) {
    m_displayName = displayName;
    m_model = model;
    m_servo = servo;
    m_brushed = brushed;
  }

  public String displayName() {
    return m_displayName;
  }

  public double getFreeSpeedRpm() {
    return m_model == null ? 0.0 : m_model.freeSpeedRadPerSec * 60.0 / (2.0 * Math.PI);
  }

  public double getFreeCurrent() {
    return m_model == null ? 0.0 : m_model.freeCurrentAmps;
  }

  public double getStallCurrent() {
    return m_model == null ? 0.0 : m_model.stallCurrentAmps;
  }

  public boolean isServo() {
    return m_servo;
  }

  public boolean isBrushed() {
    return m_brushed;
  }

  public boolean isBrushless() {
    return !m_servo && !m_brushed;
  }

  public boolean hasLinearModel() {
    return m_model != null;
  }

  /** Returns the WPILib DCMotor model for this motor kind, or null when no built-in model exists. */
  public DCMotor getDCMotor() {
    return m_model;
  }
}
