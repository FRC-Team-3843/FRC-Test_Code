# PathPlanner Paths

This directory contains individual paths created with PathPlanner.

## Setup

1. Download and install PathPlanner from: https://pathplanner.dev
2. Open PathPlanner and create your paths
3. Save path files (`.path` format) to this directory
4. Paths can be composed into autos in the `../autos/` directory

## Creating Paths

1. Open PathPlanner application
2. Define waypoints with positions, headings, and velocities
3. Configure path constraints (max velocity, max acceleration)
4. Save with a descriptive name (e.g., `StartToPickup.path`)

## Path Configuration

Each path should be tuned for your robot's capabilities:
- **Max Velocity**: Set in `settings.json` or per-path
- **Max Acceleration**: Typically 80% of max capability for smoothness
- **Rotation Target**: Use holonomic rotation or face direction of travel

## Notes

- Paths are the building blocks for autonomous routines
- Test each path individually before composing complex autos
- Use the PathPlanner preview feature to visualize robot movement
- Coordinate with `settings.json` for consistent robot configuration
