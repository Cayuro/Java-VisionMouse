package com.vision.gesture;

/**
 * MouseAction - Enumeración que representa las acciones de ratón
 * que pueden ser detectadas por el GestureRecognizer.
 */
public enum MouseAction {
    CLICK("Click izquierdo"),
    RIGHT_CLICK("Click derecho"),
    SCROLL("Scroll"),
    MOVE("Movimiento del cursor"),
    NONE("Sin acción");

    private final String description;

    MouseAction(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
