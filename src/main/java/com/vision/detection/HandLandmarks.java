package com.vision.detection;
import java.awt.geom.Point2D;
/**
 * HandLandmarks - Contenedor de los 21 puntos detectados de la mano.

 * Recibe el float[][] que entrega HandTrackingPipeline.processFrame()
 * de Nicolas (US-02.1) y expone m�todos para acceder a los puntos
 * que necesitamos.

 * Las coordenadas est�n normalizadas entre 0.0 y 1.0 (relativas al frame).
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
     * Retorna las coordenadas normalizadas (x, y) de la punta del dedo medio.
     * Corresponde al Landmark 12.

     * Criterio de aceptaci�n 1: Dado landmarks detectados, cuando se obtiene
     * el punto 12, entonces retorna coordenadas v�lidas.
     *
     * @return Point2D con x e y entre 0.0 y 1.0.
     */
    public Point2D getMiddleFingerTip() {
        int i = LandmarkIndex.MIDDLE_FINGER_TIP.value(); // = 12
        return new Point2D.Float(points[i][0], points[i][1]);
    }
    /**
     * Crea un HandLandmarks desde el float[][] que entrega processFrame().
     * Retorna null si no hay mano detectada (array vac�o).

     * Criterio de aceptaci�n 2: Dado que no hay landmarks, cuando se consulta
     * el punto 12, entonces retorna null.

     * @param rawLandmarks El float[][] de HandTrackingPipeline.processFrame().
     * @return HandLandmarks con los 21 puntos, o null si no se detect� mano.
     */
    public static HandLandmarks fromPipeline(float[][] rawLandmarks) {
        if (rawLandmarks == null || rawLandmarks.length != 21) {
            return null;
        }
        return new HandLandmarks(rawLandmarks);
    }
}

