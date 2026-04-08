# Project: Hand-Controlled Mouse

---

## 📷 Module 1: Video Infrastructure (Webcam & UI)

**Objective:** Have a window that displays the camera feed without errors.

- **US-01.1: OpenCV Initialization**  
  Configure dependencies and load the native OpenCV library in the Java project.

- **US-01.2: Single Frame Capture**  
  Create a method that opens the default camera (ID 0) and successfully captures a single frame.

- **US-01.3: Video Loop (Capture Thread)**  
  Implement a `Runnable` or `Thread` that continuously captures frames without freezing the application.

- **US-01.4: Mat to Image Conversion**  
  Create an efficient converter from OpenCV `Mat` objects to `BufferedImage` (Java Swing/FX) for visualization.

- **US-01.5: Display Interface**  
  Create a basic window with a `JLabel` or `Canvas` that renders the real-time video stream.

---

## 🧠 Module 2: Intelligence and Detection (MediaPipe / OpenCV)

**Objective:** Obtain pure coordinates of the index finger.

- **US-02.1: MediaPipe Hands Integration**  
  Configure the wrapper or API to process a frame and return a list of points (Landmarks).

- **US-02.2: Extract Point 8**  
  Create logic to filter and obtain exclusively the `(x, y)` coordinates of the tip of the index finger.

- **US-02.3: Coordinate Normalization**  
  Convert MediaPipe's relative coordinates (0.0 to 1.0) to absolute pixel coordinates of the camera resolution (e.g., 640x480).

- **US-02.4: Presence Detection**  
  Implement a boolean flag indicating whether a hand is detected in the frame to avoid null pointer errors.

---

## 🖱️ Module 3: System Control (Robot & OS)

**Objective:** Move the mouse on Windows/Mac, even in a basic way.

- **US-03.1: Robot Instantiation**  
  Create a Singleton class to manage `java.awt.Robot` and handle OS security exceptions.

- **US-03.2: Simple Linear Mapping**  
  Create a function that translates a camera point (640x480) to a screen point (e.g., 1920x1080).

- **US-03.3: Mirroring Effect**  
  Invert the X-axis mapping so that if the user moves their hand right, the mouse also moves right (mirror behavior).

- **US-03.4: Physical Movement**  
  Send the mapped coordinates to `robot.mouseMove(x, y)` to move the real cursor.

---

## ⚡ Module 4: Refinement and Gestures (UX & Smoothing)

**Objective:** Make the system usable and prevent “shaky” behavior.

- **US-04.1: EMA Filter (Calculation)**  
  Implement the Exponential Moving Average formula: `St = α ⋅ Yt + (1 − α) ⋅ St−1`.

- **US-04.2: Jitter Reduction**  
  Apply the EMA filter to mouse coordinates to eliminate natural hand tremors.

- **US-04.3: Click Gesture (Thumb-Index Distance)**  
  Calculate the Euclidean distance between Landmarks 4 and 8. If below a threshold `T`, trigger an event.

- **US-04.4: Execute Left Click**  
  Implement `robot.mousePress` and `robot.mouseRelease` triggered by the detected gesture.

- **US-04.5: Draw Landmarks**  
  Overlay a colored circle on the video over the index finger so the user knows what the system is detecting.

---

## ⚙️ Module 5: Configuration and Settings

**Objective:** Allow the user to customize the experience.

- **US-05.1: Sensitivity Slider**  
  Create a slider control to adjust the mouse movement scaling factor.

- **US-05.2: Smoothing Adjustment**  
  Create a control to vary the `α` value in the EMA filter (more smoothing vs. more responsiveness).

- **US-05.3: Dwell Time Logic (Optional)**  
  Implement a timer: if the finger stays still for 1.5 seconds within a 5px area, execute a click.

- **US-05.4: Settings Persistence**  
  Save slider values in a `.properties` or `.json` file so they load at startup.