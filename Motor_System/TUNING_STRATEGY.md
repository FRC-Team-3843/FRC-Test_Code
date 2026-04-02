# Motor_System Tuning Strategy

This document is the design direction for the advanced iterative tuning layer on top of the current on-robot setup and SysId workflow.

Use the existing per-motor `Setup`, `Tuning`, and `Testing` tabs first. Treat this document as future strategy for richer data-driven tuning, system interaction analysis, health/spec testing, and input shaping.

Any future automated tuning workflow still needs to respect:
- DS `Test`
- per-motor arm state
- held `A` deadman
- explicit cancellation behavior
