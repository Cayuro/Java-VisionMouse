package com.mousevision.detection;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HandLandmarksTest {

    @Test
    void shouldStoreCoordinatesCorrectly() {
        HandLandmarks landmarks = new HandLandmarks();

        landmarks.setIndexFingerTip(100, 200);

        assertEquals(100, landmarks.getIndexX());
        assertEquals(200, landmarks.getIndexY());
    }

    @Test
    void shouldReturnDefaultValuesWhenEmpty() {
        HandLandmarks landmarks = new HandLandmarks();

        assertEquals(0, landmarks.getIndexX());
        assertEquals(0, landmarks.getIndexY());
    }

    @Test
    void shouldDetectValidHandState() {
        HandLandmarks landmarks = new HandLandmarks();

        landmarks.setIndexFingerTip(50, 50);

        assertTrue(landmarks.isValid());
    }
}