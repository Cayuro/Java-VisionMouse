package com.vision;

import ai.onnxruntime.OrtException;
import com.vision.detection.HandDetector;
import com.vision.detection.HandLandmarks;
import com.vision.video.CameraCapture;
import com.vision.video.DisplayWindow;
import com.vision.video.FrameConverter;
import org.opencv.core.Core;
import org.opencv.core.Mat;

/**
 * Main - Punto de entrada de VisionMouse.
 *
 * Orquesta los módulos de captura, detección y visualización.
 */
public class Main {

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String detectorPath = "models/palm_detection.onnx";
        String handPath     = "models/hand_landmark.onnx";

        try (HandDetector detector = new HandDetector(detectorPath, handPath);
             CameraCapture camera  = new CameraCapture(0)) {

            if (!camera.isOpened()) {
                System.err.println("No se pudo acceder a la cámara.");
                return;
            }

            DisplayWindow window    = new DisplayWindow("VisionMouse - Java 21");
            FrameConverter converter = new FrameConverter();
            Mat frame = new Mat();

            System.out.println("Pipeline iniciado. Presiona ESC para salir.");

            while (window.waitKey(10) != 27) {
                if (!camera.read(frame)) break;

                HandLandmarks landmarks = detector.detect(frame);
                converter.drawHand(frame, landmarks);
                window.show(frame);
            }

            window.destroy();

        } catch (OrtException e) {
            System.err.println("Error al cargar modelos ONNX: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
