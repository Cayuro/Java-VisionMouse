package com.vision.cucumber;

import com.vision.detection.HandLandmarks;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Factory for test data - mock OpenCV Mats and landmarks
 */
public class TestDataFactory {

    public static Mat createEmptyFrame(int width, int height) {
        return new Mat(height, width, CvType.CV_8UC3, Scalar.all(0));
    }

    public static Mat createFrameWithHand(int width, int height) {
        Mat frame = createEmptyFrame(width, height);
        // Simulate hand region in center
        for (int y = height/3; y < 2*height/3; y++) {
            for (int x = width/3; x < 2*width/3; x++) {
                frame.put(y, x, 100, 150, 200); // Blue-ish hand color
            }
        }
        return frame;
    }

    public static float[][] createValidLandmarks() {
        float[][] landmarks = new float[21][3];
        for (int i = 0; i < 21; i++) {
            landmarks[i][0] = 0.5f + (float)Math.sin(i * 0.3) * 0.1f; // x
            landmarks[i][1] = 0.5f + (float)Math.cos(i * 0.3) * 0.1f; // y
            landmarks[i][2] = 0.9f; // visibility
        }
        return landmarks;
    }

    public static float[][] createEmptyLandmarks() {
        return new float[0][0];
    }
}
