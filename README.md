# Project: Virtual Mouse Controlled by Hand Gestures with Java and OpenCV

## Context
We are a team of 4 people who are just starting to learn Java.  
We want to develop a **computer vision project** that allows controlling the computer using **hand gestures**, without a physical mouse.

This project will be our **first functional MVP**, demonstrating the concept, and will be implemented **entirely in Java + OpenCV** (no Python or advanced ML required).

---

## Project Goals

1. **Hand Capture and Detection**
   - Detect the user’s hand from a camera or video.
   - Visually highlight the detected hand with a rectangle.
   - Obtain coordinates (x, y) to control the cursor.

2. **Cursor Movement**
   - Move the mouse cursor based on the hand’s detected position.
   - Correctly scale coordinates from camera resolution to screen resolution.
   - Keep movement smooth without sudden jumps.

3. **Basic Click by Gesture**
   - Recognize a simple gesture (e.g., closed hand) to trigger a left click.
   - Avoid accidental clicks.

4. **MVP Integration**
   - Integrate hand detection, cursor movement, and click into a single functional program.
   - The whole team participates in each user story.
   - Validate functionality under controlled conditions.

---

## User Stories

### Story 1: Hand Capture and Detection
**As a team**, we want the system to detect the user’s hand in the camera or video, **so that** we have a reference point for controlling the cursor.  

**Acceptance Criteria:**
- Camera or video opens correctly.  
- A rectangle highlights the detected hand.  
- Coordinates (x, y) update in real-time.

---

### Story 2: Cursor Movement
**As a team**, we want the mouse cursor to follow the detected hand position, **so that** we can control the computer without a physical mouse.  

**Acceptance Criteria:**
- Cursor moves according to hand coordinates.  
- Movement is smooth and scaled to the screen resolution.  
- No sudden jumps in cursor position.

---

### Story 3: Basic Click by Gesture
**As a team**, we want a simple gesture to trigger a click, **so that** we can interact with the computer without a physical mouse.  

**Acceptance Criteria:**
- The gesture is detected correctly.  
- Generates a left click.  
- Minimizes accidental clicks.

---

### Story 4: MVP Integration
**As a team**, we want to integrate all functionalities, **so that** we have a functional MVP of a virtual mouse.  

**Acceptance Criteria:**
- Cursor movement and click work together.  
- Code is modular and organized.  
- MVP tested under controlled conditions.

---

## Key Questions to Clarify

1. Will we use a real camera or simulate with video/images?  
2. What level of hand detection precision is needed? Only approximate hand position or individual fingers?  
3. Which gestures should trigger clicks? One gesture or multiple?  
4. Should the system work on any screen resolution?  
5. Will testing be only on one PC or multiple computers?  
6. Do we want smoothing of cursor movement, or prioritize basic functionality?  
7. Should it work under any lighting conditions or only controlled environments?  
8. Do we need to log activity or just demonstrate functionality?

---

## Project Feasibility in 1 Week

**Achievable if:**
- Basic hand detection (contour or color).  
- Cursor movement using simple coordinates.  
- Click by simple gesture.  
- Minimal integration for demonstrating functionality.

**Not achievable if:**
- Advanced finger/gesture recognition.  
- Full ML/MediaPipe integration.  
- Extremely smooth, high-precision movement.

**Recommended Strategy:**  
- Focus on a functional and demonstrable MVP.  
- Document potential improvements for future versions.