# Motor_System Notes

## Preflight
- Verify `motor-config.json` matches the actual controller family, transport, feedback source, CAN ID or PWM channel, and external sensor wiring.
- If using power telemetry, confirm `powerModuleType`, `powerModuleId`, and `powerChannel`.
- Secure the mechanism before any tuning or characterization run.

## Setup Tab
- Use explicit forward/reverse limit toggles instead of overloading `0`.
- `Apply Setup` saves setup-side changes including feedback wiring/scaling fields.
- `Detect Ch` pulses the motor and stages the most likely PDP/PDH current channel into the dashboard field.
- Detection is an assist tool, not guaranteed truth. Confirm before saving.
- Some setup-side feedback changes are saved immediately but still need runtime re-init before the live wrapper uses the new sensor object.
- Controller family and transport are treated as runtime-shaping settings. They are displayed on the dashboard, but changing them still requires a motor rebuild/redeploy path rather than a fake hot-apply.

## Bench Workflow
1. Enable DS `Test` mode.
2. Open the target motor's `Tuning` tab.
3. Turn on that motor's `Bench Armed` toggle.
4. Hold controller `A`.
5. Start `SysId`, `Manual Run`, or `Detect Ch`.
6. Release `A` or press `Cancel` to stop immediately.

## Current Gaps
- Power-channel detection thresholds still need hardware validation.
- Testing tabs are status/automation only in this pass.
- Full motor-spec validation is still future work.
- `MotorSysId` still owns part of its dashboard contract directly; more of that should move into `motor-dashboard` in the next cleanup pass.
- Some legal controller families are recognized in config/metadata before their runtime wrappers exist. Those paths now fail loudly instead of silently falling back.
- Some legal controller/transport combinations are recognized in config before they are fully implemented in the runtime wrapper.
- Setup changes that affect controller family, transport, CAN ID, feedback wiring, or gear ratio are not true hot-apply changes yet; they should be treated as save-and-rebuild/re-init paths.
