package com.mouseVision.detection;

import com.vision.detection.HandDetector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HandDetectorTest {

    @Test
    void shouldReturnTrueWhenHandIsVisible() {
        HandDetector detector = new HandDetector();

        float[][] landmarks = new float[21][3];
        detector.update(landmarks);

        assertTrue(detector.isHandDetected());
    }

    @Test
    void shouldReturnFalseWhenNoHandIsPresent() {
        HandDetector detector = new HandDetector();

        detector.update(new float[0][0]);

        assertFalse(detector.isHandDetected());
    }

    @Test
    void shouldNotThrowWhenAccessingLandmarksWithoutHand() {
        HandDetector detector = new HandDetector();

        detector.update(new float[0][0]);

        assertDoesNotThrow(detector::getLandmarks);
        assertTrue(detector.getLandmarks().isEmpty());
    }
}
