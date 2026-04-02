package frc.lib.motor.config;

public enum ControllerFamily {
  SPARK_MAX("SPARK MAX",
      SupportLevel.SUPPORTED, "",
      SupportLevel.SUPPORTED, ""),
  SPARK_FLEX("SPARK Flex",
      SupportLevel.SUPPORTED, "",
      SupportLevel.SUPPORTED, ""),
  SPARK("SPARK",
      SupportLevel.UNSUPPORTED, "No CAN runtime path for the legacy SPARK controller.",
      SupportLevel.SUPPORTED, ""),
  TALON_FX("Talon FX",
      SupportLevel.SUPPORTED, "",
      SupportLevel.SUPPORTED, ""),
  TALON_FXS("Talon FXS",
      SupportLevel.SUPPORTED, "",
      SupportLevel.UNSUPPORTED, "Talon FXS is not exposed through a PWM runtime path."),
  TALON_SRX("Talon SRX",
      SupportLevel.SUPPORTED, "",
      SupportLevel.SUPPORTED, ""),
  TALON("Talon",
      SupportLevel.UNSUPPORTED, "Legacy Talon support is PWM-only in this abstraction.",
      SupportLevel.SUPPORTED, ""),
  VICTOR_SPX("Victor SPX",
      SupportLevel.LIMITED, "CAN runtime is open-loop plus external-feedback/software-closed-loop only.",
      SupportLevel.SUPPORTED, ""),
  VICTOR_SP("Victor SP",
      SupportLevel.UNSUPPORTED, "Victor SP is PWM-only in this abstraction.",
      SupportLevel.SUPPORTED, ""),
  VENOM("Venom",
      SupportLevel.UNIMPLEMENTED, "CAN runtime not implemented yet for Venom.",
      SupportLevel.SUPPORTED, ""),
  KOORS_40("Koors 40",
      SupportLevel.UNSUPPORTED, "Koors 40 is PWM-only in this abstraction.",
      SupportLevel.SUPPORTED, ""),
  THRIFTY_NOVA("Thrifty Nova",
      SupportLevel.UNIMPLEMENTED, "CAN runtime not implemented yet for Thrifty Nova.",
      SupportLevel.UNSUPPORTED, "Thrifty Nova does not have a PWM runtime path here."),
  GENERIC_PWM("Generic PWM",
      SupportLevel.UNSUPPORTED, "Generic PWM has no CAN runtime path.",
      SupportLevel.SUPPORTED, ""),
  SERVO("Servo",
      SupportLevel.UNSUPPORTED, "Servo has no CAN runtime path.",
      SupportLevel.SUPPORTED, "");

  private final String m_displayName;
  private final SupportLevel m_canSupportLevel;
  private final String m_canSupportNote;
  private final SupportLevel m_pwmSupportLevel;
  private final String m_pwmSupportNote;

  ControllerFamily(
      String displayName,
      SupportLevel canSupportLevel,
      String canSupportNote,
      SupportLevel pwmSupportLevel,
      String pwmSupportNote) {
    m_displayName = displayName;
    m_canSupportLevel = canSupportLevel;
    m_canSupportNote = canSupportNote;
    m_pwmSupportLevel = pwmSupportLevel;
    m_pwmSupportNote = pwmSupportNote;
  }

  public String displayName() {
    return m_displayName;
  }

  public boolean supportsTransport(TransportType transport) {
    return supportLevel(transport).supportsTransport();
  }

  public boolean hasRuntimeSupport(TransportType transport) {
    return supportLevel(transport).hasRuntimeSupport();
  }

  public SupportLevel supportLevel(TransportType transport) {
    return switch (transport) {
      case CAN -> m_canSupportLevel;
      case PWM -> m_pwmSupportLevel;
    };
  }

  public String supportNote(TransportType transport) {
    return switch (transport) {
      case CAN -> m_canSupportNote;
      case PWM -> m_pwmSupportNote;
    };
  }
}
