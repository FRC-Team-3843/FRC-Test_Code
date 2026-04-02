package frc.lib.motor.config;

/** Runtime support level for a controller-family and transport combination. */
public enum SupportLevel {
  SUPPORTED,
  LIMITED,
  UNIMPLEMENTED,
  UNSUPPORTED;

  public boolean supportsTransport() {
    return this != UNSUPPORTED;
  }

  public boolean hasRuntimeSupport() {
    return this == SUPPORTED || this == LIMITED;
  }
}
