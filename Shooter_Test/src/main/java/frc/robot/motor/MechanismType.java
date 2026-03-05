package frc.robot.motor;

/**
 * Feedforward model type for SysId analysis.
 *
 * <p>Different mechanisms require different feedforward equations:
 * <ul>
 *   <li>SIMPLE: V = kS*sign(v) + kV*v + kA*a (flywheels, drivetrains)</li>
 *   <li>ARM: V = kG*cos(pos) + kS*sign(v) + kV*v + kA*a</li>
 *   <li>ELEVATOR: V = kG + kS*sign(v) + kV*v + kA*a</li>
 * </ul>
 */
public enum MechanismType {
  SIMPLE,
  ARM,
  ELEVATOR
}
