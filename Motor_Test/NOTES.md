# Notes - Motor_Test

## Before Testing

- Connect motor controller via CAN bus or PWM port.
- Verify CAN IDs match your hardware setup.
- Ensure motor is properly secured and can spin freely without obstruction.
- Check wire connections (power, CAN/PWM, motor leads).
- Confirm correct motor type selection (brushed vs brushless for Spark controllers).

## Hardware Configuration

- Configure controller type and motor IDs in Shuffleboard "Motor Test" tab.
- Supported controllers:
  - CAN: SparkMax, SparkFlex, TalonFX, TalonFXS, TalonSRX
  - PWM: PWMSparkMax, PWMTalonSRX, PWMVictorSPX
- Current control requires Phoenix Pro for TalonFX/FXS (not included in this project).

## Safety

- **Always hold A button** (or enable toggle on Shuffleboard) to allow motor output.
- Release button immediately to stop motor.
- Start with low values when testing unknown motors.
- Ensure motor mounting is secure before testing.

## Control Modes

1. **Duty Cycle**: Direct voltage control (-1.0 to 1.0)
2. **Velocity**: RPM control (requires encoder feedback)
3. **Position**: Rotation control (requires encoder feedback)
4. **Current**: Amperage control (SparkMax/Flex and TalonSRX only)

## Automated Health Test ("The Grader")

- Hold **B button** to run automated health test.
- Tests motor responsiveness, encoder feedback, and current draw.
- Results logged to DataLog and printed as JSON report.
- Review console output for pass/fail status and diagnostics.

## Data Logging

- All test data logged via WPILib `DataLogManager`.
- Logs stored in robot's USB drive or RoboRIO storage.
- JSON report printed to console for quick diagnostics.
- Log files can be reviewed with AdvantageScope or WPILib DataLog tool.

## Tuning PID/FF

- This project does NOT include PID tuning interfaces.
- For PID tuning:
  - Use vendor tools (REV Hardware Client, Phoenix Tuner X)
  - Or add Phoenix Pro licensing for advanced TalonFX control
  - Or implement custom tuning interface in this project

## Common Issues

- **Motor not responding**: Check enable button is held, verify CAN ID.
- **Encoder shows zero**: Check sensor type selection, verify wiring.
- **Current mode unavailable**: TalonFX requires Phoenix Pro, PWM has no current control.
- **Health test fails**: Check mechanical load, verify motor specifications match configuration.

## Future Enhancements

- Add PID tuning interface for SparkMax/Flex
- Integrate Phoenix Pro for full TalonFX current control
- Add servo testing support (see PwmServoWrapper.java)
- Implement motion profiling validation tests
