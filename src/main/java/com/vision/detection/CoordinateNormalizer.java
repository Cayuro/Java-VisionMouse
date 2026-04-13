package com.vision.detection;

import org.opencv.core.Rect;

/**
 * CoordinateNormalizer - Convierte coordenadas relativas al recorte (ROI)
 * en coordenadas relativas a la imagen original completa.
 */
public class CoordinateNormalizer {

    /**
     * Ajusta los landmarks del recorte a la imagen original.
     *
     * @param cropLandmarks Puntos en formato [0.0, 1.0] relativos al ROI.
     * @param roi           Rectángulo de la región recortada.
     * @param frameCols     Ancho total del frame original.
     * @param frameRows     Alto total del frame original.
     * @return Array de 21 puntos ajustados al frame completo.
     */
    public float[][] normalize(float[][] cropLandmarks, Rect roi, int frameCols, int frameRows) {
        float[][] result = new float[21][3];
        for (int i = 0; i < 21; i++) {
            // X_final = (X_roi * Ancho_ROI + X_origen_ROI) / Ancho_frame
            result[i][0] = (cropLandmarks[i][0] * roi.width  + roi.x) / frameCols;
            // Y_final = (Y_roi * Alto_ROI  + Y_origen_ROI) / Alto_frame
            result[i][1] = (cropLandmarks[i][1] * roi.height + roi.y) / frameRows;
            // Z se conserva sin cambio
            result[i][2] = cropLandmarks[i][2];
        }
        return result;
    }
}
