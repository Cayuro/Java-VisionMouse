package com.vision.gesture;

import com.vision.detection.HandLandmarks;
import java.awt.geom.Point2D;

/**
 * GestureRecognizer - Identifica gestos de la mano basados en landmarks.
 *
 * Arquitectura de Pipeline:
 *   Captura -> HandLandmarks -> GestureRecognizer -> Action
 *
 * Máquina de Estados:
 *   - NONE: Sin gesto activo
 *   - PINCHING: Gesto de pinza (click) activo
 *   - DRAGGING: Gesto de arrastre activo
 *
 * Eventos generados:
 *   - LEFT_CLICK: Primer frame de pinza izquierda
 *   - RIGHT_CLICK: Primer frame de pinza derecha
 *   - DRAG_START: Primer frame de puño cerrado
 *   - DRAGGING: Frames continuos de puño cerrado
 *   - DRAG_END: Primer frame después de abrir puño
 *   - NONE: Sin evento nuevo
 *
 * Prioridades:
 *   1. DRAG > PINCH (puño tiene prioridad sobre clicks)
 *   2. LEFT_CLICK > RIGHT_CLICK (prioridad al índice)
 */
public class GestureRecognizer {

    private static final double PINCH_THRESHOLD = 0.05; // Distancia euclidiana máxima para pinza

    // ===== Estados de la Máquina de Estados =====
    private State state = State.NONE;
        // Ensure proper state reset after gesture end

    private enum State {
        NONE,       // Sin gesto activo
        PINCHING,   // Gesto de pinza activo (click)
        DRAGGING    // Gesto de arrastre activo
    }

    /**
     * Procesa los landmarks de una mano y retorna la acción detectada.
     *
     * Máquina de Estados:
     *   - NONE → DRAGGING: DRAG_START (primer frame de puño cerrado)
     *   - DRAGGING → NONE: DRAG_END (primer frame de puño abierto)
     *   - NONE → PINCHING: LEFT_CLICK o RIGHT_CLICK (primer frame de pinza)
     *   - PINCHING → NONE: NONE (liberación de pinza)
     *
     * @param landmarks Los 21 puntos de la mano detectados
     * @return La acción de ratón correspondiente
     */
    public MouseAction process(HandLandmarks landmarks) {
        if (landmarks == null || !landmarks.isValid()) {
            state = State.NONE;
        // Ensure proper state reset after gesture end
            return MouseAction.NONE;
        }

        boolean isFistClosed = isFistClosed(landmarks);
        double leftDist = calculateDistance(landmarks.thumbTip(), landmarks.indexTip());
        double rightDist = calculateDistance(landmarks.thumbTip(), landmarks.middleTip());
        boolean leftPinch = leftDist < PINCH_THRESHOLD && !isFistClosed;
        boolean rightPinch = rightDist < PINCH_THRESHOLD && !isFistClosed;

        // Priority 1: DRAG has highest priority
        if (isFistClosed) {
            if (state == State.DRAGGING) {
                // Continuing drag - no event
                return MouseAction.NONE;
            } else {
                // Starting drag - transition from NONE or PINCHING
                state = State.DRAGGING;// Starting drag
                return MouseAction.DRAG_START;
            }
        }

        // If we were dragging and fist opened, end the drag
        if (state == State.DRAGGING) {
            state = State.NONE;// Clear drag state
        // Ensure proper state reset after gesture end
            return MouseAction.DRAG_END;
        }

        // Priority 2: PINCH gestures (LEFT_CLICK has priority over RIGHT_CLICK)
        if (leftPinch && rightPinch) {
            if (state != State.PINCHING) {
                state = State.PINCHING;
                return MouseAction.LEFT_CLICK; // LEFT_CLICK has priority
            }
            return MouseAction.NONE; // Debounce - already in PINCHING state
        }

        if (leftPinch) {
            if (state != State.PINCHING) {
                state = State.PINCHING;
                return MouseAction.LEFT_CLICK;
            }
            return MouseAction.NONE; // Debounce
        }

        if (rightPinch) {
            if (state != State.PINCHING) {
                state = State.PINCHING;
                return MouseAction.RIGHT_CLICK;
            }
            return MouseAction.NONE; // Debounce
        }

        // No gesture detected - reset PINCHING state if needed
        if (state == State.PINCHING) {
            state = State.NONE;
        // Ensure proper state reset after gesture end
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

