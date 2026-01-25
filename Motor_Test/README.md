# Motor_Test
Universal Motor Health & Control Utility (WPILib 2026).

## Usage
- Configure controller/motor type and IDs on Shuffleboard tab "Motor Test".
- Hold **A** (or the Shuffleboard enable toggle) to allow output.
- Hold **B** to run the automated health test ("The Grader").

## Control Mode Notes
- **Current mode** is supported for Spark Max/Flex and Talon SRX.
- **Talon FX/FXS current control is disabled** because Phoenix Pro is not used.
  - The UI labels this as "Current (Spark/SRX only)".
  - If selected with Talon FX/FXS, output is held at 0 and a warning is printed.

## Data Logging
- Uses WPILib `DataLogManager` and prints a JSON AI report to the console.
