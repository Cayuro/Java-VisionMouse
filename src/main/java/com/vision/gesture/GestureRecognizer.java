package com.vision.gesture;

import com.vision.detection.HandLandmarks;
import java.awt.geom.Point2D;

/**
 * GestureRecognizer - Identifica gestos de la mano basados en landmarks.
 *
 * Arquitectura de Pipeline:
 *   Captura -> HandLandmarks -> GestureRecognizer -> Action
 *
 * Acciones continuas (sin estado interno, el debounce lo hace MouseController):
 *   - NONE: Sin landmarks válidos
 *   - DRAG: Puño cerrado
 *   - CLICK: Pinza izquierda (Pulgar + Índice)
 *   - RIGHT_CLICK: Pinza derecha (Pulgar + Medio)
 *   - MOVE: Mano abierta sin ningún otro gesto activo
 */
public class GestureRecognizer {

    private static final double PINCH_THRESHOLD = 0.05; // Distancia euclidiana máxima para pinza

    /**
     * Procesa los landmarks de una mano y retorna la acción detectada en el frame actual.
     *
     * @param landmarks Los 21 puntos de la mano detectados
     * @return La acción de ratón correspondiente (MOVE, CLICK, DRAG, etc.)
     */
    public MouseAction process(HandLandmarks landmarks) {
        if (landmarks == null || !landmarks.isValid()) {
            return MouseAction.NONE;
        }

        boolean isFistClosed = isFistClosed(landmarks);
        double leftDist = calculateDistance(landmarks.thumbTip(), landmarks.indexTip());
        double rightDist = calculateDistance(landmarks.thumbTip(), landmarks.ringTip());
        boolean leftPinch = leftDist < PINCH_THRESHOLD && !isFistClosed;
        boolean rightPinch = rightDist < PINCH_THRESHOLD && !isFistClosed;

        // Priority 1: SCROLL (puño cerrado)
        if (isFistClosed) {
            return MouseAction.SCROLL;
        }

        // Priority 2: PINCH gestures (LEFT_CLICK tiene prioridad)
        if (leftPinch) {
            return MouseAction.CLICK;
        }

        if (rightPinch) {
            return MouseAction.RIGHT_CLICK;
        }

        // Si la mano está abierta y no hay gestos especiales, es MOVE
        return MouseAction.MOVE;
    }

    /**
     * Detecta si la mano está en posición de puño.
     *
     * Criterio: Las puntas de los 4 dedos largos (Índice, Medio, Anular, Meñique)
     * tienen coordenada Y mayor que sus respectivos nudillos (MCP).
     * En coordenadas de pantalla/imagen, Y mayor significa "más abajo".
     *
     * @param landmarks Los puntos de la mano
     * @return true si es puño, false si está abierta
     */
    private boolean isFistClosed(HandLandmarks landmarks) {
        return landmarks.indexTip().getY() > landmarks.indexMcp().getY() &&
               landmarks.middleTip().getY() > landmarks.middleMcp().getY() &&
               landmarks.ringTip().getY() > landmarks.ringMcp().getY() &&
               landmarks.pinkyTip().getY() > landmarks.pinkyMcp().getY();
    }

    /**
     * Calcula la distancia euclidiana entre dos puntos 2D normalizados.
     *
     * @param p1 Primer punto
     * @param p2 Segundo punto
     * @return Distancia euclidiana
     */
    private double calculateDistance(Point2D p1, Point2D p2) {
        double dx = p1.getX() - p2.getX();
        double dy = p1.getY() - p2.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

}

