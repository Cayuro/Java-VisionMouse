package com.vision.gesture;

import com.vision.detection.HandLandmarks;

public class GestureRecognizer {
    public String detect(HandLandmarks landmarks) {
        if (landmarks == null || !landmarks.isValid()) {
            return "NONE";
        }

        float[][] points = landmarks.points();
        float wristY = points[0][1];
        float thumbTipY = points[4][1];
        float indexTipY = points[8][1];
        float middleTipY = points[12][1];
        float ringTipY = points[16][1];
        float pinkyTipY = points[20][1];

        boolean pointing = indexTipY + 0.12f < middleTipY
                && indexTipY + 0.12f < ringTipY
                && indexTipY + 0.12f < pinkyTipY;
        if (pointing) {
            return "DRAG";
        }

        boolean closedFist = indexTipY > wristY - 0.12f
                && middleTipY > wristY - 0.12f
                && ringTipY > wristY - 0.12f
                && pinkyTipY > wristY - 0.12f
                && thumbTipY > wristY - 0.12f;
        if (closedFist) {
            return "CLICK";
        }

        boolean openPalm = indexTipY + 0.12f < wristY
                && middleTipY + 0.12f < wristY
                && ringTipY + 0.12f < wristY
                && pinkyTipY + 0.12f < wristY;
        if (openPalm) {
            return "MOVE";
        }

        return "MOVE";
    }
}
