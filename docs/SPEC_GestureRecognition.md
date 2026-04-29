# 📋 SPECIFICATION: Gesture Recognition (Pinch Detection & Click)

> **Spec ID:** `SPEC-GR-001`
> **Feature:** Pinch gesture detection and left mouse button execution
> **Author:** MouseVision Team
> **Date:** 2026-04-20
> **Status:** ✅ Approved for Sprint 2
> **Related Epic:** EP-04 — Gestos (Click via Pinch)

---

## 1. Business Goal

**As a** vision application user,
**I need** to perform clicks by pinching my thumb and index finger together,
**So that** I can interact with desktop applications without using a physical mouse button.

---

## 2. Hard Business Rules

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-GR-001` | Calculate Euclidean distance between Landmark 4 (thumb) and Landmark 8 (index) | If either point null/invalid, distance = infinity (no click) |
| `BR-GR-002` | Pinch is triggered when distance < 0.05 (threshold T, normalized [0, 1]) | No click if distance >= threshold |
| `BR-GR-003` | Only ONE click event per pinch gesture (no repeats while fingers pinched) | Debounce 300ms minimum between clicks |
| `BR-GR-004` | Pinch must transition from IDLE → PINCHING → RELEASED to fire one click | State machine prevents accidental multi-clicks |
| `BR-GR-005` | Click only fires if hand was detected when pinch occurs | No click if hand detection lost during pinch |
| `BR-GR-006` | Threshold T must be externally configurable (app.properties) | Default: 0.05, adjustable by user |
| `BR-GR-007` | Gesture recognizer must be thread-safe for concurrent reads from multiple threads | Use volatile fields + synchronized where needed |
| `BR-GR-008` | Each click must execute mousePress (50-100ms) + mouseRelease atomically | Robot handles atomicity, press/release timing verified |

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Pinch Gesture Triggers Click
> **Validates:** `BR-GR-001`, `BR-GR-002`, `BR-GR-003`, `BR-GR-008`

```gherkin
Given hand detected with index finger and thumb
  And Landmark 8 (index) at (0.4, 0.5)
  And Landmark 4 (thumb) at (0.42, 0.51)
  And distance = sqrt((0.42-0.4)² + (0.51-0.5)²) ≈ 0.0224 < threshold 0.05
When  GestureRecognizer evaluates the gesture
Then  state transitions from IDLE → PINCHING
  And Robot.mousePress(InputEvent.BUTTON1_DOWN_MASK) is called
  And delay of 75ms occurs
  And Robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK) is called
  And ONE click event is fired (not multiple)
```

**Test Method:** `shouldExecuteClickOnPinchGesture()`

---

### Scenario 2 — Pinch Gesture NOT Triggered (Distance Too Large)
> **Validates:** `BR-GR-002`

```gherkin
Given hand detected
  And distance between thumb and index = 0.08 > threshold 0.05
When  GestureRecognizer evaluates the gesture
Then  state remains IDLE
  And NO click event is fired
  And state does NOT transition to PINCHING
```

**Test Method:** `shouldNotClickWhenDistanceAboveThreshold()`

---

### Scenario 3 — Debounce: No Multiple Clicks While Pinching
> **Validates:** `BR-GR-003`, `BR-GR-004`

```gherkin
Given fingers are pinched (distance < threshold)
  And click event was fired 50ms ago
  And fingers are still pinched (distance still < threshold)
When  the next frame evaluates the gesture
Then  NO new click event is fired (debounce active)
  And gesture remains in PINCHING state (not RELEASED)
  And minimum 300ms must elapse before next click can fire
```

**Test Method:** `shouldDebounceClicksWhilePinching()`

---

### Scenario 4 — Pinch Release Resets State
> **Validates:** `BR-GR-004`

```gherkin
Given gesture state was PINCHING (fingers together)
  And fingers move apart (distance > threshold)
When  GestureRecognizer processes the separation
Then  state transitions from PINCHING → RELEASED
  And next pinch can fire a new click (debounce reset)
  And the gesture recognizer is ready for the next pinch
```

**Test Method:** `shouldResetStateOnPinchRelease()`

---

### Scenario 5 — Hand Lost During Pinch (No Click)
> **Validates:** `BR-GR-005`

```gherkin
Given hand detected and pinch gesture initiated
  And fingers are together (distance < threshold)
When  hand is lost (isHandDetected() = false)
Then  current click event is CANCELLED (not fired if in PINCHING)
  And state resets to IDLE
  And gesture recognizer waits for hand reappearance
```

**Test Method:** `shouldCancelClickIfHandLostDuringPinch()`

---

### Scenario 6 — Configurable Threshold
> **Validates:** `BR-GR-006`

```gherkin
Given app.properties contains gesture.pinch.distance.threshold=0.08
When  GestureRecognizer is initialized
Then  it loads threshold value 0.08 (not hardcoded 0.05)
  And pinch triggers when distance < 0.08
```

**Test Method:** `shouldLoadConfigurableThresholdFromProperties()`

---

### Scenario 7 — Missing Landmarks (Distance = Infinity)
> **Validates:** `BR-GR-001`, `BR-GR-005`

```gherkin
Given hand detected but Landmark 4 (thumb) is missing/invalid
  Or Landmark 8 (index) is missing/invalid
When  distance calculation is attempted
Then  distance is considered infinity
  And pinch is NOT triggered
  And NO click event fires
```

**Test Method:** `shouldHandleMissingLandmarks()`

---

### Scenario 8 — Press-Release Timing (Atomic Click)
> **Validates:** `BR-GR-008`

```gherkin
Given pinch gesture detected and click is about to execute
When  Robot.mousePress() is called
  And 75ms delay occurs
  And Robot.mouseRelease() is called
Then  OS receives one complete click event
  And timing is sufficient for OS to register (not too fast)
  And the click is delivered to the focused window
```

**Test Method:** `shouldExecuteAtomicPressReleaseClick()`

---

## 4. UI Requirements

| Element | Binding | Validation/Action |
|---------|---------|-------------------|
| Pinch Status | `isPinching()` | Display "Pinch Active" visual feedback |
| Last Click Time | `getLastClickTime()` | Show timestamp of last click (debug) |
| Distance Display | `getLastDistance()` | Show current thumb-index distance (debug) |
| Threshold Display | Configurable threshold | Show "Threshold: 0.05" |
| Click Counter | Count successful clicks | Increment on each click for testing |

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer | Class Name | Key Responsibility |
|-------|-----------|-------------------|
| Recognizer | `GestureRecognizer.java` | Pinch detection, state machine, debounce |
| Filter | `EMAFilter.java` | Already created for mouse smoothing; reuse here if needed |

### 5.2 State Machine Diagram

```
        ┌─────────────────────────────────────────┐
        │                                         │
        ↓                                         │
    ┌─────────┐                              
    │  IDLE   │  ← Initial state
    └────┬────┘                              
         │                                   
         │ distance < threshold              
         ↓                                   
    ┌─────────────┐                         
    │  PINCHING   │  ← Fingers together, click fired here
    └─────┬───────┘                         
          │                                  
          │ distance >= threshold OR
          │ hand lost (isHandDetected=false) 
          ↓                                  
    ┌──────────────┐                        
    │  RELEASED    │  ← Debounce timer active
    └──────┬───────┘                        
           │                                 
           │ 300ms elapsed                  
           ↓                                 
      → IDLE                                
```

### 5.3 Distance Calculation

```java
double distance = calculateDistance(point4, point8);
// Euclidean distance in normalized space [0.0, 1.0]
// distance = sqrt((x8 - x4)² + (y8 - y4)²)

// Example values:
// • Fingers far apart: 0.3 → no click
// • Fingers slightly touching: 0.04 → click! ✓
// • Fingers pressed together: 0.01 → click! ✓
```

### 5.4 Debounce & State Machine Logic

```java
public class GestureRecognizer {
    enum State { IDLE, PINCHING, RELEASED }
    
    private State state = State.IDLE;
    private long lastClickTime = 0;
    private static final long DEBOUNCE_MS = 300;
    
    public void evaluate(HandLandmarks landmarks) {
        if (!isHandDetected(landmarks)) {
            state = State.IDLE;
            return;
        }
        
        double distance = calculateDistance(
            landmarks.getThumbTip(),
            landmarks.getIndexFingerTip()
        );
        
        boolean isPinching = distance < threshold;
        
        switch (state) {
            case IDLE:
                if (isPinching) state = State.PINCHING;
                break;
                
            case PINCHING:
                if (isPinching) {
                    long now = System.currentTimeMillis();
                    if (now - lastClickTime >= DEBOUNCE_MS) {
                        executeClick();
                        lastClickTime = now;
                    }
                } else {
                    state = State.RELEASED;
                }
                break;
                
            case RELEASED:
                if (!isPinching) {
                    state = State.IDLE;  // Reset after release
                }
                break;
        }
    }
    
    private void executeClick() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        Thread.sleep(75);  // Hold for 75ms
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
}
```

### 5.5 Thread Safety

```java
// Shared state between capture thread and control thread
private volatile State state = State.IDLE;
private volatile double lastDistance = 0.0;
private volatile long lastClickTime = 0;

// Safe reads from any thread
public State getState() { return state; }
public double getLastDistance() { return lastDistance; }

// Write operations synchronized if needed
public synchronized void executeClick() { ... }
```

### 5.6 Configuration Parameters

```properties
# Gesture Recognition
gesture.pinch.distance.threshold=0.05
gesture.pinch.debounce.ms=300
gesture.pinch.press.duration.ms=75

# Logging
gesture.logging.enabled=true
gesture.log.events=PINCH_START, PINCH_RELEASE, CLICK_FIRED
```

### 5.7 Exception Hierarchy

```
Throwable
  └─ Exception
      ├─ RuntimeException
      │   ├─ GestureRecognitionException ("Invalid landmarks")
      │   ├─ ClickExecutionException ("Robot.mousePress() failed")
      │   └─ ConfigurationException ("Invalid threshold value")
```

### 5.8 Performance Metrics

```
Gesture detection latency: < 5ms per frame
Click execution latency:   ~100ms (press 75ms + release 25ms)
State machine overhead:    < 1ms
Debounce check:            < 0.1ms
```

---

## 6. Dependencies & Assumptions

- [ ] EMAFilter class available (created in Sprint 2 — SPEC_MouseControl.md)
- [ ] HandLandmarks provides Landmark 4 and 8 reliably
- [ ] Robot singleton initialized and working (MouseController)
- [ ] app.properties file with gesture thresholds

---

## 7. Out of Scope

- Two-finger swipe gestures (future feature)
- Gesture customization UI (future feature)
- Machine learning-based gesture detection (using simple distance metric)
- Palm gesture recognition (open/closed hand — not in MVP)
- Hold-to-drag functionality (future)

---

## 8. Approval

| Role | Name | Date | ✓ |
|------|------|------|---|
| Tech Lead | TBD | 2026-04-20 | ☐ |
| Product Owner | TBD | 2026-04-20 | ☐ |

---

> **Ready for Implementation:** Once approved, feed this spec + CONSTITUTION_MouseVision.md to AI for code generation.
