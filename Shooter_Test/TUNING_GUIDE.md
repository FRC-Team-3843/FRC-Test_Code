# Motor System Tuning Guide

Per-mechanism-type tuning workflow for the universal motor system.

---

## SIMPLE (Flywheel / Drive Wheel)

**Use case:** Shooters, intakes, drive motors — velocity control

1. **Configure motor-config.json:**
   - `name`, `controllerType`, `motorKind`, `canId`, `gearRatio`
   - `wheelDiameter` (inches) — for real-world unit display (ft/s)
   - `mechanismType`: `"SIMPLE"`

2. **Set current limit:**
   - Default 40A, increase to 60-80A for high-demand shooters
   - Editable on dashboard: `CurrentLimit`

3. **Run SysId:**
   - Hold the configured SysId button ~10 seconds (runs all 4 phases automatically)
   - Watch SysId Status on the motor tab for progress

4. **Review results:**
   - `R² Accel` > 0.95 — good fit
   - `kS` reasonable: 0.1-1.5V (static friction)
   - `Est Efficiency` > 80% — mechanical losses acceptable
   - `Est FreeSpeed` matches expected free speed at mechanism

5. **Apply SysId results:**
   - Click "Apply SysId" toggle on dashboard
   - Feedforward (kS, kV, kA) and kP_velocity are auto-set
   - Config saved to JSON automatically

6. **Test velocity control:**
   - Set `ControlMode` to `velocity`
   - Set `RunTarget` to desired RPM
   - Toggle `Run` — verify motor reaches setpoint

7. **Optional — velocity ramping:**
   - Set `Motion/CruiseVel` (rot/s) and `Motion/Accel` (rot/s²)
   - Click Apply PID to save
   - Use `ControlMode: profile` to test

---

## ELEVATOR

**Use case:** Linear mechanisms — position + motion profiling

1. **Configure motor-config.json:**
   - `name`, `controllerType`, `motorKind`, `canId`, `gearRatio`
   - `distancePerRotation` (inches) — linear travel per mechanism rotation
   - `mass` (lbs) — mechanism mass for estimation validation
   - `mechanismType`: `"ELEVATOR"`

2. **Set soft limits:**
   - `Limits/Forward` — max height (in rotations)
   - `Limits/Reverse` — min height (in rotations, often 0)
   - These are controller-native limits — motor won't exceed them

3. **Set current limit:**
   - Default 40A, adjust based on load

4. **Run SysId:**
   - Ensure mechanism can move through full range safely
   - Hold SysId button — 4 phases run automatically

5. **Review results:**
   - `kG` should match expected gravity load (non-zero for elevator)
   - `Est Mass(kg)` should be close to actual mechanism mass
   - `R² Accel` > 0.95

6. **Apply SysId results:**
   - Click "Apply SysId"
   - Sets feedforward + velocity kP

7. **Enable position control:**
   - Set `MaxPosErr` > 0 in SysId params (e.g., 0.1 rotations)
   - Re-run SysId or re-apply to get position gains (kP_pos, kD_pos)

8. **Configure motion profiling:**
   - `Motion/CruiseVel` — cruise velocity (rot/s)
   - `Motion/Accel` — max acceleration (rot/s²)
   - `Motion/Jerk` — optional S-curve smoothing (rot/s³, 0 = trapezoidal)
   - Click Apply PID to save

9. **Test profiled position:**
   - Set `ControlMode` to `profile`
   - Set `RunTarget` to target position (rotations)
   - Toggle `Run` — motor should move smoothly to position

---

## ARM

**Use case:** Rotating mechanisms — position + gravity compensation + motion profiling

1. **Configure motor-config.json:**
   - `name`, `controllerType`, `motorKind`, `canId`, `gearRatio`
   - `armLength` (inches) — pivot-to-tip distance
   - `mass` (lbs) — arm mass for estimation validation
   - `mechanismType`: `"ARM"`

2. **Set soft limits:**
   - `Limits/Forward` — max angle (in rotations)
   - `Limits/Reverse` — min angle (in rotations)
   - Critical for arms — prevents mechanical damage

3. **Set current limit:**
   - Default 40A, adjust based on arm mass and speed

4. **Run SysId:**
   - Ensure arm moves through full range of motion during test
   - SysId uses cos+sin model to capture gravity variation with angle

5. **Review results:**
   - `kG` represents gravity compensation voltage — should be significant
   - `Est Mass(kg)` x `armLength/2` should match expected moment
   - ARM model uses 5-variable OLS (velocity, voltage, sign, cos, sin)

6. **Apply SysId results:**
   - Click "Apply SysId"
   - Gravity compensation is included in feedforward

7. **Enable position control:**
   - Set `MaxPosErr` > 0 for LQR position gains
   - Re-apply SysId

8. **Configure motion profiling:**
   - `Motion/CruiseVel`, `Motion/Accel`, `Motion/Jerk`
   - Arms typically need lower cruise velocity than flywheels
   - Click Apply PID to save

9. **Test profiled position:**
   - Set `ControlMode` to `profile`
   - Test at multiple positions (gravity load varies with angle)
   - Verify arm holds position at rest (kG working)

---

## Dashboard Reference

### Per-Motor Tab Layout

| Row | Fields |
|-----|--------|
| 0 | Workflow hint (tuning steps) |
| 1 | Mechanism, Controller, Geometry, Enable, Apply PID, Mode, Run, Target, At SP |
| 2 | kP, kI, kD, kV, kS, kA, kG, kP_pos, kD_pos, setpoints |
| 3 | CruiseVel, Accel, Jerk, FwdLimit, RevLimit, CurrLimit, RealSpeed, RealPos |
| 4-5 | RPM Graph, Current Graph, LQR feedback gains |
| 6 | SysId Status, Apply SysId, AutoVelThr, Samples, RMSE, R-squared |
| 7 | SysId FF gains, MaxEffort, MaxVelErr, MaxPosErr, MedianWin, VelThresh |
| 8 | Physical estimates: Inertia, Friction, Mass, Efficiency, MaxAccel, FreeSpeed |

### Control Modes

| Mode | Description |
|------|-------------|
| `velocity` | Closed-loop velocity (RPM via RunTarget) |
| `position` | Closed-loop position (rotations via RunTarget) |
| `profile` | Motion-profiled position (trapezoidal/S-curve) |

### Unit Toggle

- `UseMetric` toggle on Overview tab switches all real-world displays:
  - Imperial: ft/s, inches, degrees, lbs
  - Metric: m/s, meters, radians, kg

### Physical Estimates (after SysId)

| Estimate | What it means |
|----------|---------------|
| `Est Inertia` | Mechanism-side moment of inertia (kg*m^2) |
| `Est Friction` | Static friction torque (N*m) |
| `Est Mass` | Estimated mechanism mass from kG (ELEVATOR/ARM only) |
| `Efficiency` | Measured vs theoretical motor constant (0-1, >0.8 good) |
| `MaxAccel` | Max achievable acceleration (rot/s^2) |
| `FreeSpeed` | Free speed at 12V (rot/s) |

---

## Troubleshooting

**SysId R-squared too low (<0.9):**
- Ensure motor moves freely during test (no binding)
- Check gear ratio is correct in config
- Try increasing test duration (default ~10s per phase)
- Verify encoder is working (check ActualRPM graph)

**kS unexpectedly high (>2V):**
- High mechanical friction — check bearings, alignment
- Gear ratio might be wrong (kS scales with gearing)

**Estimated mass way off:**
- Verify gear ratio and distancePerRotation/armLength
- Check that kG was computed (non-zero) — only for ELEVATOR/ARM
- Mass estimate assumes center of mass at armLength/2 for ARM

**Motor oscillates around setpoint:**
- kP too high — reduce by 50%
- Try increasing kD slightly
- Check measurement delay is correct for your controller

**Profile motion jerky:**
- Increase jerk value for S-curve smoothing
- Reduce CruiseVel or Accel
- Ensure feedforward gains are correct (run SysId)

---

## Hardware Reference

### Default Configuration (Shooter_Test)

| Motor | Kind | CAN ID | Controller |
|-------|------|--------|------------|
| Preshooter | Kraken X44 | 20 | TalonFX |
| MainShooter | Kraken X60 | 21 | TalonFX |
| Servo | PWM | Ch 0 | PWM |

### Button Bindings

| Button | Function |
|--------|----------|
| A | Run Setpoint 1 (all motors + servo) |
| B | Run Setpoint 2 (all motors + servo) |
| X | Apply PID values from dashboard (all motors) |
| Y | Characterize Preshooter (hold) |
| Back | Characterize MainShooter (hold) |
| LB | Servo position 1 |
| RB | Servo position 2 |

### Controller Presets (timing for LQR)

| Controller | Period | Meas Delay | Output Factor |
|------------|--------|------------|---------------|
| TalonFX (Phoenix6) | 1ms | 1ms | 1.0 |
| TalonSRX (Phoenix5) | 1ms | 81.5ms | 1023/12 |
| SparkMax | 1ms | 112ms | 1/12 |
| SparkFlex | 1ms | 112ms | 1/12 |
