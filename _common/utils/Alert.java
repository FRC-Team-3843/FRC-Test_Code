package frc.robot.utils;

import edu.wpi.first.util.sendable.Sendable;
import edu.wpi.first.util.sendable.SendableBuilder;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for managing persistent alerts to be sent over NetworkTables.
 * Based on Team 6328's Alert class.
 */
public class Alert {
  private static final Map<String, SendableAlerts> groups = new HashMap<>();

  private final AlertType type;
  private final String group;
  private final String text;
  private boolean active = false;
  private double activeStartTime = 0.0;

  /**
   * Creates a new Alert in the default group - "Alerts".
   *
   * @param text Text to be displayed when the alert is active.
   * @param type Alert level specifying urgency.
   */
  public Alert(String text, AlertType type) {
    this("Alerts", text, type);
  }

  /**
   * Creates a new Alert in a custom group.
   *
   * @param group Group identifier, also used as NetworkTables title.
   * @param text Text to be displayed when the alert is active.
   * @param type Alert level specifying urgency.
   */
  public Alert(String group, String text, AlertType type) {
    if (!groups.containsKey(group)) {
      groups.put(group, new SendableAlerts());
      SmartDashboard.putData(group, groups.get(group));
    }

    this.text = text;
    this.type = type;
    this.group = group;
    groups.get(group).alerts.add(this);
  }

  /**
   * Sets whether the alert should currently be displayed.
   *
   * @param active Whether the alert is active.
   */
  public void set(boolean active) {
    if (active && !this.active) {
      activeStartTime = Timer.getFPGATimestamp();
    }
    this.active = active;
  }

  /**
   * Updates current alert status.
   *
   * @param active Whether the alert should be active.
   */
  public void setText(boolean active) {
    set(active);
  }

  /**
   * Returns whether the alert is active.
   *
   * @return active status.
   */
  public boolean get() {
    return active;
  }

  /**
   * Returns the text of the alert.
   *
   * @return alert text.
   */
  public String getText() {
    return text;
  }

  /**
   * Returns the type of the alert.
   *
   * @return alert type.
   */
  public AlertType getType() {
    return type;
  }

  private static class SendableAlerts implements Sendable {
    public final List<Alert> alerts = new ArrayList<>();

    public String[] getStrings(AlertType type) {
      return alerts.stream()
          .filter(x -> x.type == type && x.active)
          .sorted(Comparator.comparingDouble((Alert a) -> a.activeStartTime).reversed())
          .map(a -> a.text)
          .toArray(String[]::new);
    }

    @Override
    public void initSendable(SendableBuilder builder) {
      builder.setSmartDashboardType("Alerts");
      builder.addStringArrayProperty("errors", () -> getStrings(AlertType.ERROR), null);
      builder.addStringArrayProperty("warnings", () -> getStrings(AlertType.WARNING), null);
      builder.addStringArrayProperty("infos", () -> getStrings(AlertType.INFO), null);
    }
  }

  /**
   * Represents the severity of the alert.
   */
  public enum AlertType {
    /**
     * High priority alert - displayed first on the dashboard.
     */
    ERROR,

    /**
     * Medium priority alert.
     */
    WARNING,

    /**
     * Low priority alert.
     */
    INFO
  }
}
