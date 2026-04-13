package com.vision.detection;

import java.awt.geom.Point2D;

/**
 * HandLandmarks - Contenedor de los 21 puntos detectados de la mano.
 *
 * Recibe el float[][] que entrega HandDetector y expone métodos para
 * acceder a los puntos que necesitamos.
 *
 * Las coordenadas están normalizadas entre 0.0 y 1.0 (relativas al frame).
 *
 * @param points Array de 21 puntos, cada uno con [x, y, z] normalizado.
 */
public record HandLandmarks(float[][] points) {

    /**
     * Valida que el array tenga exactamente 21 puntos al construirse.
     */
    public HandLandmarks {
        if (points == null || points.length != 21) {
            throw new IllegalArgumentException(
                    "HandLandmarks requiere exactamente 21 puntos. Recibidos: " +
                            (points == null ? "null" : points.length)
            );
        }
    }

    /**
     * Siempre retorna true para un record correctamente construido.
     * Facilita chequeos semánticos en GestureRecognizer y otros consumidores.
     */
    public boolean isValid() {
        return points != null && points.length == 21;
    }

    /**
     * Retorna las coordenadas normalizadas (x, y) de la punta del dedo medio.
     * Corresponde al Landmark 12 según el estándar MediaPipe.
     *
     * @return Point2D con x e y entre 0.0 y 1.0.
     */
    public Point2D getMiddleFingerTip() {
        int i = LandmarkIndex.MIDDLE_FINGER_TIP.value();
        return new Point2D.Float(points[i][0], points[i][1]);
    }

    /**
     * Factory method: crea un HandLandmarks desde el float[][] de detección.
     * Retorna null si no hay mano detectada (array vacío o incompleto).
     *
     * @param rawLandmarks El float[][] de HandDetector.detect().
     * @return HandLandmarks con los 21 puntos, o null si no se detectó mano.
     */
    public static HandLandmarks fromPipeline(float[][] rawLandmarks) {
        if (rawLandmarks == null || rawLandmarks.length != 21) {
            return null;
        }
        return new HandLandmarks(rawLandmarks);
    }
}

