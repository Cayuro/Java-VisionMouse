# 📖 DOCUMENTATION INDEX — MouseVision Complete Reference

> **Updated:** 2026-04-20  
> **Status:** Complete for MVP Specification Phase  
> **Audience:** Developers, Architects, Team Leads  

---

## 🎯 Where to Start?

### ✅ 1. Understanding the System (15 minutes)

Start here if you're new to the project:

1. **[README_DOCUMENTATION.md](README_DOCUMENTATION.md)** (5 min)
   - Quick start guide
   - Project overview
   - Documentation map

2. **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** (10 min)
   - System layers
   - Data flow
   - Module breakdown

---

## 📋 By Role

### Project Manager / Product Owner

**What you need:**
- Overview of scope, timeline, dependencies
- Sprint planning, team assignments
- Risk tracking

**Read:**
1. [ProductBacklog_MouseVision.md](docs/ProductBacklog_MouseVision.md) — 17 user stories, 4 epics, 57 SP
2. [SprintPlanning_MouseVision.md](docs/SprintPlanning_MouseVision.md) — Detailed sprint breakdown, task estimates
3. [README_DOCUMENTATION.md](README_DOCUMENTATION.md#team--roles) — Team roles, dependencies

---

### System Architect

**What you need:**
- End-to-end system design
- Thread architecture, latency budget
- Resource management strategy

**Read in order:**
1. [ARCHITECTURE.md](docs/ARCHITECTURE.md) — System overview
2. [CONSTITUTION_MouseVision.md](docs/CONSTITUTION_MouseVision.md) — Architectural rules
3. [THREADING_GUIDE.md](docs/THREADING_GUIDE.md) — Thread model deep-dive

---

### Developer (Implementing a Feature)

**What you need:**
- Feature specification (business rules, acceptance criteria)
- Implementation patterns
- Debugging tips

**Read in order:**
1. Choose your module spec:
   - [SPEC_VideoCapture.md](docs/SPEC_VideoCapture.md) (EP-01)
   - [SPEC_HandDetection.md](docs/SPEC_HandDetection.md) (EP-02)
   - [SPEC_MouseControl.md](docs/SPEC_MouseControl.md) (EP-03)
   - [SPEC_GestureRecognition.md](docs/SPEC_GestureRecognition.md) (EP-04)

2. Pattern guides:
   - [THREADING_GUIDE.md](docs/THREADING_GUIDE.md) (if using threads)
   - [OPENCV_PATTERNS.md](docs/OPENCV_PATTERNS.md) (if using OpenCV/Mat)

3. Rules:
   - [CONSTITUTION_MouseVision.md](docs/CONSTITUTION_MouseVision.md) (non-negotiable)

---

### DevOps / Release Manager

**What you need:**
- Build configuration
- Deployment requirements
- System requirements

**Read:**
1. [README_DOCUMENTATION.md](README_DOCUMENTATION.md#deployment--system-requirements) — System requirements
2. [EJECUTAR.md](docs/EJECUTAR.md) — Running instructions

---

## 📚 Complete Documentation Tree

```
docs/
├── 🏗️  ARCHITECTURE.md
│       System design, layers, threading model
│       • Read: Architecture deep-dive
│       • Length: 12 KB
│       • Time: 15 min
│
├── 📜 CONSTITUTION_MouseVision.md
│       Rules, patterns, constraints (non-negotiable)
│       • Read: Before coding
│       • Length: 18 KB
│       • Time: 20 min
│
├── 🎯 SPEC_VideoCapture.md (EP-01)
│       Camera capture, frame buffering
│       • Read: Before implementing video module
│       • Scenarios: 7
│       • Rules: 7 BR
│
├── 🎯 SPEC_HandDetection.md (EP-02)
│       ONNX inference, landmarks extraction
│       • Read: Before implementing detection module
│       • Scenarios: 8
│       • Rules: 8 BR
│
├── 🎯 SPEC_MouseControl.md (EP-03)
│       Coordinate mapping, mirroring, smoothing
│       • Read: Before implementing control module
│       • Scenarios: 8
│       • Rules: 8 BR
│
├── 🎯 SPEC_GestureRecognition.md (EP-04)
│       Pinch detection, click execution
│       • Read: Before implementing gesture module
│       • Scenarios: 8
│       • Rules: 8 BR
│
├── 🧵 THREADING_GUIDE.md
│       Real-time threading patterns, EDT handling
│       • Read: When working with threads
│       • Length: 15 KB
│       • Patterns: 8
│
├── 🎨 OPENCV_PATTERNS.md
│       Mat lifecycle, resource management, color conversion
│       • Read: When working with OpenCV/ONNX
│       • Length: 20 KB
│       • Patterns: 12
│
├── 📊 ProductBacklog_MouseVision.md
│       17 user stories, 4 epics, 57 SP MVP
│       • Read: For sprint planning
│
├── 📅 SprintPlanning_MouseVision.md
│       Sprint 1 & 2 task breakdown, estimates
│       • Read: Before sprint starts
│
├── 📋 SRS_MouseVision.md
│       Software Requirements Specification
│       • Read: Functional & non-functional requirements
│
├── 👥 User-Story.md
│       High-level user stories (English version)
│       • Read: For stakeholder communication
│
└── ⚙️  EJECUTAR.md
        How to run the application
        • Read: First time running project
```

---

## 🔍 By Topic

### Threading

- [THREADING_GUIDE.md](docs/THREADING_GUIDE.md) — Full threading tutorial
- [ARCHITECTURE.md → Section 5](docs/ARCHITECTURE.md#5-threading-model) — Architecture's threading section
- [CONSTITUTION_MouseVision.md → Section 2.1](docs/CONSTITUTION_MouseVision.md#21-threading-model) — Threading rules

### OpenCV & ONNX

- [OPENCV_PATTERNS.md](docs/OPENCV_PATTERNS.md) — Complete OpenCV guide
- [SPEC_HandDetection.md → Section 5.3](docs/SPEC_HandDetection.md#53-pre-post-processing-pipeline) — Inference pipeline
- [CONSTITUTION_MouseVision.md → Section 2.2](docs/CONSTITUTION_MouseVision.md#22-opencv-resource-management-critical) — Mat cleanup rules

### Real-Time Performance

- [CONSTITUTION_MouseVision.md → Section 2.3](docs/CONSTITUTION_MouseVision.md#23-real-time-performance-constraints) — Performance constraints
- [ARCHITECTURE.md → Section 8](docs/ARCHITECTURE.md#8-performance-characteristics) — Performance characteristics
- [SPEC_VideoCapture.md → Section 5.4](docs/SPEC_VideoCapture.md#54-performance-constraints) — Latency budget

### Error Handling

- [CONSTITUTION_MouseVision.md → Section 2.6](docs/CONSTITUTION_MouseVision.md#26-error-handling-strategy) — Error strategy
- [ARCHITECTURE.md → Section 7](docs/ARCHITECTURE.md#7-error-handling-strategy) — Failure modes & responses

### Configuration

- [CONSTITUTION_MouseVision.md → Section 5](docs/CONSTITUTION_MouseVision.md#5-configuration-externalisation) — Config externalization
- [ARCHITECTURE.md → Section 9](docs/ARCHITECTURE.md#9-configuration-points) — Configuration parameters

---

## 🔗 Cross-References

### From SPEC to Implementation

Each spec maps to code generation:

| SPEC | Artifacts | Location |
|------|-----------|----------|
| SPEC_VideoCapture.md | CameraCapture, FrameConverter | src/main/java/com/vision/video/ |
| SPEC_HandDetection.md | HandDetector, HandLandmarks, OnnxModelLoader | src/main/java/com/vision/detection/ |
| SPEC_MouseControl.md | MouseController, EMAFilter | src/main/java/com/vision/control/gesture/ |
| SPEC_GestureRecognition.md | GestureRecognizer, state machine | src/main/java/com/vision/gesture/ |

### From CONSTITUTION to Rules

Each rule maps to code patterns:

| Rule | Guide | Implementation |
|------|-------|-----------------|
| BR: Threading | THREADING_GUIDE.md | volatile, AtomicReference |
| BR: Mat cleanup | OPENCV_PATTERNS.md | try-finally, .release() |
| BR: ONNX caching | OPENCV_PATTERNS.md | Singleton pattern |
| BR: EDT safety | THREADING_GUIDE.md | SwingUtilities.invokeLater() |

---

## 📊 Documentation Statistics

```
Total Documentation: 8 + guides
├── Constitution: 1 file (18 KB, 8 sections)
├── Specifications: 4 files (32 scenarios, 32 BR total)
├── Guides: 3 files (45 KB, 25 patterns)
├── Project Docs: 5 files (backlog, planning, SRS)
└── Support: 2 files (this index, README)

Total Size: ~150 KB (highly navigable)
Total Time to Read: 2-3 hours (comprehensive)
```

---

## ⚡ Quick Access Links

### Most Frequently Used

1. **For API Reference:** [ARCHITECTURE.md Section 3](docs/ARCHITECTURE.md#3-module-organization)
2. **For Rules:** [CONSTITUTION_MouseVision.md Section 2](docs/CONSTITUTION_MouseVision.md#2-non-negotiable-rules)
3. **For Debugging:** [THREADING_GUIDE.md Section 8](docs/THREADING_GUIDE.md#8-debugging-threading-issues) + [OPENCV_PATTERNS.md Section 8](docs/OPENCV_PATTERNS.md#8-debugging-opencv-issues)
4. **For Specs:** [SPEC_TEMPLATE.md](sdd-assets/SPEC_TEMPLATE.md) (to create new specs)
5. **For Configuration:** [CONSTITUTION_MouseVision.md Section 5](docs/CONSTITUTION_MouseVision.md#5-configuration-externalisation)

---

## 🔄 Document Update Process

When you need to update documentation:

1. **Find the relevant document** (use this index)
2. **Check what section** needs updating
3. **Update the source document** (in docs/)
4. **Update cross-references** in related documents
5. **Update this index** if new documents added
6. **Commit with message:** `docs: [description]`

---

## 🚀 Next Steps

Choose based on your role:

- **👨‍💻 Developer:** Jump to your module's SPEC
- **🏗️ Architect:** Read ARCHITECTURE.md + CONSTITUTION
- **📊 Manager:** Read ProductBacklog + SprintPlanning
- **🔧 DevOps:** Read EJECUTAR.md + System Requirements

---

## 📧 Questions?

- **Architecture questions:** Open issue with `[arch]` label
- **Specification questions:** Comment in relevant SPEC file
- **Implementation questions:** Check THREADING_GUIDE or OPENCV_PATTERNS first
- **Build/deployment questions:** Check README_DOCUMENTATION or EJECUTAR

---

**Last Updated:** 2026-04-20  
**Status:** Ready for Sprint 1  
**Next Review:** At end of Sprint 1 (before Sprint 2)
