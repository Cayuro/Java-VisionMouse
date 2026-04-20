package com.vision.gesture;

import com.vision.detection.HandLandmarks;
import com.vision.detection.LandmarkIndex;

/**
 * GestureRecognizer - Identifica gestos de la mano basados en landmarks.
 * Optimizado para consistencia comparando puntas con articulaciones (TIP vs PIP).
 */
public class GestureRecognizer {
    public String detect(HandLandmarks landmarks) {
        if (landmarks == null || !landmarks.isValid()) {
            return "NONE";
        }

        float[][] points = landmarks.points();
        
        // Determinar si cada dedo está extendido (Punta por encima de la articulación PIP)
        // Recordar: En OpenCV, Y disminuye hacia arriba.
        boolean indexExtended = points[LandmarkIndex.INDEX_FINGER_TIP.value()][1] < points[LandmarkIndex.INDEX_FINGER_PIP.value()][1];
        boolean middleExtended = points[LandmarkIndex.MIDDLE_FINGER_TIP.value()][1] < points[LandmarkIndex.MIDDLE_FINGER_PIP.value()][1];
        boolean ringExtended = points[LandmarkIndex.RING_FINGER_TIP.value()][1] < points[LandmarkIndex.RING_FINGER_PIP.value()][1];
        boolean pinkyExtended = points[LandmarkIndex.PINKY_TIP.value()][1] < points[LandmarkIndex.PINKY_PIP.value()][1];

        // DRAG: Solo el índice extendido
        if (indexExtended && !middleExtended && !ringExtended && !pinkyExtended) {
            return "DRAG";
        }

        // CLICK: Puño cerrado (ningún dedo extendido)
        if (!indexExtended && !middleExtended && !ringExtended && !pinkyExtended) {
            return "CLICK";
        }

        // MOVE: Palma abierta (al menos índice y medio extendidos)
        if (indexExtended && middleExtended) {
            return "MOVE";
        }

        return "NONE";
    }
}
