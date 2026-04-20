# 🖱️ MouseVision — Hand-Controlled Mouse System

> **A real-time desktop application for controlling your mouse with hand gestures using vision AI.**

---

## 📋 Table of Contents

1. [Quick Start](#quick-start)
2. [Project Overview](#project-overview)
3. [Documentation Map](#documentation-map)
4. [Architecture](#architecture)
5. [Technology Stack](#technology-stack)
6. [Development Guide](#development-guide)
7. [Team & Roles](#team--roles)

---

## Quick Start

### Prerequisites

- **Java 21+** — Download from [oracle.com](https://www.oracle.com/java/technologies/downloads/)
- **Maven 3.8+** — Download from [maven.apache.org](https://maven.apache.org/)
- **Webcam** — USB or built-in camera
- **Git** — For version control

### Installation & Run

```bash
# 1. Clone the repository
git clone <repo-url>
cd Java-VisionMouse

# 2. Download dependencies (first time)
mvn clean compile

# 3. Run the application
mvn exec:java -Dexec.mainClass="com.vision.Main"

# 4. A window appears → Hand detection + mouse control active
# 5. Close window to exit cleanly
```

### First Test

1. Launch the app
2. Show your hand to the camera
3. Move your hand → cursor follows your index finger
4. Pinch thumb + index → left-click executed
5. Move hand away → cursor freezes (no detection)

---

## Project Overview

### Vision

MouseVision enables **gesture-based mouse control** without any physical device. Using only a webcam, hand landmarks are detected via AI (MediaPipe ONNX), mapped to screen coordinates, and the cursor responds in real-time.

### MVP Scope (Sprint 1 + 2 — 57 Story Points)

| Epic | Feature | Status |
|------|---------|--------|
| **EP-01: Video** | Capture camera feed, display in window | ✅ Planned |
| **EP-02: Detection** | Detect hand & extract 21 landmarks | ✅ Planned |
| **EP-03: Control** | Map hand position to mouse cursor | ✅ Planned |
| **EP-04: Gestures** | Pinch gesture → left click | ✅ Planned |

### Key Metrics

```
⏱️  Latency:          < 100ms (hand → cursor move)
🎥 Frame Rate:       25-30 FPS (stable)
📊 Accuracy:         > 95% hand detection confidence
💾 Memory:           ~100 MB (heap + native resources)
🧵 Threads:          3 (capture, control, UI)
```

---

## 📚 Documentation Map

### For Understanding the System

1. **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** ← START HERE
   - System design, layers, module overview
   - Data flow, threading model
   - Performance characteristics

2. **[CONSTITUTION_MouseVision.md](docs/CONSTITUTION_MouseVision.md)**
   - Architectural rules & patterns
   - Non-negotiable constraints
   - Threading, resource management, error handling

### For Detailed Module Specs

Each module has its own specification:

- **[SPEC_VideoCapture.md](docs/SPEC_VideoCapture.md)** (EP-01)
  - Camera capture, frame buffering
  - Business rules, acceptance criteria
  - 6 scenarios, implementation guide

- **[SPEC_HandDetection.md](docs/SPEC_HandDetection.md)** (EP-02)
  - ONNX inference, landmark extraction
  - Pre/post-processing pipeline
  - 8 scenarios, model caching

- **[SPEC_MouseControl.md](docs/SPEC_MouseControl.md)** (EP-03)
  - Coordinate mapping, mirroring, EMA filtering
  - Mouse movement, frequency limiting
  - 8 scenarios, math validation

- **[SPEC_GestureRecognition.md](docs/SPEC_GestureRecognition.md)** (EP-04)
  - Pinch detection, state machine, debounce
  - Click execution, timing
  - 8 scenarios, thread safety

### For Implementation Patterns

3. **[THREADING_GUIDE.md](docs/THREADING_GUIDE.md)** — Must Read
   - Real-time threading patterns
   - Capture thread, EDT handling
   - Common pitfalls & debugging

4. **[OPENCV_PATTERNS.md](docs/OPENCV_PATTERNS.md)** — Critical
   - Mat lifecycle, resource management
   - ONNX model caching
   - BGR/RGB conversion, performance tips

### For Project Management

- **[ProductBacklog_MouseVision.md](docs/ProductBacklog_MouseVision.md)**
  - All 17 user stories, story points, dependencies

- **[SprintPlanning_MouseVision.md](docs/SprintPlanning_MouseVision.md)**
  - Sprint 1 & 2 breakdown, task estimates, risks

- **[SRS_MouseVision.md](docs/SRS_MouseVision.md)**
  - Software requirements, functional & non-functional

---

## Architecture

### High-Level Pipeline

```
┌──────────────────────────────────────────────────────┐
│  Real-Time Vision Pipeline                           │
│                                                      │
│  [Capture] → [Detect] → [Map] → [Smooth] → [Move]   │
│    5ms       30ms       5ms      5ms        5ms      │
│                                                      │
│  Total Latency: ~80-100ms (< target: 100ms) ✓       │
└──────────────────────────────────────────────────────┘
```

### Modules

| Module | Responsibility | Thread |
|--------|-----------------|--------|
| **CameraCapture** | Read frames, run ONNX inference | Capture thread |
| **HandDetector** | Extract 21 landmarks | Capture thread |
| **MouseController** | Map coords, smooth, move cursor | Control thread |
| **GestureRecognizer** | Detect pinch, fire clicks | Processing thread |
| **DisplayWindow** | Render video, show status | EDT (Swing) |

### Package Structure

```
src/main/java/com/vision/
├── video/         ← Camera capture + display (EP-01)
├── detection/     ← Hand detection (EP-02)
├── control/       ← Mouse control (EP-03)
├── gesture/       ← Gesture recognition (EP-04)
├── Main.java
└── HandTrackingApp.java
```

---

## Technology Stack

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Language** | Java | 21 LTS | Type-safe, garbage-collected (except OpenCV) |
| **Build** | Maven | 3.8+ | Dependency management, build automation |
| **Vision** | JavaCV (OpenCV) | 1.5.13 | Image capture, processing |
| **AI** | ONNX (MediaPipe) | Runtime | Hand landmark detection (21 points) |
| **OS Control** | java.awt.Robot | JDK 21 | Mouse movement, click execution |
| **UI** | Swing | JDK 21 | Display window (JFrame + JLabel) |
| **Threading** | java.util.concurrent | JDK 21 | AtomicReference, volatile flags |
| **Testing** | JUnit 5 | 5.9+ | Unit tests for algorithms |

---

## Development Guide

### Setting Up Your IDE

#### VS Code

```bash
# Install extensions
code --install-extension vscjava.extension-pack-for-java
code --install-extension vscjava.debugger-for-java

# Open project
code .

# Build (Ctrl+Shift+B)
# Debug → Click "Debug" on Main.java
```

#### IntelliJ IDEA

```
1. File → Open → Select Java-VisionMouse folder
2. Wait for Maven indexing
3. Run → Run 'Main' (Shift+F10)
```

### Coding Standards

1. **Follow CONSTITUTION_MouseVision.md** — Non-negotiable rules
2. **Release all Mat objects** — Failure = memory leak → crash
3. **Thread-safe state sharing** — Use AtomicReference, volatile
4. **Never block EDT** — Always use SwingUtilities.invokeLater()
5. **Externalize configuration** — Use app.properties for thresholds
6. **Add Javadoc** — Public classes & methods

### Running Tests

```bash
# All tests
mvn test

# Single test class
mvn test -Dtest=EMAFilterTest

# With coverage
mvn test jacoco:report
# Open: target/site/jacoco/index.html
```

### Debugging Tips

```bash
# Monitor memory (heap usage)
jps          # List running Java processes
jmap -heap <pid>  # Heap snapshot
jstack <pid>      # Thread dump

# Monitor OpenCV Mat allocations
# → Use MatTracker utility from OPENCV_PATTERNS.md

# Profile ONNX inference time
# → Add System.nanoTime() before/after net.forward()
```

---

## Feature Implementation Workflow (Spec-Driven Development)

### Step 1: Create Specification

```bash
# Copy template (done — in docs/)
cp SPEC_TEMPLATE.md docs/SPEC_MyFeature.md

# Fill in:
# - Business Goal
# - Hard Business Rules (BR-001, BR-002, ...)
# - Acceptance Criteria (Given-When-Then scenarios)
# - Technical Notes (artifacts, SQL, config)
```

### Step 2: Generate Code (AI-Assisted)

Provide to AI:
- `CONSTITUTION_MouseVision.md` (rules)
- `SPEC_MyFeature.md` (requirement)

AI generates in order:
1. Exception classes
2. Data models
3. Unit tests (RED phase)
4. DAO/Service logic (GREEN phase)
5. Integration layer

### Step 3: Verify & Commit

```bash
# Run tests → should pass (GREEN)
mvn test

# Commit following conventional commits
git commit -m "feat(video): implement CameraCapture thread"
```

---

## Team & Roles

### Sprint 1 (Weeks 1) — Video + Detection

| Epic | Stories | Estimate | Assignee |
|------|---------|----------|----------|
| **EP-01** | Video Capture (US-01.1 to US-01.5) | 18 SP | Juan Esteban + Nicolas + Jainer + Salvador |
| **EP-02** | Hand Detection (US-02.1 to US-02.4) | 15 SP | Nicolas + Salvador |
| **Total** | — | **34 SP** | Team |

**Sprint Goal:** *Camera captures, displays, detects hand, extracts landmarks.*

### Sprint 2 (Week 2) — Control + Gestures

| Epic | Stories | Estimate | Assignee |
|------|---------|----------|----------|
| **EP-03** | Mouse Control (US-03.1 to US-03.4) | 10 SP | Juan Esteban + Nicolas + Salvador |
| **EP-04** | Gestures (US-04.1 to US-04.4) | 13 SP | Jainer + Salvador |
| **Total** | — | **23 SP** | Team |

**Sprint Goal:** *Cursor follows hand, clicks on pinch.*

### Roles (Rotating)

- **Scrum Master / PO:** Rotates weekly (schedule: TBD)
- **Tech Lead:** Ensures CONSTITUTION compliance
- **Daily Standup:** 10 min, each morning

---

## FAQ

**Q: Why Java 21?**
A: Records (data classes), switch expressions, virtual threads (future). Requires LTS for stability.

**Q: Why ONNX not TensorFlow/PyTorch?**
A: ONNX is framework-agnostic, lightweight, no Python runtime needed in Java.

**Q: Can I use Spring Boot?**
A: No. CONSTITUTION forbids frameworks. Direct JDBC + Swing only.

**Q: What if hand detection is slow?**
A: Reduce resolution (640×480 → 320×240) or skip frames (process every 2nd).

**Q: How do I add right-click?**
A: Future feature. See SPEC_GestureRecognition.md "Out of Scope" section.

**Q: Where are the ONNX models?**
A: Download from OpenCV Zoo, place in `src/main/resources/models/`.

**Q: Can I run this headless (no UI)?**
A: Yes — remove DisplayWindow, keep cursor control. See `Main.java`.

---

## Related Reading

- **ARCHITECTURE.md** — System design, data flow, threading
- **CONSTITUTION_MouseVision.md** — Rules, patterns, constraints
- **THREADING_GUIDE.md** — Real-time threading, EDT handling
- **OPENCV_PATTERNS.md** — Mat lifecycle, resource management

---

## License

TBD — Specify license (MIT, Apache 2.0, GPL, etc.)

## Contact

- **Questions:** Open an issue in the repo
- **Bugs:** Report in GitHub Issues
- **Contributions:** Follow CONSTITUTION_MouseVision.md before submitting PR

---

> **Start Here:** Read [ARCHITECTURE.md](docs/ARCHITECTURE.md) for a complete system overview.
> 
> **Implement First Feature:** Choose a module from [ProductBacklog_MouseVision.md](docs/ProductBacklog_MouseVision.md), follow the workflow above.
