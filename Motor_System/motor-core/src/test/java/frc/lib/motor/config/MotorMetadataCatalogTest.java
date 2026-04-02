package frc.lib.motor.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

class MotorMetadataCatalogTest {
  @Test
  void controllerFamilyOptionsUseDisplayNames() {
    Optional<MotorMetadataCatalog.Field> field =
        MotorMetadataCatalog.motorFields().stream()
            .filter(metadataField -> metadataField.key().equals("controllerFamily"))
            .findFirst();

    assertTrue(field.isPresent());
    String sparkFlexLabel =
        field.get().options().stream()
            .filter(option -> option.value().equals(ControllerFamily.SPARK_FLEX.name()))
            .findFirst()
            .map(MotorMetadataCatalog.Option::label)
            .orElseThrow();
    assertEquals("SPARK Flex", sparkFlexLabel);
  }

  @Test
  void motorKindOptionsUseHumanReadableLabels() {
    Optional<MotorMetadataCatalog.Field> field =
        MotorMetadataCatalog.motorFields().stream()
            .filter(metadataField -> metadataField.key().equals("motorKind"))
            .findFirst();

    assertTrue(field.isPresent());
    String krakenLabel =
        field.get().options().stream()
            .filter(option -> option.value().equals(MotorKind.KRAKEN.name()))
            .findFirst()
            .map(MotorMetadataCatalog.Option::label)
            .orElseThrow();
    assertEquals("Kraken X60", krakenLabel);
  }

  @Test
  void metadataSchemaJsonStillSerializes() {
    String json = MotorMetadataCatalog.schemaJson();

    assertNotNull(json);
    assertTrue(json.contains("controllerFamily"));
    assertTrue(json.contains("motorKind"));
    assertTrue(json.contains("SPARK Flex"));
    assertTrue(json.contains("LIVE_APPLY"));
    assertTrue(json.contains("supportsMotionProfile"));
  }

  @Test
  void motionFieldCarriesApplyBehaviorAndCapabilityRequirement() {
    MotorMetadataCatalog.Field field =
        MotorMetadataCatalog.motorFieldMap().get("motionCruiseVelocity");

    assertNotNull(field);
    assertEquals(MotorMetadataCatalog.ApplyBehavior.LIVE_APPLY, field.applyBehavior());
    assertEquals("supportsMotionProfile", field.requiredCapability());
    assertEquals("Motion/CruiseVel", field.topicSuffix());
  }

  @Test
  void readOnlyIdentityFieldIsNotEditable() {
    MotorMetadataCatalog.Field field =
        MotorMetadataCatalog.motorFieldMap().get("controllerFamily");

    assertNotNull(field);
    assertEquals(MotorMetadataCatalog.Editability.READ_ONLY, field.editability());
    assertFalse(field.topicSuffix().isBlank());
  }
}
