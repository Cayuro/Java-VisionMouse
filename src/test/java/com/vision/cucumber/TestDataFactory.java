package com.vision.cucumber;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Factory for test data - mock OpenCV Mats and landmarks
 */
public class TestDataFactory {
    static {
        org.bytedeco.javacpp.Loader.load(org.bytedeco.opencv.opencv_java.class);
    }

    private static float[][] baseLandmarks() {
        float[][] landmarks = new float[21][3];
        for (int i = 0; i < 21; i++) {
            landmarks[i][0] = 0.5f;
            landmarks[i][1] = 0.5f;
            landmarks[i][2] = 0.9f;
        }

        landmarks[0][1] = 0.75f;
        return landmarks;
    }

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
        float[][] landmarks = baseLandmarks();
        landmarks[0][1] = 0.72f;
        landmarks[4][0] = 0.38f;
        landmarks[4][1] = 0.24f;
        landmarks[8][0] = 0.50f;
        landmarks[8][1] = 0.20f;
        landmarks[12][0] = 0.56f;
        landmarks[12][1] = 0.23f;
        landmarks[16][0] = 0.62f;
        landmarks[16][1] = 0.24f;
        landmarks[20][0] = 0.68f;
        landmarks[20][1] = 0.25f;
        return landmarks;
    }

    public static float[][] createClosedFistLandmarks() {
        float[][] landmarks = baseLandmarks();
        landmarks[0][1] = 0.68f;
        landmarks[4][1] = 0.60f;
        landmarks[8][1] = 0.61f;
        landmarks[12][1] = 0.62f;
        landmarks[16][1] = 0.61f;
        landmarks[20][1] = 0.60f;
        return landmarks;
    }

    public static float[][] createPointingLandmarks() {
        float[][] landmarks = baseLandmarks();
        landmarks[0][1] = 0.70f;
        landmarks[4][1] = 0.58f;
        landmarks[8][0] = 0.50f;
        landmarks[8][1] = 0.18f;
        landmarks[12][1] = 0.62f;
        landmarks[16][1] = 0.63f;
        landmarks[20][1] = 0.64f;
        return landmarks;
    }

    public static float[][] createCursorLandmarks(float middleTipX, float middleTipY) {
        float[][] landmarks = baseLandmarks();
        landmarks[12][0] = middleTipX;
        landmarks[12][1] = middleTipY;
        landmarks[8][1] = middleTipY - 0.02f;
        landmarks[16][1] = middleTipY + 0.02f;
        landmarks[20][1] = middleTipY + 0.04f;
        return landmarks;
    }

    public static float[][] createEmptyLandmarks() {
        return new float[0][0];
    }
}
