# 🧵 THREADING GUIDE — MouseVision Real-Time Processing

> **Document Version:** 1.0
> **Date:** 2026-04-20
> **Audience:** Developers implementing capture/processing threads

---

## 1. Threading Philosophy

MouseVision **MUST** maintain **real-time responsiveness**. This means:

1. **UI thread (EDT) must NEVER block** → Swing remains responsive
2. **Expensive I/O must be on separate threads** → Camera, ONNX inference
3. **Thread communication must be lock-free where possible** → Low latency
4. **Resource cleanup must be explicit** → Native objects must be released

---

## 2. Thread Architecture

### High-Level View

```
┌─────────────────────────────────────┐
│      User Interaction (UI)          │
│    (Swing EDT)                      │
└────────────────┬────────────────────┘
                 │ Requests (start/stop)
                 ↓
┌─────────────────────────────────────┐
│    Capture Thread                   │
│    (CameraCapture.run())            │
│                                     │
│  • Reads from VideoCapture          │
│  • Runs ONNX inference              │
│  • Updates shared state             │
│  • Long-running, blocking I/O       │
└────┬─────────────────┬──────────────┘
     │                 │
     │ Frame           │ Landmarks
     ↓                 ↓
┌────────────────────────────────────────┐
│    Control Thread                      │
│    (MouseController)                   │
│                                        │
│  • Moves cursor (Robot)                │
│  • Executes clicks                     │
│  • Reads landmarks                     │
│  • Fires events to listeners           │
└────────────────────────────────────────┘
```

### Thread Lifecycle

```
[App Start]
    ↓
[Main Thread] Initialize OpenCV, load models
    ↓
[Main Thread] Create CameraCapture, MouseController
    ↓
[User clicks START]
    ↓
[EDT] → Signal capture thread to start
    ↓
[Capture Thread starts] while (running) { capture → detect → send landmarks }
    ↓
[Control Thread consumes landmarks] move mouse, detect gestures
    ↓
[User clicks STOP]
    ↓
[EDT] → Signal capture thread to stop (running = false)
    ↓
[Capture Thread exits loop] ← Waits for loop condition to fail
    ↓
[Main/EDT waits for thread] join() ← Blocks main until capture thread dies
    ↓
[Cleanup] Release Mat, close VideoCapture
    ↓
[App ends]
```

---

## 3. Capture Thread Implementation

### Template

```java
public class CameraCapture implements Runnable {
    
    private volatile boolean running = false;
    private final AtomicReference<Mat> latestFrame = new AtomicReference<>(null);
    private VideoCapture capture;
    
    public void start() {
        if (running) return;
        running = true;
        new Thread(this, "CameraCapture").start();
    }
    
    public void stop() {
        running = false;  // Signal thread to stop
        // Thread will exit on next loop iteration when running = false
    }
    
    @Override
    public void run() {
        try {
            capture = new VideoCapture(0);  // Device ID 0
            if (!capture.isOpened()) {
                throw new CameraUnavailableException("Cannot open camera");
            }
            
            Mat frame = new Mat();
            long lastFPSCheck = System.currentTimeMillis();
            int frameCount = 0;
            
            while (running) {  // ← Check flag each iteration
                if (!capture.read(frame)) {
                    System.err.println("Failed to read frame");
                    continue;
                }
                
                // Process frame
                try {
                    HandLandmarks landmarks = detectHand(frame);
                    
                    // Update UI with frame
                    BufferedImage img = FrameConverter.matToBufferedImage(frame);
                    SwingUtilities.invokeLater(() -> 
                        displayWindow.updateFrame(img)
                    );
                    
                    // Update mouse control
                    mouseController.moveMouse(landmarks);
                    
                } catch (Exception e) {
                    logger.error("Processing error: " + e.getMessage(), e);
                }
                
                // Update frame buffer (release old frame)
                Mat oldFrame = latestFrame.getAndSet(frame);
                if (oldFrame != null && oldFrame != frame) {
                    oldFrame.release();
                }
                
                // FPS monitoring
                frameCount++;
                if (System.currentTimeMillis() - lastFPSCheck >= 1000) {
                    System.out.println("FPS: " + frameCount);
                    frameCount = 0;
                    lastFPSCheck = System.currentTimeMillis();
                }
            }
            
        } catch (Exception e) {
            logger.error("Capture thread error: " + e.getMessage(), e);
        } finally {
            cleanup();
        }
    }
    
    private void cleanup() {
        running = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        Mat frame = latestFrame.getAndSet(null);
        if (frame != null) {
            frame.release();
        }
    }
    
    public Mat getLatestFrame() {
        return latestFrame.get();  // Thread-safe read
    }
}
```

### Key Patterns

#### 1. Volatile Flag for Exit Signal

```java
private volatile boolean running = false;

public void stop() {
    running = false;  // ← Thread WILL see this and exit
}

@Override
public void run() {
    while (running) {  // ← Check on every iteration
        // Work
    }
}
```

✅ **Why:** Volatile ensures visibility across threads without locks
✅ **Benefit:** Low-latency, lock-free thread termination
❌ **Don't:** Use sleep() in the loop — use volatile flag instead

#### 2. AtomicReference for Frame Buffer

```java
private final AtomicReference<Mat> latestFrame = new AtomicReference<>(null);

// Capture thread writes
Mat oldFrame = latestFrame.getAndSet(newFrame);
if (oldFrame != null) oldFrame.release();

// UI thread reads (safe)
Mat current = latestFrame.get();
if (current != null) { /* use it */ }
```

✅ **Why:** Atomic guarantees thread-safe compare-and-swap
✅ **Benefit:** No locks, automatic cleanup of old frames
❌ **Don't:** Use synchronized Map or queue (overkill, adds latency)

#### 3. SwingUtilities.invokeLater for UI Updates

```java
// Capture thread (NOT on EDT)
BufferedImage image = FrameConverter.matToBufferedImage(frame);

// Update UI safely
SwingUtilities.invokeLater(() -> {
    displayWindow.updateFrame(image);  // ← Runs on EDT
});
```

✅ **Why:** Swing is not thread-safe; UI updates must happen on EDT
✅ **Benefit:** Prevents race conditions in JComponent state
❌ **Don't:** Call displayWindow.updateFrame() directly from capture thread

#### 4. Try-Finally for Resource Cleanup

```java
@Override
public void run() {
    try {
        while (running) {
            // Work
        }
    } finally {
        cleanup();  // ← ALWAYS runs, even if exception thrown
    }
}

private void cleanup() {
    if (capture != null) capture.release();
    Mat frame = latestFrame.getAndSet(null);
    if (frame != null) frame.release();
}
```

✅ **Why:** Ensures native resources are released even on error
✅ **Benefit:** Prevents memory leaks from crashes
❌ **Don't:** Forget try-finally; VideoCapture might stay open

---

## 4. Control Thread (MouseController)

### Template

```java
public class MouseController {
    private volatile HandLandmarks lastLandmarks = null;
    private EMAFilter emaX = new EMAFilter(0.3);
    private EMAFilter emaY = new EMAFilter(0.3);
    private long lastMouseMoveTime = 0;
    
    public void moveMouse(HandLandmarks landmarks) {
        if (landmarks == null || !landmarks.isHandDetected()) {
            // Hand lost → don't move cursor
            lastLandmarks = null;
            emaX.reset();
            emaY.reset();
            return;
        }
        
        lastLandmarks = landmarks;
        
        // Get index finger position
        Point2D indexTip = landmarks.getIndexFingerTip();
        
        // Apply mirror (horizontal flip)
        double mirroredX = 1.0 - indexTip.getX();
        
        // Map to screen coordinates
        int screenX = (int) (mirroredX * screenWidth);
        int screenY = (int) (indexTip.getY() * screenHeight);
        
        // Clamp to boundaries
        screenX = Math.max(0, Math.min(screenWidth - 1, screenX));
        screenY = Math.max(0, Math.min(screenHeight - 1, screenY));
        
        // Apply EMA smoothing
        double smoothX = emaX.apply(screenX);
        double smoothY = emaY.apply(screenY);
        
        // Rate limiting (30 Hz max)
        long now = System.currentTimeMillis();
        if (now - lastMouseMoveTime >= 1000 / 30) {
            robot.mouseMove((int) smoothX, (int) smoothY);
            lastMouseMoveTime = now;
        }
    }
    
    public void executeClick(HandLandmarks landmarks) {
        if (landmarks == null || !landmarks.isHandDetected()) {
            return;  // No click if hand lost
        }
        
        double distance = calculateDistance(
            landmarks.getThumbTip(),
            landmarks.getIndexFingerTip()
        );
        
        if (distance < 0.05) {  // Threshold
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            try {
                Thread.sleep(75);  // Hold for 75ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        }
    }
}
```

### Thread Safety Notes

```java
// Safe: Can be called from ANY thread (capture, UI, main)
public void moveMouse(HandLandmarks landmarks) { ... }

// WHY: Does NOT modify shared state (emaX/emaY are thread-confined)
// Captures landmarks reference (immutable data class)
// Calls Robot.mouseMove (thread-safe, OS handles it)
```

---

## 5. EDT (Swing Event Dispatch Thread) Handling

### The Problem

```java
// ❌ WRONG — Called from capture thread
capture_thread.run() {
    BufferedImage img = convertFrame();
    displayWindow.setIcon(img);  // ← ERROR: Not on EDT!
}

// This crashes with:
// Exception in thread "CameraCapture" 
// javax.swing.SwingUtilities$ThreadingViolationException: Swing access from non-EDT
```

### The Solution

```java
// ✅ CORRECT — Route through EDT
capture_thread.run() {
    BufferedImage img = convertFrame();
    SwingUtilities.invokeLater(() -> {
        displayWindow.setIcon(img);  // ← Runs on EDT
    });
}
```

### SwingUtilities Patterns

```java
// Pattern 1: Update single component
SwingUtilities.invokeLater(() -> {
    label.setText("Hand detected: true");
});

// Pattern 2: Multiple updates (batch)
SwingUtilities.invokeLater(() -> {
    statusLabel.setText("Detecting...");
    fpsLabel.setText("FPS: 30");
    imageLabel.setIcon(img);
});

// Pattern 3: With error handling
SwingUtilities.invokeLater(() -> {
    try {
        displayWindow.updateFrame(img);
    } catch (Exception e) {
        logger.error("UI update failed: " + e);
    }
});
```

---

## 6. Common Threading Pitfalls

### Pitfall 1: Blocking the EDT

```java
// ❌ WRONG — Blocks EDT for 5 seconds
@Override
public void actionPerformed(ActionEvent e) {
    Thread.sleep(5000);  // EDT FROZEN during this time
    label.setText("Done");
}

// ✅ CORRECT — Use separate thread
@Override
public void actionPerformed(ActionEvent e) {
    new Thread(() -> {
        Thread.sleep(5000);  // Separate thread sleeps
        SwingUtilities.invokeLater(() -> {
            label.setText("Done");  // Update on EDT
        });
    }).start();
}
```

### Pitfall 2: Not Releasing Resources

```java
// ❌ WRONG — Mat never released
private void processFrame(Mat frame) {
    Mat resized = new Mat();
    Imgproc.resize(frame, resized, ...);
    // resized never released → memory leak!
}

// ✅ CORRECT — Release in finally
private void processFrame(Mat frame) {
    Mat resized = new Mat();
    try {
        Imgproc.resize(frame, resized, ...);
    } finally {
        resized.release();  // Always released
    }
}
```

### Pitfall 3: Missing Volatile Flag

```java
// ❌ WRONG — Thread won't see the stop signal
private boolean running = false;  // NOT volatile

public void stop() {
    running = false;  // Capture thread might not see this
}

// ✅ CORRECT
private volatile boolean running = false;  // Volatile

public void stop() {
    running = false;  // Guaranteed to be seen
}
```

### Pitfall 4: Race Condition on Frame Buffer

```java
// ❌ WRONG — Not atomic
private Mat latestFrame;

public void captureLoop() {
    latestFrame = capture.read();  // No sync, race condition!
}

public Mat getFrame() {
    return latestFrame;  // Might be null, partial read
}

// ✅ CORRECT — Atomic
private final AtomicReference<Mat> latestFrame = new AtomicReference<>();

public void captureLoop() {
    latestFrame.set(capture.read());  // Atomic write
}

public Mat getFrame() {
    return latestFrame.get();  // Atomic read
}
```

---

## 7. Debugging Threading Issues

### Problem: Deadlock (Thread Hangs)

**Symptoms:** App freezes, threads not progressing
**Cause:** Circular lock dependency (Thread A waits for B, B waits for A)
**Solution:** 
```java
// Use jstack to see thread states
jstack <pid> | grep -A 10 "CameraCapture"

// Look for: waiting to lock held by other thread
```

### Problem: Memory Leak (Memory Grows)

**Symptoms:** Heap memory increases over time, doesn't decrease
**Cause:** Mat objects not released, circular references
**Solution:**
```java
// Check for unreleased Mat:
private void cleanup() {
    if (frame != null) frame.release();  // MUST release!
}

// Verify in finally block:
try { ... } finally { cleanup(); }
```

### Problem: Frame Drops (Inconsistent FPS)

**Symptoms:** FPS counter shows 15 instead of 25
**Cause:** Processing too slow, frames skipped
**Solution:**
```java
// Add FPS monitoring:
if (frameCount % 30 == 0) {
    System.out.println("FPS: " + fps);
}

// If < 25 FPS:
// 1. Profile ONNX inference time
// 2. Reduce resolution 640×480 → 320×240
// 3. Process every 2nd frame (skip odd frames)
```

---

## 8. Best Practices Summary

| Practice | Why | Example |
|----------|-----|---------|
| Use `volatile` for flags | Visibility across threads | `private volatile boolean running` |
| Use `AtomicReference` for complex objects | Atomic compare-and-swap | `AtomicReference<Mat> frame` |
| Use `SwingUtilities.invokeLater()` for UI updates | Thread safety | `SwingUtilities.invokeLater(() -> {...})` |
| Use try-finally for resource cleanup | Guarantee cleanup | `finally { frame.release(); }` |
| Check `running` flag every loop iteration | Responsive stop signal | `while (running) { ... }` |
| Profile ONNX inference time | Detect bottlenecks | Log frame timestamps |
| Set thread names | Debugging | `new Thread(this, "CameraCapture")` |
| Use Thread.sleep(ms) in EDT? | NO — Use SwingWorker or separate thread | Never block EDT |

---

> **Next Step:** Read `OPENCV_PATTERNS.md` for OpenCV-specific patterns.
