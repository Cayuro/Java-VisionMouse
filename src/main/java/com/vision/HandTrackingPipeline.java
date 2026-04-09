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

    public HandTrackingPipeline(String detectorPath, String landmarkPath) throws OrtException {
        this.detector = new PalmDetector(detectorPath);
        this.landmarkLoader = new OnnxModelLoader(landmarkPath);
    }

    /**
     * PROCESO COMPLETO:
     * 1. Detectar palma.
     * 2. Recortar mano.
     * 3. Extraer 21 puntos del recorte.
     * 4. Ajustar puntos a la imagen original.
     */
    public float[][] processFrame(Mat frame) throws OrtException {
        // PASO 1: Buscar la caja de la palma
        Rect2d handBox = detector.detect(frame);
        
        if (handBox == null) {
            // Criterio de Aceptación 3: Si no hay mano, retornamos lista vacía.
            return new float[0][0];
        }

        // PASO 2: Recortar y preparar para el especialista en landmarks
        // (Aseguramos que el recorte no se salga de los bordes de la imagen)
        Rect roi = new Rect((int)Math.max(0, handBox.x), (int)Math.max(0, handBox.y), 
                            (int)Math.min(frame.cols() - handBox.x, handBox.width), 
                            (int)Math.min(frame.rows() - handBox.y, handBox.height));
        
        if (roi.width <= 0 || roi.height <= 0) return new float[0][0];
        
        Mat handCrop = new Mat(frame, roi);
        
        // PASO 3: El especialista extrae los 21 puntos del recorte
        float[] cropData = landmarkLoader.preprocess(handCrop);
        float[][] cropLandmarks = landmarkLoader.predict(cropData);
        
        // PASO 4: AJUSTE DE COORDENADAS (Crítico)
        // Los landmarks vienen en formato 0.0 a 1.0 relativo al recorte.
        // Debemos convertirlos a píxeles relativos a la imagen ORIGINAL.
        float[][] finalLandmarks = new float[21][3];
        for (int i = 0; i < 21; i++) {
            // X_final = (X_relativo * Ancho_Recorte) + X_Origen_Recorte
            finalLandmarks[i][0] = (float) ((cropLandmarks[i][0] * roi.width + roi.x) / frame.cols());
            finalLandmarks[i][1] = (float) ((cropLandmarks[i][1] * roi.height + roi.y) / frame.rows());
            finalLandmarks[i][2] = cropLandmarks[i][2]; // Z se queda igual
        }
        
        handCrop.release();
        return finalLandmarks;
    }

    @Override
    public void close() throws OrtException {
        detector.close();
        landmarkLoader.close();
    }
}
