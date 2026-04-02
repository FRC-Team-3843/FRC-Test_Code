package frc.lib.motor.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/** UI-agnostic metadata describing configurable motor-system fields and enum options. */
public final class MotorMetadataCatalog {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public enum Scope {
    SYSTEM,
    MOTOR
  }

  public enum FieldType {
    STRING,
    BOOLEAN,
    INTEGER,
    DOUBLE,
    ENUM
  }

  public enum Editability {
    READ_ONLY,
    EDITABLE
  }

  public enum ApplyBehavior {
    READ_ONLY,
    LIVE_APPLY,
    SAVE_ONLY,
    SAVE_AND_REBUILD
  }

  public record Option(String value, String label) {}

  public record Field(
      String key,
      Scope scope,
      String category,
      String label,
      FieldType type,
      String description,
      String unit,
      String topicSuffix,
      Editability editability,
      ApplyBehavior applyBehavior,
      String requiredCapability,
      String unlockHint,
      List<Option> options,
      Map<String, String> visibleWhen) {}

  private MotorMetadataCatalog() {}

  public static List<Field> systemFields() {
    return List.of(
        field("systemName", Scope.SYSTEM, "identity", "System Name", FieldType.STRING,
            "Top-level dashboard/config prefix for this motor system.",
            "Metadata/SystemName", Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        enumField("powerModuleType", Scope.SYSTEM, "power", "Power Module",
            "Configured system PD module.", "Metadata/PowerModuleType",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY, null,
            "Choose a power module to unlock per-channel current telemetry.",
            enumOptions(PowerModuleType.values())),
        field("powerModuleId", Scope.SYSTEM, "power", "Power Module ID", FieldType.INTEGER,
            "CAN ID of the configured PD module.", "Metadata/PowerModuleId",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        field("servoChannel", Scope.SYSTEM, "actuation", "Servo PWM", FieldType.INTEGER,
            "Optional shared servo channel. Use -1 to disable.", "Metadata/ServoChannel",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        field("velocityToleranceRpm", Scope.SYSTEM, "tuning", "Velocity Tolerance",
            FieldType.DOUBLE, "Default at-setpoint window used by the bench app.", "rpm",
            "Metadata/VelocityToleranceRpm", Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        field("driverControllerPort", Scope.SYSTEM, "controls", "Driver Port", FieldType.INTEGER,
            "USB port for the Xbox controller used by the bench app.", "Metadata/DriverControllerPort",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        field("enableLogging", Scope.SYSTEM, "diagnostics", "Enable Logging", FieldType.BOOLEAN,
            "Enable WPILib DataLog capture in the bench app.", "Metadata/EnableLogging",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY));
  }

  public static List<Field> motorFields() {
    return List.of(
        field("name", Scope.MOTOR, "identity", "Motor Name", FieldType.STRING,
            "Display name and topic suffix for this motor channel.", "Metadata/Name",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        enumField("controllerFamily", Scope.MOTOR, "identity", "Controller Family",
            "Controller family for this motor channel.", "ControllerFamily",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY, null, null,
            enumOptions(ControllerFamily.values())),
        enumField("transport", Scope.MOTOR, "identity", "Transport",
            "Physical transport path to the controller.", "Transport",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY, null, null,
            enumOptions(TransportType.values())),
        enumField("feedbackSource", Scope.MOTOR, "feedback", "Feedback Source",
            "Sensor source used for closed-loop and testing.", "Metadata/FeedbackSource",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY, null,
            "Configure an external or integrated sensor to unlock tuning/testing features.",
            enumOptions(FeedbackSource.values())),
        enumField("motorKind", Scope.MOTOR, "identity", "Motor Kind",
            "Installed FRC motor type or custom placeholder.", "Metadata/MotorKind",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY, null, null,
            enumOptions(MotorKind.values())),
        enumField("mechanismType", Scope.MOTOR, "geometry", "Mechanism Type",
            "Feedforward model used by SysId and tuning.", "MechanismType",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY, null, null,
            enumOptions(MechanismType.values())),
        field("supportLevel", Scope.MOTOR, "support", "Support Level", FieldType.STRING,
            "Resolved runtime support level for this controller-family and transport path.",
            "Support/Level", Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        field("supportNote", Scope.MOTOR, "support", "Support Note", FieldType.STRING,
            "Plain-language explanation of what is limited or missing on this runtime path.",
            "Support/Note", Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        field("capabilityTelemetry", Scope.MOTOR, "support", "Telemetry Available", FieldType.STRING,
            "Summary of telemetry available on the current runtime path.",
            "Capabilities/Telemetry", Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        field("capabilityTests", Scope.MOTOR, "support", "Automated Tests", FieldType.STRING,
            "Summary of tuning/testing workflows enabled on the current runtime path.",
            "Capabilities/Tests", Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        field("capabilityUnlock", Scope.MOTOR, "support", "Needed To Unlock", FieldType.STRING,
            "What additional hardware or support is needed to unlock more capability.",
            "Capabilities/Unlock", Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        visibleField("canId", "identity", "CAN ID", FieldType.INTEGER,
            "Controller CAN ID.", "Metadata/CanId", Editability.READ_ONLY,
            ApplyBehavior.SAVE_AND_REBUILD, null, null, Map.of("transport", "CAN")),
        visibleField("canBus", "identity", "CAN Bus", FieldType.STRING,
            "Named CAN bus or empty string for default bus.", "Metadata/CanBus",
            Editability.READ_ONLY, ApplyBehavior.SAVE_AND_REBUILD, null, null,
            Map.of("transport", "CAN")),
        visibleField("pwmChannel", "identity", "PWM Channel", FieldType.INTEGER,
            "PWM output channel.", "Metadata/PwmChannel", Editability.READ_ONLY,
            ApplyBehavior.SAVE_AND_REBUILD, null, null, Map.of("transport", "PWM")),
        field("powerChannel", Scope.MOTOR, "power", "Power Channel", FieldType.INTEGER,
            "Optional PD channel for current telemetry and power detection.", "Power/Channel",
            Editability.EDITABLE, ApplyBehavior.SAVE_ONLY, null,
            "Assign a PD channel to unlock current telemetry and channel detection."),
        field("gearRatio", Scope.MOTOR, "geometry", "Gear Ratio", FieldType.DOUBLE,
            "Motor rotations per mechanism rotation.", "GearRatio",
            Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD, null,
            "Changing gear ratio requires rebuilding controller conversions."),
        field("inverted", Scope.MOTOR, "identity", "Inverted", FieldType.BOOLEAN,
            "Invert motor output direction.", "Metadata/Inverted",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        field("brakeMode", Scope.MOTOR, "identity", "Brake Mode", FieldType.BOOLEAN,
            "Brake/coast setting where supported.", "Metadata/BrakeMode",
            Editability.READ_ONLY, ApplyBehavior.READ_ONLY),
        visibleField("quadratureChannelA", "feedback", "Quadrature A", FieldType.INTEGER,
            "DIO channel A for quadrature feedback.", "Feedback/QuadratureA",
            Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD, null,
            "Set both quadrature channels to unlock DIO feedback.",
            Map.of("feedbackSource", "QUADRATURE_DIO")),
        visibleField("quadratureChannelB", "feedback", "Quadrature B", FieldType.INTEGER,
            "DIO channel B for quadrature feedback.", "Feedback/QuadratureB",
            Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD, null,
            "Set both quadrature channels to unlock DIO feedback.",
            Map.of("feedbackSource", "QUADRATURE_DIO")),
        visibleField("dutyCycleChannel", "feedback", "Duty Cycle Channel", FieldType.INTEGER,
            "DIO channel for duty-cycle encoder feedback.", "Feedback/DutyCycleChannel",
            Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD, null,
            "Set a duty-cycle channel to unlock duty-cycle feedback.",
            Map.of("feedbackSource", "DUTY_CYCLE")),
        visibleField("analogChannel", "feedback", "Analog Channel", FieldType.INTEGER,
            "Analog channel for absolute or scaled feedback.", "Feedback/AnalogChannel",
            Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD, null,
            "Set an analog channel to unlock analog feedback.",
            Map.of("feedbackSource", "ANALOG")),
        field("feedbackDistancePerPulseRotations", Scope.MOTOR, "feedback", "Distance Per Pulse",
            FieldType.DOUBLE, "Mechanism rotations per sensor pulse or tick.", null,
            "Feedback/DistancePerPulseRot", Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD,
            "supportsSysId", "Feedback scaling must be configured for meaningful tuning."),
        field("feedbackFullRangeRotations", Scope.MOTOR, "feedback", "Full Range", FieldType.DOUBLE,
            "Full sensor travel expressed in mechanism rotations.", null,
            "Feedback/FullRangeRot", Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD,
            "supportsSysId", "Analog and absolute sensors need scaling to unlock tuning."),
        field("feedbackOffsetRotations", Scope.MOTOR, "feedback", "Feedback Offset", FieldType.DOUBLE,
            "Offset applied to external feedback before use.", null,
            "Feedback/OffsetRot", Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD, null, null),
        field("feedbackInverted", Scope.MOTOR, "feedback", "Feedback Inverted", FieldType.BOOLEAN,
            "Invert external feedback direction.", "Feedback/Inverted",
            Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD, null, null),
        field("feedbackContinuousWrap", Scope.MOTOR, "feedback", "Continuous Wrap", FieldType.BOOLEAN,
            "Treat external feedback as a wrapping sensor.", "Feedback/ContinuousWrap",
            Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD, "supportsPositionClosedLoop",
            "Wrapping feedback is only meaningful when position feedback is active."),
        field("feedbackSamplesToAverage", Scope.MOTOR, "feedback", "Samples To Average",
            FieldType.INTEGER, "Averaging/filter window for supported external sensors.", null,
            "Feedback/SamplesToAverage", Editability.EDITABLE, ApplyBehavior.SAVE_AND_REBUILD, null, null),
        field("kP", Scope.MOTOR, "tuning", "Velocity kP", FieldType.DOUBLE,
            "Velocity-loop proportional gain.", "PID/kP",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsVelocityClosedLoop",
            "Velocity feedback is required to unlock velocity control."),
        field("kI", Scope.MOTOR, "tuning", "Velocity kI", FieldType.DOUBLE,
            "Velocity-loop integral gain.", "PID/kI",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsVelocityClosedLoop",
            "Velocity feedback is required to unlock velocity control."),
        field("kD", Scope.MOTOR, "tuning", "Velocity kD", FieldType.DOUBLE,
            "Velocity-loop derivative gain.", "PID/kD",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsVelocityClosedLoop",
            "Velocity feedback is required to unlock velocity control."),
        field("kV", Scope.MOTOR, "tuning", "kV", FieldType.DOUBLE,
            "Velocity feedforward gain.", "PID/kV",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsVelocityClosedLoop",
            "Velocity feedback is required to unlock tuning."),
        field("kS", Scope.MOTOR, "tuning", "kS", FieldType.DOUBLE,
            "Static friction feedforward.", "PID/kS",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsSysId",
            "Feedback is required to characterize and apply feedforward gains."),
        field("kA", Scope.MOTOR, "tuning", "kA", FieldType.DOUBLE,
            "Acceleration feedforward.", "PID/kA",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsSysId",
            "Feedback is required to characterize and apply feedforward gains."),
        field("kG", Scope.MOTOR, "tuning", "kG", FieldType.DOUBLE,
            "Gravity feedforward.", "PID/kG",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsPositionClosedLoop",
            "Mechanism feedback is required to apply gravity compensation meaningfully."),
        field("kP_pos", Scope.MOTOR, "tuning", "Position kP", FieldType.DOUBLE,
            "Position-loop proportional gain.", "PID/kP_pos",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsPositionClosedLoop",
            "Position feedback is required to unlock position control."),
        field("kD_pos", Scope.MOTOR, "tuning", "Position kD", FieldType.DOUBLE,
            "Position-loop derivative gain.", "PID/kD_pos",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsPositionClosedLoop",
            "Position feedback is required to unlock position control."),
        field("currentLimit", Scope.MOTOR, "protection", "Current Limit", FieldType.DOUBLE,
            "Configured current limit where supported.", "A", "CurrentLimit",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, null,
            "Controller-side current limiting depends on controller support."),
        field("motionCruiseVelocity", Scope.MOTOR, "motion", "Cruise Velocity", FieldType.DOUBLE,
            "Motion-profile cruise velocity.", "rps", "Motion/CruiseVel",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsMotionProfile",
            "Motion profiling requires a controller/runtime path that supports it."),
        field("motionAcceleration", Scope.MOTOR, "motion", "Acceleration", FieldType.DOUBLE,
            "Motion-profile acceleration.", "rps^2", "Motion/Accel",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsMotionProfile",
            "Motion profiling requires a controller/runtime path that supports it."),
        field("motionJerk", Scope.MOTOR, "motion", "Jerk", FieldType.DOUBLE,
            "Motion-profile jerk.", "rps^3", "Motion/Jerk",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, "supportsMotionProfile",
            "Motion profiling requires a controller/runtime path that supports it."),
        field("forwardLimit", Scope.MOTOR, "protection", "Forward Limit", FieldType.DOUBLE,
            "Forward soft limit in mechanism rotations.", "Limits/Forward",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, null,
            "Soft limits only apply on runtimes that expose them."),
        field("reverseLimit", Scope.MOTOR, "protection", "Reverse Limit", FieldType.DOUBLE,
            "Reverse soft limit in mechanism rotations.", "Limits/Reverse",
            Editability.EDITABLE, ApplyBehavior.LIVE_APPLY, null,
            "Soft limits only apply on runtimes that expose them."),
        field("wheelDiameter", Scope.MOTOR, "geometry", "Wheel Diameter", FieldType.DOUBLE,
            "Wheel diameter used for surface-speed conversion.", "in", "WheelDiameter",
            Editability.EDITABLE, ApplyBehavior.SAVE_ONLY, null,
            "Wheel diameter affects geometry conversion, not controller state."),
        field("distancePerRotation", Scope.MOTOR, "geometry", "Distance Per Rotation", FieldType.DOUBLE,
            "Linear mechanism travel per mechanism rotation.", "in", "DistancePerRotation",
            Editability.EDITABLE, ApplyBehavior.SAVE_ONLY, null,
            "This affects geometry conversion and testing interpretation."),
        field("armLength", Scope.MOTOR, "geometry", "Arm Length", FieldType.DOUBLE,
            "Arm length used for arc-length conversion.", "in", "ArmLength",
            Editability.EDITABLE, ApplyBehavior.SAVE_ONLY, null,
            "This affects geometry conversion and analysis estimates."),
        field("mass", Scope.MOTOR, "geometry", "Mass", FieldType.DOUBLE,
            "Optional mechanism mass used by future analysis.", "kg", "Mass",
            Editability.EDITABLE, ApplyBehavior.SAVE_ONLY, null,
            "Mass is used for estimates and future testing, not controller state."));
  }

  public static Map<String, Field> motorFieldMap() {
    Map<String, Field> fields = new LinkedHashMap<>();
    for (Field field : motorFields()) {
      fields.put(field.key(), field);
    }
    return fields;
  }

  public static String schemaJson() {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("systemFields", systemFields());
    root.put("motorFields", motorFields());
    try {
      return MAPPER.writeValueAsString(root);
    } catch (JsonProcessingException e) {
      return "{\"error\":\"metadata serialization failed\"}";
    }
  }

  public static boolean isVisible(Field field, MotorConfig motorConfig) {
    if (field.visibleWhen().isEmpty()) {
      return true;
    }
    for (var entry : field.visibleWhen().entrySet()) {
      String actualValue = motorValueForKey(motorConfig, entry.getKey());
      if (!entry.getValue().equals(actualValue)) {
        return false;
      }
    }
    return true;
  }

  private static String motorValueForKey(MotorConfig config, String key) {
    return switch (key) {
      case "transport" -> config.transport;
      case "feedbackSource" -> config.feedbackSource;
      default -> "";
    };
  }

  private static Field field(String key, Scope scope, String category, String label,
                             FieldType type, String description, String topicSuffix,
                             Editability editability, ApplyBehavior applyBehavior) {
    return new Field(key, scope, category, label, type, description, null, topicSuffix,
        editability, applyBehavior, null, null, List.of(), Map.of());
  }

  private static Field field(String key, Scope scope, String category, String label,
                             FieldType type, String description, String unit, String topicSuffix,
                             Editability editability, ApplyBehavior applyBehavior) {
    return new Field(key, scope, category, label, type, description, unit, topicSuffix,
        editability, applyBehavior, null, null, List.of(), Map.of());
  }

  private static Field field(String key, Scope scope, String category, String label,
                             FieldType type, String description, String topicSuffix,
                             Editability editability, ApplyBehavior applyBehavior,
                             String requiredCapability, String unlockHint) {
    return new Field(key, scope, category, label, type, description, null, topicSuffix,
        editability, applyBehavior, requiredCapability, unlockHint, List.of(), Map.of());
  }

  private static Field field(String key, Scope scope, String category, String label,
                             FieldType type, String description, String unit, String topicSuffix,
                             Editability editability, ApplyBehavior applyBehavior,
                             String requiredCapability, String unlockHint) {
    return new Field(key, scope, category, label, type, description, unit, topicSuffix,
        editability, applyBehavior, requiredCapability, unlockHint, List.of(), Map.of());
  }

  private static Field enumField(String key, Scope scope, String category, String label,
                                 String description, String topicSuffix,
                                 Editability editability, ApplyBehavior applyBehavior,
                                 String requiredCapability, String unlockHint,
                                 List<Option> options) {
    return new Field(key, scope, category, label, FieldType.ENUM, description, null, topicSuffix,
        editability, applyBehavior, requiredCapability, unlockHint, options, Map.of());
  }

  private static Field visibleField(String key, String category, String label, FieldType type,
                                    String description, String topicSuffix,
                                    Editability editability, ApplyBehavior applyBehavior,
                                    String requiredCapability, String unlockHint,
                                    Map<String, String> visibleWhen) {
    return new Field(key, Scope.MOTOR, category, label, type, description, null, topicSuffix,
        editability, applyBehavior, requiredCapability, unlockHint, List.of(), visibleWhen);
  }

  private static <E extends Enum<E>> List<Option> enumOptions(E[] values) {
    return Stream.of(values)
        .map(value -> new Option(value.name(), optionLabel(value)))
        .toList();
  }

  private static String optionLabel(Enum<?> value) {
    if (value instanceof ControllerFamily controllerFamily) {
      return controllerFamily.displayName();
    }
    if (value instanceof MotorKind motorKind) {
      return motorKind.displayName();
    }
    return value.name();
  }
}
