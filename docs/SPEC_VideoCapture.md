# 📋 SPECIFICATION: Video Capture Pipeline

> **Spec ID:** `SPEC-VP-001`
> **Feature:** Real-time camera capture with frame buffering
> **Author:** MouseVision Team
> **Date:** 2026-04-20
> **Status:** ✅ Approved for Sprint 1
> **Related Epic:** EP-01 — Video (Webcam & UI)

---

## 1. Business Goal

**As a** vision application developer,
**I need** continuous real-time camera capture running on a separate thread with frame buffering,
**So that** the UI remains responsive and hand detection can process frames without blocking the capture loop.

---

## 2. Hard Business Rules

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-VP-001` | Camera must open successfully on device ID 0 | Throw `CameraUnavailableException("Camera not found or in use")` |
| `BR-VP-002` | Frame resolution must be 640×480 | Throw `InvalidResolutionException("Resolution must be 640×480")` |
| `BR-VP-003` | Capture loop must maintain ≥ 25 FPS | Warn if average FPS < 25 (log warning, continue) |
| `BR-VP-004` | Each Mat must be released explicitly | Runtime memory leak if not released → eventual JVM crash |
| `BR-VP-005` | Only ONE frame buffer should exist (latest frame) | No frame queue (excess memory) — always keep latest only |
| `BR-VP-006` | Thread must stop gracefully when stop() is called | Thread should exit within 1 second, no hanging threads |
| `BR-VP-007` | Frame buffer access must be thread-safe | Use `AtomicReference<Mat>` for concurrent reads/writes |

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Camera Opens and Captures Frames
> **Validates:** `BR-VP-001`, `BR-VP-003`

```gherkin
Given a webcam is connected and available on device ID 0
  And no other application is using the camera
When  CameraCapture.start() is called
Then  the capture thread launches successfully
  And getLatestFrame() returns non-null Mat objects
  And the frame resolution is 640×480
  And FPS counter shows ≥ 25 frames per second
```

**Test Method:** `shouldCaptureFramesContinuously()`

---

### Scenario 2 — Camera Not Available
> **Validates:** `BR-VP-001`

```gherkin
Given a webcam is NOT connected
  Or the camera device is locked by another application
When  CameraCapture.start() is called
Then  a CameraUnavailableException is thrown with message "Camera not found or in use"
  And the capture thread does not start
  And the application can continue (no crash)
```

**Test Method:** `shouldThrowExceptionWhenCameraUnavailable()`

---

### Scenario 3 — Invalid Resolution Configuration
> **Validates:** `BR-VP-002`

```gherkin
Given CameraCapture is configured with resolution 320×240 (not 640×480)
When  CameraCapture.start() is called
Then  an InvalidResolutionException is thrown
  And the message is "Resolution must be 640×480"
```

**Test Method:** `shouldThrowExceptionForInvalidResolution()`

---

### Scenario 4 — Mat Resource Cleanup
> **Validates:** `BR-VP-004`

```gherkin
Given CameraCapture is running and capturing 100 frames
When  each frame is processed and replaced by the next
Then  the OLD frame Mat is released (release() called)
  And memory usage remains stable (no leak)
```

**Test Method:** `shouldReleaseOldFrameMatsOnUpdate()`

---

### Scenario 5 — Thread-Safe Frame Access
> **Validates:** `BR-VP-007`

```gherkin
Given CameraCapture is running
  And Frame consumer thread calls getLatestFrame() concurrently
When  getLatestFrame() is called 1000 times in parallel
Then  no exception is thrown
  And Mat objects returned are valid (not corrupted)
  And AtomicReference handles concurrent access safely
```

**Test Method:** `shouldHandleConcurrentFrameAccess()`

---

### Scenario 6 — Graceful Thread Stop
> **Validates:** `BR-VP-006`

```gherkin
Given CameraCapture is running
  And the capture loop is active
When  stop() is called
Then  the capture loop exits within 1 second
  And the camera is released
  And the thread terminates (no hanging threads)
```

**Test Method:** `shouldStopThreadGracefully()`

---

### Scenario 7 — FPS Monitoring and Logging
> **Validates:** `BR-VP-003`

```gherkin
Given CameraCapture is running
  And frame processing is taking too long (reducing FPS)
When  average FPS drops below 25
Then  a warning is logged: "Average FPS: 18 (below target of 25)"
  And the capture continues (no exception)
```

**Test Method:** `shouldLogWarningWhenFPSDropsBelowTarget()`

---

## 4. UI Requirements

| Element | Binding | Validation/Action |
|---------|---------|-------------------|
| Video Display Label | Latest BufferedImage from FrameConverter | Updates every frame via `SwingUtilities.invokeLater()` |
| FPS Counter | `getAverageFPS()` | Display in top-right corner, update every 10 frames |
| Status Text | "Capturing..." or "Camera Error" | Shows if capture thread is alive |
| Start Button | Calls `CameraCapture.start()` | Disabled after start, enabled after stop |
| Stop Button | Calls `CameraCapture.stop()` | Enabled only after start |

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer | Class Name | Key Responsibility |
|-------|-----------|-------------------|
| Capture | `CameraCapture.java` | Runnable, VideoCapture, frame buffering |
| Converter | `FrameConverter.java` | Mat → BufferedImage, BGR→RGB conversion |
| Initializer | `InitializingOpenCV.java` | OpenCV lib loading verification |
| Data | `FrameMetadata.java` | FPS counter, timestamps |

### 5.2 SQL Queries

N/A (Not applicable — this is not a database feature)

### 5.3 Thread Safety Requirements

```java
// Frame buffer MUST be AtomicReference
private final AtomicReference<Mat> latestFrame = new AtomicReference<>(null);

// Capture loop flag MUST be volatile
private volatile boolean running = true;

// FPS counter MUST be synchronized
private synchronized void recordFPSMeasurement(long frameTimestamp) { ... }
```

### 5.4 Performance Constraints

- **Capture latency:** < 5ms per frame
- **Memory per frame:** ~3.7 MB (640×480 RGB)
- **Total memory:** ~7.4 MB (2 Mat: current + previous)
- **No thread pooling:** Single capture thread, no workers needed

### 5.5 Exception Hierarchy

```
Throwable
  └─ Exception
      ├─ RuntimeException
      │   ├─ CameraUnavailableException    (checked cast to get device error)
      │   ├─ InvalidResolutionException
      │   └─ FrameCapturedException       (frame read error)
      └─ IOException (if properties loading fails)
```

---

## 6. Dependencies & Assumptions

- [ ] OpenCV (JavaCV 1.5.13) must be installed
- [ ] Camera driver must be available and accessible
- [ ] Java 21+ with FX or Swing UI framework
- [ ] No concurrent use of camera from other processes

---

## 7. Out of Scope

- GPU-accelerated capture (future optimization)
- Multiple camera support (single camera only)
- Configurable resolution (hardcoded 640×480)
- USB camera persistence (device ID is fixed at 0)

---

## 8. Approval

| Role | Name | Date | ✓ |
|------|------|------|---|
| Tech Lead | TBD | 2026-04-20 | ☐ |
| Product Owner | TBD | 2026-04-20 | ☐ |

---

> **Ready for Implementation:** Once approved, feed this spec + CONSTITUTION_MouseVision.md to AI for code generation.
