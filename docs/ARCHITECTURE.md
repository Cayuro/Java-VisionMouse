# 🏗️ ARCHITECTURE — MouseVision System Design

> **Document Version:** 1.0
> **Date:** 2026-04-20
> **Status:** Reference Guide

---

## 1. System Overview

MouseVision is a **real-time computer vision application** that enables gesture-based mouse control. The system processes video frames, detects hand landmarks, and translates them to mouse movements and clicks.

### Core Pipeline

```
┌───────────────────────────────────────────────────────────────────────┐
│                                                                       │
│  REAL-TIME VISION PIPELINE                                          │
│                                                                       │
│  [Capture] → [Detect] → [Map] → [Smooth] → [Control] → [OS]        │
│     30ms      30ms      5ms      5ms       5ms        5ms            │
│                                                                       │
│  ~80-100ms total latency (acceptable for mouse control)              │
│                                                                       │
└───────────────────────────────────────────────────────────────────────┘
```

---

## 2. Layered Architecture

### Layer Model

```
┌──────────────────────────────────────────────────────────────────┐
│                        USER INTERFACE                             │
│                                                                   │
│    DisplayWindow (Swing JFrame)                                  │
│    • Shows video feed (camera + landmarks)                       │
│    • Displays hand detection status                              │
│    • Shows cursor position (debug)                               │
│    • Receives BufferedImage updates from video thread            │
└──────────────────────────────────────────────────────────────────┘
           ↑                                    ↓
    (BufferedImage)                  (user interactions)
           ↑                                    ↓
┌──────────────────────────────────────────────────────────────────┐
│                    PROCESSING PIPELINE                            │
│                                                                   │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────┐      │
│  │  Capture    │→ │   Detection  │→ │  Control & Gesture  │      │
│  │ CameraThread│  │  (ONNX Infr.)│  │  (Mapping + Click)   │      │
│  └─────────────┘  └──────────────┘  └─────────────────────┘      │
│                                                                   │
│  • ThreadSafe state sharing (AtomicReference<Mat>)               │
│  • Latency optimized (skip frames if needed)                     │
│  • Graceful degradation (hand lost → no cursor movement)         │
└──────────────────────────────────────────────────────────────────┘
           ↑                                    ↓
    (OpenCV Mat)                    (Robot.mouseMove/mousePress)
           ↑                                    ↓
┌──────────────────────────────────────────────────────────────────┐
│                   NATIVE RESOURCES                                │
│                                                                   │
│  • OpenCV (VideoCapture, Mat)                                    │
│  • ONNX Models (DNN)                                             │
│  • java.awt.Robot (OS Control)                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Detailed Layer Breakdown

| Layer | Component | Responsibility | Thread | I/O Type |
|-------|-----------|-----------------|--------|----------|
| **UI** | DisplayWindow | Render video, show status | EDT (Swing) | User input |
| **Capture** | CameraCapture | Read frames, buffer latest | Capture thread | Camera (VideoCapture) |
| **Detection** | HandDetector | ONNX inference, landmarks | Capture thread | CPU (DNN) |
| **Processing** | CoordinateNormalizer, EMAFilter | Transform coordinates | Capture thread | In-memory |
| **Control** | MouseController | Move cursor, execute clicks | Control thread | OS (Robot) |
| **Gesture** | GestureRecognizer | Pinch detection, state machine | Capture/Control thread | In-memory |
| **Resource** | OnnxModelLoader | Singleton model cache | Init thread | Disk (models) |

---

## 3. Module Organization

### Package Structure

```
src/main/java/com/vision/
│
├── Main.java                     ← Entry point
├── HandTrackingApp.java          ← Main event loop
├── HandTrackingPipeline.java     ← Pipeline coordinator
│
├── video/                        ← EP-01 Module
│   ├── CameraCapture.java        ← Thread: continuous frame capture
│   ├── DisplayWindow.java        ← Swing UI (EDT)
│   ├── FrameConverter.java       ← Mat → BufferedImage
│   └── InitializingOpenCV.java   ← OpenCV init check
│
├── detection/                    ← EP-02 Module
│   ├── HandDetector.java         ← ONNX pipeline (inference)
│   ├── HandLandmarks.java        ← Data class: 21 points
│   ├── LandmarkIndex.java        ← Enum: landmark IDs
│   ├── CoordinateNormalizer.java ← Pixel → normalized coords
│   └── OnnxModelLoader.java      ← Singleton model cache
│
├── control/                      ← EP-03 Module
│   └── MouseController.java      ← Singleton Robot, mapping, EMA
│
├── gesture/                      ← EP-04 Module
│   ├── GestureRecognizer.java    ← Pinch detection + state machine
│   └── EMAFilter.java            ← Exponential Moving Average filter
│
└── models/ (config)
    └── PalmDetector.java         ← (utility for palm detection)
```

### Module Responsibilities (RACI)

| Module | Role | Responsibility |
|--------|------|-----------------|
| **CameraCapture** | RESPONSIBLE | Capture frames, maintain buffer, run capture thread |
| **HandDetector** | RESPONSIBLE | ONNX inference, extract landmarks, detect presence |
| **MouseController** | RESPONSIBLE | Map coords, move cursor, execute clicks, manage Robot |
| **GestureRecognizer** | RESPONSIBLE | Detect pinches, state machine, debounce, fire clicks |
| **FrameConverter** | CONSULTED | Convert Mat to BufferedImage for display |
| **DisplayWindow** | ACCOUNTABLE | Show video + status to user |
| **Main** | INFORMED | Coordinate startup/shutdown |

---

## 4. Data Flow

### Single Frame Processing

```
Frame ← Camera (VideoCapture)
  │
  ├─ CameraCapture.captureLoop() [Capture Thread]
  │  └─ Reads Mat from VideoCapture
  │     └─ Stores in AtomicReference<Mat> latestFrame
  │
  ├─ (Parallel) UI Thread polls latestFrame
  │  └─ FrameConverter.matToBufferedImage()
  │     └─ DisplayWindow.updateFrame()
  │        └─ JLabel renders BufferedImage (EDT)
  │
  └─ HandDetector.detect(frame) [In Capture Thread]
     ├─ Palm Detection ONNX (192×192 input)
     │  └─ Extracts hand ROI
     │
     ├─ Hand Landmark ONNX (256×256 input)
     │  └─ Extracts 21 landmarks
     │
     └─ CoordinateNormalizer.toNormalized()
        └─ HandLandmarks (21 points in [0.0, 1.0])
           │
           ├─ GestureRecognizer.evaluate()
           │  ├─ Calculate distance(Point4, Point8)
           │  ├─ State machine evaluation
           │  └─ Robot.mouseClick() if pinch detected
           │
           └─ MouseController.moveMouse()
              ├─ Apply mirror effect
              ├─ Map to screen coordinates
              ├─ Apply EMA smoothing
              └─ Robot.mouseMove(x, y)
```

### Frame Buffering Strategy

```
Capture Thread:
  ┌───────────────────────────────────────┐
  │ Continuously reads from VideoCapture  │
  │                                       │
  │ Mat frame = capture.read();           │
  │ old = latestFrame.getAndSet(frame);   │
  │ if (old != null) old.release();       │
  └───────────────────────────────────────┘
           ↓
  Only 1 Mat in memory at a time
  (latest frame always available)

UI Thread:
  ┌───────────────────────────────────────┐
  │ Polls latestFrame whenever ready      │
  │                                       │
  │ Mat current = latestFrame.get();      │
  │ if (current != null) convert & show   │
  └───────────────────────────────────────┘
           ↓
  NO queue, NO frame drops
  (UI shows whatever is latest)
```

---

## 5. Threading Model

### Thread Responsibilities

| Thread | Name | Role | Lifecycle | I/O Blocking |
|--------|------|------|-----------|--------------|
| **Main/EDT** | Swing EDT | UI rendering, user input | App lifetime | No (must never block) |
| **Capture** | CameraCapture thread | Read frames, ONNX inference | App lifetime | YES (VideoCapture, ONNX) |
| **Control** | MouseController thread | Move cursor, execute clicks | App lifetime | YES (Robot.mouseMove) |
| **Init** | Main thread | OpenCV init, model loading | App startup | YES (file I/O, ONNX load) |

### Inter-Thread Communication

```
Capture Thread ←→ EDT
  Data: AtomicReference<Mat> (frame buffer)
  Sync: volatile reads/writes
  Note: Never call Swing methods from capture thread
        Always use SwingUtilities.invokeLater()

Capture Thread → Control Thread
  Data: HandLandmarks (landmarks, hand presence)
  Sync: Thread passes data, control thread reads
  Note: Coordinate mapping happens in control thread

Capture Thread → UI (via EDT)
  Data: BufferedImage (for display)
  Sync: SwingUtilities.invokeLater(updateFrame)
```

### Synchronization Patterns

```java
// Pattern 1: AtomicReference for thread-safe frame buffer
private final AtomicReference<Mat> latestFrame = new AtomicReference<>(null);

public Mat getLatestFrame() {
    return latestFrame.get();  // Thread-safe read
}

// Pattern 2: volatile for flags
private volatile boolean running = true;
private volatile double lastCursorX = 0;

public void stop() {
    running = false;  // Signal from main to capture thread
}

// Pattern 3: synchronized blocks for state changes
private synchronized void executeClick() {
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    // ...
}

// Pattern 4: SwingUtilities for UI updates from non-EDT threads
SwingUtilities.invokeLater(() -> {
    displayWindow.updateFrame(bufferedImage);
});
```

---

## 6. Resource Management

### OpenCV Mat Lifecycle

```
Create (Mat object in JVM):
  Mat frame = capture.read();           ← Native memory allocated

Use (in processing):
  roi = frame.submat(...);              ← Submatrix (shares memory)
  resized = new Mat();
  Imgproc.resize(frame, resized, ...);  ← New Mat created

Release (MUST DO before frame exits scope):
  frame.release();                      ← Native memory freed
  resized.release();

Failure to release:
  → Native memory leak → JVM heap corruption → eventual crash
```

### ONNX Model Lifecycle

```
Load (once at startup):
  Net palmDetector = Dnn.readNetFromONNX("palm.onnx");      ← ~3.5 MB loaded
  Net handLandmarks = Dnn.readNetFromONNX("hand.onnx");     ← ~50 MB loaded
  
Cache (reuse for every frame):
  for each frame:
    Mat blobs = Dnn.blobFromImage(frame, ...);
    palmDetector.forward(blobs);        ← Reuses same Net object

Release (at app shutdown):
  palmDetector.release();               ← Free GPU/CPU memory
  handLandmarks.release();
```

### Robot Initialization

```
Singleton Pattern:
  MouseController instance = MouseController.getInstance();
  ← First call: Creates Robot, caches it
  ← Subsequent calls: Return same instance
  
Lifecycle:
  • Created once on app start
  • Reused for all mouse operations
  • NO cleanup needed (Robot holds OS resources)
```

---

## 7. Error Handling Strategy

### Failure Modes & Responses

| Failure | Capture Thread Behavior | User Experience | Recovery |
|---------|-------------------------|-----------------|----------|
| Camera disconnected | Catch exception, log, stop loop | "Camera Error" shown | User reconnects camera, restarts app |
| ONNX model missing | Catch on init, throw exception | Error dialog, app exits | Install models, restart |
| Hand lost mid-frame | Continue loop, set flag false | Cursor freezes in place | Hand reappears, cursor moves again |
| Robot permission denied (macOS) | Catch AWTException, log | "Permission required" dialog | User enables accessibility, restarts |
| OOM during inference | Catch OutOfMemoryError | Log critical error | Free memory (no click), continue |

### Exception Hierarchy

```
Throwable
  └─ Exception
      ├─ RuntimeException
      │   ├─ CameraUnavailableException
      │   ├─ ModelLoadException
      │   ├─ RobotInitializationException
      │   └─ GestureRecognitionException
      │
      └─ IOException (config loading)
```

---

## 8. Performance Characteristics

### Latency Budget (Per Frame)

```
Total Target: < 100ms (for responsive mouse control)

Breakdown:
  Capture (read from VideoCapture):        ~5ms
  Palm Detection ONNX (192×192):          ~20ms
  Hand Landmark ONNX (256×256):           ~30ms
  Post-processing (normalize, extract):    ~3ms
  Coordinate mapping + EMA:                ~5ms
  Robot.mouseMove() execution:             ~5ms
  ─────────────────────────────────────────────
  Total:                                  ~68ms ✓ (well under 100ms)
```

### Memory Usage (Steady State)

```
Java Heap:
  • HandLandmarks object (21 Point2D):     ~1 KB
  • State machines, filters:               ~10 KB
  
Native Memory (OpenCV):
  • Latest Mat (640×480 BGR):              ~3.7 MB
  • ONNX Models (cached):                  ~53.5 MB (loaded once)
  ─────────────────────────────────────────────
  Total:                                   ~57 MB
```

### FPS Target & Achieved

```
Camera FPS: 30 (typical webcam)
Processing FPS: 25 (target minimum)
  → Allow ~33-40ms per frame

If processing time > frame time:
  • Drop frames (don't process every captured frame)
  • Maintain FPS by skipping detection on every 2nd frame
  • Monitor and warn if sustained < 25 FPS
```

---

## 9. Configuration Points

### app.properties

```properties
# Camera
camera.resolution.width=640
camera.resolution.height=480
camera.device.id=0
camera.fps.target=30

# Hand Detection
hand.detection.confidence.threshold=0.5
hand.landmark.confidence.threshold=0.3
hand.nms.iou.threshold=0.3

# Mouse Control
mouse.ema.alpha=0.3
mouse.movement.frequency.hz=30
mouse.sensitivity.scale=1.0
mouse.mirroring.enabled=true

# Gesture Recognition
gesture.pinch.distance.threshold=0.05
gesture.pinch.debounce.ms=300
gesture.pinch.press.duration.ms=75

# Logging
log.level=INFO
log.file=logs/mousevision.log
```

---

## 10. Deployment & System Requirements

### Minimum Requirements

- **OS:** Windows 7+, macOS 10.12+, or Linux (Ubuntu 16.04+)
- **Java:** JDK 21+
- **Memory:** 256 MB RAM (heap) + 100 MB (native resources)
- **Disk:** 100 MB free (models + app)
- **Hardware:** USB Webcam or built-in camera
- **Dependencies:** OpenCV (via JavaCV), ONNX Runtime

### Permission Requirements

- **macOS:** Accessibility permissions (System Preferences → Security → Accessibility)
- **Linux:** Camera permissions (typically default)
- **Windows:** No special permissions needed

---

> **Next Step:** Read `THREADING_GUIDE.md` for detailed threading patterns.
> Read `CONSTITUTION_MouseVision.md` for architectural rules.
