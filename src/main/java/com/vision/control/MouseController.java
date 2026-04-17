package com.vision.control;

import com.vision.detection.HandLandmarks;
import java.awt.geom.Point2D;

public class MouseController {
    private int lastCursorX = -1;
    private int lastCursorY = -1;
    private boolean leftClickPerformed;
    private boolean smoothingApplied;

    public String processGesture(String gesture, HandLandmarks landmarks, int screenWidth, int screenHeight) {
        leftClickPerformed = false;
        smoothingApplied = false;

        if (gesture == null) {
            return "NONE";
        }

        if ("MOVE".equals(gesture) && landmarks != null && landmarks.isValid()) {
            Point2D indexFingerTip = landmarks.getMiddleFingerTip(); // Changed to middleFingerTip for better stability
            lastCursorX = (int) Math.round(indexFingerTip.getX() * screenWidth);
            lastCursorY = (int) Math.round(indexFingerTip.getY() * screenHeight);
            smoothingApplied = true;
            return "MOVE";
        }

        if ("CLICK".equals(gesture)) {
            leftClickPerformed = true;
            return "CLICK";
        }

        if ("DRAG".equals(gesture)) {
            smoothingApplied = true;
            return "DRAG";
        }

        return "NONE";
    }

    public int getLastCursorX() {
        return lastCursorX;
    }

    public int getLastCursorY() {
        return lastCursorY;
    }

    public boolean wasLeftClickPerformed() {
        return leftClickPerformed;
    }

    public boolean wasSmoothingApplied() {
        return smoothingApplied;
    }
}