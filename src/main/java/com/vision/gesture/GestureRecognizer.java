package com.vision.gesture;

import com.vision.detection.HandLandmarks;

public class GestureRecognizer {
    public String detect(HandLandmarks landmarks) {
        if (!landmarks.isValid()) {
            return "NONE";
        }
        // Simple stub: always MOVE if valid
        return "MOVE";
    }
}
