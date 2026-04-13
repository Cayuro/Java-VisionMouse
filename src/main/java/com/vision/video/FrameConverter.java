package com.vision.video;

import com.vision.detection.HandLandmarks;
import com.vision.detection.LandmarkIndex;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * FrameConverter - Responsable de dibujar los resultados sobre el frame de video.
 *
 * Separa la lógica de renderizado de la lógica de detección y captura.
 */
public class FrameConverter {

    // Conexiones para dibujar el esqueleto de la mano (21 puntos MediaPipe)
    private static final int[][] CONNECTIONS = {
        {0, 1}, {1, 2}, {2, 3}, {3, 4},
        {0, 5}, {5, 6}, {6, 7}, {7, 8},
        {0, 9}, {9, 10}, {10, 11}, {11, 12},
        {0, 13}, {13, 14}, {14, 15}, {15, 16},
        {0, 17}, {17, 18}, {18, 19}, {19, 20},
        {5, 9}, {9, 13}, {13, 17}
    };

    private static final Scalar COLOR_SKELETON  = new Scalar(255, 255, 255);
    private static final Scalar COLOR_LANDMARK  = new Scalar(0, 255, 0);
    private static final Scalar COLOR_NO_HAND   = new Scalar(0, 0, 255);

    /**
     * Dibuja el esqueleto y los landmarks si hay mano detectada;
     * de lo contrario dibuja un texto de estado.
     */
    public void drawHand(Mat frame, HandLandmarks landmarks) {
        if (landmarks == null) {
            drawStatus(frame, "No se detecta mano", COLOR_NO_HAND);
            return;
        }

        int w = frame.cols();
        int h = frame.rows();

        // Dibujar esqueleto
        for (int[] c : CONNECTIONS) {
            Point p1 = toPoint(landmarks.points()[c[0]], w, h);
            Point p2 = toPoint(landmarks.points()[c[1]], w, h);
            Imgproc.line(frame, p1, p2, COLOR_SKELETON, 2);
        }

        // Dibujar los 21 puntos
        for (int i = 0; i < 21; i++) {
            Point p = toPoint(landmarks.points()[i], w, h);
            Imgproc.circle(frame, p, 5, COLOR_LANDMARK, -1);
        }
    }

    /** Dibuja un texto de estado en la parte superior del frame. */
    public void drawStatus(Mat frame, String text, Scalar color) {
        Imgproc.putText(frame, text, new Point(20, 50),
                        Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, color, 2);
    }

    private Point toPoint(float[] lm, int width, int height) {
        return new Point(lm[0] * width, lm[1] * height);
    }
}
