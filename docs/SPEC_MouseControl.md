# 📋 SPECIFICATION: Mouse Control (Coordinate Mapping & Robot)

> **Spec ID:** `SPEC-MC-001`
> **Feature:** Map hand landmarks to screen coordinates and control OS mouse cursor
> **Author:** MouseVision Team
> **Date:** 2026-04-20
> **Status:** ✅ Approved for Sprint 2
> **Related Epic:** EP-03 — Control (Robot & OS)

---

## 1. Business Goal

**As a** vision application user,
**I need** my index finger position to seamlessly translate to mouse cursor movement with a mirrored effect,
**So that** I can naturally control my computer without holding a mouse, and the interaction feels intuitive.

---

## 2. Hard Business Rules

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-MC-001` | Robot singleton must initialize once and be reused globally | Throw `RobotInitializationException("Cannot create Robot")` on first failure |
| `BR-MC-002` | Cursor shall NOT move if hand is not detected | Freeze cursor at last valid position, don't move to (0, 0) |
| `BR-MC-003` | Index finger position (cam: 0.5, 0.5) must map to screen center | Assert: input (0.5, 0.5) → output (screenWidth/2, screenHeight/2) |
| `BR-MC-004` | Horizontal axis must be MIRRORED (natural reflection) | Input hand moves RIGHT → cursor moves RIGHT (not left) |
| `BR-MC-005` | EMA filter with α=0.3 MUST be applied before mouseMove() | Smoothing reduces jitter, must happen post-mapping |
| `BR-MC-006` | Mouse movement frequency must be limited to max 30 Hz | Prevent excessive OS events (Robot not designed for 1000 Hz) |
| `BR-MC-007` | Cursor must be clamped to screen boundaries | No negative coordinates, no beyond-screen coordinates |
| `BR-MC-008` | Permission errors must be caught and reported gracefully | Catch AWTException, log error, continue (don't crash) |

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Coordinate Mapping (Hand Center → Screen Center)
> **Validates:** `BR-MC-003`, `BR-MC-007`

```gherkin
Given hand index finger at camera coordinates (0.5, 0.5)
  And camera resolution 640×480
  And screen resolution 1920×1080
When  MouseController.moveMouse() is called with normalized coords
Then  cursor moves to screen coordinates (960, 540)
  And cursor position is within screen bounds
```

**Test Method:** `shouldMapCameraCenter ToScreenCenter()`

---

### Scenario 2 — Mirror Effect: Hand Right → Cursor Right
> **Validates:** `BR-MC-004`

```gherkin
Given hand index finger moves from x=0.4 to x=0.6 (moving right in camera)
When  MouseController applies mirroring and mapping
Then  cursor moves to the RIGHT on screen (not left)
  And horizontal axis is inverted BEFORE mapping (not after)
```

**Test Method:** `shouldMirrorHorizontalAxis()`

---

### Scenario 3 — No Hand Detected: Cursor Freezes
> **Validates:** `BR-MC-002`

```gherkin
Given cursor is at (500, 500)
  And HandLandmarks.isHandDetected() returns false
When  MouseController.moveMouse() is called
Then  robot.mouseMove() is NOT called
  And cursor remains at (500, 500)
  And no exception is thrown
```

**Test Method:** `shouldNotMoveCursorWhenHandNotDetected()`

---

### Scenario 4 — Robot Initialization Failure (Permissions)
> **Validates:** `BR-MC-001`, `BR-MC-008`

```gherkin
Given OS denies access to Robot (macOS accessibility permissions not granted)
When  MouseController.getInstance() is called
Then  a RobotInitializationException is thrown
  And the message includes "Accessibility permissions required"
  And the application continues (exception handled in main)
```

**Test Method:** `shouldThrowExceptionWhenRobotPermissionsDenied()`

---

### Scenario 5 — Boundary Clamping (Negative/Beyond-Screen)
> **Validates:** `BR-MC-007`

```gherkin
Given hand at camera edge (x=-0.1, y=1.1) after normalization
When  coordinate is mapped to screen
Then  coordinates are clamped to screen bounds
  And cursor is at (0, 0) or (screenWidth-1, screenHeight-1) depending on edge
```

**Test Method:** `shouldClampCursorToScreenBoundaries()`

---

### Scenario 6 — EMA Filter Applied (Smoothing)
> **Validates:** `BR-MC-005`

```gherkin
Given raw cursor coordinates with jitter: (500, 500), (502, 499), (501, 501)
  And EMA filter with α=0.3
When  MoveMouse processes each coordinate through EMA
Then  output coordinates are smoothed: (500, 500), (500.6, 499.7), (500.8, 500.5)
  And jitter is reduced compared to raw coordinates
```

**Test Method:** `shouldApplyEMAFilterBeforeMouseMove()`

---

### Scenario 7 — Movement Frequency Limiting (Max 30 Hz)
> **Validates:** `BR-MC-006`

```gherkin
Given MouseController receives 300 coordinate updates in 1 second
When  it processes them
Then  Robot.mouseMove() is called max 30 times (not 300)
  And OS cursor moves smoothly (not jerky from 300 updates)
```

**Test Method:** `shouldLimitMouseMovementFrequencyTo30Hz()`

---

### Scenario 8 — Screen Resolution Adaptability
> **Validates:** `BR-MC-003`

```gherkin
Given screen resolution changes from 1920×1080 to 3840×2160
When  coordinates are remapped
Then  mapping scales correctly for new resolution
  And camera center (0.5, 0.5) still maps to screen center
```

**Test Method:** `shouldAdaptToVariousScreenResolutions()`

---

## 4. UI Requirements

| Element | Binding | Action |
|---------|---------|--------|
| Cursor Position Display | Real-time cursor coordinates | Show (x, y) in debug panel |
| Mirroring Toggle (Optional) | Enable/disable mirror effect | Checkbox: "Mirror Movement" |
| Sensitivity Slider | Scale factor for mapping | Range 0.5 - 2.0 (1.0 = 1:1) |
| Smoothing Slider | EMA alpha value | Range 0.1 - 0.8 (higher = smoother) |
| FPS Indicator | Mouse movement frequency | Show current Hz (target: 30) |

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer | Class Name | Key Responsibility |
|-------|-----------|-------------------|
| Control | `MouseController.java` | Singleton Robot, coordinate mapping, EMA integration |
| Filter | `EMAFilter.java` | Exponential Moving Average: `St = α·Yt + (1−α)·St−1` |
| Mapper | `CoordinateMapper.java` (part of MouseController) | Camera → Screen conversion |

### 5.2 Coordinate Transformation Pipeline

```
Step 1: Get normalized hand position
  Input: (x_norm, y_norm) ∈ [0.0, 1.0] from HandLandmarks

Step 2: Apply horizontal mirror
  x_mirrored = 1.0 - x_norm

Step 3: Convert to pixel coordinates (camera resolution)
  x_pixel_cam = x_mirrored * 640
  y_pixel_cam = y_norm * 480

Step 4: Map to screen coordinates (screen resolution)
  screen_width = Toolkit.getDefaultToolkit().getScreenSize().width
  screen_height = Toolkit.getDefaultToolkit().getScreenSize().height
  x_screen = (x_pixel_cam / 640) * screen_width
  y_screen = (y_pixel_cam / 480) * screen_height

Step 5: Clamp to screen boundaries
  x_screen = max(0, min(screen_width - 1, x_screen))
  y_screen = max(0, min(screen_height - 1, y_screen))

Step 6: Apply EMA smoothing filter
  x_smoothed = α · x_screen + (1 - α) · x_previous
  y_smoothed = α · y_screen + (1 - α) · y_previous

Step 7: Call Robot.mouseMove()
  robot.mouseMove((int)x_smoothed, (int)y_smoothed)
```

### 5.3 Frequency Limiting (30 Hz Target)

```java
private long lastMouseMoveTime = 0;
private static final long MIN_MOVE_INTERVAL_MS = 1000 / 30;  // ~33ms for 30 Hz

public void moveMouse(int screenX, int screenY) {
    long now = System.currentTimeMillis();
    if (now - lastMouseMoveTime >= MIN_MOVE_INTERVAL_MS) {
        robot.mouseMove(screenX, screenY);
        lastMouseMoveTime = now;
    }
    // else: skip this movement, wait for next interval
}
```

### 5.4 Thread Safety

```java
// Singleton with synchronized initialization
public class MouseController {
    private static volatile MouseController instance = null;
    
    public static MouseController getInstance() {
        if (instance == null) {
            synchronized (MouseController.class) {
                if (instance == null) {
                    instance = new MouseController();
                }
            }
        }
        return instance;
    }
}

// Robot methods are thread-safe (Robot uses internal synchronization)
// EMAFilter states must be guarded by callers if accessed from multiple threads
```

### 5.5 Configuration Parameters

```properties
# Mouse Control
mouse.mirroring.enabled=true
mouse.sensitivity.scale=1.0
mouse.ema.alpha=0.3
mouse.movement.frequency.hz=30

# Coordinate System
mouse.screen.boundary.clamp=true
mouse.coordinate.rounding=true
```

### 5.6 Exception Hierarchy

```
Throwable
  └─ Exception
      ├─ RuntimeException
      │   ├─ RobotInitializationException ("Cannot create Robot")
      │   ├─ CoordinateMappingException ("Invalid screen resolution")
      │   └─ MouseMovementException ("Robot.mouseMove() failed")
      └─ AWTException (caught, not rethrown)
```

### 5.7 Math Validation Examples

```
Test 1: Center mapping
  Input:  (0.5, 0.5) normalized, camera 640×480, screen 1920×1080
  Mirror: (0.5, 0.5) (X doesn't change at center)
  Screen: ((0.5 * 640 / 640) * 1920, (0.5 * 480 / 480) * 1080) = (960, 540)
  Expected: (960, 540) ✓

Test 2: Left edge mirroring
  Input:  (0.1, 0.5) normalized → hand on LEFT camera side
  Mirror: (0.9, 0.5) → inverted to RIGHT side
  Screen: ((0.9 * 640 / 640) * 1920, ...) = (1728, 540) → RIGHT side of screen ✓

Test 3: EMA filter
  α = 0.3, previous = 100, current = 120
  smoothed = 0.3 * 120 + 0.7 * 100 = 36 + 70 = 106 ✓
```

---

## 6. Dependencies & Assumptions

- [ ] Java 21+ (Robot is in java.awt)
- [ ] OS provides Mouse control API (Windows, Linux, macOS)
- [ ] macOS requires accessibility permissions (System Preferences → Security)
- [ ] Screen resolution is accessible via Toolkit

---

## 7. Out of Scope

- Right-click mouse button (MVP only left click via gestures)
- Mouse wheel scrolling (future feature)
- Configurable mirroring axis (always horizontal mirror in MVP)
- Touch screen support (camera-only in MVP)

---

## 8. Approval

| Role | Name | Date | ✓ |
|------|------|------|---|
| Tech Lead | TBD | 2026-04-20 | ☐ |
| Product Owner | TBD | 2026-04-20 | ☐ |

---

> **Ready for Implementation:** Once approved, feed this spec + CONSTITUTION_MouseVision.md to AI for code generation.
