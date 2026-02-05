# Working Notes - FRC-Test_Code

> **Documentation Guide:**
> - **This file (NOTES):** Cross-project setup notes and testing workflow
> - **README.md:** Repository overview
> - **STANDARDS.md:** Test-specific coding standards

---

## Cross-Project Setup Notes

### Hardware Requirements

All test projects require:
- **RoboRIO** (RoboRIO 1 or RoboRIO 2)
- **Power Distribution Hub (PDH)** or Power Distribution Panel (PDP)
- **Radio** for wireless connection (or USB tethering)
- **Battery** (12V FRC-legal battery)

### Common Hardware Setup

#### CAN Bus Wiring
- Chain CAN devices: PDH → Motor Controllers → Sensors
- Use proper CAN termination (120Ω resistors at each end)
- Keep CAN wires twisted and away from high-current wires

#### Motor Controller Setup
- **SparkMax/SparkFlex:** Set motor type (brushed vs brushless) with mode button
- **TalonFX/TalonSRX:** Configure using Phoenix Tuner before first use
- Always verify CAN IDs match your configuration files

#### Power Safety
- Always connect battery last
- Verify motor mounting before first test
- Start with low power values (0.1-0.3) when testing new hardware

---

## Testing Workflow

### Pre-Deployment Checklist

- [ ] Verify all CAN IDs in configuration files
- [ ] Check motor controller firmware versions
- [ ] Confirm motor inversions are correct
- [ ] Test with robot on blocks (wheels off ground)
- [ ] Have e-stop accessible

### Standard Testing Procedure

1. **Hardware Inspection**
   - Check all wire connections
   - Verify CAN bus continuity
   - Inspect motor mounting

2. **Deploy Code**
   - From project directory: `./gradlew deploy`
   - Wait for deployment confirmation
   - Check Driver Station for connection

3. **Initial Test (Low Power)**
   - Enable robot in teleop mode
   - Test at 10-30% power first
   - Verify motor directions
   - Listen for unusual sounds

4. **Full Test**
   - Gradually increase power
   - Test all control modes
   - Log data for analysis
   - Document any issues

### Data Logging

All test projects use WPILib DataLogManager:
- Logs stored on RoboRIO at `/home/lvuser/logs/`
- Download via WPILib → Retrieve Robot Logs
- Analyze with AdvantageScope

---

## Common Issues and Troubleshooting

### CAN Bus Issues

**Symptom:** Motor controllers not responding, orange/red status lights

**Causes & Solutions:**
- Missing CAN termination → Add 120Ω resistors at bus ends
- Loose CAN connections → Check all CAN High/Low connections
- CAN ID conflicts → Verify no duplicate IDs using Phoenix Tuner or REV Hardware Client
- Bad CAN wire → Replace twisted pair CAN cable

### Motor Direction Issues

**Symptom:** Motors spinning opposite of expected direction

**Solutions:**
- Update `inverted` flag in configuration files
- For SparkMax: Use `config.inverted(true)` in code
- For TalonFX: Set `InvertedValue` in configuration
- Test one motor at a time to isolate issue

### Encoder Issues

**Symptom:** Position/velocity readings are zero or erratic

**Causes & Solutions:**
- Wrong encoder type configured → Verify encoder matches motor type
- Encoder not plugged in → Check encoder cable connection
- Phase mismatch → Verify encoder wiring matches motor controller manual
- Conversion factors wrong → Check position/velocity conversion factors in config

### Driver Station Connection Issues

**Symptom:** Robot won't enable or loses connection

**Solutions:**
- Check radio power and ethernet cables
- Verify team number in Driver Station and robot code match
- Try USB tethering for troubleshooting
- Check firewall settings on driver station laptop

### Code Won't Deploy

**Symptom:** Gradle deploy fails or times out

**Solutions:**
- Verify robot is powered on and connected
- Check team number in `.wpilib/wpilib_preferences.json`
- Try deploying via USB instead of wireless
- Check disk space on RoboRIO: `ssh admin@roboRIO-3843-FRC.local` then `df -h`

---

## Project-Specific Notes

See individual project NOTES.md files for setup details:
- **Motor_Test:** See `Motor_Test\NOTES.md`
- **Swerve_Base:** See `Swerve_Base\NOTES.md`
- **Mecanum_Base:** See `Mecanum_Base\NOTES.md`
- **Wheeled_Base:** See `Wheeled_Base\NOTES.md`

---

## Hardware Calibration Notes

### Motor Current Limits

Always configure current limits for safety:
- **NEO Motors:** 40-60A typical
- **Falcon/Kraken (TalonFX):** 40-80A typical
- **CIM Motors (TalonSRX):** 30-40A typical

### PID Tuning Guidelines

General starting values for position control:
- **P:** 0.1 (increase until oscillation, then reduce by 50%)
- **I:** 0.0 (only add if steady-state error exists)
- **D:** 0.01 (increase to dampen oscillation)

For velocity control:
- **P:** 0.0001-0.001 (scale based on units)
- **I:** 0.0
- **D:** 0.0
- **F (Feed-forward):** 1/max_velocity

### Encoder Conversion Factors

Common conversions:
- **NEO Integrated Encoder:** 42 counts/revolution
- **TalonFX Integrated Encoder:** 2048 counts/revolution
- **Position to meters:** (counts / counts_per_rev) * wheel_diameter_m * π / gear_ratio

---

## TODO

- [ ] Add PathPlanner setup guide
- [ ] Document Choreo integration workflow
- [ ] Add PhotonVision calibration notes
- [ ] Create CAN bus troubleshooting flowchart

---

**Last Updated:** 2026-01-26
