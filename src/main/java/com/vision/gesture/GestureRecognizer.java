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
    private boolean isLeftPinched = false;  // true si detectamos pinza izquierda activa
    private boolean isRightPinched = false; // true si detectamos pinza derecha activa
    private boolean isFistClosed = false;   // true si la mano está en puño

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
            // Si los landmarks no son válidos, resetea estados
            resetStates();
            return MouseAction.NONE;
        }

        // ===== PASO 1: DETECTAR PUÑO (PRIORIDAD MÁXIMA) =====
        boolean currentFistClosed = isFistClosed(landmarks);

        if (currentFistClosed && !isFistClosed) {
            // Transición: mano abierta → puño cerrado
            isFistClosed = true;
            // Resetea estados de clicks para evitar interferencia
            resetClickStates();
            return MouseAction.DRAG_START;
        } else if (!currentFistClosed && isFistClosed) {
            // Transición: puño cerrado → mano abierta
            isFistClosed = false;
            resetClickStates();
            return MouseAction.DRAG_END;
        }

        // ===== PASO 2: SI PUÑO CERRADO, IGNORA CLICKS =====
        if (isFistClosed) {
            return MouseAction.NONE;
        }

        // ===== PASO 3: DETECTAR CLICKS (SOLO SI PUÑO ABIERTO) =====

        // Click Izquierdo: Índice + Pulgar
        double leftDistance = calculateDistance(landmarks.thumbTip(), landmarks.indexTip());
        if (leftDistance < PINCH_THRESHOLD) {
            if (!isLeftPinched) {
                // Dispara click por primera vez
                isLeftPinched = true;
                return MouseAction.LEFT_CLICK;
            }
            // Si ya estaba pinchado, no dispara de nuevo (debounce)
        } else {
            // La pinza se liberó
            isLeftPinched = false;
        }

        // Click Derecho: Medio + Pulgar
        double rightDistance = calculateDistance(landmarks.thumbTip(), landmarks.middleTip());
        if (rightDistance < PINCH_THRESHOLD) {
            if (!isRightPinched) {
                // Dispara click por primera vez
                isRightPinched = true;
                return MouseAction.RIGHT_CLICK;
            }
            // Si ya estaba pinchado, no dispara de nuevo (debounce)
        } else {
            // La pinza se liberó
            isRightPinched = false;
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

    /**
     * Resetea todos los estados de debounce.
     * Se utiliza cuando los landmarks no son válidos o se detectan cambios abruptos.
     */
    private void resetStates() {
        resetClickStates();
        isFistClosed = false;
    }

    /**
     * Resetea solo los estados de clicks (no toca el estado del puño).
     * Se utiliza al entrar/salir del modo puño.
     */
    private void resetClickStates() {
        isLeftPinched = false;
        isRightPinched = false;
    }
}