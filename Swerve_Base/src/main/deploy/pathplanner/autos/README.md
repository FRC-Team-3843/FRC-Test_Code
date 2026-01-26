# PathPlanner Autos

This directory contains autonomous routines created with PathPlanner.

## Setup

1. Download and install PathPlanner from: https://pathplanner.dev
2. Open PathPlanner and create your autonomous routines
3. Save auto files (`.auto` format) to this directory
4. Auto files are automatically discovered by `AutoBuilder.buildAutoChooser()`

## Usage

The robot will automatically discover all `.auto` files in this directory and add them to the autonomous chooser on the dashboard.

## Creating Autos

1. Open PathPlanner application
2. Create or select paths from the `paths/` directory
3. Compose autonomous routines by sequencing paths and commands
4. Save with a descriptive name (e.g., `FourBallAuto.auto`)

## Notes

- Autos reference paths from the `../paths/` directory
- Test each auto in simulation before deploying to hardware
- Update `Constants.AutoConstants.PATHPLANNER_DEFAULT_AUTO` to change the default auto selection
