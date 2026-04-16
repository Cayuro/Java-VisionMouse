package com.vision;

import ai.onnxruntime.OrtException;
import com.vision.models.OnnxModelLoader;
import com.vision.models.PalmDetector;
import org.opencv.core.*;

/**
 * HandTrackingPipeline - El "Director de Orquesta".
 * 
 * ¿POR QUÉ ESTA CLASE?: Aquí es donde sucede la magia de la Opcion B.
 * Unimos el Detector de Palmas con el Extractor de Landmarks.
 */
public class HandTrackingPipeline implements AutoCloseable {
    private PalmDetector detector;
    private OnnxModelLoader landmarkLoader;

    // ESTADO DE RASTREO (Para evitar parpadeos y ganar velocidad)
    private Rect lastRoi = null;
    private int lostFrames = 0;
    private boolean isFlipped = false; // ¿Estamos rastreando una mano volteada (derecha)?
    private static final int MAX_LOST_FRAMES = 5;

    public HandTrackingPipeline(String detectorPath, String landmarkPath) throws OrtException {
        this.detector = new PalmDetector(detectorPath);
        this.landmarkLoader = new OnnxModelLoader(landmarkPath);
        System.out.println("✅ Pipeline de Rastreo listo.");
    }

    public float[][] processFrame(Mat frame) throws OrtException {
        Rect roi = null;

        // ESTRATEGIA: Si ya teníamos un ROI válido, lo usamos directamente
        if (lastRoi != null) {
            roi = lastRoi;
        } else {
            // 1. Probamos detección NORMAL (Mano Izquierda)
            Rect2d handBox = detector.detect(frame);
            if (handBox != null) {
                isFlipped = false;
                roi = calculateRoiFromPalm(handBox, frame.cols(), frame.rows());
            } else {
                // 2. Probamos detección ESPEJO (Mano Derecha)
                Mat flipped = new Mat();
                Core.flip(frame, flipped, 1);
                handBox = detector.detect(flipped);
                if (handBox != null) {
                    isFlipped = true;
                    roi = calculateRoiFromPalm(handBox, frame.cols(), frame.rows());
                    // Nota: El ROI calculado aquí es sobre la imagen volteada
                }
                flipped.release();
            }
        }

        if (roi == null) {
            return resetTracking();
        }

        // PROCESAR RECORTE
        Mat targetFrame = frame;
        if (isFlipped) {
            targetFrame = new Mat();
            Core.flip(frame, targetFrame, 1);
        }
        
        Mat handCrop = new Mat(targetFrame, roi);
        float[] cropData = landmarkLoader.preprocess(handCrop);
        OnnxModelLoader.HandResult result = landmarkLoader.predict(cropData);
        
        handCrop.release();
        if (isFlipped) targetFrame.release();

        // VALIDACIÓN
        float threshold = (lastRoi != null) ? 0.35f : 0.55f; // Reducido de 0.70 a 0.55 para captar ambas manos
        if (result.score() < threshold) {
            lostFrames++;
            if (lostFrames > MAX_LOST_FRAMES) {
                return resetTracking();
            }
            // Mantenemos el ROI anterior un poco más por si acaso
            return new float[0][0]; 
        }

        // ÉXITO: Tenemos landmarks
        lostFrames = 0;
        float[][] cropLandmarks = result.landmarks();
        float[][] finalLandmarks = transformLandmarks(cropLandmarks, roi, frame.cols(), frame.rows(), isFlipped);

        // ACTUALIZAR RASTREO
        lastRoi = calculateRoiFromLandmarks(finalHandmarks, frame.cols(), frame.rows());
        
        return finalLandmarks;
    }

    private float[][] resetTracking() {
        lastRoi = null;
        lostFrames = 0;
        return new float[0][0];
    }

    private Rect calculateRoiFromPalm(Rect2d handBox, int imgW, int imgH) {
        double scale = 3.2; 
        double centerX = handBox.x + handBox.width / 2.0;
        double centerY = handBox.y + handBox.height / 2.0 - (handBox.height * 0.4);
        double size = Math.max(handBox.width, handBox.height) * scale;
        return createSafeRect(centerX, centerY, size, imgW, imgH);
    }

    private Rect calculateRoiFromLandmarks(float[][] landmarks, int imgW, int imgH) {
        // Encontramos los límites de los 21 puntos
        float minX = 1, maxX = 0, minY = 1, maxY = 0;
        for (float[] p : landmarks) {
            minX = Math.min(minX, p[0]); maxX = Math.max(maxX, p[0]);
            minY = Math.min(minY, p[1]); maxY = Math.max(maxY, p[1]);
        }
        
        double centerX = (minX + maxX) / 2.0 * imgW;
        double centerY = (minY + maxY) / 2.0 * imgH;
        double boxW = (maxX - minX) * imgW;
        double boxH = (maxY - minY) * imgH;
        
        // El nuevo ROI para el siguiente frame será un poco más grande que la mano actual
        double size = Math.max(boxW, boxH) * 1.6; 
        return createSafeRect(centerX, centerY, size, imgW, imgH);
    }

    private Rect createSafeRect(double cx, double cy, double size, int imgW, int imgH) {
        int x = (int) (cx - size / 2.0);
        int y = (int) (cy - size / 2.0);
        int w = (int) size;
        int h = (int) size;

        int ix = Math.max(0, x);
        int iy = Math.max(0, y);
        int iw = Math.min(imgW - ix, w);
        int ih = Math.min(imgH - iy, h);

        if (iw < 10 || ih < 10) return null;
        return new Rect(ix, iy, iw, ih);
    }

    private float[][] transformLandmarks(float[][] cropLms, Rect roi, int imgW, int imgH, boolean flipped) {
        float[][] finalLms = new float[21][3];
        for (int i = 0; i < 21; i++) {
            float x_norm = cropLms[0][i * 3 + 0] / 224.0f;
            float y_norm = cropLms[0][i * 3 + 1] / 224.0f;
            
            // Si la imagen estaba volteada, los puntos están en coordenadas de espejo
            // Debemos transformarlos al espacio original de la cámara
            double gx = (x_norm * roi.width + roi.x);
            double gy = (y_norm * roi.height + roi.y);

            if (flipped) {
                // En espejo, la coordenada X real es (AnchoTotal - X_en_espejo)
                finalLms[i][0] = (float) ((imgW - gx) / imgW);
            } else {
                finalLms[i][0] = (float) (gx / imgW);
            }
            
            finalLms[i][1] = (float) (gy / imgH);
            finalLms[i][2] = cropLms[0][i * 3 + 2];
        }
        return finalLms;
    }

    @Override
    public void close() throws OrtException {
        detector.close();
        landmarkLoader.close();
    }
}
