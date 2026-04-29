package com.vision.gesture;

import com.vision.detection.HandLandmarks;

/**
 * HandLandmarksTestBuilder - Constructor de prueba para HandLandmarks.
 *
 * Facilita la creación de landmarks con configuraciones específicas
 * para las pruebas unitarias del GestureRecognizer.
 *
 * Uso:
 *   HandLandmarks landmarks = new HandLandmarksTestBuilder()
 *       .thumbTip(0.5, 0.5)
 *       .indexTip(0.51, 0.50)
 *       .buildWithOpenFist();
 */
class HandLandmarksTestBuilder {

    private float[][] points;

    /**
     * Constructor: Inicializa array de 21 puntos con valores por defecto.
     * Todos los puntos comienzan en (0.5, 0.5, 0.5).
     */
    HandLandmarksTestBuilder() {
        this.points = new float[21][3];
        // Inicializa todos los puntos en posición neutra (0.5, 0.5, 0.5)
        for (int i = 0; i < 21; i++) {
            points[i][0] = 0.5f; // x
            points[i][1] = 0.5f; // y
            points[i][2] = 0.5f; // z
        }
    }

    /**
     * Configura la posición de THUMB_TIP (punto 4).
     */
    HandLandmarksTestBuilder thumbTip(double x, double y) {
        setPoint(4, x, y);
        return this;
    }

    /**
     * Configura la posición de THUMB_MCP (punto 2).
     */
    HandLandmarksTestBuilder thumbMcp(double x, double y) {
        setPoint(2, x, y);
        return this;
    }

    /**
     * Configura la posición de INDEX_FINGER_TIP (punto 8).
     */
    HandLandmarksTestBuilder indexTip(double x, double y) {
        setPoint(8, x, y);
        return this;
    }

    /**
     * Configura la posición de INDEX_FINGER_MCP (punto 5).
     */
    HandLandmarksTestBuilder indexMcp(double x, double y) {
        setPoint(5, x, y);
        return this;
    }

    /**
     * Configura la posición de MIDDLE_FINGER_TIP (punto 12).
     */
    HandLandmarksTestBuilder middleTip(double x, double y) {
        setPoint(12, x, y);
        return this;
    }

    /**
     * Configura la posición de MIDDLE_FINGER_MCP (punto 9).
     */
    HandLandmarksTestBuilder middleMcp(double x, double y) {
        setPoint(9, x, y);
        return this;
    }

    /**
     * Configura la posición de RING_FINGER_TIP (punto 16).
     */
    HandLandmarksTestBuilder ringTip(double x, double y) {
        setPoint(16, x, y);
        return this;
    }

    /**
     * Configura la posición de RING_FINGER_MCP (punto 13).
     */
    HandLandmarksTestBuilder ringMcp(double x, double y) {
        setPoint(13, x, y);
        return this;
    }

    /**
     * Configura la posición de PINKY_TIP (punto 20).
     */
    HandLandmarksTestBuilder pinkyTip(double x, double y) {
        setPoint(20, x, y);
        return this;
    }

    /**
     * Configura la posición de PINKY_MCP (punto 17).
     */
    HandLandmarksTestBuilder pinkyMcp(double x, double y) {
        setPoint(17, x, y);
        return this;
    }

    /**
     * Configura un punto individual en el array.
     *
     * @param index El índice del landmark (0-20)
     * @param x Coordenada X normalizada (0.0-1.0)
     * @param y Coordenada Y normalizada (0.0-1.0)
     */
    private void setPoint(int index, double x, double y) {
        points[index][0] = (float) x;
        points[index][1] = (float) y;
        // z permanece como 0.5 (no usado en las pruebas)
    }

    /**
     * Construye un HandLandmarks con la mano ABIERTA.
     *
     * Configuración: Las puntas de los dedos están POR ENCIMA de los nudillos.
     * - TIP.y < MCP.y
     *
     * @return HandLandmarks configurado con mano abierta
     */
    HandLandmarks buildWithOpenFist() {
        // Asegura que todas las puntas están por encima de los nudillos
        // (Y menor significa más arriba en coordenadas normalizadas)

        // INDEX
        setPoint(5, 0.5, 0.6);   // INDEX_MCP
        setPoint(8, 0.5, 0.3);   // INDEX_TIP (arriba del MCP)

        // MIDDLE
        setPoint(9, 0.5, 0.6);   // MIDDLE_MCP
        setPoint(12, 0.5, 0.3);  // MIDDLE_TIP (arriba del MCP)

        // RING
        setPoint(13, 0.5, 0.6);  // RING_MCP
        setPoint(16, 0.5, 0.3);  // RING_TIP (arriba del MCP)

        // PINKY
        setPoint(17, 0.5, 0.6);  // PINKY_MCP
        setPoint(20, 0.5, 0.3);  // PINKY_TIP (arriba del MCP)

        // THUMB
        setPoint(2, 0.5, 0.5);   // THUMB_MCP
        setPoint(4, 0.5, 0.3);   // THUMB_TIP

        return new HandLandmarks(points);
    }

    /**
     * Construye un HandLandmarks con la mano CERRADA (puño).
     *
     * Configuración: Las puntas de los dedos están POR DEBAJO de los nudillos.
     * - TIP.y > MCP.y
     *
     * @return HandLandmarks configurado con puño cerrado
     */
    HandLandmarks buildWithClosedFist() {
        // INDEX
        setPoint(5, 0.5, 0.3);   // INDEX_MCP
        setPoint(8, 0.5, 0.6);   // INDEX_TIP (debajo del MCP)

        // MIDDLE
        setPoint(9, 0.5, 0.3);   // MIDDLE_MCP
        setPoint(12, 0.5, 0.6);  // MIDDLE_TIP (debajo del MCP)

        // RING
        setPoint(13, 0.5, 0.3);  // RING_MCP
        setPoint(16, 0.5, 0.6);  // RING_TIP (debajo del MCP)

        // PINKY
        setPoint(17, 0.5, 0.3);  // PINKY_MCP
        setPoint(20, 0.5, 0.6);  // PINKY_TIP (debajo del MCP)

        // THUMB
        setPoint(2, 0.5, 0.5);   // THUMB_MCP
        setPoint(4, 0.5, 0.3);   // THUMB_TIP

        return new HandLandmarks(points);
    }

    /**
     * Construye un HandLandmarks con la configuración actual del array.
     *
     * @return HandLandmarks con los puntos configurados
     */
    HandLandmarks build() {
        return new HandLandmarks(points);
    }
}
