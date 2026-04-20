# 📋 SPECIFICATION: Hand Detection Pipeline (MediaPipe ONNX)

> **Spec ID:** `SPEC-HD-001`
> **Feature:** Real-time hand detection and landmark extraction using MediaPipe ONNX models
> **Author:** MouseVision Team
> **Date:** 2026-04-20
> **Status:** ✅ Approved for Sprint 1
> **Related Epic:** EP-02 — Detección (MediaPipe ONNX)

---

## 1. Business Goal

**As a** vision application developer,
**I need** reliable hand detection with 21 landmark points using ONNX models,
**So that** the system can accurately track the index finger and thumb for mouse control and gesture recognition.

---

## 2. Hard Business Rules

| Rule ID | Rule Description | Error Behavior |
|---------|-----------------|----------------|
| `BR-HD-001` | ONNX models must load successfully at app startup | Throw `ModelLoadException("Failed to load palm_detection.onnx")` |
| `BR-HD-002` | Detection pipeline must return 21 landmark points when hand is visible | Return empty list if no hand, no null |
| `BR-HD-003` | Landmark coordinates must be normalized to range [0.0, 1.0] | Clamp to [0.0, 1.0] if out of bounds (shouldn't happen) |
| `BR-HD-004` | Hand detection confidence must exceed threshold (0.5 default) | Set `isHandDetected = false` if confidence < threshold |
| `BR-HD-005` | Only ONE hand per frame shall be detected | Return only the highest-confidence hand if multiple detected |
| `BR-HD-006` | Models must be cached (loaded once, reused for all frames) | Never reload models per frame (prohibitively slow) |
| `BR-HD-007` | Index finger (Point 8) and Thumb (Point 4) must be extractable | Always return Point 8 and Point 4 if hand detected |
| `BR-HD-008` | Landmark confidence scores must be tracked | Return confidence score for each point (used by gesture recognizer) |

---

## 3. Acceptance Criteria (BDD)

### Scenario 1 — Happy Path: Hand Detection with Valid Frame
> **Validates:** `BR-HD-002`, `BR-HD-003`, `BR-HD-007`

```gherkin
Given a camera frame (640×480, BGR) with a clear hand visible
  And both ONNX models are loaded successfully
When  HandDetector.detect(frame) is called
Then  a list of 21 HandLandmarks is returned (not empty, not null)
  And each landmark has normalized coordinates in range [0.0, 1.0]
  And Point 8 (index finger tip) is present and valid
  And Point 4 (thumb tip) is present and valid
  And isHandDetected() returns true
```

**Test Method:** `shouldDetectHandWith21Landmarks()`

---

### Scenario 2 — No Hand in Frame
> **Validates:** `BR-HD-002`, `BR-HD-004`

```gherkin
Given a camera frame with NO hand visible (only background)
When  HandDetector.detect(frame) is called
Then  an empty list is returned (not null)
  And isHandDetected() returns false
  And no exception is thrown
```

**Test Method:** `shouldReturnEmptyListWhenNoHandDetected()`

---

### Scenario 3 — Model Loading Failure
> **Validates:** `BR-HD-001`, `BR-HD-006`

```gherkin
Given ONNX model files are missing or corrupted
When  HandDetector is initialized
Then  a ModelLoadException is thrown
  And the message specifies which model failed ("palm_detection.onnx" or "hand_landmarks.onnx")
  And the application does not crash (exception is caught by main app)
```

**Test Method:** `shouldThrowExceptionWhenModelsNotFound()`

---

### Scenario 4 — Model Caching (Not Reloaded)
> **Validates:** `BR-HD-006`

```gherkin
Given HandDetector initialized with models cached
When  detect() is called 1000 times
Then  models are loaded ONCE at initialization
  And subsequent calls reuse the same cached Net objects
  And performance is consistent (no slowdown from reloading)
```

**Test Method:** `shouldCacheModelsAndNotReloadPerFrame()`

---

### Scenario 5 — Low Confidence Hand Detection
> **Validates:** `BR-HD-004`

```gherkin
Given a frame with a partially visible or blurry hand (confidence = 0.3)
  And confidence threshold is set to 0.5
When  HandDetector.detect(frame) is called
Then  isHandDetected() returns false
  And the landmark list is empty
  And the low-confidence hand is rejected
```

**Test Method:** `shouldRejectHandBelowConfidenceThreshold()`

---

### Scenario 6 — Multiple Hands (Only Highest Confidence)
> **Validates:** `BR-HD-005`

```gherkin
Given a frame with TWO hands visible
  Hand 1: confidence = 0.95
  Hand 2: confidence = 0.85
When  HandDetector.detect(frame) is called
Then  only ONE hand is returned (Hand 1 with confidence 0.95)
  And no multiple-hand list is returned
```

**Test Method:** `shouldReturnOnlyHighestConfidenceHandWhenMultipleDetected()`

---

### Scenario 7 — Coordinate Normalization
> **Validates:** `BR-HD-003`, `BR-HD-008`

```gherkin
Given a detected hand with raw pixel coordinates
  Point 8: (320, 240) in 640×480 frame
When  landmarks are normalized
Then  Point 8 is converted to (0.5, 0.5)
  And all coordinates are in range [0.0, 1.0]
```

**Test Method:** `shouldNormalizeLandmarkCoordinates()`

---

### Scenario 8 — Landmark Confidence Scores
> **Validates:** `BR-HD-008`

```gherkin
Given a detected hand with 21 landmarks
When  each landmark includes a confidence score
Then  scores are accessible via landmark.getConfidence()
  And scores indicate reliability of each point detection
  And scores can be used by gesture recognizer to filter noise
```

**Test Method:** `shouldIncludeLandmarkConfidenceScores()`

---

## 4. UI Requirements

| Element | Binding | Validation/Action |
|---------|---------|-------------------|
| Hand Detection Status | `isHandDetected()` | Display "Hand Detected" or "No Hand" |
| Landmark Overlay | Draw circles at each landmark position (optional for MVP) | Debug visualization |
| Index Finger Highlight | Highlight Point 8 in red | User feedback for tracking |
| Confidence Display | Show confidence score in debug mode | Transparency/opacity of overlay |

---

## 5. Technical Notes

### 5.1 Artifacts to Generate

| Layer | Class Name | Key Responsibility |
|-------|-----------|-------------------|
| Model Data | `HandLandmarks.java` | Record with 21 Point2D + confidence scores |
| Index | `LandmarkIndex.java` | Enum: INDEX_FINGER_TIP=8, THUMB_TIP=4, etc. |
| Detector | `HandDetector.java` | ONNX inference, pre/post-processing |
| Loader | `OnnxModelLoader.java` | Singleton cache for Palm + Hand models |
| Normalizer | `CoordinateNormalizer.java` | Pixel → normalized [0.0, 1.0] conversion |

### 5.2 ONNX Model Details

```
Palm Detection Model:
  Input: 192×192 RGB image
  Output: Palm bounding box + confidence
  File: palm_detection_mediapipe.onnx (~3.5 MB)
  Source: OpenCV Model Zoo

Hand Landmark Model:
  Input: 256×256 RGB image (cropped hand ROI from palm detection)
  Output: 21 landmarks (x, y, z) + handedness
  File: hand_landmark_full_mediapipe.onnx (~50 MB)
  Source: OpenCV Model Zoo
```

### 5.3 Pre/Post-Processing Pipeline

```
Frame (640×480)
  ↓
Palm Detector:
  1. Resize to 192×192
  2. Normalize to [0, 1]
  3. Run ONNX inference
  4. Decode anchors + NMS (Non-Maximum Suppression)
  5. Extract palm bounding box
  ↓
Crop Hand ROI (256×256 around palm)
  ↓
Hand Landmark Detector:
  1. Resize to 256×256
  2. Normalize to [0, 1]
  3. Run ONNX inference
  4. Extract 21 landmarks + confidence
  5. Denormalize back to original 640×480 space
  ↓
Output: HandLandmarks with 21 Point2D
```

### 5.4 Thread Model

```
Capture Thread:
  1. Read frame from VideoCapture
  2. Call HandDetector.detect(frame)     ← Blocking call (ONNX inference)
  3. Update isHandDetected flag
  4. Return landmarks to next stage
  
Implication: ONNX inference blocks capture thread
  → FPS = 1 / (capture_time + inference_time)
  → 30ms inference → max 33 FPS (acceptable, target is 25 FPS)
```

### 5.5 Exception Hierarchy

```
Throwable
  └─ Exception
      ├─ RuntimeException
      │   ├─ ModelLoadException ("palm_detection.onnx not found")
      │   ├─ InvalidFrameException ("Frame is null or empty")
      │   ├─ LandmarkExtractionException ("Failed to extract landmarks")
      │   └─ ConfigurationException ("Invalid model configuration")
```

### 5.6 Configuration Parameters

```properties
# Hand Detection
hand.detection.palm.model=models/palm_detection.onnx
hand.detection.landmark.model=models/hand_landmark_full.onnx
hand.detection.palm.confidence.threshold=0.5
hand.detection.landmark.confidence.threshold=0.3
hand.detection.max.hands.per.frame=1
hand.nms.iou.threshold=0.3

# Coordinate System
hand.landmark.coordinate.system=NORMALIZED_0_TO_1
hand.landmark.output.format=Point2D
```

---

## 6. Dependencies & Assumptions

- [ ] MediaPipe ONNX models downloaded from OpenCV Model Zoo
- [ ] OpenCV 4.5+ with DNN module (included in JavaCV 1.5.13)
- [ ] Models placed in `src/main/resources/models/`
- [ ] Frame input is valid Mat object (640×480 BGR)
- [ ] Sufficient memory (min 100MB for model caching)

---

## 7. Out of Scope

- Multi-hand support (only 1 hand per frame in MVP)
- 3D coordinate extraction (z coordinate ignored in MVP)
- Handedness classification (left vs. right — not used in MVP)
- Face detection or body pose (only hands)
- GPU-accelerated inference (future optimization)

---

## 8. Approval

| Role | Name | Date | ✓ |
|------|------|------|---|
| Tech Lead | TBD | 2026-04-20 | ☐ |
| Product Owner | TBD | 2026-04-20 | ☐ |

---

> **Ready for Implementation:** Once approved, feed this spec + CONSTITUTION_MouseVision.md to AI for code generation.
