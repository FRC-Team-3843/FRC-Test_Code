# Motor_System Tuning Guide

This guide covers the current workflow for the extracted generic motor project.

## Safety Gate
Every live motor action requires:
- Driver Station `Test` mode
- that motor's `Bench Armed` toggle enabled
- controller `A` held as the deadman

## Recommended Workflow
1. Configure motors in `motor-config.json`.
2. Deploy and open Elastic.
3. Use each motor's `Setup` tab to verify controller type, transport, feedback source, geometry, and unlock messages.
4. Save setup-side changes with `Apply Setup`.
5. Switch to DS `Test`.
6. Arm the target motor.
7. Hold `A`.
8. Run `SysId` for baseline gains.
9. Apply results with `SysId/ApplyResults`.
10. Refine live gains with `ApplyPID`.
11. Validate using `Manual Run` and the `Testing` tab diagnostics.

## Capability Rules
- CAN + integrated feedback: full tuning path
- PWM + external feedback: software closed-loop and SysId supported
- PWM + no feedback: manual/open-loop only until feedback is added
- Recognized but unimplemented controller/transport paths should report as unsupported instead of silently running through the wrong wrapper

## Testing Tab
The testing tab currently shows:
- feedback source and connected state
- control-path source
- power telemetry source
- measured velocity, position, voltage, current, and temperature when available
- capability summary and unlock guidance

It does not yet implement full spec/health grading.
