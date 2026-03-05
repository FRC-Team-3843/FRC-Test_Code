package frc.robot.motor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;

/**
 * Generates elastic-layout.json programmatically from MotorSystemConfig.
 *
 * <p>Run at build time via Gradle ({@code ./gradlew generateDashboard}) or
 * automatically before deploy. Reads motor-config.json and writes
 * elastic-layout.json into the same deploy directory so Gradle deploys both.
 *
 * <p>Creates:
 * <ul>
 *   <li><b>Overview tab</b>: All motor switchable graphs, enable toggles, setpoints, servo</li>
 *   <li><b>Per-motor tab</b>: Compact 6-row layout with switchable graph, PID, motion profiling,
 *       SysId results + physical estimates, all tunable parameters</li>
 * </ul>
 */
public class DashboardGenerator {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final int GRID = 128;

  // Elastic widget colors
  private static final long TRUE_COLOR = 4283215696L;  // green
  private static final long FALSE_COLOR = 4294198070L; // red

  /**
   * Build-time entry point. Called by Gradle before deploy.
   *
   * @param args [0] = path to motor-config.json, [1] = output path for elastic-layout.json
   */
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

  /**
   * Generates elastic-layout.json and writes it to the specified file.
   *
   * @param config the motor system config
   * @param outFile the output file to write
   */
  public static void generate(MotorSystemConfig config, File outFile) {
    try {
      ObjectNode root = mapper.createObjectNode();
      root.put("version", 1.0);
      root.put("grid_size", GRID);

      ArrayNode tabs = root.putArray("tabs");
      tabs.add(buildOverviewTab(config));

      for (MotorConfig mc : config.motors) {
        tabs.add(buildMotorTab(config, mc));
      }

      mapper.writerWithDefaultPrettyPrinter().writeValue(outFile, root);
      System.out.println("[DashboardGenerator] Wrote " + outFile.getAbsolutePath());
    } catch (IOException e) {
      System.err.println("[DashboardGenerator] Failed: " + e.getMessage());
    }
  }

  // ─── Overview Tab ──────────────────────────────────────────────────

  private static ObjectNode buildOverviewTab(MotorSystemConfig config) {
    ObjectNode tab = mapper.createObjectNode();
    tab.put("name", "Run");

    ObjectNode grid = tab.putObject("grid_layout");
    grid.putArray("layouts");
    ArrayNode containers = grid.putArray("containers");

    String sys = config.systemName;
    int y = 0;

    // Row 0: Controls + enable toggles + UseMetric toggle + servo positions
    containers.add(textDisplay("Controls", 0, y, 3 * GRID, GRID,
        "/SmartDashboard/" + sys + "/Controls", "string", false));

    int x = 3 * GRID;
    for (MotorConfig mc : config.motors) {
      containers.add(toggleButton(mc.name + " EN", x, y, GRID, GRID,
          "/SmartDashboard/" + sys + "/" + mc.name + "/Enabled"));
      x += GRID;
    }

    // UseMetric toggle
    containers.add(toggleButton("Metric", x, y, GRID, GRID,
        "/SmartDashboard/" + sys + "/UseMetric"));
    x += GRID;

    if (config.servoChannel >= 0) {
      for (var entry : config.servoPositions.entrySet()) {
        containers.add(textDisplay("Servo " + entry.getKey(), x, y, GRID, GRID,
            "/SmartDashboard/" + sys + "/Servo/" + entry.getKey(), "double", true));
        x += GRID;
      }
    }

    y += GRID;

    // Per motor: switchable graph (5x2) + chart type chooser + SP + AtSP + RPM text
    for (MotorConfig mc : config.motors) {
      String prefix = "/SmartDashboard/" + sys + "/" + mc.name + "/";

      // Switchable graph (5 wide, 2 tall) — shows ChartValue
      containers.add(graph(mc.name, 0, y, 5 * GRID, 2 * GRID,
          prefix + "ChartValue", 10.0, -1.0, -1.0));

      // Chart type chooser (dropdown)
      containers.add(comboBoxChooser("Chart", 5 * GRID, y, 2 * GRID, GRID,
          prefix + "ChartType"));

      // Chart label (shows what's being graphed)
      containers.add(textDisplay("Showing", 5 * GRID, y + GRID, 2 * GRID, GRID,
          prefix + "ChartLabel", "string", false));

      // Setpoint + AtSetpoint + RPM
      containers.add(textDisplay("SP", 7 * GRID, y, GRID, GRID,
          prefix + "SetpointRPM", "double", false));
      containers.add(booleanBox(mc.name + " OK", 7 * GRID, y + GRID, GRID, GRID,
          prefix + "AtSetpoint"));
      containers.add(textDisplay("RPM", 8 * GRID, y, GRID, GRID,
          prefix + "ActualRPM", "double", false));
      containers.add(textDisplay("Amps", 8 * GRID, y + GRID, GRID, GRID,
          prefix + "CurrentAmps", "double", false));

      y += 2 * GRID;
    }

    // Setpoint rows (editable)
    containers.add(textDisplay("--- Setpoints ---", 0, y, 2 * GRID, GRID,
        "/SmartDashboard/" + sys + "/Controls", "string", false));
    y += GRID;

    x = 0;
    for (var entry : config.setpoints.entrySet()) {
      containers.add(textDisplay(entry.getKey(), x, y, 2 * GRID, GRID,
          "/SmartDashboard/" + sys + "/Setpoints/" + entry.getKey(), "double", true));
      x += 2 * GRID;
      if (x >= 8 * GRID) {
        x = 0;
        y += GRID;
      }
    }

    return tab;
  }

  // ─── Per-Motor Tab (compact 6-row layout, 12 grid units wide) ─────
  //
  // Row 0: Mech | Ctrl | Geo | EN | ApplyPID | Mode | Run | Target | AtSP | CurrLim | ChartType | ChartLabel
  // Row 1: kP kI kD kV kS kA kG | kP_pos kD_pos | CruiseVel Accel Jerk
  // Row 2-3: Switchable Graph (7x2) | FwdLim RevLim kP(vel) kP(pos) kD(pos) / SysId Status(3) Apply AutoVelThr
  // Row 4: SysId kS kV kA kG | R²Accel R²Sim RMSE Samples | MaxEffort MaxVelErr MaxPosErr MeasDelay
  // Row 5: MedianWin VelThresh | EstInertia EstFriction EstMass Efficiency MaxAccel FreeSpeed | RealSpd RealPos

  private static ObjectNode buildMotorTab(MotorSystemConfig config, MotorConfig mc) {
    // Tab name includes workflow hint
    String hint = getWorkflowHint(mc.getMechanismType());
    ObjectNode tab = mapper.createObjectNode();
    tab.put("name", mc.name + " | " + hint);

    ObjectNode grid = tab.putObject("grid_layout");
    grid.putArray("layouts");
    ArrayNode containers = grid.putArray("containers");

    String sys = config.systemName;
    String prefix = "/SmartDashboard/" + sys + "/" + mc.name + "/";
    int y = 0;
    int x;

    // ── Row 0: Header + controls ──────────────────────────────────
    x = 0;
    containers.add(textDisplay(mc.mechanismType, x, y, GRID, GRID,
        prefix + "MechanismType", "string", false));
    x += GRID;
    containers.add(textDisplay(mc.controllerType, x, y, GRID, GRID,
        prefix + "ControllerType", "string", false));
    x += GRID;
    containers.add(textDisplay("Geo", x, y, GRID, GRID,
        prefix + "Geometry", "string", false));
    x += GRID;
    containers.add(toggleButton("EN", x, y, GRID, GRID,
        prefix + "Enabled"));
    x += GRID;
    containers.add(booleanBox("Apply", x, y, GRID, GRID,
        prefix + "ApplyPID"));
    x += GRID;
    containers.add(textDisplay("Mode", x, y, GRID, GRID,
        prefix + "ControlMode", "string", true));
    x += GRID;
    containers.add(toggleButton("Run", x, y, GRID, GRID,
        prefix + "Run"));
    x += GRID;
    containers.add(textDisplay("Target", x, y, GRID, GRID,
        prefix + "RunTarget", "double", true));
    x += GRID;
    containers.add(booleanBox("AtSP", x, y, GRID, GRID,
        prefix + "AtSetpoint"));
    x += GRID;
    containers.add(textDisplay("CurrLim", x, y, GRID, GRID,
        prefix + "CurrentLimit", "double", true));
    x += GRID;
    // Chart type chooser + label
    containers.add(comboBoxChooser("Chart", x, y, GRID, GRID,
        prefix + "ChartType"));
    x += GRID;
    containers.add(textDisplay("Showing", x, y, GRID, GRID,
        prefix + "ChartLabel", "string", false));
    y += GRID;

    // ── Row 1: PID + position PID + motion profiling ────────────
    x = 0;
    String[] pidParams = { "kP", "kI", "kD", "kV", "kS", "kA", "kG" };
    for (String param : pidParams) {
      containers.add(textDisplay(param, x, y, GRID, GRID,
          prefix + "PID/" + param, "double", true));
      x += GRID;
    }
    containers.add(textDisplay("kP_pos", x, y, GRID, GRID,
        prefix + "PID/kP_pos", "double", true));
    x += GRID;
    containers.add(textDisplay("kD_pos", x, y, GRID, GRID,
        prefix + "PID/kD_pos", "double", true));
    x += GRID;
    containers.add(textDisplay("Cruise", x, y, GRID, GRID,
        prefix + "Motion/CruiseVel", "double", true));
    x += GRID;
    containers.add(textDisplay("Accel", x, y, GRID, GRID,
        prefix + "Motion/Accel", "double", true));
    x += GRID;
    containers.add(textDisplay("Jerk", x, y, GRID, GRID,
        prefix + "Motion/Jerk", "double", true));
    y += GRID;

    // ── Row 2-3: Switchable graph (left) + SysId status/gains (right) ──
    // Switchable graph — 7 wide, 2 tall — shows ChartValue
    containers.add(graph(mc.name, 0, y, 7 * GRID, 2 * GRID,
        prefix + "ChartValue", 10.0, -1.0, -1.0));

    // Right column top (row 2): limits + LQR gains
    x = 7 * GRID;
    containers.add(textDisplay("FwdLim", x, y, GRID, GRID,
        prefix + "Limits/Forward", "double", true));
    x += GRID;
    containers.add(textDisplay("RevLim", x, y, GRID, GRID,
        prefix + "Limits/Reverse", "double", true));
    x += GRID;
    containers.add(textDisplay("kP(vel)", x, y, GRID, GRID,
        prefix + "SysId/kP_vel", "double", false));
    x += GRID;
    containers.add(textDisplay("kP(pos)", x, y, GRID, GRID,
        prefix + "SysId/kP_pos", "double", false));
    x += GRID;
    containers.add(textDisplay("kD(pos)", x, y, GRID, GRID,
        prefix + "SysId/kD_pos", "double", false));

    // Right column bottom (row 3): SysId status + apply + AutoQv
    x = 7 * GRID;
    containers.add(textDisplay("SysId Status", x, y + GRID, 3 * GRID, GRID,
        prefix + "SysId/Status", "string", false));
    containers.add(booleanBox("Apply SysId", 10 * GRID, y + GRID, GRID, GRID,
        prefix + "SysId/ApplyResults"));
    containers.add(textDisplay("AutoQv", 11 * GRID, y + GRID, GRID, GRID,
        prefix + "SysId/AutoQv", "double", false));
    y += 2 * GRID;

    // ── Row 4: SysId FF + quality + analysis params ─────────────
    x = 0;
    String[] ffParams = { "kS", "kV", "kA", "kG" };
    for (String param : ffParams) {
      containers.add(textDisplay("FF " + param, x, y, GRID, GRID,
          prefix + "SysId/" + param, "double", false));
      x += GRID;
    }
    containers.add(textDisplay("R²Accel", x, y, GRID, GRID,
        prefix + "SysId/R2_Accel", "double", false));
    x += GRID;
    containers.add(textDisplay("R²Sim", x, y, GRID, GRID,
        prefix + "SysId/R2_SimVel", "double", false));
    x += GRID;
    containers.add(textDisplay("RMSE", x, y, GRID, GRID,
        prefix + "SysId/RMSE", "double", false));
    x += GRID;
    containers.add(textDisplay("N", x, y, GRID, GRID,
        prefix + "SysId/Samples", "double", false));
    x += GRID;
    containers.add(textDisplay("MaxEff", x, y, GRID, GRID,
        prefix + "SysId/MaxEffort", "double", true));
    x += GRID;
    containers.add(textDisplay("MaxVelE", x, y, GRID, GRID,
        prefix + "SysId/MaxVelErr", "double", true));
    x += GRID;
    containers.add(textDisplay("MaxPosE", x, y, GRID, GRID,
        prefix + "SysId/MaxPosErr", "double", true));
    x += GRID;
    containers.add(textDisplay("MDelay", x, y, GRID, GRID,
        prefix + "SysId/MeasDelay(ms)", "double", false));
    y += GRID;

    // ── Row 5: Remaining analysis params + physical estimates + real-world ──
    x = 0;
    containers.add(textDisplay("MedWin", x, y, GRID, GRID,
        prefix + "SysId/MedianWindow", "double", true));
    x += GRID;
    containers.add(textDisplay("VelThr", x, y, GRID, GRID,
        prefix + "SysId/VelThreshold", "double", true));
    x += GRID;
    containers.add(textDisplay("Inertia", x, y, GRID, GRID,
        prefix + "SysId/Est_Inertia", "double", false));
    x += GRID;
    containers.add(textDisplay("Friction", x, y, GRID, GRID,
        prefix + "SysId/Est_Friction", "double", false));
    x += GRID;
    containers.add(textDisplay("Mass(kg)", x, y, GRID, GRID,
        prefix + "SysId/Est_Mass_kg", "double", false));
    x += GRID;
    containers.add(textDisplay("Eff", x, y, GRID, GRID,
        prefix + "SysId/Est_Efficiency", "double", false));
    x += GRID;
    containers.add(textDisplay("MaxAcc", x, y, GRID, GRID,
        prefix + "SysId/Est_MaxAccel", "double", false));
    x += GRID;
    containers.add(textDisplay("FreSpd", x, y, GRID, GRID,
        prefix + "SysId/Est_FreeSpeed", "double", false));
    x += GRID;
    // Real-world telemetry
    containers.add(textDisplay("RealSpd", x, y, GRID, GRID,
        prefix + "RealSpeed", "double", false));
    x += GRID;
    containers.add(textDisplay("RealPos", x, y, GRID, GRID,
        prefix + "RealPosition", "double", false));
    x += GRID;
    // Setpoints for this motor
    for (var entry : config.setpoints.entrySet()) {
      if (entry.getKey().contains(mc.name)) {
        String label = entry.getKey().replace("_" + mc.name, "");
        containers.add(textDisplay(label, x, y, GRID, GRID,
            "/SmartDashboard/" + sys + "/Setpoints/" + entry.getKey(), "double", true));
        x += GRID;
      }
    }

    return tab;
  }

  /** Returns a tuning workflow hint string based on mechanism type. */
  private static String getWorkflowHint(MechanismType mechType) {
    switch (mechType) {
      case SIMPLE:
        return "1.SysId 2.Apply 3.Test velocity";
      case ELEVATOR:
        return "1.Limits 2.SysId 3.Apply 4.Cruise/Accel 5.Profile";
      case ARM:
        return "1.Limits 2.SysId 3.Apply 4.Cruise/Accel 5.Profile";
      default:
        return "1.SysId 2.Apply 3.Test";
    }
  }

  // ─── Widget Builders ───────────────────────────────────────────────

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
