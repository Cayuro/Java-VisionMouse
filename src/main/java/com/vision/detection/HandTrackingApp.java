package com.vision.detection;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import com.vision.models.HandTrackingPipeline;

/**
 * HandTrackingApp - Aplicación final para probar el Pipeline Profesional.
 * 
 * ¿QUÉ HACE?: Une todo lo que hemos construido para mostrarte la cámara
 * con los 21 landmarks dibujados en tiempo real.
 */
public class HandTrackingApp {

    // Conexiones para dibujar el esqueleto de la mano
    private static final int[][] CONNECTIONS = {
        {0, 1}, {1, 2}, {2, 3}, {3, 4}, {0, 5}, {5, 6}, {6, 7}, {7, 8},
        {0, 9}, {9, 10}, {11, 12}, {0, 13}, {13, 14}, {15, 16}, {0, 17}, 
        {17, 18}, {19, 20}, {5, 9}, {9, 13}, {13, 17}
    };

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String detectorPath = "models/palm_detection.onnx";
        String handPath = "models/hand_landmark.onnx";

        try (HandTrackingPipeline pipeline = new HandTrackingPipeline(detectorPath, handPath)) {
            VideoCapture camera = new VideoCapture(0);
            if (!camera.isOpened()) {
                System.err.println("No se pudo acceder a la cámara.");
                return;
            }

            Mat frame = new Mat();
            while (HighGui.waitKey(10) != 27) { // 27 = ESC para salir
                if (!camera.read(frame)) break;

                // PROCESAR FRAME (Aquí es donde ocurre la detección)
                float[][] landmarks = pipeline.processFrame(frame);

                // DIBUJAR RESULTADOS (Criterios 2 y 3)
                if (landmarks.length == 21) {
                    drawHand(frame, landmarks);
                } else {
                    Imgproc.putText(frame, "No se detecta mano", new Point(20, 50), 
                                   Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 0, 255), 2);
                }

                HighGui.imshow("Hand Tracking Profesional - Java 21", frame);
            }

            camera.release();
            HighGui.destroyAllWindows();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void drawHand(Mat frame, float[][] landmarks) {
        int w = frame.cols();
        int h = frame.rows();

        // Dibujar Conexiones (Esqueleto)
        for (int[] c : CONNECTIONS) {
            Point p1 = new Point(landmarks[c[0]][0] * w, landmarks[c[0]][1] * h);
            Point p2 = new Point(landmarks[c[1]][0] * w, landmarks[c[1]][1] * h);
            Imgproc.line(frame, p1, p2, new Scalar(255, 255, 255), 2);
        }

        // Dibujar los 21 Landmarks
        for (int i = 0; i < 21; i++) {
            Point p = new Point(landmarks[i][0] * w, landmarks[i][1] * h);
            Imgproc.circle(frame, p, 5, new Scalar(0, 255, 0), -1);
        }
    }
}
