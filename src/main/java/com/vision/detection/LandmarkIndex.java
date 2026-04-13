package com.vision.detection;

/**
 * LandmarkIndex - Constantes con nombre para los 21 puntos de la mano.

 * ¿POR QUÉ?: En lugar de usar números mágicos como landmarks[12],
 * usamos LandmarkIndex.MIDDLE_FINGER_TIP.value() para que el código
 * sea legible y fácil de mantener.
 */
public enum LandmarkIndex {

    WRIST(0),

    THUMB_CMC(1),
    THUMB_MCP(2),
    THUMB_IP(3),
    THUMB_TIP(4),

    INDEX_FINGER_MCP(5),
    INDEX_FINGER_PIP(6),
    INDEX_FINGER_DIP(7),
    INDEX_FINGER_TIP(8),

    MIDDLE_FINGER_MCP(9),
    MIDDLE_FINGER_PIP(10),
    MIDDLE_FINGER_DIP(11),
    MIDDLE_FINGER_TIP(12),

    RING_FINGER_MCP(13),
    RING_FINGER_PIP(14),
    RING_FINGER_DIP(15),
    RING_FINGER_TIP(16),

    PINKY_MCP(17),
    PINKY_PIP(18),
    PINKY_DIP(19),
    PINKY_TIP(20);

    private final int value;

    LandmarkIndex(int value) {
        this.value = value;
    }

    /**
     * Retorna el índice numérico del landmark (0-20).
     */
    public int value() {
        return value;
    }
}