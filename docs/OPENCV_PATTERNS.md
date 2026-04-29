# 🎨 OPENCV PATTERNS — JavaCV/OpenCV Best Practices

> **Document Version:** 1.0
> **Date:** 2026-04-20
> **Audience:** Developers working with OpenCV Mat, VideoCapture, DNN

---

## 1. Overview

OpenCV in Java (via JavaCV wrapper) uses **native memory** for image data. Unlike regular Java objects that are garbage-collected, OpenCV objects are **allocated on the C++ heap** and must be **explicitly freed**.

### The Critical Rule

> **Every `Mat` you create MUST be released with `.release()` before it goes out of scope.**
> **Failure = memory leak → JVM crash.**

---

## 2. Mat Lifecycle

### Creation & Cleanup

```
┌─────────────────────────────────────┐
│  JVM Heap                           │
│                                     │
│  ┌─────────────────────────────────┐│
│  │ Mat object (Java wrapper)      ││
│  │ • field: memory pointer to C++ ││
│  │ • field: width, height, type   ││
│  └────────┬────────────────────────┘│
│           │ references             │
└───────────┼────────────────────────┘
            │
            ↓
┌─────────────────────────────────────┐
│  C++ Heap (Native Memory)           │
│                                     │
│  [Image Data Buffer]  ← 3.7 MB      │
│  (allocated by cv::Mat constructor) │
│                                     │
└─────────────────────────────────────┘

When mat.release() is called:
  • C++ heap memory is freed immediately
  • Java wrapper becomes invalid
  • Accessing it after release() → crash or corruption
```

### Patterns

#### Pattern 1: Single Mat (Simple Case)

```java
// ✅ CORRECT
Mat frame = new Mat();
try {
    capture.read(frame);  // Populate with data
    // Process frame
    detectHand(frame);
} finally {
    frame.release();  // Release when done
}
```

#### Pattern 2: Multiple Mats (Need Cleanup)

```java
// ✅ CORRECT
Mat frame = new Mat();
Mat resized = new Mat();
Mat gray = new Mat();

try {
    capture.read(frame);
    Imgproc.resize(frame, resized, new Size(640, 480));
    Imgcodecs.cvtColor(frame, gray, Imgcodecs.COLOR_BGR2GRAY);
    // Use resized, gray
} finally {
    frame.release();
    resized.release();
    gray.release();  // Release ALL Mats
}
```

#### Pattern 3: Temporary Mat (In Method)

```java
// ✅ CORRECT — Temporary mat for preprocessing
public Mat preprocessFrame(Mat input) {
    Mat temp = new Mat();
    try {
        Imgproc.GaussianBlur(input, temp, new Size(5, 5), 0);
        return temp.clone();  // ← IMPORTANT: Return a copy
    } finally {
        temp.release();  // Release temporary
    }
}

// Caller:
Mat original = capture.read();
Mat preprocessed = preprocessFrame(original);
// Use preprocessed
preprocessed.release();
original.release();
```

#### Pattern 4: Submatrix (Shared Memory)

```java
// ⚠️ IMPORTANT: Submatrix shares memory with parent
Mat fullImage = new Mat(480, 640, CvType.CV_8UC3);

// Create a ROI (Region of Interest)
Mat roi = fullImage.submat(100, 200, 50, 150);  // y_start:y_end, x_start:x_end

// DO NOT release roi independently
// Releasing roi might corrupt fullImage

// Correct cleanup:
roi = null;  // Clear reference
fullImage.release();  // Release parent
```

---

## 3. VideoCapture Lifecycle

### Proper Usage

```java
// ✅ CORRECT
VideoCapture capture = new VideoCapture(0);  // Open camera

if (!capture.isOpened()) {
    throw new CameraUnavailableException("Cannot open camera");
}

try {
    Mat frame = new Mat();
    while (stillCapturing) {
        if (capture.read(frame)) {
            // Process frame
            processFrame(frame);
        }
        // release() is NOT called here (frame is reused)
    }
    
    // After loop, release:
    frame.release();
} finally {
    capture.release();  // Close camera
}
```

### Properties & Configuration

```java
VideoCapture capture = new VideoCapture(0);

// Set resolution (BEFORE first read)
capture.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
capture.set(Videoio.CAP_PROP_FPS, 30);
capture.set(Videoio.CAP_PROP_BUFFERSIZE, 1);  // Single frame buffer

// Read frame
Mat frame = new Mat();
if (capture.read(frame)) {
    System.out.println("Frame: " + frame.width() + "x" + frame.height());
}

// Cleanup
frame.release();
capture.release();
```

---

## 4. DNN (ONNX Model) Patterns

### Singleton Model Cache (MANDATORY)

> **Rule:** Load ONNX models **ONCE at startup**, **cache globally**.

```java
// ✅ CORRECT — Singleton cache
public class OnnxModelCache {
    private static volatile Net palmDetector = null;
    private static volatile Net handLandmarks = null;
    
    public static synchronized Net getPalmDetector() {
        if (palmDetector == null) {
            logger.info("Loading palm_detection.onnx...");
            palmDetector = Dnn.readNetFromONNX("models/palm_detection.onnx");
            logger.info("Model loaded");
        }
        return palmDetector;
    }
    
    public static synchronized Net getHandLandmarks() {
        if (handLandmarks == null) {
            logger.info("Loading hand_landmarks.onnx...");
            handLandmarks = Dnn.readNetFromONNX("models/hand_landmarks.onnx");
            logger.info("Model loaded");
        }
        return handLandmarks;
    }
    
    public static void cleanup() {
        if (palmDetector != null) {
            palmDetector.release();
            palmDetector = null;
        }
        if (handLandmarks != null) {
            handLandmarks.release();
            handLandmarks = null;
        }
    }
}

// Usage:
Net model = OnnxModelCache.getPalmDetector();  // Loaded once
// Reuse model for 10000 frames → no reload
```

### ONNX Inference Patterns

```java
// ✅ CORRECT — Inference with resource cleanup
public List<Mat> detect(Mat inputFrame) {
    Net net = OnnxModelCache.getPalmDetector();
    
    // Preprocess: resize to model input size (192×192)
    Mat blob = Dnn.blobFromImage(
        inputFrame,
        1.0 / 127.5,        // Scale
        new Size(192, 192),  // Size
        new Scalar(127.5, 127.5, 127.5),  // Mean
        false,               // Swap RB
        false                // Crop
    );
    
    try {
        net.setInput(blob);
        List<Mat> outputs = new ArrayList<>();
        net.forward(outputs);  // Run inference
        
        // Process outputs...
        return outputs;
        
    } finally {
        blob.release();  // Release blob after inference
        // outputs Mat list will be cleaned by caller
    }
}

// Caller:
List<Mat> detections = detect(frame);
for (Mat out : detections) {
    // Process out
    out.release();  // Release each output
}
```

### Post-Processing: NMS (Non-Maximum Suppression)

```java
// ✅ CORRECT — NMS with Rect cleanup
public static void nms(List<Rect> boxes, List<Float> confidences, 
                       float threshold) {
    // OpenCV's groupRectangles does NMS
    MatOfRect matRects = new MatOfRect(boxes.toArray(new Rect[0]));
    MatOfInt groupings = new MatOfInt();
    
    try {
        Imgproc.groupRectangles(matRects, groupings, 1, threshold);
        // Update boxes with grouped results
        boxes.clear();
        boxes.addAll(Arrays.asList(matRects.toArray()));
    } finally {
        matRects.release();
        groupings.release();
    }
}
```

---

## 5. Mat Operations (BGR, RGB, Color Conversion)

### BGR vs RGB (OpenCV Quirk)

```
OpenCV uses BGR (Blue-Green-Red) not RGB!
Reason: Legacy from OpenCV 1.0 (built on Windows with BGR drivers)

Image Data Layout:
  Pixel (0,0): [B, G, R]  ← Not [R, G, B]!
```

### Color Space Conversion

```java
// ✅ CORRECT — BGR to grayscale
Mat colorFrame = capture.read(new Mat());  // BGR
Mat grayFrame = new Mat();

try {
    // BGR to GRAY
    Imgcodecs.cvtColor(colorFrame, grayFrame, Imgcodecs.COLOR_BGR2GRAY);
    // Now grayFrame is single-channel (intensity)
} finally {
    colorFrame.release();
    grayFrame.release();
}

// ✅ CORRECT — BGR to RGB (for display)
Mat rgbFrame = new Mat();
try {
    Imgcodecs.cvtColor(bgrFrame, rgbFrame, Imgcodecs.COLOR_BGR2RGB);
    // Now rgbFrame can be displayed without color swap
} finally {
    rgbFrame.release();
}
```

---

## 6. Mat ↔ BufferedImage Conversion

### Conversion Pattern

```java
// ✅ CORRECT — Mat (BGR) to BufferedImage (RGB)
public static BufferedImage matToBufferedImage(Mat mat) {
    if (mat.empty()) {
        return null;
    }
    
    int width = mat.width();
    int height = mat.height();
    int channels = mat.channels();
    
    byte[] sourcePixels = new byte[width * height * channels];
    mat.get(0, 0, sourcePixels);  // Extract pixel data
    
    BufferedImage image = new BufferedImage(
        width, height, BufferedImage.TYPE_INT_RGB
    );
    
    int[] rgbPixels = new int[width * height];
    
    // Convert BGR bytes to RGB int
    for (int i = 0; i < sourcePixels.length; i += 3) {
        int b = sourcePixels[i] & 0xFF;
        int g = sourcePixels[i + 1] & 0xFF;
        int r = sourcePixels[i + 2] & 0xFF;
        
        rgbPixels[i / 3] = (r << 16) | (g << 8) | b;
    }
    
    image.setRGB(0, 0, width, height, rgbPixels, 0, width);
    return image;
    
    // Note: DO NOT release mat in this function
    // Caller is responsible for releasing
}

// Usage:
Mat frame = capture.read(new Mat());
try {
    BufferedImage img = matToBufferedImage(frame);
    displayWindow.setIcon(img);
} finally {
    frame.release();  // Caller releases, not conversion function
}
```

---

## 7. Common OpenCV Operations

### Resize

```java
// ✅ CORRECT
Mat source = capture.read(new Mat());
Mat resized = new Mat();

try {
    Imgproc.resize(source, resized, new Size(640, 480));
    // Use resized
} finally {
    source.release();
    resized.release();
}
```

### GaussianBlur (Smoothing)

```java
// ✅ CORRECT
Mat source = new Mat();
Mat blurred = new Mat();

try {
    Imgproc.GaussianBlur(source, blurred, new Size(5, 5), 1.0);
} finally {
    source.release();
    blurred.release();
}
```

### Threshold

```java
// ✅ CORRECT
Mat source = new Mat();
Mat binary = new Mat();

try {
    Imgcodecs.threshold(source, binary, 127, 255, Imgcodecs.THRESH_BINARY);
} finally {
    source.release();
    binary.release();
}
```

### Drawing Landmarks (Circles)

```java
// ✅ CORRECT — Draw on frame
Mat frame = capture.read(new Mat());

try {
    // Draw circle at landmark position
    Point center = new Point(320, 240);
    Imgproc.circle(frame, center, 5, new Scalar(0, 255, 0), -1);  // Green filled circle
    
    // Display frame
    BufferedImage img = matToBufferedImage(frame);
    displayWindow.show(img);
} finally {
    frame.release();
}
```

---

## 8. Debugging OpenCV Issues

### Problem: "Native Stack Overflow"

**Symptom:** 
```
Exception: java.lang.StackOverflowError
at ...openblas...
```

**Cause:** OpenCV Mat not released → heap corruption → stack overflow

**Solution:**
```java
// Add logging to track Mat allocations
public static class MatTracker {
    private static AtomicInteger created = new AtomicInteger(0);
    private static AtomicInteger released = new AtomicInteger(0);
    
    public static Mat createMat() {
        created.incrementAndGet();
        return new Mat();
    }
    
    public static void releaseMat(Mat m) {
        if (m != null) {
            m.release();
            released.incrementAndGet();
        }
    }
    
    public static void printStats() {
        System.out.println("Mats created: " + created.get() + 
                           ", released: " + released.get() + 
                           ", LEAK: " + (created.get() - released.get()));
    }
}

// Use:
Mat frame = MatTracker.createMat();
try { ... } finally { MatTracker.releaseMat(frame); }
MatTracker.printStats();  // Should show LEAK = 0
```

### Problem: "UnsatisfiedLinkError: opencv_java"

**Symptom:**
```
Exception: java.lang.UnsatisfiedLinkError: no opencv_java4100 in java.library.path
```

**Cause:** OpenCV native library not found

**Solution:**
```
1. Ensure javacv-platform dependency in pom.xml
2. Dependency should include classifier for your OS (windows, macosx, linux)
3. Run: mvn clean compile (forces re-download)
4. Check: ~/.m2/repository/org/bytedeco/
```

### Problem: "Memory Leak" (Heap Growing)

**Symptom:** Heap size increases from 100MB → 500MB over 10 minutes

**Cause:** Mat objects not released

**Solution:**
```java
// Add memory monitoring
Runtime runtime = Runtime.getRuntime();
System.out.println("Memory used: " + 
    (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");

// In capture loop, add every 100 frames:
if (frameCount % 100 == 0) {
    System.out.println("Frame " + frameCount + 
        " - Memory: " + (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 + " MB");
}

// If memory keeps growing:
// 1. Check all Mat objects are released
// 2. Use try-finally
// 3. Profile with profiler tool
```

---

## 9. Performance Optimization

### Avoid Unnecessary Mat Copies

```java
// ❌ WRONG — Creates 3 Mat copies
Mat frame = capture.read(new Mat());
Mat temp1 = frame.clone();
Mat temp2 = temp1.clone();
Mat temp3 = temp2.clone();
detectHand(temp3);  // Only temp3 is used

// ✅ CORRECT — Single Mat
Mat frame = capture.read(new Mat());
detectHand(frame);  // Reuse single Mat
```

### Batch Operations (Reduce Allocations)

```java
// ❌ WRONG — Allocates new Mat per frame
for (int i = 0; i < 1000; i++) {
    Mat frame = capture.read(new Mat());  // ← NEW Mat each iteration
    Mat resized = new Mat();              // ← NEW Mat each iteration
    Imgproc.resize(frame, resized, ...);
    // ... release both
}

// ✅ CORRECT — Reuse Mats
Mat frame = new Mat();
Mat resized = new Mat();

try {
    for (int i = 0; i < 1000; i++) {
        capture.read(frame);  // ← Reuse frame
        Imgproc.resize(frame, resized, ...);
        // resized is reused
    }
} finally {
    frame.release();
    resized.release();
}
```

### Resolution Adjustment

```
If ONNX inference is too slow (> 30ms):

OPTION 1: Reduce resolution
  640×480 → 320×240  (4x fewer pixels)
  Inference time: 30ms → 7-10ms ✓

OPTION 2: Skip frames
  Process every frame → Process every 2nd frame
  Effective FPS: 30 → 15, but inference faster

OPTION 3: Combine
  320×240 resolution + process every 2nd frame
  → Very fast inference, acceptable UX
```

---

## 10. Checklist for OpenCV Code

- [ ] Every `new Mat()` has corresponding `.release()`?
- [ ] VideoCapture opened with `.isOpened()` check?
- [ ] Try-finally used for guarantee cleanup?
- [ ] No Mat operations on EDT?
- [ ] ONNX models loaded once (singleton cache)?
- [ ] BGR vs RGB conversion correct?
- [ ] Submatrix references don't outlive parent?
- [ ] Memory monitoring shows stable heap?

---

> **Next Step:** Read `CONSTITUTION_MouseVision.md` for full architectural rules.
