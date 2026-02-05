# Motor_Test

Universal Motor Health & Control Utility (WPILib 2026).

> **Documentation Guide:**
> - **This file (README):** Project overview and quick start
> - **NOTES.md:** Setup procedures, tuning values, troubleshooting
> - **STANDARDS.md:** Coding standards (see C:\GitHub\FRC-Test_Code\STANDARDS.md)

## Usage
- Configure controller/motor type and IDs in Elastic Dashboard "Motor Test" tab.
- Hold **A** (or the dashboard enable toggle) to allow output.
- Hold **B** to run the automated health test ("The Grader").

## Control Mode Notes
- **Current mode** is supported for Spark Max/Flex and Talon SRX.
- **Talon FX/FXS current control is disabled** because Phoenix Pro is not used.
  - The UI labels this as "Current (Spark/SRX only)".
  - If selected with Talon FX/FXS, output is held at 0 and a warning is printed.

## Data Logging
- Uses WPILib `DataLogManager` and prints a JSON AI report to the console.
