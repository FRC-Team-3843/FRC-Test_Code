package frc.robot.motor;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.controller.LinearQuadraticRegulator;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N2;
import edu.wpi.first.math.system.LinearSystem;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.system.plant.LinearSystemId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.ejml.simple.SimpleMatrix;

/**
 * On-robot SysId analyzer matching WPILib's desktop SysId tool algorithm.
 *
 * <p>Key differences from a naive approach:
 * <ul>
 *   <li>OLS regresses <b>acceleration</b> as dependent variable (not voltage)</li>
 *   <li>Median filter for velocity smoothing (not moving average)</li>
 *   <li>Auto-computed velocity threshold from noise floor</li>
 *   <li>Dynamic test trimming (peak accel → 90% max speed)</li>
 *   <li>ARM model uses cos + sin terms (5 variables)</li>
 *   <li>Adjusted R² and WPILib-style sim R²</li>
 *   <li>LQR with latency compensation and output conversion</li>
 * </ul>
 */
public class SysIdAnalyzer {

  /** A single data sample collected during a SysId test. */
  public static class DataPoint {
    public final double timestamp;
    public final double voltage;
    public final double velocityRps;
    public final double positionRot;

    public DataPoint(double timestamp, double voltage, double velocityRps, double positionRot) {
      this.timestamp = timestamp;
      this.voltage = voltage;
      this.velocityRps = velocityRps;
      this.positionRot = positionRot;
    }
  }

  /** Result of the OLS regression and LQR feedback gain analysis. */
  public static class AnalysisResult {
    // Feedforward gains (from OLS on acceleration)
    public final double kS;
    public final double kV;
    public final double kA;
    public final double kG;

    // Quality metrics
    public final double rSquaredAccel;  // adjusted R² on acceleration OLS
    public final double rSquaredSimVel; // WPILib-style 1-RMSE/RMS sim R²
    public final double rmse;           // root mean square error of forward sim
    public final int sampleCount;

    // Feedback gains (from LQR, in voltage domain)
    public final double kP_velocity;
    public final double kP_position;
    public final double kD_position;

    // Diagnostics
    public final double autoVelocityThreshold; // computed from noise floor
    public final double autoQv;                // auto-computed max velocity error

    // LQR parameters used
    public final double maxControlEffort;
    public final double maxVelocityError;
    public final double maxPositionError;

    // Physical estimates
    public final PhysicalEstimate physicalEstimate;

    public final boolean valid;
    public final String errorMessage;

    private AnalysisResult(double kS, double kV, double kA, double kG,
                           double rSquaredAccel, double rSquaredSimVel, double rmse,
                           int sampleCount,
                           double kP_velocity, double kP_position, double kD_position,
                           double autoVelocityThreshold, double autoQv,
                           double maxControlEffort, double maxVelocityError, double maxPositionError,
                           PhysicalEstimate physicalEstimate,
                           boolean valid, String errorMessage) {
      this.kS = kS;
      this.kV = kV;
      this.kA = kA;
      this.kG = kG;
      this.rSquaredAccel = rSquaredAccel;
      this.rSquaredSimVel = rSquaredSimVel;
      this.rmse = rmse;
      this.sampleCount = sampleCount;
      this.kP_velocity = kP_velocity;
      this.kP_position = kP_position;
      this.kD_position = kD_position;
      this.autoVelocityThreshold = autoVelocityThreshold;
      this.autoQv = autoQv;
      this.maxControlEffort = maxControlEffort;
      this.maxVelocityError = maxVelocityError;
      this.maxPositionError = maxPositionError;
      this.physicalEstimate = physicalEstimate;
      this.valid = valid;
      this.errorMessage = errorMessage;
    }

    static AnalysisResult success(double kS, double kV, double kA, double kG,
                                  double rSquaredAccel, double rSquaredSimVel, double rmse,
                                  int sampleCount,
                                  double kP_velocity, double kP_position, double kD_position,
                                  double autoVelocityThreshold, double autoQv,
                                  double maxControlEffort, double maxVelocityError,
                                  double maxPositionError,
                                  PhysicalEstimate physicalEstimate) {
      return new AnalysisResult(kS, kV, kA, kG, rSquaredAccel, rSquaredSimVel, rmse,
          sampleCount, kP_velocity, kP_position, kD_position,
          autoVelocityThreshold, autoQv,
          maxControlEffort, maxVelocityError, maxPositionError,
          physicalEstimate, true, "");
    }

    static AnalysisResult error(String message) {
      return new AnalysisResult(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
          PhysicalEstimate.invalid(), false, message);
    }
  }

  /** Physical estimates derived from feedforward gains + motor constants + mechanism geometry. */
  public static class PhysicalEstimate {
    public final double momentOfInertia;   // kg*m^2 (mechanism-side)
    public final double frictionTorque;    // N*m
    public final double estimatedMass;     // kg (from kG, ARM/ELEVATOR only, 0 for SIMPLE)
    public final double efficiency;        // 0-1 (measured kV vs theoretical)
    public final double maxAcceleration;   // mechanism rot/s^2
    public final double freeSpeed;         // mechanism rot/s
    public final boolean valid;

    public PhysicalEstimate(double momentOfInertia, double frictionTorque,
                            double estimatedMass, double efficiency,
                            double maxAcceleration, double freeSpeed, boolean valid) {
      this.momentOfInertia = momentOfInertia;
      this.frictionTorque = frictionTorque;
      this.estimatedMass = estimatedMass;
      this.efficiency = efficiency;
      this.maxAcceleration = maxAcceleration;
      this.freeSpeed = freeSpeed;
      this.valid = valid;
    }

    public static PhysicalEstimate invalid() {
      return new PhysicalEstimate(0, 0, 0, 0, 0, 0, false);
    }

    /**
     * Computes physical estimates from feedforward gains + motor constants + geometry.
     *
     * @param kS static friction voltage
     * @param kV velocity gain (V/(rot/s))
     * @param kA acceleration gain (V/(rot/s^2))
     * @param kG gravity gain (V)
     * @param dcMotor WPILib DCMotor model (null = cannot estimate)
     * @param gearRatio gear ratio (motor rotations per mechanism rotation)
     * @param mechType mechanism type
     * @param distPerRot distance per mechanism rotation in inches (ELEVATOR)
     * @param armLengthIn arm length in inches (ARM)
     */
    public static PhysicalEstimate compute(double kS, double kV, double kA, double kG,
                                            DCMotor dcMotor, double gearRatio,
                                            MechanismType mechType,
                                            double distPerRot, double armLengthIn) {
      if (dcMotor == null || kV <= 0 || kA <= 0) {
        return invalid();
      }

      double R = dcMotor.rOhms;
      double Kt = dcMotor.KtNMPerAmp;

      // Moment of inertia: J = kA * Kt * G / R  (mechanism-referenced)
      double J = kA * Kt * gearRatio / R;

      // Friction torque: tau_f = kS * Kt * G / R
      double frictionTorque = Math.abs(kS) * Kt * gearRatio / R;

      // Efficiency: eta = (Ke_theoretical * G) / kV_measured
      // Ke in rot/s units = 1 / (KvRadPerSecPerVolt / (2*pi))
      double Ke_rps = (2.0 * Math.PI) / dcMotor.KvRadPerSecPerVolt;
      double efficiency = (Ke_rps * gearRatio) / kV;
      efficiency = Math.min(efficiency, 1.5); // cap at 150% for display

      // Mass estimation
      double estimatedMass = 0.0; // kg
      double g = 9.81; // m/s^2
      if (mechType == MechanismType.ELEVATOR && distPerRot > 0 && kG > 0) {
        // r = distPerRot / (2*pi) converted to meters
        double r_m = (distPerRot * 0.0254) / (2.0 * Math.PI);
        estimatedMass = kG * Kt * gearRatio / (g * r_m * R);
      } else if (mechType == MechanismType.ARM && armLengthIn > 0 && kG > 0) {
        // L_com = armLength/2 converted to meters (center of mass)
        double L_com_m = (armLengthIn / 2.0) * 0.0254;
        estimatedMass = kG * Kt * gearRatio / (g * L_com_m * R);
      }

      // Max acceleration: a_max = (12.0 - |kS|) / kA  (rot/s^2)
      double maxAccel = (12.0 - Math.abs(kS)) / kA;

      // Free speed: v_free = 12.0 / kV  (rot/s at 12V)
      double freeSpeed = 12.0 / kV;

      System.out.printf("[PhysicalEstimate] J=%.6f kg*m^2, friction=%.4f N*m, mass=%.2f kg, eff=%.1f%%, maxAccel=%.1f rot/s^2, freeSpeed=%.1f rot/s%n",
          J, frictionTorque, estimatedMass, efficiency * 100, maxAccel, freeSpeed);

      return new PhysicalEstimate(J, frictionTorque, estimatedMass, efficiency,
          maxAccel, freeSpeed, true);
    }
  }

  // Phase detection
  private static final double PHASE_GAP_THRESHOLD = 0.1; // seconds
  private static final int NOISE_MEAN_WINDOW = 9;

  private final MechanismType mechanismType;
  private final SysIdParams params;
  private final DCMotor dcMotor;
  private final double gearRatio;
  private final double distPerRot;
  private final double armLengthIn;

  public SysIdAnalyzer(MechanismType mechanismType, SysIdParams params) {
    this(mechanismType, params, null, 1.0, 0.0, 0.0);
  }

  public SysIdAnalyzer(MechanismType mechanismType, SysIdParams params,
                        DCMotor dcMotor, double gearRatio,
                        double distPerRot, double armLengthIn) {
    this.mechanismType = mechanismType;
    this.params = params;
    this.dcMotor = dcMotor;
    this.gearRatio = gearRatio;
    this.distPerRot = distPerRot;
    this.armLengthIn = armLengthIn;
  }

  /**
   * Analyzes collected data points and returns feedforward + feedback gains.
   * Matches WPILib's SysId desktop tool algorithm.
   */
  public AnalysisResult analyze(List<DataPoint> data) {
    if (data.size() < 20) {
      return AnalysisResult.error("Not enough data points: " + data.size() + " (need >= 20)");
    }

    // Split data into 4 test phases by timestamp gaps
    List<List<DataPoint>> phases = splitPhases(data);
    System.out.printf("[SysIdAnalyzer] Split %d points into %d phases%n", data.size(), phases.size());

    if (phases.size() < 4) {
      return AnalysisResult.error("Expected 4 test phases, found " + phases.size());
    }

    // Phases: 0=quasistatic forward, 1=quasistatic reverse, 2=dynamic forward, 3=dynamic reverse
    List<DataPoint> quasiForward = phases.get(0);
    List<DataPoint> quasiReverse = phases.get(1);
    List<DataPoint> dynForward = phases.get(2);
    List<DataPoint> dynReverse = phases.get(3);

    // Compute auto velocity threshold from quasistatic noise floor
    double autoVelThreshold = params.velocityThreshold;
    if (autoVelThreshold <= 0) {
      double noiseF = computeNoiseFloor(quasiForward);
      double noiseR = computeNoiseFloor(quasiReverse);
      autoVelThreshold = Math.min(noiseF, noiseR);
      if (autoVelThreshold <= 0) autoVelThreshold = 0.1;
      System.out.printf("[SysIdAnalyzer] Auto velocity threshold: %.6f RPS (noiseF=%.6f, noiseR=%.6f)%n",
          autoVelThreshold, noiseF, noiseR);
    }

    // Preprocess each phase
    List<double[]> quasiData = preprocessQuasistatic(quasiForward, autoVelThreshold);
    quasiData.addAll(preprocessQuasistatic(quasiReverse, autoVelThreshold));

    List<double[]> dynData = preprocessDynamic(dynForward, autoVelThreshold);
    dynData.addAll(preprocessDynamic(dynReverse, autoVelThreshold));

    System.out.printf("[SysIdAnalyzer] After preprocessing: quasi=%d, dynamic=%d%n",
        quasiData.size(), dynData.size());

    // Combine all data for OLS
    List<double[]> allData = new ArrayList<>(quasiData);
    allData.addAll(dynData);

    // Remove any points where acceleration is exactly 0
    allData.removeIf(row -> row[2] == 0.0);

    if (allData.size() < 6) {
      return AnalysisResult.error("Not enough filtered data: " + allData.size() + " (need >= 6)");
    }

    // OLS regression: acceleration as dependent variable
    // Design matrix columns depend on mechanism type
    int n = allData.size();
    int cols = getColumnCount();

    SimpleMatrix X = new SimpleMatrix(n, cols);
    SimpleMatrix y = new SimpleMatrix(n, 1);

    for (int i = 0; i < n; i++) {
      double[] row = allData.get(i);
      double voltage = row[0];
      double vel = row[1];
      double accel = row[2];
      double posRad = row[3]; // already in radians for ARM

      y.set(i, 0, accel);
      buildDesignRow(X, i, vel, voltage, posRad);
    }

    // Solve OLS via normal equations: β = (X'X)^-1 X'y
    SimpleMatrix Xt = X.transpose();
    SimpleMatrix XtX = Xt.mult(X);

    double det = XtX.determinant();
    if (Math.abs(det) < 1e-12) {
      return AnalysisResult.error("Design matrix is singular (det=" + det + ")");
    }

    SimpleMatrix beta = XtX.invert().mult(Xt).mult(y);

    // Extract feedforward gains from regression coefficients
    double alpha = beta.get(0, 0); // velocity coefficient = -kV/kA
    double betaV = beta.get(1, 0); // voltage coefficient = 1/kA
    double gamma = beta.get(2, 0); // sgn(velocity) coefficient = -kS/kA

    if (Math.abs(betaV) < 1e-12) {
      return AnalysisResult.error("Voltage coefficient is zero — cannot extract gains");
    }

    double kS = -gamma / betaV;
    double kV = -alpha / betaV;
    double kA = 1.0 / betaV;
    double kG = 0.0;

    if (mechanismType == MechanismType.ELEVATOR) {
      double delta = beta.get(3, 0); // constant = -kG/kA
      kG = -delta / betaV;
    } else if (mechanismType == MechanismType.ARM) {
      double delta = beta.get(3, 0); // cos coefficient
      double epsilon = beta.get(4, 0); // sin coefficient
      kG = Math.hypot(delta, epsilon) / betaV;
    }

    // Compute adjusted R²
    SimpleMatrix yHat = X.mult(beta);
    SimpleMatrix residuals = y.minus(yHat);

    double sse = 0.0;
    double ySum = 0.0;
    double ySqSum = 0.0;
    for (int i = 0; i < n; i++) {
      double resid = residuals.get(i, 0);
      sse += resid * resid;
      double yi = y.get(i, 0);
      ySum += yi;
      ySqSum += yi * yi;
    }

    // SSTO = y'y - (1/n)(Σy)² — WPILib formula
    double ssto = ySqSum - (ySum * ySum) / n;
    double rSquared = (ssto > 0) ? 1.0 - (sse / ssto) : 0.0;
    double adjRSquared = 1.0 - (1.0 - rSquared) * (n - 1.0) / (n - cols - 1.0);

    // Compute forward simulation R² (WPILib-style: 1 - RMSE/RMS)
    double[] simResult = computeSimVelocityR2(data, kS, kV, kA, kG);
    double rSquaredSimVel = simResult[0];
    double rmse = simResult[1];

    // LQR feedback gains
    double autoQv = params.maxVelocityError;
    if (autoQv <= 0 && kV > 0) {
      // Auto-compute: qv = 0.25 * (maxEffort - kS) / kV
      autoQv = 0.25 * (params.maxControlEffort - Math.abs(kS)) / kV;
      System.out.printf("[SysIdAnalyzer] Auto qv: %.4f RPS%n", autoQv);
    }

    double kP_velocity = 0.0;
    double kP_position = 0.0;
    double kD_position = 0.0;

    if (kV > 0 && kA > 0 && autoQv > 0) {
      try {
        // Velocity control: 1-state [velocity]
        LinearSystem<N1, N1, N1> velocityPlant =
            LinearSystemId.identifyVelocitySystem(kV, kA);
        LinearQuadraticRegulator<N1, N1, N1> velocityLQR =
            new LinearQuadraticRegulator<>(
                velocityPlant,
                VecBuilder.fill(autoQv),
                VecBuilder.fill(params.maxControlEffort),
                params.controllerPeriod);
        kP_velocity = velocityLQR.getK().get(0, 0);

        // Apply output conversion factor and latency compensation
        kP_velocity *= params.outputConversionFactor;

        // Position control: 2-state [position, velocity]
        if (params.maxPositionError > 0) {
          LinearSystem<N2, N1, N2> positionPlant =
              LinearSystemId.identifyPositionSystem(kV, kA);
          LinearQuadraticRegulator<N2, N1, N2> positionLQR =
              new LinearQuadraticRegulator<>(
                  positionPlant,
                  VecBuilder.fill(params.maxPositionError, autoQv),
                  VecBuilder.fill(params.maxControlEffort),
                  params.controllerPeriod);
          kP_position = positionLQR.getK().get(0, 0) * params.outputConversionFactor;
          kD_position = positionLQR.getK().get(0, 1) * params.outputConversionFactor;
        }
      } catch (Exception e) {
        System.out.println("[SysIdAnalyzer] LQR failed: " + e.getMessage());
      }
    }

    // Compute physical estimates from gains + motor constants + geometry
    PhysicalEstimate physEst = PhysicalEstimate.compute(
        kS, kV, kA, kG, dcMotor, gearRatio, mechanismType, distPerRot, armLengthIn);

    System.out.printf("[SysIdAnalyzer] Results: kS=%.6f kV=%.6f kA=%.6f kG=%.6f adjR²=%.6f simR²=%.6f RMSE=%.6f n=%d%n",
        kS, kV, kA, kG, adjRSquared, rSquaredSimVel, rmse, n);

    return AnalysisResult.success(kS, kV, kA, kG, adjRSquared, rSquaredSimVel, rmse, n,
        kP_velocity, kP_position, kD_position,
        autoVelThreshold, autoQv,
        params.maxControlEffort, autoQv, params.maxPositionError,
        physEst);
  }

  // ─── Preprocessing ─────────────────────────────────────────────────

  /** Splits data into phases by detecting timestamp gaps > PHASE_GAP_THRESHOLD. */
  private List<List<DataPoint>> splitPhases(List<DataPoint> data) {
    List<List<DataPoint>> phases = new ArrayList<>();
    List<DataPoint> current = new ArrayList<>();

    for (int i = 0; i < data.size(); i++) {
      if (i > 0 && data.get(i).timestamp - data.get(i - 1).timestamp > PHASE_GAP_THRESHOLD) {
        if (!current.isEmpty()) {
          phases.add(current);
          current = new ArrayList<>();
        }
      }
      current.add(data.get(i));
    }
    if (!current.isEmpty()) {
      phases.add(current);
    }
    return phases;
  }

  /**
   * Preprocesses quasistatic data: median filter, compute acceleration,
   * filter by velocity threshold and voltage polarity.
   *
   * @return list of [voltage, velocity, acceleration, positionRad]
   */
  private List<double[]> preprocessQuasistatic(List<DataPoint> phase, double velThreshold) {
    if (phase.size() < 5) return new ArrayList<>();

    // Apply median filter to velocities
    double[] velocities = medianFilter(
        phase.stream().mapToDouble(p -> p.velocityRps).toArray(),
        params.medianWindowSize);

    // Compute mean dt (only valid intervals 0 < dt < 0.5s)
    double meanDt = computeMeanDt(phase);
    if (meanDt <= 0) return new ArrayList<>();

    // Compute acceleration via 3-point central difference
    double[] accels = centralDifference(velocities, meanDt);

    // Filter: remove low-velocity, wrong-polarity-voltage, zero-accel
    List<double[]> result = new ArrayList<>();
    for (int i = 0; i < phase.size(); i++) {
      double vel = velocities[i];
      double accel = accels[i];
      double voltage = phase.get(i).voltage;
      double posRad = phase.get(i).positionRot * 2.0 * Math.PI;

      // Ensure voltage polarity matches velocity polarity (WPILib)
      voltage = Math.copySign(Math.abs(voltage), vel);

      if (Math.abs(vel) < velThreshold) continue;
      if (Math.abs(voltage) <= 0) continue;
      if (accel == 0.0) continue;

      result.add(new double[] { voltage, vel, accel, posRad });
    }
    return result;
  }

  /**
   * Preprocesses dynamic (step voltage) data: median filter, compute acceleration,
   * trim to peak acceleration → 90% max speed.
   *
   * @return list of [voltage, velocity, acceleration, positionRad]
   */
  private List<double[]> preprocessDynamic(List<DataPoint> phase, double velThreshold) {
    if (phase.size() < 5) return new ArrayList<>();

    // Apply median filter to velocities
    double[] velocities = medianFilter(
        phase.stream().mapToDouble(p -> p.velocityRps).toArray(),
        params.medianWindowSize);

    // Compute mean dt
    double meanDt = computeMeanDt(phase);
    if (meanDt <= 0) return new ArrayList<>();

    // Compute acceleration
    double[] accels = centralDifference(velocities, meanDt);

    // Find peak acceleration (accounting for direction)
    int maxAccelIdx = 0;
    double maxAccelValue = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < phase.size(); i++) {
      double signedAccel = Math.signum(velocities[i]) * accels[i];
      if (signedAccel > maxAccelValue) {
        maxAccelValue = signedAccel;
        maxAccelIdx = i;
      }
    }

    // Find 90% of max speed — trim end
    double maxSpeed = 0.0;
    for (int i = maxAccelIdx; i < phase.size(); i++) {
      maxSpeed = Math.max(maxSpeed, Math.abs(velocities[i]));
    }
    int endIdx = phase.size();
    for (int i = maxAccelIdx; i < phase.size(); i++) {
      if (Math.abs(velocities[i]) > 0.9 * maxSpeed) {
        endIdx = i;
        break;
      }
    }

    // Build filtered data from peak accel to 90% max speed
    List<double[]> result = new ArrayList<>();
    for (int i = maxAccelIdx; i < endIdx; i++) {
      double vel = velocities[i];
      double accel = accels[i];
      double voltage = phase.get(i).voltage;
      double posRad = phase.get(i).positionRot * 2.0 * Math.PI;

      if (Math.abs(vel) < velThreshold) continue;
      if (accel == 0.0) continue;

      result.add(new double[] { voltage, vel, accel, posRad });
    }

    System.out.printf("[SysIdAnalyzer] Dynamic trim: %d→%d (peakAccel@%d, end@%d, maxSpeed=%.2f)%n",
        phase.size(), result.size(), maxAccelIdx, endIdx, maxSpeed);

    return result;
  }

  // ─── Median Filter ─────────────────────────────────────────────────

  /**
   * Applies a centered median filter to the velocity array.
   * Matches WPILib's causal-shifted approach.
   */
  private double[] medianFilter(double[] velocities, int window) {
    if (window <= 1 || velocities.length < window) {
      return velocities.clone();
    }

    int halfW = (window - 1) / 2;
    double[] result = velocities.clone();
    double[] buf = new double[window];

    for (int i = 0; i < velocities.length; i++) {
      // Gather window centered at i
      int start = Math.max(0, i - halfW);
      int end = Math.min(velocities.length - 1, i + halfW);
      int len = end - start + 1;

      System.arraycopy(velocities, start, buf, 0, len);
      Arrays.sort(buf, 0, len);
      result[i] = buf[len / 2];
    }
    return result;
  }

  // ─── Acceleration (Central Difference) ─────────────────────────────

  /**
   * Computes acceleration via 3-point central finite difference.
   * accel[i] = (v[i+1] - v[i-1]) / (2 * meanDt)
   */
  private double[] centralDifference(double[] velocity, double meanDt) {
    int n = velocity.length;
    double[] accel = new double[n];
    double twoDt = 2.0 * meanDt;

    // Boundary: forward/backward difference
    if (n >= 2) {
      accel[0] = (velocity[1] - velocity[0]) / meanDt;
      accel[n - 1] = (velocity[n - 1] - velocity[n - 2]) / meanDt;
    }

    // Interior: central difference
    for (int i = 1; i < n - 1; i++) {
      accel[i] = (velocity[i + 1] - velocity[i - 1]) / twoDt;
    }
    return accel;
  }

  // ─── Noise Floor ───────────────────────────────────────────────────

  /**
   * Computes velocity noise floor from quasistatic data.
   * Uses a moving average of window=9, then RMSE of deviations.
   */
  private double computeNoiseFloor(List<DataPoint> phase) {
    if (phase.size() < NOISE_MEAN_WINDOW) return 0.1;

    int step = NOISE_MEAN_WINDOW / 2;
    double[] velocities = phase.stream().mapToDouble(p -> p.velocityRps).toArray();

    // Moving average
    double[] means = new double[velocities.length];
    double runningSum = 0.0;
    int count = 0;

    for (int i = 0; i < velocities.length; i++) {
      runningSum += velocities[i];
      count++;
      if (count > NOISE_MEAN_WINDOW) {
        runningSum -= velocities[i - NOISE_MEAN_WINDOW];
        count = NOISE_MEAN_WINDOW;
      }
      means[i] = runningSum / count;
    }

    // RMSE of (velocity - mean) for indices >= step
    double sumSq = 0.0;
    int validCount = 0;
    for (int i = step; i < velocities.length; i++) {
      double diff = velocities[i - step] - means[i];
      sumSq += diff * diff;
      validCount++;
    }

    return (validCount > 0) ? Math.sqrt(sumSq / validCount) : 0.1;
  }

  // ─── Design Matrix ─────────────────────────────────────────────────

  /** Returns column count for the OLS design matrix. */
  private int getColumnCount() {
    switch (mechanismType) {
      case SIMPLE: return 3;     // [vel, voltage, sgn(vel)]
      case ELEVATOR: return 4;   // [vel, voltage, sgn(vel), 1.0]
      case ARM: return 5;        // [vel, voltage, sgn(vel), cos(pos), sin(pos)]
      default: return 3;
    }
  }

  /** Builds one row of the design matrix. */
  private void buildDesignRow(SimpleMatrix X, int row, double vel, double voltage, double posRad) {
    X.set(row, 0, vel);               // velocity
    X.set(row, 1, voltage);           // voltage
    X.set(row, 2, Math.signum(vel));  // sign(velocity)

    switch (mechanismType) {
      case ELEVATOR:
        X.set(row, 3, 1.0);           // constant gravity term
        break;
      case ARM:
        X.set(row, 3, Math.cos(posRad));
        X.set(row, 4, Math.sin(posRad));
        break;
      default:
        break;
    }
  }

  // ─── Forward Simulation ────────────────────────────────────────────

  /** Computes mean dt from timestamp differences, filtering outliers (0 < dt < 0.5s). */
  private double computeMeanDt(List<DataPoint> phase) {
    double sum = 0.0;
    int count = 0;
    for (int i = 1; i < phase.size(); i++) {
      double dt = phase.get(i).timestamp - phase.get(i - 1).timestamp;
      if (dt > 0 && dt < 0.5) {
        sum += dt;
        count++;
      }
    }
    return (count > 0) ? sum / count : 0.02;
  }

  /**
   * Forward-simulates the identified model and computes WPILib-style sim R².
   *
   * <p>WPILib uses: simR² = 1 - RMSE / RMS(velocity), NOT standard R².
   *
   * @return [simR², RMSE]
   */
  private double[] computeSimVelocityR2(List<DataPoint> data, double kS, double kV, double kA, double kG) {
    if (data.size() < 2 || Math.abs(kA) < 1e-10) return new double[] { 0.0, 0.0 };

    double simSquaredErrorSum = 0.0;
    double squaredVelocitySum = 0.0;
    double simVel = data.get(0).velocityRps;
    int count = 0;

    for (int i = 1; i < data.size(); i++) {
      double dt = data.get(i).timestamp - data.get(i - 1).timestamp;
      double actualVel = data.get(i).velocityRps;

      // Reset at phase boundaries
      if (dt > PHASE_GAP_THRESHOLD || dt <= 0) {
        simVel = actualVel;
        continue;
      }

      double v = simVel;
      double voltage = data.get(i - 1).voltage;

      double gravityTerm = 0.0;
      if (mechanismType == MechanismType.ELEVATOR) {
        gravityTerm = kG;
      } else if (mechanismType == MechanismType.ARM) {
        double posRad = data.get(i - 1).positionRot * 2.0 * Math.PI;
        gravityTerm = kG * Math.cos(posRad);
      }

      // dv/dt = (V - kS*sign(v) - kV*v - gravityTerm) / kA
      double accel = (voltage - kS * Math.signum(v) - kV * v - gravityTerm) / kA;
      simVel = v + accel * dt;

      double error = actualVel - simVel;
      simSquaredErrorSum += error * error;
      squaredVelocitySum += actualVel * actualVel;
      count++;
    }

    if (count == 0) return new double[] { 0.0, 0.0 };

    double rmseVal = Math.sqrt(simSquaredErrorSum / count);
    double rmsVel = Math.sqrt(squaredVelocitySum / count);

    // WPILib formula: 1 - RMSE / RMS(velocity)
    double simR2 = (rmsVel > 0) ? 1.0 - (rmseVal / rmsVel) : 0.0;

    return new double[] { simR2, rmseVal };
  }
}
