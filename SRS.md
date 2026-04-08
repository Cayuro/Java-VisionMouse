# 📄 Software Requirements Specification (SRS)
**Project:** VisionMouse  
**Version:** 1.0.0  
**Status:** Base Document  
**Standard:** ISO 29148 / IEEE 830  

---

## 1. Introduction

### 1.1 Purpose
The purpose of this document is to define the technical and functional specifications for the **VisionMouse** system. This SRS serves as a technical contract between the development team and stakeholders to ensure quality, consistency, and traceability of the final product.

### 1.2 Scope
VisionMouse is a desktop application that uses computer vision to control the mouse cursor through hand movements captured by a webcam. The system detects hand landmarks, maps them to screen coordinates, and enables gesture-based interactions such as cursor movement and clicking.

---

## 2. Overall Description

### 2.1 Product Perspective
VisionMouse is a standalone desktop application built in Java. It uses OpenCV for video capture and image processing, and MediaPipe (or fallback methods) for hand detection. The system interacts with the operating system via `java.awt.Robot` to control the mouse.

### 2.2 Product Functions
- Real-time webcam video capture  
- Hand detection and landmark extraction  
- Mapping of camera coordinates to screen coordinates  
- Mouse cursor control using hand movement  
- Gesture recognition for click actions  
- Visual feedback via GUI (camera + overlays)  
- Low-latency processing for real-time interaction  

---

## 3. Specific Requirements

### 3.1 Functional Requirements (FR)

| ID | Name | Description | Priority |
|:---|:---|:---|:---:|
| **FR-01** | Video Capture | The system must access the webcam and capture video in real time. | High |
| **FR-02** | Hand Detection | The system must detect a hand and extract the index fingertip landmark (ID=8) using MediaPipe or fallback OpenCV methods. | High |
| **FR-03** | Coordinate Mapping | The system must map camera coordinates (e.g., 640x480) to screen resolution (e.g., 1920x1080) with horizontal mirroring. | High |
| **FR-04** | Cursor Control | The system must move the system cursor using `java.awt.Robot` based on mapped coordinates. | High |
| **FR-05** | Click Detection | The system must perform a left click using gesture detection (pinch) or dwell time. | High |
| **FR-06** | Visual Feedback | The system must display the webcam feed with overlays showing detection and tracking points. | Medium |
| **FR-07** | Movement Smoothing | The system must apply an exponential moving average (EMA) filter to smooth cursor movement. | High |
| **FR-08** | Configuration Settings | The system should allow adjusting parameters such as sensitivity, smoothing, and dwell time. | Medium |

---

### 3.2 Non-Functional Requirements (NFR)

| ID | Attribute | Description |
|:---|:---|:---|
| **NFR-01** | Latency | The system must process frames with latency < 50ms. |
| **NFR-02** | Accuracy | The system should minimize jitter and improve tracking precision. |
| **NFR-03** | Portability | The system should run on Windows and be adaptable to other OS environments. |
| **NFR-04** | Security | Camera access and system control must respect OS-level permissions. |
| **NFR-05** | Usability | The UI must be simple, intuitive, and provide clear visual feedback. |
| **NFR-06** | Maintainability | The code must be modular, documented, and easy to extend. |

---

## 4. Logical Modeling (Mermaid Diagrams)

### 4.1 Detection and Control Flow

```mermaid
graph TD
    Start([Start]) --> Capture[Capture Video]
    Capture --> Detect{Hand Detected?}
    Detect -- No --> Display[Show Raw Video]
    Detect -- Yes --> Extract[Extract Index Finger Landmark]
    Extract --> Map[Map Coordinates to Screen]
    Map --> Smooth[Apply Smoothing]
    Smooth --> Move[Move Cursor]
    Move --> ClickCheck{Click Gesture?}
    ClickCheck -- Yes --> Click[Perform Left Click]
    ClickCheck -- No --> Loop[Continue Loop]
    Loop --> Capture