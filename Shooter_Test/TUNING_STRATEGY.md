# AI-Driven PID Tuning Strategy

## Philosophy: Neural Network Intelligence vs Rules-Based Systems

**CRITICAL UNDERSTANDING: This document describes an AI-driven tuning workflow that uses Claude's neural network intelligence for pattern recognition and analysis, NOT hardcoded rules.**

### Why This Matters

**Rules-based systems (including SysId) have limitations:**
- Operate under specific test conditions
- Follow fixed algorithms
- Can't adapt to unexpected patterns
- Don't consider context-specific factors
- Miss subtle non-linearities and interactions

**Real-world motor control is complex:**
- Non-linear behavior at different speeds
- Thermal effects during operation
- Battery voltage variations
- Motor-to-motor variations
- Load-dependent behavior
- Coupling effects between multiple motors
- Saturation and physical limits

**Claude's neural network can:**
- ✅ Recognize patterns across multiple datasets
- ✅ Form hypotheses about root causes
- ✅ Design custom tests to validate theories
- ✅ Adapt strategy based on observations
- ✅ Balance competing objectives
- ✅ Provide context-aware recommendations
- ✅ Learn from iteration to iteration

**What Claude should NOT do:**
- ❌ Write hardcoded rules like "if overshoot > 5%, reduce kP by 20%"
- ❌ Use simple heuristics
- ❌ Apply generic tuning formulas
- ❌ Ignore the actual data and guess

**What Claude SHOULD do:**
- ✅ Read actual time-series data from CSV files
- ✅ Analyze with genuine intelligence and pattern recognition
- ✅ Form specific hypotheses: "I notice the overshoot only occurs above 4500 RPM and correlates with voltage saturation"
- ✅ Design targeted tests: "Let's test gain scheduling at the 4500 RPM boundary"
- ✅ Provide insights that rules can't capture

---

## The Core Constraint: How Claude Works

**Claude needs discrete prompts to operate:**
- Cannot "watch" data streaming in real-time
- Needs to be invoked by user for each iteration
- Reads data from files between invocations
- Maintains context through saved files and conversation history

**This leads to an iterative workflow:**
1. Robot runs test → saves data to file
2. User prompts Claude: "analyze run-001"
3. Claude reads data, analyzes, suggests next test
4. User runs next test
5. User prompts: "continue"
6. Claude analyzes new data, iterates
7. Repeat until optimal

**This is actually BETTER than real-time for our use case:**
- Allows deep analysis without time pressure
- Can compare across multiple runs
- Can maintain comprehensive context
- Enables hypothesis-driven testing

---

## Primary Approach: Option 1 - Goal-Oriented Iterative Workflow

### Overview

**The best approach for leveraging Claude's intelligence:**
- Start with SysId for physics-based baseline (gets us 90% there)
- Use Claude to iteratively refine based on real performance data
- Fast 2-3 minute iteration cycles
- Claude designs custom tests to investigate specific hypotheses
- Goal-oriented: work toward measurable performance targets

### Detailed Workflow

#### Phase 0: Baseline Characterization (10 minutes, one-time)

**Run WPILib SysId:**
```
1. Connect robot to SysId tool
2. Run quasistatic + dynamic tests
3. Obtain: kS, kV, kA values
4. Deploy to robot via shooter-config.json
5. Verify basic functionality works
```

**Expected baseline values (Kraken motors):**
- kS: 0.15 - 0.35 V
- kV: 0.10 - 0.13 V/RPS
- kA: 0.01 - 0.05 (if relevant)
- kP: 0.1 - 0.3 (starting point)

#### Phase 1: Initial Performance Assessment (5 minutes)

**Run comprehensive performance test:**
```java
// Robot executes test-config.json
{
  "runNumber": 1,
  "testType": "performance_baseline",
  "setpoints": [2000, 3000, 4000, 5000, 6000],
  "testDuration": 3.0,  // seconds per setpoint
  "pidValues": {
    "kS": 0.25,  // From SysId
    "kV": 0.118, // From SysId
    "kP": 0.2,   // Starting point
    "kI": 0.0,
    "kD": 0.0
  }
}
```

**Robot logs at 50Hz:**
- Timestamp
- Commanded RPM
- Actual RPM
- Error (commanded - actual)
- Applied voltage
- Motor current
- Motor temperature
- Battery voltage

**Saves to:** `tuning-data/run-001.csv`

**Transfer to local machine** (USB, FTP, or NetworkTables dump)

#### Phase 2: Claude Analysis & Iteration (3 minutes per cycle)

**User prompts:** "Analyze run-001 and suggest next iteration"

**Claude does:**

1. **Read the data:**
   ```
   Read: C:/GitHub/FRC-Test_Code/Shooter_Test/tuning-data/run-001.csv
   ```

2. **Calculate performance metrics:**
   - Rise time (10% to 90% of setpoint)
   - Overshoot (peak RPM vs setpoint)
   - Settling time (within 2% of setpoint)
   - Steady-state error
   - Oscillation frequency and damping

3. **Identify patterns with AI intelligence:**
   - "Overshoot increases with RPM: 2% at 2000 RPM → 8% at 6000 RPM"
   - "Rise time is consistent, but settling time degrades at high RPM"
   - "Voltage saturates at 11.8V during 6000 RPM test"
   - "Current spike suggests mechanical binding at startup"

4. **Form hypothesis:**
   - "The increased overshoot at high RPM suggests kP is too aggressive when operating near voltage limits"
   - "The system is hitting saturation, causing feedforward to undershoot, then kP overcorrects"

5. **Design next test:**
   - "Test gain scheduling: kP=0.25 below 4000 RPM, kP=0.18 above 4000 RPM"
   - OR: "Increase kV slightly to reduce feedforward error at high speeds"
   - OR: "Add small kD=0.02 to dampen overshoot without slowing response"

6. **Update test-config.json:**
   ```
   Edit: tuning-data/test-config.json
   Update: runNumber = 2, pidValues with new parameters
   Add notes field: "Testing hypothesis: gain scheduling to reduce high-RPM overshoot"
   ```

7. **Instruct user:**
   "Run test-002 with updated config. I'm testing whether gain scheduling reduces overshoot at high speeds while maintaining fast response at low speeds."

**User does:**
```
1. Deploy updated config to robot (or reload from file)
2. Run test-002
3. Data saves to run-002.csv
4. Transfer data
```

**User prompts:** "continue"

**Claude does:**

1. **Read new data:**
   ```
   Read: run-002.csv
   ```

2. **Compare to baseline:**
   - "Overshoot at 6000 RPM reduced from 8% to 3% ✓"
   - "Rise time unchanged ✓"
   - "Settling time improved by 40% ✓"

3. **Evaluate hypothesis:**
   - "Gain scheduling was effective. The lower kP at high RPM prevents overcorrection when near saturation."

4. **Decide next step:**
   - If good enough: "Performance meets goals. Recommend deploying these values."
   - If needs refinement: "Still seeing minor oscillation. Let's test run-003 with kD=0.015 for additional damping."

5. **Iterate 2-4 more times until:**
   - Rise time < 1.0 second
   - Overshoot < 3%
   - Settling time < 1.5 seconds
   - Steady-state error < 50 RPM
   - No oscillation

#### Phase 3: Validation & Deployment (5 minutes)

**Final validation test:**
```
1. Run extended test at all operating points
2. Include thermal stress (run for 30 seconds continuously)
3. Test at low battery voltage (11.5V)
4. Verify consistency
```

**Deploy final values to shooter-config.json**

### Key Files & Data Structure

**Directory structure:**
```
Shooter_Test/
├── tuning-data/
│   ├── run-001.csv          (baseline performance)
│   ├── run-002.csv          (first iteration)
│   ├── run-003.csv          (second iteration)
│   ├── ...
│   ├── test-config.json     (current test parameters)
│   └── analysis-notes.md    (Claude's running notes)
├── TUNING_STRATEGY.md       (this file)
├── TUNING_GUIDE.md          (user-facing guide)
└── shooter-config.json      (deployed PID values)
```

**CSV format (50Hz samples):**
```csv
time,commandedRPM,actualRPM,error,voltage,current,temperature,batteryVoltage
0.000,2000,0,2000,0.5,1.2,25.0,12.4
0.020,2000,150,1850,2.1,8.5,25.1,12.3
0.040,2000,520,1480,3.8,15.2,25.2,12.3
...
```

**test-config.json format:**
```json
{
  "runNumber": 3,
  "testType": "performance",
  "setpoints": [2000, 3000, 4000, 5000, 6000],
  "testDuration": 3.0,
  "pidValues": {
    "kS": 0.25,
    "kV": 0.118,
    "kP_low": 0.25,
    "kP_high": 0.18,
    "kP_threshold": 4000,
    "kI": 0.0,
    "kD": 0.015
  },
  "notes": "Testing gain scheduling + light damping. Previous iteration reduced overshoot but still seeing minor oscillation."
}
```

### Expected Timeline

**Total time to optimal tune: 25-35 minutes**
- SysId baseline: 10 minutes
- Initial test + transfer: 5 minutes
- Claude analysis: 2 minutes
- Iterations (3-5 cycles @ 3 min each): 10-15 minutes
- Final validation: 5 minutes

**Per-iteration breakdown:**
- Robot test run: 30-60 seconds
- Data transfer: 10-20 seconds
- Claude analysis: 60-90 seconds
- Config update: 10 seconds
- Deploy: 10 seconds

---

## Alternative Approaches

### Option 2: Comprehensive Single-Shot Analysis

**When to use:**
- You want fewer iterations with more upfront data
- Robot is available for extended testing (10+ minutes)
- You want to test under many conditions simultaneously

**Workflow:**
1. Run SysId baseline
2. Robot runs comprehensive test suite:
   - 10+ setpoints
   - Thermal stress test
   - Battery voltage variation
   - Disturbance rejection
   - Ramp rate tests
3. Claude analyzes entire dataset (5000+ points)
4. Suggests comprehensive tuning strategy
5. 1-2 refinement iterations

**Pros:** Very thorough, fewer iterations
**Cons:** Long initial test, harder to analyze, less interactive

**Time:** 30-40 minutes

---

### Option 3: Lightweight Performance Profiling

**When to use:**
- Quick iterative tuning without SysId
- Time-constrained tuning sessions
- Fine-tuning already-decent values

**Workflow:**
1. Start with recommended values (no SysId)
2. Simple 2-setpoint test (low + high RPM)
3. Very fast iterations (1 min each)
4. 5-10 iterations to convergence

**Pros:** Very fast iterations, simple
**Cons:** More iterations needed, less rigorous

**Time:** 20-30 minutes

---

### Option 4: Hybrid Real-World Performance

**When to use:**
- You want to optimize for actual match conditions
- Testing with game pieces or realistic loads
- After basic tuning is complete

**Workflow:**
1. Start with SysId baseline
2. Test in realistic scenarios (not just step responses)
3. Claude analyzes real-world performance
4. Refine for practical usage patterns

**Pros:** Optimizes for actual use case
**Cons:** Requires realistic test setup

**Time:** 30 minutes

---

## Implementation Requirements

### Robot Code Requirements

**Must implement:**
1. **PerformanceTestCommand.java**
   - Reads test-config.json
   - Executes configurable test sequence
   - Logs data at 50Hz
   - Saves to CSV file

2. **Data logging infrastructure**
   - WPILib DataLog or custom CSV writer
   - Timestamps with microsecond precision
   - All relevant telemetry channels

3. **Config file loading**
   - Read test-config.json on command start
   - Support for gain scheduling parameters
   - Hot-reload capability (no redeploy needed)

4. **Data transfer mechanism**
   - USB file transfer (simplest)
   - OR: FTP server on robot
   - OR: NetworkTables data dump

### Local Machine Requirements

**Directory setup:**
```
C:/GitHub/FRC-Test_Code/Shooter_Test/tuning-data/
```

**Tools Claude uses:**
- Read tool: Load CSV files
- Edit/Write tools: Update test-config.json
- Bash tool: Optional data processing (gnuplot, pandas, etc.)

---

## Examples of AI-Driven Analysis

### Example 1: Claude Identifies Saturation Issue

**Data observation:**
```
At 6000 RPM test:
- Applied voltage: 11.8V (saturated at battery voltage)
- Rise time: 2.1 seconds (slower than expected)
- Overshoot: 12% (excessive)
- Current: 45A peak (normal)
```

**Claude's analysis (using intelligence, not rules):**
"I see that at 6000 RPM, the applied voltage hits 11.8V and stays there, indicating saturation. The feedforward (kV * 6000/60 + kS = 0.118 * 100 + 0.25 = 12.05V) is asking for more than battery can provide. This causes the motor to accelerate more slowly than expected, then when it gets close, kP sees a large error and overcorrects, causing the 12% overshoot.

**Two solutions:**
1. Reduce maximum setpoint to 5500 RPM (avoids saturation)
2. Increase kV slightly to 0.122 to account for real-world losses at high speed, reducing the feedforward undershoot

I recommend testing option 2 first, as it maintains your desired speed range."

### Example 2: Claude Identifies Thermal Effects

**Data observation:**
```
Run 1 (cold motor):
- 4000 RPM: Rise time 0.8s, overshoot 3%, steady-state error 30 RPM

Run 5 (after 10 minutes of testing):
- 4000 RPM: Rise time 0.9s, overshoot 2%, steady-state error 65 RPM
- Motor temperature: 55°C (up from 25°C)
```

**Claude's analysis:**
"I notice the steady-state error has doubled from run-1 to run-5, correlating with motor temperature increase. This suggests kV needs thermal compensation. As the motor heats up, resistance increases and back-EMF changes slightly, causing feedforward to undershoot.

I recommend adding temperature compensation:
- kV_effective = kV * (1 + 0.0005 * (temp - 25))
- This 0.05% per degree correction should account for the observed error increase.

Let's test this in run-6 after the motor has warmed up."

### Example 3: Claude Identifies Motor Coupling

**Data observation:**
```
Preshooter alone at 4000 RPM: 0.8s rise time
Main shooter alone at 4000 RPM: 0.8s rise time
Both at 4000 RPM: 0.95s rise time, main shooter overshoots 5%
```

**Claude's analysis:**
"Interesting - when both motors run simultaneously, the main shooter's rise time increases and it overshoots. This suggests they're coupled through the power system (voltage sag) or mechanically. When the preshooter draws current, it causes voltage drop that affects the main shooter's feedforward accuracy.

Two approaches:
1. Stagger the startup: bring preshooter up first, then main shooter
2. Add voltage compensation: adjust kV based on measured battery voltage during operation

I recommend testing voltage-compensated feedforward:
- kV_effective = kV * (12.0 / batteryVoltage)

This should account for the voltage sag effect."

---

## Success Criteria

**Performance goals:**
- Rise time: < 1.0 second
- Overshoot: < 3%
- Settling time: < 1.5 seconds
- Steady-state error: < 50 RPM
- No sustained oscillation
- Consistent across battery voltage range (11.5V - 12.7V)
- Consistent across temperature range (25°C - 60°C)
- Consistent between preshooter and main shooter

**When to stop iterating:**
- All performance goals met
- OR: Diminishing returns (< 5% improvement per iteration)
- OR: Physical limits reached (can't improve further without hardware changes)

---

## Critical Reminders for Claude

**When the user says "analyze the data" or "continue":**

1. ✅ **DO:** Read the actual CSV file and look at the data
2. ✅ **DO:** Calculate real metrics (rise time, overshoot, etc.)
3. ✅ **DO:** Form specific hypotheses based on patterns you observe
4. ✅ **DO:** Design targeted tests to validate theories
5. ✅ **DO:** Compare across multiple runs to see trends
6. ✅ **DO:** Consider physical constraints (voltage limits, thermal effects, etc.)

7. ❌ **DON'T:** Write hardcoded tuning rules
8. ❌ **DON'T:** Use generic formulas without understanding the data
9. ❌ **DON'T:** Guess without looking at data
10. ❌ **DON'T:** Assume simple linear relationships

**Your value is in:**
- Pattern recognition across complex datasets
- Hypothesis formation and testing
- Context-aware recommendations
- Identifying non-obvious relationships
- Balancing multiple competing objectives

**You are not:**
- A simple rules engine
- A PID calculator
- A generic tuning wizard

**You are:**
- An intelligent analyst using neural network pattern recognition
- A hypothesis-driven experimenter
- A context-aware optimization system

---

## Conclusion

This strategy leverages Claude's actual intelligence (neural network pattern recognition) rather than hardcoded rules. The iterative workflow accommodates Claude's constraint (needs discrete prompts) while enabling fast, intelligent refinement of PID values.

**Start with Option 1 (Goal-Oriented Iterative) for most tuning tasks.**

**The workflow is:**
1. SysId → baseline (10 min)
2. Performance test → data (5 min)
3. Claude analyzes → hypothesis → next test (2 min)
4. Iterate 3-5 times (10-15 min)
5. Validation → done (5 min)

**Total: 25-35 minutes to optimal tune using AI intelligence.**
