package com.vision.video;

import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;

/**
 * DisplayWindow - Abstracción sobre HighGui de OpenCV.
 *
 * Centraliza la creación de ventanas, renderizado de frames y
 * lectura de teclas para mantener Main limpio de detalles de GUI.
 */
public class DisplayWindow {

    private final String windowName;

    public DisplayWindow(String windowName) {
        this.windowName = windowName;
    }

    /** Muestra el frame en la ventana. */
    public void show(Mat frame) {
        HighGui.imshow(windowName, frame);
    }

    /** Espera {@code delayMs} ms y devuelve el código de la tecla pulsada. */
    public int waitKey(int delayMs) {
        return HighGui.waitKey(delayMs);
    }

    /** Cierra todas las ventanas de OpenCV. */
    public void destroy() {
        HighGui.destroyAllWindows();
    }
}
