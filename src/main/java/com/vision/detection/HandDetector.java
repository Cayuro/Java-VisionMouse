package com.vision.detection;

import ai.onnxruntime.OrtException;
import com.vision.models.OnnxModelLoader;
import com.vision.models.PalmDetector;
import org.opencv.core.*;

/**
 * HandDetector - Orquesta la detección de la mano usando modelos ONNX.
 *
 * Reemplaza a HandTrackingPipeline aplicando separación de responsabilidades:
 * la normalización de coordenadas se delega a {@link CoordinateNormalizer}.
 */
public class HandDetector implements AutoCloseable {

    private final PalmDetector      palmDetector;
    private final OnnxModelLoader   landmarkLoader;
    private final CoordinateNormalizer normalizer;

    public HandDetector(String detectorPath, String landmarkPath) throws OrtException {
        this.palmDetector   = new PalmDetector(detectorPath);
        this.landmarkLoader = new OnnxModelLoader(landmarkPath);
        this.normalizer     = new CoordinateNormalizer();
    }

    /**
     * Procesa un frame y retorna los 21 landmarks detectados.
     *
     * @return HandLandmarks con 21 puntos, o null si no se detectó mano.
     */
    public HandLandmarks detect(Mat frame) throws OrtException {
        // 1. Detectar palma
        Rect2d handBox = palmDetector.detect(frame);
        if (handBox == null) return null;

        // 2. Recortar ROI con validación de bordes
        Rect roi = new Rect(
            (int) Math.max(0, handBox.x),
            (int) Math.max(0, handBox.y),
            (int) Math.min(frame.cols() - handBox.x, handBox.width),
            (int) Math.min(frame.rows() - handBox.y, handBox.height)
        );
        if (roi.width <= 0 || roi.height <= 0) return null;

        Mat handCrop = new Mat(frame, roi);

        // 3. Extraer landmarks desde el recorte
        float[]   cropData      = landmarkLoader.preprocess(handCrop);
        float[][] cropLandmarks = landmarkLoader.predict(cropData);

        handCrop.release();

        // 4. Convertir coordenadas del recorte a la imagen original
        float[][] finalPoints = normalizer.normalize(cropLandmarks, roi, frame.cols(), frame.rows());

        return HandLandmarks.fromPipeline(finalPoints);
    }

    @Override
    public void close() throws OrtException {
        palmDetector.close();
        landmarkLoader.close();
    }
}
