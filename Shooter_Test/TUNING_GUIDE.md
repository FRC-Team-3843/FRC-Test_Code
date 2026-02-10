# Shooter PID Tuning Guide

This guide explains how to use the automatic characterization feature to tune shooter motor PID values.

## Quick Start

1. **Deploy** updated code to robot with `./gradlew deploy`
2. **Enable** robot in Teleop mode
3. **Hold Y button** to characterize preshooter (~20 seconds)
4. **Hold Back button** to characterize main shooter (~20 seconds)
5. **Check Elastic Dashboard** for calculated values
6. **Copy values** to `shooter-config.json`
7. **Redeploy** and test with A/B buttons

## Button Bindings

| Button | Function |
|--------|----------|
| A | Run Setpoint 1 (both motors) |
| B | Run Setpoint 2 (both motors) |
| X | Apply PID values from dashboard |
| Y | Characterize Preshooter (hold ~20s) |
| Back | Characterize Main Shooter (hold ~20s) |

## Characterization Process

The characterization command runs through three phases automatically:

### Phase 1: Find kS (Static Friction) - ~5 seconds
- Gradually increases voltage from 0V
- Monitors velocity to detect when motor starts moving
- Records minimum voltage needed to overcome friction
- Expected range: 0.1 - 0.5 V

### Phase 2: Find kV (Velocity Feedforward) - ~10 seconds
- Tests at 3V, 6V, and 9V
- Waits for steady-state at each voltage
- Calculates volts per RPS relationship
- Expected range for Kraken: 0.10 - 0.15 V/RPS

### Phase 3: Calculate kP (Proportional Gain) - ~5 seconds
- Applies feedforward-only control to target 3000 RPM
- Measures steady-state error
- Recommends conservative starting kP = 0.1
- Can be fine-tuned after deployment

## Understanding the Results

### Dashboard Widgets

**Tuning Section** (top row):
- `Tuning Status` - Current phase or "Complete!"
- `Tuning Phase` - Phase number (0-4)
- `Calculated kS` - Static friction voltage
- `Calculated kV` - Velocity feedforward (V/RPS)
- `Calculated kP` - Recommended proportional gain
- `Steady State Error` - Error in RPM with FF-only

### What the Values Mean

**kS (Static Friction):**
- Minimum voltage to overcome bearing/motor friction
- Typical range: 0.1 - 0.5 V
- Too low: Motor won't start spinning
- Too high: Jerky motion at low speeds

**kV (Velocity Feedforward):**
- Volts needed per RPS of velocity
- Does 90% of the work in velocity control
- Kraken X44: ~0.11 - 0.13 V/RPS
- Kraken X60: ~0.10 - 0.12 V/RPS

**kP (Proportional Gain):**
- Corrects remaining error after feedforward
- Start with 0.1 and adjust as needed
- Too low: Slow to reach setpoint, steady-state error
- Too high: Oscillation, overshoot

**kI and kD:**
- Usually NOT needed for flywheel velocity control
- Leave at 0.0 unless you have specific issues
- kI can cause windup and instability
- kD can amplify noise

## Applying the Results

### Method 1: Update Config File (Recommended)

1. Copy values from dashboard console output
2. Edit `shooter-config.json`:
   ```json
   {
     "preshooterKs": 0.1234,
     "preshooterKv": 0.1234,
     "preshooterKp": 0.1000,
     "preshooterKi": 0.0,
     "preshooterKd": 0.0,
     ...
   }
   ```
3. Redeploy code: `./gradlew deploy`
4. Test with A/B buttons

### Method 2: Live Tuning (Fine-tuning)

1. After characterization, manually adjust PID values in dashboard
2. Press X button to apply new values (hot-reload)
3. Test with A/B buttons
4. Repeat until satisfied
5. Copy final values to `shooter-config.json`

## Verification

After applying PID values, test with setpoint buttons (A/B):

### Good Performance Indicators:
- Motor reaches setpoint within 1-2 seconds
- Minimal overshoot (<5%)
- Steady-state error < 50 RPM
- No oscillation or hunting

### If Performance is Poor:

**Too slow to reach setpoint:**
- Increase kP slightly (0.1 → 0.2 → 0.3)

**Oscillates around setpoint:**
- Decrease kP (0.3 → 0.2 → 0.1)

**Steady-state error (doesn't quite reach target):**
- Check kV is accurate
- Slightly increase kP
- Only use kI as last resort (start with 0.001)

**Overshoots significantly:**
- Decrease kP
- Check that kI = 0.0

## Tuning Philosophy for Flywheels

Velocity control priority:
1. **kV** - Does 90% of the work (feedforward)
2. **kS** - Overcomes static friction
3. **kP** - Corrects remaining error (small value)
4. **kI, kD** - Usually not needed

This is fundamentally different from position control!

## Hardware Requirements

**Preshooter (CAN ID 20):**
- Motor: Kraken X44
- Gear ratio: Configured in shooter-config.json
- Current limit: Default TalonFX settings

**Main Shooter (CAN ID 21):**
- Motor: Kraken X60
- Gear ratio: Configured in shooter-config.json
- Current limit: Default TalonFX settings

## Troubleshooting

### Motor doesn't spin during characterization
- Check CAN ID matches config
- Verify motor is not mechanically jammed
- Check power connections
- Enable robot in Teleop mode

### kS value seems too high (>1.0V)
- Motor may be mechanically loaded
- Check for binding or excessive friction
- Verify motor is spinning freely

### kV values inconsistent
- Allow full settle time (2 seconds per test)
- Ensure steady power supply (battery voltage)
- Check for mechanical slipping

### Characterization interrupted
- Must hold button for full ~20 seconds
- Release button only when "Complete!" shows
- Check driver station for disconnections

## Console Output Example

```
=== Starting Characterization: Preshooter ===
Phase 1: Finding kS (static friction)...
Found kS = 0.2500 V
Phase 2: Finding kV (velocity feedforward)...
  Test 1: 3.0V -> 25.43 RPS -> kV = 0.1180
  Test 2: 6.0V -> 50.21 RPS -> kV = 0.1195
  Test 3: 9.0V -> 75.88 RPS -> kV = 0.1186
Calculated kV = 0.1187 V/RPS
Phase 3: Calculating kP from step response...
Feedforward test: Target = 50.00 RPS, Actual = 48.32 RPS
Steady-state error: 100.80 RPM
Recommended kP = 0.1000

=== Characterization Results: Preshooter ===
kS (static friction): 0.2500 V
kV (velocity FF):     0.1187 V/RPS
kP (proportional):    0.1000
kI (integral):        0.0000 (not needed for velocity)
kD (derivative):      0.0000 (not needed for velocity)

Copy these values to shooter-config.json
Then test with A/B buttons and fine-tune kP if needed.
=========================================
```

## Next Steps

1. Run characterization for both motors
2. Apply values to config file
3. Deploy and test with setpoint buttons
4. Fine-tune kP if needed (usually 0.1 - 0.3 works)
5. Document final values in changelog
6. Optional: Test at different RPM ranges for validation
