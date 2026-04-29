package com.vision;

import com.vision.video.CameraCapture;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;

/**
 * HandTrackingApp - Aplicación final para probar el Pipeline Profesional.
 * 
 * ¿QUÉ HACE?: Une todo lo que hemos construido para mostrarte la cámara
 * con los 21 landmarks dibujados en tiempo real, ahora con captura multihilo.
 */
public class HandTrackingApp {

    // Conexiones para dibujar el esqueleto de la mano (Corregido para MediaPipe)
    private static final int[][] CONNECTIONS = {
        {0, 1}, {1, 2}, {2, 3}, {3, 4},              // Pulgar
        {0, 5}, {5, 6}, {6, 7}, {7, 8},              // Índice
        {0, 9}, {9, 10}, {10, 11}, {11, 12},         // Medio
        {0, 13}, {13, 14}, {14, 15}, {15, 16},       // Anular
        {0, 17}, {17, 18}, {18, 19}, {19, 20},       // Meñique
        {5, 9}, {9, 13}, {13, 17}                    // Palma
    };

    public static void main(String[] args) {
        // En lugar de System.loadLibrary, usamos el Loader de ByteDeco para cargar las nativas de OpenCV
        // que vienen empaquetadas en el JAR de javacv-platform.
        org.bytedeco.javacpp.Loader.load(org.bytedeco.opencv.opencv_java.class);

        String detectorPath = "models/palm_detection.onnx";
        String handPath = "models/hand_landmark.onnx";

        try (HandTrackingPipeline pipeline = new HandTrackingPipeline(detectorPath, handPath)) {
            CameraCapture camera = new CameraCapture(0);
            try {
                camera.start();
            } catch (Exception e) {
                System.err.println(" No se pudo abrir cámara 0. Intentando con cámara 1...");
                try {
                    camera = new CameraCapture(1);
                    camera.start();
                } catch (Exception e2) {
                    System.err.println(" No se pudo abrir cámara 1. Intentando con cámara 2...");
                    camera = new CameraCapture(2);
                    camera.start();
                }
            }
            
            System.out.println(" Sistema listo. Pulsa ESC en la ventana de video para salir.");

            com.vision.control.MouseController mouseController = new com.vision.control.MouseController();
            
            // Detectar resolución real de pantalla
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = (int) screenSize.getWidth();
            int screenHeight = (int) screenSize.getHeight();
            System.out.println(" Resolución detectada: " + screenWidth + "x" + screenHeight);

            com.vision.gesture.GestureRecognizer recognizer = new com.vision.gesture.GestureRecognizer();

            boolean running = true;
            while (running) {
                Mat frame = camera.getLatestFrame();
                if (frame == null || frame.empty()) {
                    if (frame != null) frame.release();
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                    continue; 
                }

                // PROCESAR FRAME (Detección e Inferencia)
                float[][] landmarksRaw = pipeline.processFrame(frame);
                com.vision.detection.HandLandmarks handLandmarks = com.vision.detection.HandLandmarks.fromPipeline(landmarksRaw);

                // DIBUJAR Y CONTROLAR
                if (handLandmarks != null) {
                    drawHand(frame, landmarksRaw);
                    
                    // Reconocer Gesto
                    com.vision.gesture.MouseAction mouseAction = recognizer.process(handLandmarks);
                    String gesture = (mouseAction != null) ? mouseAction.name() : "NONE";
                    
                    // Controlar Mouse (Lógica + Ejecución Física)
                    String action = mouseController.processGesture(gesture, handLandmarks, screenWidth, screenHeight);
                    mouseController.execute(); // <--- EJECUCIÓN REAL
                    
                    // Dibujar estado en pantalla
                    Imgproc.putText(frame, "Accion: " + action, new Point(20, 80), 
                                   Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 255, 255), 2);
                    
                    if (mouseController.wasSmoothingApplied()) {
                        Imgproc.putText(frame, "Pos: " + mouseController.getLastCursorX() + "," + mouseController.getLastCursorY(), 
                                       new Point(20, 110), Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(0, 255, 0), 1);
                    }

                    // MOSTRAR ERRORES SI EXISTEN
                    if (mouseController.hasError()) {
                        Imgproc.rectangle(frame, new Point(0, 440), new Point(640, 480), new Scalar(0, 0, 255), -1);
                        Imgproc.putText(frame, "ERROR: " + mouseController.getLastError(), new Point(10, 465), 
                                       Imgproc.FONT_HERSHEY_SIMPLEX, 0.6, new Scalar(255, 255, 255), 2);
                    }
                } else {
                    Imgproc.putText(frame, "No se detecta mano", new Point(20, 50), 
                                   Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 0, 255), 2);
                }

                // MOSTRAR FPS DE CAPTURA
                Imgproc.putText(frame, String.format("FPS: %.1f", camera.getFps()), new Point(20, 30),
                               Imgproc.FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 255, 0), 2);

                HighGui.imshow("Hand Tracking Profesional - Java 21", frame);
                
                if (HighGui.waitKey(30) == 27) {
                    running = false;
                }

                // IMPORTANTE: Liberar el frame solo después de que waitKey haya terminado de usarlo
                frame.release();
            }

            camera.stop();
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
            Imgproc.line(frame, p1, p2, new Scalar(0, 0, 0), 10);
        }

        // Dibujar los 21 Landmarks
        for (int i = 0; i < 21; i++) {
            Point p = new Point(landmarks[i][0] * w, landmarks[i][1] * h);
            Imgproc.circle(frame, p, 5, new Scalar(255, 255, 255), -1);
        }
    }
}
