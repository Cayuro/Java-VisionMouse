package com.vision.gesture;

import com.vision.detection.HandLandmarks;
import java.awt.geom.Point2D;

/**
 * GestureRecognizer - Identifica gestos de la mano basados en landmarks.
 *
 * Arquitectura de Pipeline:
 *   Captura -> HandLandmarks -> GestureRecognizer -> Action
 *
 * Gestos soportados:
 *   - Click Izquierdo: Pinza (INDEX_FINGER_TIP ∪ THUMB_TIP, distancia < 0.05)
 *   - Click Derecho: Pinza (MIDDLE_FINGER_TIP ∪ THUMB_TIP)
 *   - Drag: Puño cerrado (TIP > MCP para los 4 dedos largos)
 *
 * Prioridades:
 *   1. Si puño está cerrado → DRAG_START/DRAG_END, ignora clicks
 *   2. Si puño abierto → detecta LEFT_CLICK, RIGHT_CLICK
 *   3. Debounce: Un gesto solo se dispara UNA VEZ por activación
 */
public class GestureRecognizer {

    private static final double PINCH_THRESHOLD = 0.05; // Distancia euclidiana máxima para pinza

    // ===== Estados de Debounce =====
    // Para evitar que un mismo gesto genere múltiples eventos en frames consecutivos
private State state = State.IDLE;

private enum State {
    IDLE,
    LEFT_PINCH,
    RIGHT_PINCH,
    FIST_DRAG
}

    /**
     * Procesa los landmarks de una mano y retorna la acción detectada.
     *
     * Flujo de lógica:
     *   1. Detectar estado del puño (cierre/apertura)
     *   2. Si puño cerrado → solo DRAG_START/DRAG_END, ignora clicks
     *   3. Si puño abierto → detecta clicks con debounce
     *
     * @param landmarks Los 21 puntos de la mano detectados
     * @return La acción de ratón correspondiente (LEFT_CLICK, RIGHT_CLICK, DRAG_START, DRAG_END, NONE)
     */
public MouseAction process(HandLandmarks landmarks) {
        if (landmarks == null || !landmarks.isValid()) {
            state = State.IDLE;
            return MouseAction.NONE;
        }

        boolean currentFist = isFistClosed(landmarks);
        double leftDist = calculateDistance(landmarks.thumbTip(), landmarks.indexTip());
        double rightDist = calculateDistance(landmarks.thumbTip(), landmarks.middleTip());
        boolean pinchLeft = leftDist < PINCH_THRESHOLD && !currentFist;
        boolean pinchRight = rightDist < PINCH_THRESHOLD && !currentFist;

        // Priority: FIST > LEFT_PINCH > RIGHT_PINCH
        if (currentFist) {
            if (state != State.FIST_DRAG) {
                state = State.FIST_DRAG;
                return MouseAction.DRAG_START;
            }
            return MouseAction.NONE;
        } else if (state == State.FIST_DRAG) {
            state = State.IDLE;
            return MouseAction.DRAG_END;
        }

        if (pinchLeft) {
            if (state != State.LEFT_PINCH) {
                state = State.LEFT_PINCH;
                return MouseAction.LEFT_CLICK;
            }
            return MouseAction.NONE;
        }

        if (pinchRight) {
            if (state != State.RIGHT_PINCH) {
                state = State.RIGHT_PINCH;
                return MouseAction.RIGHT_CLICK;
            }
            return MouseAction.NONE;
        }

        // Release pinch
        if (state == State.LEFT_PINCH || state == State.RIGHT_PINCH) {
            state = State.IDLE;
        }

        return MouseAction.NONE;
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

