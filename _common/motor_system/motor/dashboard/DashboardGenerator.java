package frc.robot.motor.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import frc.robot.motor.config.MotorConfig;
import frc.robot.motor.config.MotorSystemConfig;
import java.io.File;
import java.io.IOException;

/**
 * Generates a fixed-width Elastic dashboard layout with separate per-motor Setup,
 * Tuning, and Testing tabs.
 */
public class DashboardGenerator {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final int GRID = 128;
  private static final long TRUE_COLOR = 4283215696L;
  private static final long FALSE_COLOR = 4294198070L;

  public static void main(String[] args) {
    if (args.length < 2) {
      System.err.println("Usage: DashboardGenerator <config.json> <output.json>");
      System.exit(1);
    }

    try {
      MotorSystemConfig config = MotorSystemConfig.load(new File(args[0]));
      generate(config, new File(args[1]));
    } catch (Exception e) {
      System.err.println("[DashboardGenerator] Failed: " + e.getMessage());
      System.exit(1);
    }
  }

  public static void generate(MotorSystemConfig config, File outFile) {
    try {
      ObjectNode root = mapper.createObjectNode();
      root.put("version", 1.0);
      root.put("grid_size", GRID);

      ArrayNode tabs = root.putArray("tabs");
      tabs.add(buildOverviewTab(config));
      for (MotorConfig mc : config.motors) {
        tabs.add(buildSetupTab(config, mc));
        tabs.add(buildTuningTab(config, mc));
        tabs.add(buildTestingTab(config, mc));
      }

      mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, root);
      System.out.println("[DashboardGenerator] Wrote " + outFile.getAbsolutePath());
    } catch (IOException e) {
      System.err.println("[DashboardGenerator] Failed: " + e.getMessage());
    }
  }

  private static ObjectNode buildOverviewTab(MotorSystemConfig config) {
    ObjectNode tab = baseTab("Overview");
    ArrayNode containers = containers(tab);
    String sys = config.systemName;

    containers.add(textDisplay("Controls", 0, 0, 4 * GRID, GRID,
        "/SmartDashboard/" + sys + "/Controls", "string", false));
    containers.add(toggleButton("Metric", 4 * GRID, 0, GRID, GRID,
        "/SmartDashboard/" + sys + "/UseMetric"));

    int y = GRID;
    for (MotorConfig mc : config.motors) {
      String prefix = "/SmartDashboard/" + sys + "/" + mc.name + "/";
      containers.add(textDisplay(mc.name, 0, y, 2 * GRID, GRID,
          prefix + "ControllerType", "string", false));
      containers.add(toggleButton("Arm", 2 * GRID, y, GRID, GRID,
          prefix + "Bench/Armed"));
      containers.add(booleanBox("A Hold", 3 * GRID, y, GRID, GRID,
          prefix + "Bench/DeadmanActive"));
      containers.add(textDisplay("Status", 4 * GRID, y, 3 * GRID, GRID,
          prefix + "Bench/Status", "string", false));
      containers.add(textDisplay("Owner", 7 * GRID, y, 2 * GRID, GRID,
          prefix + "Bench/ActionOwner", "string", false));
      containers.add(textDisplay("Telemetry", 9 * GRID, y, 3 * GRID, GRID,
          prefix + "Capabilities/Telemetry", "string", false));
      y += GRID;
    }

    return tab;
  }

  private static ObjectNode buildSetupTab(MotorSystemConfig config, MotorConfig mc) {
    ObjectNode tab = baseTab(mc.name + " Setup");
    ArrayNode containers = containers(tab);
    String prefix = "/SmartDashboard/" + config.systemName + "/" + mc.name + "/";

    containers.add(textDisplay("Controller", 0, 0, 3 * GRID, GRID,
        prefix + "ControllerType", "string", false));
    containers.add(textDisplay("Mechanism", 3 * GRID, 0, 2 * GRID, GRID,
        prefix + "MechanismType", "string", false));
    containers.add(toggleButton("Enabled", 5 * GRID, 0, GRID, GRID,
        prefix + "Enabled"));
    containers.add(toggleButton("Bench Armed", 6 * GRID, 0, GRID, GRID,
        prefix + "Bench/Armed"));
    containers.add(textDisplay("DS Mode", 7 * GRID, 0, GRID, GRID,
        prefix + "Bench/DSMode", "string", false));
    containers.add(booleanBox("A Hold", 8 * GRID, 0, GRID, GRID,
        prefix + "Bench/DeadmanActive"));

    containers.add(textDisplay("Geometry", 0, GRID, 4 * GRID, GRID,
        prefix + "Geometry", "string", false));
    containers.add(textDisplay("Current Limit", 4 * GRID, GRID, GRID, GRID,
        prefix + "CurrentLimit", "double", true));
    containers.add(toggleButton("Apply Setup", 5 * GRID, GRID, GRID, GRID,
        prefix + "Setup/Apply"));
    containers.add(textDisplay("Power Ch", 6 * GRID, GRID, GRID, GRID,
        prefix + "Power/Channel", "double", true));
    containers.add(toggleButton("Detect Ch", 7 * GRID, GRID, GRID, GRID,
        prefix + "Power/Detect"));
    containers.add(textDisplay("Detect Status", 8 * GRID, GRID, 4 * GRID, GRID,
        prefix + "Power/DetectStatus", "string", false));

    containers.add(booleanBox("Fwd On", 0, 2 * GRID, GRID, GRID,
        prefix + "Limits/ForwardEnabled"));
    containers.add(textDisplay("Fwd Limit", GRID, 2 * GRID, GRID, GRID,
        prefix + "Limits/Forward", "double", true));
    containers.add(booleanBox("Rev On", 2 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Limits/ReverseEnabled"));
    containers.add(textDisplay("Rev Limit", 3 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Limits/Reverse", "double", true));
    containers.add(textDisplay("Quad A", 4 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Feedback/QuadratureA", "double", true));
    containers.add(textDisplay("Quad B", 5 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Feedback/QuadratureB", "double", true));
    containers.add(textDisplay("Duty Ch", 6 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Feedback/DutyCycleChannel", "double", true));
    containers.add(textDisplay("Analog Ch", 7 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Feedback/AnalogChannel", "double", true));
    containers.add(textDisplay("Avg", 8 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Feedback/SamplesToAverage", "double", true));
    containers.add(booleanBox("FB Inv", 9 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Feedback/Inverted"));
    containers.add(booleanBox("Wrap", 10 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Feedback/ContinuousWrap"));

    containers.add(textDisplay("Dist/Pulse", 0, 3 * GRID, 2 * GRID, GRID,
        prefix + "Feedback/DistancePerPulseRot", "double", true));
    containers.add(textDisplay("FullRange", 2 * GRID, 3 * GRID, 2 * GRID, GRID,
        prefix + "Feedback/FullRangeRot", "double", true));
    containers.add(textDisplay("Offset", 4 * GRID, 3 * GRID, 2 * GRID, GRID,
        prefix + "Feedback/OffsetRot", "double", true));

    containers.add(textDisplay("Telemetry", 0, 4 * GRID, 4 * GRID, GRID,
        prefix + "Capabilities/Telemetry", "string", false));
    containers.add(textDisplay("Automated", 4 * GRID, 4 * GRID, 4 * GRID, GRID,
        prefix + "Capabilities/Tests", "string", false));
    containers.add(textDisplay("Unlock", 0, 5 * GRID, 8 * GRID, GRID,
        prefix + "Capabilities/Unlock", "string", false));
    containers.add(textDisplay("Status", 0, 6 * GRID, 6 * GRID, GRID,
        prefix + "Bench/Status", "string", false));
    containers.add(textDisplay("Owner", 6 * GRID, 6 * GRID, 2 * GRID, GRID,
        prefix + "Bench/ActionOwner", "string", false));

    return tab;
  }

  private static ObjectNode buildTuningTab(MotorSystemConfig config, MotorConfig mc) {
    ObjectNode tab = baseTab(mc.name + " Tuning");
    ArrayNode containers = containers(tab);
    String prefix = "/SmartDashboard/" + config.systemName + "/" + mc.name + "/";

    containers.add(toggleButton("Bench Armed", 0, 0, GRID, GRID, prefix + "Bench/Armed"));
    containers.add(booleanBox("A Hold", GRID, 0, GRID, GRID, prefix + "Bench/DeadmanActive"));
    containers.add(toggleButton("Start SysId", 2 * GRID, 0, GRID, GRID, prefix + "Bench/StartSysId"));
    containers.add(toggleButton("Manual Run", 3 * GRID, 0, GRID, GRID, prefix + "Bench/StartManualRun"));
    containers.add(toggleButton("Cancel", 4 * GRID, 0, GRID, GRID, prefix + "Bench/Cancel"));
    containers.add(textDisplay("Status", 5 * GRID, 0, 4 * GRID, GRID,
        prefix + "Bench/Status", "string", false));
    containers.add(textDisplay("Owner", 9 * GRID, 0, 2 * GRID, GRID,
        prefix + "Bench/ActionOwner", "string", false));

    containers.add(comboBoxChooser("Control Mode", 0, GRID, 2 * GRID, GRID,
        prefix + "ControlModeChooser"));
    containers.add(textDisplay("Target", 2 * GRID, GRID, GRID, GRID,
        prefix + "RunTarget", "double", true));
    containers.add(textDisplay("Actual RPM", 3 * GRID, GRID, GRID, GRID,
        prefix + "ActualRPM", "double", false));
    containers.add(booleanBox("At SP", 4 * GRID, GRID, GRID, GRID,
        prefix + "AtSetpoint"));
    containers.add(textDisplay("Current", 5 * GRID, GRID, GRID, GRID,
        prefix + "CurrentAmps", "double", false));
    containers.add(comboBoxChooser("Chart", 6 * GRID, GRID, 2 * GRID, GRID,
        prefix + "ChartType"));
    containers.add(textDisplay("Showing", 8 * GRID, GRID, 2 * GRID, GRID,
        prefix + "ChartLabel", "string", false));

    containers.add(graph(mc.name, 0, 2 * GRID, 7 * GRID, 2 * GRID,
        prefix + "ChartValue", 10.0, -1.0, -1.0));

    int x = 7 * GRID;
    String[] pidParams = { "kP", "kI", "kD", "kV", "kS" };
    for (String param : pidParams) {
      containers.add(textDisplay(param, x, 2 * GRID, GRID, GRID,
          prefix + "PID/" + param, "double", true));
      x += GRID;
    }
    containers.add(textDisplay("kA", 7 * GRID, 3 * GRID, GRID, GRID,
        prefix + "PID/kA", "double", true));
    containers.add(textDisplay("kG", 8 * GRID, 3 * GRID, GRID, GRID,
        prefix + "PID/kG", "double", true));
    containers.add(textDisplay("kP_pos", 9 * GRID, 3 * GRID, GRID, GRID,
        prefix + "PID/kP_pos", "double", true));
    containers.add(textDisplay("kD_pos", 10 * GRID, 3 * GRID, GRID, GRID,
        prefix + "PID/kD_pos", "double", true));
    containers.add(booleanBox("Apply PID", 11 * GRID, 3 * GRID, GRID, GRID,
        prefix + "ApplyPID"));

    containers.add(textDisplay("SysId Status", 0, 4 * GRID, 4 * GRID, GRID,
        prefix + "SysId/Status", "string", false));
    containers.add(booleanBox("Apply SysId", 4 * GRID, 4 * GRID, GRID, GRID,
        prefix + "SysId/ApplyResults"));
    containers.add(textDisplay("kS", 5 * GRID, 4 * GRID, GRID, GRID,
        prefix + "SysId/kS", "double", false));
    containers.add(textDisplay("kV", 6 * GRID, 4 * GRID, GRID, GRID,
        prefix + "SysId/kV", "double", false));
    containers.add(textDisplay("kA", 7 * GRID, 4 * GRID, GRID, GRID,
        prefix + "SysId/kA", "double", false));
    containers.add(textDisplay("kP vel", 8 * GRID, 4 * GRID, GRID, GRID,
        prefix + "SysId/kP_vel", "double", false));
    containers.add(textDisplay("kP pos", 9 * GRID, 4 * GRID, GRID, GRID,
        prefix + "SysId/kP_pos", "double", false));
    containers.add(textDisplay("R2", 10 * GRID, 4 * GRID, GRID, GRID,
        prefix + "SysId/R2_Accel", "double", false));
    containers.add(textDisplay("RMSE", 11 * GRID, 4 * GRID, GRID, GRID,
        prefix + "SysId/RMSE", "double", false));

    return tab;
  }

  private static ObjectNode buildTestingTab(MotorSystemConfig config, MotorConfig mc) {
    ObjectNode tab = baseTab(mc.name + " Testing");
    ArrayNode containers = containers(tab);
    String prefix = "/SmartDashboard/" + config.systemName + "/" + mc.name + "/";

    containers.add(textDisplay("Telemetry Available", 0, 0, 4 * GRID, GRID,
        prefix + "Capabilities/Telemetry", "string", false));
    containers.add(textDisplay("Automated Tests", 4 * GRID, 0, 4 * GRID, GRID,
        prefix + "Capabilities/Tests", "string", false));
    containers.add(textDisplay("Capability Summary", 8 * GRID, 0, 3 * GRID, GRID,
        prefix + "Testing/CapabilitySummary", "string", false));
    containers.add(textDisplay("Needed To Unlock", 0, GRID, 6 * GRID, GRID,
        prefix + "Capabilities/Unlock", "string", false));
    containers.add(textDisplay("Bench Status", 0, 2 * GRID, 3 * GRID, GRID,
        prefix + "Bench/Status", "string", false));
    containers.add(textDisplay("Closed Loop", 3 * GRID, 2 * GRID, 3 * GRID, GRID,
        prefix + "Testing/ClosedLoopSource", "string", false));
    containers.add(textDisplay("Power Source", 6 * GRID, 2 * GRID, 3 * GRID, GRID,
        prefix + "Testing/PowerTelemetrySource", "string", false));
    containers.add(booleanBox("FB OK", 9 * GRID, 2 * GRID, GRID, GRID,
        prefix + "Testing/FeedbackConnected"));
    containers.add(textDisplay("Feedback", 10 * GRID, 2 * GRID, 2 * GRID, GRID,
        prefix + "Testing/FeedbackSource", "string", false));

    containers.add(textDisplay("Health", 0, 3 * GRID, 4 * GRID, GRID,
        prefix + "Testing/TelemetryHealth", "string", false));
    containers.add(textDisplay("Vel RPS", 4 * GRID, 3 * GRID, GRID, GRID,
        prefix + "Testing/MeasuredVelocityRps", "double", false));
    containers.add(textDisplay("Pos Rot", 5 * GRID, 3 * GRID, GRID, GRID,
        prefix + "Testing/MeasuredPositionRot", "double", false));
    containers.add(textDisplay("Volts", 6 * GRID, 3 * GRID, GRID, GRID,
        prefix + "Testing/MeasuredVoltage", "double", false));
    containers.add(textDisplay("Current", 7 * GRID, 3 * GRID, GRID, GRID,
        prefix + "Testing/MeasuredCurrent", "double", false));
    containers.add(textDisplay("Temp C", 8 * GRID, 3 * GRID, GRID, GRID,
        prefix + "Testing/MeasuredTemperatureC", "double", false));
    containers.add(textDisplay("Detect", 9 * GRID, 3 * GRID, 3 * GRID, GRID,
        prefix + "Power/DetectStatus", "string", false));

    containers.add(graph(mc.name + " Test Graph", 0, 4 * GRID, 8 * GRID, 2 * GRID,
        prefix + "Testing/MeasuredVelocityRps", 10.0, -1.0, -1.0));
    return tab;
  }

  private static ObjectNode baseTab(String name) {
    ObjectNode tab = mapper.createObjectNode();
    tab.put("name", name);
    ObjectNode grid = tab.putObject("grid_layout");
    grid.putArray("layouts");
    grid.putArray("containers");
    return tab;
  }

  private static ArrayNode containers(ObjectNode tab) {
    return (ArrayNode) tab.get("grid_layout").get("containers");
  }

  private static ObjectNode textDisplay(String title, int x, int y, int w, int h,
                                        String topic, String dataType, boolean showSubmit) {
    ObjectNode widget = mapper.createObjectNode();
    widget.put("title", title);
    widget.put("x", (double) x);
    widget.put("y", (double) y);
    widget.put("width", (double) w);
    widget.put("height", (double) h);
    widget.put("type", "Text Display");
    ObjectNode props = widget.putObject("properties");
    props.put("topic", topic);
    props.put("period", 0.1);
    props.put("data_type", dataType);
    props.put("show_submit_button", showSubmit);
    return widget;
  }

  private static ObjectNode toggleButton(String title, int x, int y, int w, int h, String topic) {
    ObjectNode widget = mapper.createObjectNode();
    widget.put("title", title);
    widget.put("x", (double) x);
    widget.put("y", (double) y);
    widget.put("width", (double) w);
    widget.put("height", (double) h);
    widget.put("type", "Toggle Button");
    ObjectNode props = widget.putObject("properties");
    props.put("topic", topic);
    props.put("period", 0.06);
    props.put("data_type", "boolean");
    return widget;
  }

  private static ObjectNode booleanBox(String title, int x, int y, int w, int h, String topic) {
    ObjectNode widget = mapper.createObjectNode();
    widget.put("title", title);
    widget.put("x", (double) x);
    widget.put("y", (double) y);
    widget.put("width", (double) w);
    widget.put("height", (double) h);
    widget.put("type", "Boolean Box");
    ObjectNode props = widget.putObject("properties");
    props.put("topic", topic);
    props.put("period", 0.06);
    props.put("data_type", "boolean");
    props.put("true_color", TRUE_COLOR);
    props.put("false_color", FALSE_COLOR);
    props.put("true_icon", "None");
    props.put("false_icon", "None");
    return widget;
  }

  private static ObjectNode graph(String title, int x, int y, int w, int h,
                                  String topic, double timeRange, double yMin, double yMax) {
    ObjectNode widget = mapper.createObjectNode();
    widget.put("title", title);
    widget.put("x", (double) x);
    widget.put("y", (double) y);
    widget.put("width", (double) w);
    widget.put("height", (double) h);
    widget.put("type", "Graph");
    ObjectNode props = widget.putObject("properties");
    props.put("topic", topic);
    props.put("period", 0.02);
    props.put("data_type", "double");
    props.put("time_range", timeRange);
    if (yMin >= 0 && yMax > yMin) {
      props.put("y_axis_min", yMin);
      props.put("y_axis_max", yMax);
    }
    return widget;
  }

  private static ObjectNode comboBoxChooser(String title, int x, int y, int w, int h,
                                            String topic) {
    ObjectNode widget = mapper.createObjectNode();
    widget.put("title", title);
    widget.put("x", (double) x);
    widget.put("y", (double) y);
    widget.put("width", (double) w);
    widget.put("height", (double) h);
    widget.put("type", "ComboBox Chooser");
    ObjectNode props = widget.putObject("properties");
    props.put("topic", topic);
    props.put("period", 0.1);
    return widget;
  }
}
