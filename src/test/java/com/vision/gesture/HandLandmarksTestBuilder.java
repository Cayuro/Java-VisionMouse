package com.vision.gesture;

import com.vision.detection.HandLandmarks;

/**
 * HandLandmarksTestBuilder - Constructor de prueba robusto para HandLandmarks.
 */
class HandLandmarksTestBuilder {

    private final float[][] points;

    /**
     * Inicializa la mano con puntos dispersos para evitar colisiones accidentales (0,0).
     */
    HandLandmarksTestBuilder() {
        this.points = new float[21][3];
        // Inicialización dispersa: cada punto en una coordenada X distinta
        for (int i = 0; i < 21; i++) {
            points[i][0] = i * 0.04f; // Evita que thumbTip e indexTip nazcan en el mismo lugar
            points[i][1] = 0.1f; 
            points[i][2] = 0.0f;
        }
    }

    // --- SETTERS DE POSICIÓN ---

    HandLandmarksTestBuilder thumbTip(double x, double y) { return setPoint(4, x, y); }
    HandLandmarksTestBuilder thumbMcp(double x, double y) { return setPoint(2, x, y); }
    HandLandmarksTestBuilder indexTip(double x, double y) { return setPoint(8, x, y); }
    HandLandmarksTestBuilder indexMcp(double x, double y) { return setPoint(5, x, y); }
    HandLandmarksTestBuilder middleTip(double x, double y) { return setPoint(12, x, y); }
    HandLandmarksTestBuilder middleMcp(double x, double y) { return setPoint(9, x, y); }
    HandLandmarksTestBuilder ringTip(double x, double y) { return setPoint(16, x, y); }
    HandLandmarksTestBuilder ringMcp(double x, double y) { return setPoint(13, x, y); }
    HandLandmarksTestBuilder pinkyTip(double x, double y) { return setPoint(20, x, y); }
    HandLandmarksTestBuilder pinkyMcp(double x, double y) { return setPoint(17, x, y); }

    private HandLandmarksTestBuilder setPoint(int index, double x, double y) {
        points[index][0] = (float) x;
        points[index][1] = (float) y;
        return this;
    }

    /**
     * Configura una anatomía de mano estándar donde los dedos están separados 
     * y extendidos para evitar interferencias entre sí.
     */
    private void setupNaturalHandLayout() {
        // Nudillos (MCP) en una base horizontal Y=0.6
        setPoint(2, 0.2, 0.6);  // THUMB_MCP
        setPoint(5, 0.4, 0.6);  // INDEX_MCP
        setPoint(9, 0.5, 0.6);  // MIDDLE_MCP
        setPoint(13, 0.6, 0.6); // RING_MCP
        setPoint(17, 0.7, 0.6); // PINKY_MCP

        // Puntas (TIP) extendidas hacia arriba Y=0.2
        setPoint(4, 0.1, 0.2);  // THUMB_TIP
        setPoint(8, 0.4, 0.2);  // INDEX_TIP
        setPoint(12, 0.5, 0.2); // MIDDLE_TIP
        setPoint(16, 0.6, 0.2); // RING_TIP
        setPoint(20, 0.7, 0.2); // PINKY_TIP
    }

    /**
     * Construye una mano con dedos extendidos (TIP.y < MCP.y).
     */
    HandLandmarks buildWithOpenFist() {
        setupNaturalHandLayout();
        return new HandLandmarks(points);
    }

    /**
     * Construye una mano con puño cerrado (TIP.y > MCP.y).
     */
    HandLandmarks buildWithClosedFist() {
        setupNaturalHandLayout();
        // Bajamos las puntas de los 4 dedos principales por debajo de los nudillos
        setPoint(8, 0.4, 0.8);  // INDEX_TIP
        setPoint(12, 0.5, 0.8); // MIDDLE_TIP
        setPoint(16, 0.6, 0.8); // RING_TIP
        setPoint(20, 0.7, 0.8); // PINKY_TIP
        
        // El pulgar suele quedar a un lado en un puño, lo dejamos en su posición natural
        return new HandLandmarks(points);
    }

    /**
     * Crea un estado de click izquierdo (Pulgar e Índice juntos).
     */
    HandLandmarks buildWithLeftPinch() {
        setupNaturalHandLayout();
        setPoint(4, 0.4, 0.2);  // THUMB_TIP en (0.4, 0.2)
        setPoint(8, 0.4, 0.21); // INDEX_TIP casi en el mismo lugar
        return new HandLandmarks(points);
    }

    /**
     * Crea un estado de click derecho (Pulgar y Medio juntos).
     */
    HandLandmarks buildWithRightPinch() {
        setupNaturalHandLayout();
        setPoint(4, 0.5, 0.2);  // THUMB_TIP se mueve hacia el medio
        setPoint(12, 0.5, 0.21); // MIDDLE_TIP en el mismo lugar
        return new HandLandmarks(points);
    }

    HandLandmarks build() {
        return new HandLandmarks(points);
    }
}