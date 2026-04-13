package com.mousevision.gesture;

import com.mousevision.detection.HandLandmarks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GestureRecognizerTest {

    @Test
    void shouldDetectMoveGesture() {
        GestureRecognizer recognizer = new GestureRecognizer();
        HandLandmarks landmarks = new HandLandmarks();

        landmarks.setIndexFingerTip(100, 200);

        String gesture = recognizer.detect(landmarks);

        assertEquals("MOVE", gesture);
    }

    @Test
    void shouldDetectClickGesture() {
        GestureRecognizer recognizer = new GestureRecognizer();
        HandLandmarks landmarks = new HandLandmarks();

        landmarks.setIndexFingerTip(100, 200);
        landmarks.setThumbTip(102, 202); // muy cerca → click

        String gesture = recognizer.detect(landmarks);

        assertEquals("CLICK", gesture);
    }

    @Test
    void shouldReturnNoGestureWhenInvalid() {
        GestureRecognizer recognizer = new GestureRecognizer();
        HandLandmarks landmarks = new HandLandmarks();

        String gesture = recognizer.detect(landmarks);

        assertEquals("NONE", gesture);
    }
}