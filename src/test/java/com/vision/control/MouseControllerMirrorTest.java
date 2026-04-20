package com.vision.control;

import com.vision.detection.HandLandmarks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MouseControllerMirrorTest {

    private static HandLandmarks landmarksAt(float x, float y) {
        float[][] points = new float[21][3];
        for (int i = 0; i < points.length; i++) {
            points[i][0] = 0.5f;
            points[i][1] = 0.5f;
            points[i][2] = 0.9f;
        }
        points[8][0] = x;  // Index fingertip x
        points[8][1] = y;  // Index fingertip y
        return new HandLandmarks(points);
    }

    @Test
    void shouldMoveCursorRightWhenFingerMovesRightWithMirrorEnabled() {
        MouseController controller = new MouseController();
        controller.setDebounceThreshold(1);
        controller.setMirroringEnabled(true);

        controller.processGesture("MOVE", landmarksAt(0.7f, 0.5f), 1920, 1080);
        int firstX = controller.getLastCursorX();

        // In mirrored camera interaction, moving hand right maps to lower normalized x.
        controller.processGesture("MOVE", landmarksAt(0.3f, 0.5f), 1920, 1080);
        int secondX = controller.getLastCursorX();

        assertTrue(secondX > firstX,
                "Expected cursor to move right: secondX=" + secondX + ", firstX=" + firstX);
    }

    @Test
    void shouldInvertHorizontalMappingWhenMirrorEnabled() {
        MouseController controller = new MouseController();
        controller.setDebounceThreshold(1);
        controller.setMirroringEnabled(true);

        controller.processGesture("MOVE", landmarksAt(0.2f, 0.5f), 1920, 1080);
        int mirroredX = controller.getLastCursorX();

        controller.setMirroringEnabled(false);
        controller.processGesture("MOVE", landmarksAt(0.2f, 0.5f), 1920, 1080);
        int nonMirroredX = controller.getLastCursorX();

        assertTrue(mirroredX > nonMirroredX,
                "Expected mirrored x to be on the opposite side of non-mirrored x");
    }
}
