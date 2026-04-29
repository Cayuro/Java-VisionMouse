package com.vision.gesture;

/**
 * MouseAction - Enumeración que representa las acciones de ratón
 * que pueden ser detectadas por el GestureRecognizer.
 *
 * Estados:
 *   LEFT_CLICK  - Click izquierdo (pinza: Índice + Pulgar)
 *   RIGHT_CLICK - Click derecho (pinza: Medio + Pulgar)
 *   DRAG_START  - Inicio de arrastre (puño cerrado)
 *   DRAG_END    - Fin de arrastre (puño abierto)
 *   NONE        - Sin acción detectada
 */
public enum MouseAction {
    LEFT_CLICK("Click izquierdo"),
    RIGHT_CLICK("Click derecho"),
    DRAG_START("Inicio de arrastre"),
    DRAG_END("Fin de arrastre"),
    NONE("Sin acción");

    private final String description;

    MouseAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
