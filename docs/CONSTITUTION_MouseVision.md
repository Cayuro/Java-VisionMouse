# 📜 PROJECT CONSTITUTION — SDD for MouseVision (Desktop Vision App)

> **This file is the single source of truth for MouseVision architecture, patterns, and AI-assisted code generation.**
> **Feed this document as context to the AI before requesting any code implementation.**

---

## 🎯 Core Principle

MouseVision is **NOT a traditional CRUD web/desktop app**. It is a **real-time vision processing system** with these unique characteristics:

| Aspect | Traditional CRUD | MouseVision | Implication |
|--------|------------------|-------------|------------|
| **I/O Model** | Database queries | Camera stream + OS control | Threading, synchronization critical |
| **Latency** | 100-500ms acceptable | **< 100ms required** | Frame skipping, non-blocking operations |
| **State** | Persistent in DB | In-memory, real-time | Memory efficiency, garbage collection |
| **Resources** | Connections pooled | Native objects (Mat, Net) | Explicit cleanup, reference counting |
| **Failure Mode** | Rollback transactions | Degrade gracefully (no hand → stop moving cursor) | Fault tolerance embedded |

---

## 1. Architectural Layers

MouseVision uses a **pipeline architecture** instead of traditional layered architecture:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         USER INTERFACE (Swing)                          │
│                        DisplayWindow (JFrame)                           │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↑ / ↓
                          (BufferedImage updates)
┌─────────────────────────────────────────────────────────────────────────┐
│                    REAL-TIME PROCESSING PIPELINE                        │
│                                                                         │
│  [Capture Thread]     [Detection]      [Control]      [Gesture]        │
│  ─────────────────    ─────────────    ────────────   ─────────────   │
│  • CameraCapture      • HandDetector   • MouseControl • GestureRec.    │
│  • Frame buffering    • Landmarks      • Coordinate   • Distance calc  │
│  • 30 FPS buffer      • Normalization  • Mapping      • Debounce       │
│  • MatConverter       • Presence flag  • EMA filter   • State machine  │
└─────────────────────────────────────────────────────────────────────────┘
                            (Java threads)
┌─────────────────────────────────────────────────────────────────────────┐
│                         NATIVE RESOURCES                                │
│  OpenCV (Mat, VideoCapture)  ·  ONNX Models (DNN)  ·  java.awt.Robot   │
└─────────────────────────────────────────────────────────────────────────┘
```

### Layer Responsibilities

| Layer | Module | Responsibility | Threading |
|-------|--------|-----------------|-----------|
| **UI** | DisplayWindow | Render frames, display status | Main (Swing/EDT) |
| **Capture** | CameraCapture | Grab frames, buffer latest | Capture thread |
| **Detection** | HandDetector | Run ONNX inference | Capture or processing thread |
| **Processing** | Coordinate normalization, EMA | Transform coordinates | Same as detection |
| **Control** | MouseController | Move cursor, execute clicks | Control thread (can be separate) |
| **Recognition** | GestureRecognizer | Detect pinch gesture | Processing thread |

---

## 2. Non-Negotiable Rules

### 2.1 Threading Model

> **Rule:** Every I/O operation that blocks (camera, ONNX inference) MUST run on a separate thread.

```
Main Thread (UI/EDT):
  └─ Updates DisplayWindow only via SwingUtilities.invokeLater()

Capture Thread (CameraCapture):
  ├─ Reads frames from VideoCapture in a loop
  ├─ Calls HandDetector for inference
  ├─ Updates volatile AtomicReference<Mat> latestFrame
  └─ Never touches UI directly

Control Thread (MouseController — optional):
  ├─ Consumes latestFrame from capture thread
  ├─ Calls Robot.mouseMove() and Robot.mousePress()
  └─ Never calls ONNX inference (too slow)

UI Updates (from any thread):
  └─ ALWAYS wrap in SwingUtilities.invokeLater(() -> { ... })
```

### 2.2 OpenCV Resource Management (CRITICAL)

> **Rule:** Every `Mat` object MUST be released. Memory leaks with native objects CRASH the JVM.

```java
// ✅ CORRECT — Explicit release in finally block
Mat frame = capture.read();
try {
    Mat resized = new Mat();
    Imgproc.resize(frame, resized, new Size(640, 480));
    // Use resized
} finally {
    frame.release();
    resized.release();  // Both released
}

// ❌ WRONG — No release → native memory leak → eventual crash
Mat frame = capture.read();
Mat resized = new Mat();
Imgproc.resize(frame, resized, new Size(640, 480));
// frame and resized never released → heap corruption
```

**Cleanup Checklist per Module:**

| Module | Resource | Cleanup Point | Pattern |
|--------|----------|----------------|---------|
| CameraCapture | VideoCapture, Mat | `stop()` method | `capture.release()` + `frame.release()` each loop |
| HandDetector | DNN Net, Mat | Inference end | Release all temporary Mat in finally block |
| FrameConverter | Mat → BufferedImage | After conversion | Release input Mat after extraction |
| DisplayWindow | BufferedImage | Window close | Automatically GC'd (not native) |

### 2.3 Real-Time Performance Constraints

> **Rule:** Frame processing must maintain **> 25 FPS** with **< 100ms latency** (hand → cursor move).

```
Target Pipeline (per frame):
  Capture:      5ms  (wait for frame)
  Detection:    30ms (ONNX inference)
  Processing:   5ms  (coordinate mapping, EMA)
  Control:      5ms  (Robot.mouseMove)
  ─────────────────
  TOTAL:       ~45ms ✅ (well under 100ms target)

If detection > 30ms consistently:
  → Reduce camera resolution from 640×480 to 320×240
  → Reduce ONNX inference frequency (process every 2nd frame)
  → Consider GPU acceleration (future)
```

### 2.4 Thread-Safe State Management

> **Rule:** Shared mutable state between threads MUST use `AtomicReference` or `volatile`.

```java
// ✅ CORRECT — AtomicReference for complex objects
private final AtomicReference<Mat> latestFrame = new AtomicReference<>(null);

public Mat getLatestFrame() {
    return latestFrame.get();  // Thread-safe read
}

private void captureLoop() {
    Mat old = latestFrame.getAndSet(newFrame);
    if (old != null) old.release();  // Clean up old frame
}

// ✅ CORRECT — volatile for primitives and flags
private volatile boolean running = true;
private volatile double lastCursorX = 0;

public void stop() {
    running = false;  // Signal capture thread to exit
}

// ❌ WRONG — Non-volatile, non-atomic access
private Mat latestFrame;  // Race condition!
private boolean running;
```

### 2.5 ONNX Model Loading (Cached, Not Reloaded)

> **Rule:** ONNX models are large (50-100MB each). Load ONCE during init, cache globally.

```java
// ✅ CORRECT — Singleton model cache
public class OnnxModelCache {
    private static volatile Net palmDetector = null;
    private static volatile Net handLandmarks = null;

    public static synchronized Net getPalmDetector() {
        if (palmDetector == null) {
            palmDetector = Dnn.readNetFromONNX("models/palm_detection.onnx");
        }
        return palmDetector;
    }

    public static synchronized Net getHandLandmarks() {
        if (handLandmarks == null) {
            handLandmarks = Dnn.readNetFromONNX("models/hand_landmarks.onnx");
        }
        return handLandmarks;
    }

    public static void cleanup() {
        if (palmDetector != null) palmDetector.release();
        if (handLandmarks != null) handLandmarks.release();
    }
}

// ❌ WRONG — Reloading models on every inference
for (Mat frame : frames) {
    Net model = Dnn.readNetFromONNX("palm_detection.onnx");  // SLOW!
    detect(frame, model);
}
```

### 2.6 Error Handling Strategy

> **Rule:** When hand is not detected, **degrade gracefully** — don't crash, don't move cursor.

```
Scenario: Hand disappears from frame
  Current hand detection: YES
  New frame: NO hand
  Action: Set isHandDetected=false
          DO NOT call mouseMove()
          UI shows "No hand detected"
          
Scenario: ONNX inference fails (corrupted model, OOM)
  Try-catch in HandDetector
  Log error with stack trace
  Set isHandDetected=false
  Continue loop (don't crash)

Scenario: Robot.mouseMove() fails (permissions denied)
  Catch AWTException
  Log "Accessibility permissions required (macOS)"
  Show dialog to user
  Continue loop (don't crash)
```

---

## 3. Code Organization

```
src/main/java/com/vision/
├── Main.java                          # Entry point
├── HandTrackingApp.java               # Main event loop coordinator
├── HandTrackingPipeline.java          # Pipeline orchestrator
│
├── video/
│   ├── CameraCapture.java             # Camera thread, frame buffering
│   ├── DisplayWindow.java             # Swing UI (EDT)
│   ├── FrameConverter.java            # Mat → BufferedImage converter
│   └── InitializingOpenCV.java        # OpenCV initialization check
│
├── detection/
│   ├── HandDetector.java              # ONNX inference pipeline
│   ├── HandLandmarks.java             # Data class for 21 landmarks
│   ├── LandmarkIndex.java             # Enum: INDEX_FINGER_TIP=8, THUMB_TIP=4
│   ├── CoordinateNormalizer.java      # Relative (0.0-1.0) → pixel coords
│   └── OnnxModelLoader.java           # Singleton ONNX model cache
│
├── control/
│   └── MouseController.java           # Singleton Robot, coordinate mapping, EMA
│
├── gesture/
│   ├── GestureRecognizer.java         # Pinch detection, state machine
│   └── EMAFilter.java                 # Exponential Moving Average filter
│
├── models/
│   └── PalmDetector.java              # Wrapper for palm detection logic
│
└── util/
    └── [utility classes if needed]

src/main/resources/
├── models/
│   ├── palm_detection.onnx
│   └── hand_landmarks.onnx
└── config/
    └── app.properties                 # Thresholds, FPS limits, α value
```

---

## 4. Test Generation Rules

> **For MouseVision, tests are UNIT tests, NOT integration tests with cameras.**

### 4.1 Unit Test Strategy

| Module | What to Test | What to Mock |
|--------|--------------|--------------|
| **CoordinateNormalizer** | Math transformation (0.5, 0.5) → (320, 240) | Nothing (pure function) |
| **EMAFilter** | Filter formula α·Yt + (1−α)·St-1 | Nothing (pure function) |
| **GestureRecognizer** | Distance calculation, state machine (pinch detection) | HandLandmarks mock |
| **HandDetector** | None (depends on OpenCV + ONNX — integration only) | Test with real frame in integration suite |
| **CameraCapture** | None (depends on hardware camera) | Skip in unit tests, use integration with dummy frame |

### 4.2 Test File Structure

```java
// ✅ PATTERN — Unit test for stateless logic
@DisplayName("EMAFilter — Exponential Moving Average")
class EMAFilterTest {

    private EMAFilter filter;

    @BeforeEach
    void setup() {
        filter = new EMAFilter(0.3);  // α = 0.3
    }

    @Test
    @DisplayName("should calculate first value as raw input")
    void testFirstValue() {
        double result = filter.apply(100.0);
        assertEquals(100.0, result, "First value should be raw input");
    }

    @Test
    @DisplayName("should apply formula: St = α·Yt + (1−α)·St−1")
    void testEMAFormula() {
        filter.apply(100.0);  // Initialize with 100
        double result = filter.apply(120.0);  // Apply 120
        // 0.3 * 120 + 0.7 * 100 = 36 + 70 = 106
        assertEquals(106.0, result, 0.01, "Formula result should be 106.0");
    }

    @Test
    @DisplayName("should reset filter state")
    void testReset() {
        filter.apply(100.0);
        filter.reset();
        double result = filter.apply(200.0);
        assertEquals(200.0, result, "After reset, next value should be raw input");
    }
}
```

---

## 5. Configuration Externalisation

> **Rule:** All thresholds, limits, and parameters go in `src/main/resources/app.properties`.

```properties
# Camera
camera.resolution.width=640
camera.resolution.height=480
camera.fps.target=30

# Hand Detection
hand.detection.confidence.threshold=0.5
hand.landmark.confidence.threshold=0.3

# Gesture Recognition
gesture.pinch.distance.threshold=0.05
gesture.pinch.debounce.ms=300

# Mouse Control
mouse.ema.alpha=0.3
mouse.movement.frequency.hz=30

# Logging
log.level=INFO
log.file=logs/mousevision.log
```

```java
// ✅ CORRECT — Load config at startup
public class ConfigLoader {
    private static final Properties props = new Properties();

    static {
        try (InputStream is = ConfigLoader.class.getResourceAsStream("/app.properties")) {
            props.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load app.properties", e);
        }
    }

    public static int getCameraWidth() {
        return Integer.parseInt(props.getProperty("camera.resolution.width", "640"));
    }

    public static double getEmaAlpha() {
        return Double.parseDouble(props.getProperty("mouse.ema.alpha", "0.3"));
    }
}
```

---

## 6. Exception Handling Strategy

> **Rule:** Catch exceptions early, log them, degrade gracefully.

```java
// ✅ CORRECT — Graceful degradation
public class HandTrackingApp {
    public void run() {
        try {
            CameraCapture capture = new CameraCapture(0);
            capture.start();
            // ... main loop
        } catch (AWTException e) {
            logger.error("Accessibility permissions required: " + e.getMessage());
            showDialog("Please enable accessibility permissions in System Settings");
        } catch (UnsatisfiedLinkError e) {
            logger.error("OpenCV native library not found: " + e.getMessage());
            showDialog("OpenCV not properly installed. Reinstall dependencies.");
        } catch (Exception e) {
            logger.error("Unexpected error: " + e.getMessage(), e);
            showDialog("An error occurred. Check logs for details.");
        }
    }
}
```

---

## 7. Conventional Commits — MouseVision Aligned

```
1. docs(spec):          add spec for [feature]
2. feat(model):         add [Entity] data class
3. feat(logic):         add [Feature] core algorithm/logic
4. test(unit):          add unit tests for [feature]
5. feat(integration):   integrate [feature] into pipeline
6. docs(guide):         add architecture/threading guide for [feature]
```

---

## 8. Violation Policy

| Severity | Violation |
|----------|-----------|
| 🔴 BLOCKER | Using blocking I/O on main/EDT thread |
| 🔴 BLOCKER | Mat objects not released (memory leak) |
| 🔴 BLOCKER | ONNX models reloaded on every inference |
| 🔴 BLOCKER | Cursor movement when `isHandDetected() == false` |
| 🟡 MAJOR | Not using `volatile` or `AtomicReference` for thread-shared state |
| 🟡 MAJOR | Calling Robot methods from capture/detection thread (should be separate thread) |
| 🟡 MAJOR | Hardcoded thresholds (should be in `app.properties`) |
| 🟠 MINOR | Missing exception handling for Robot/Camera failures |

---

> **This constitution is the law for MouseVision. The AI must follow every rule without exception.**
