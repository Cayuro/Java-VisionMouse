package com.vision.video;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

/**
 * CameraCapture - Abstracción sobre VideoCapture de OpenCV.
 *
 * Encapsula el acceso a la cámara para aislar al resto de la aplicación
 * de los detalles de bajo nivel de captura de video.
 */
public class CameraCapture implements AutoCloseable {

    private final VideoCapture camera;

    /**
     * Abre la cámara en el índice indicado (0 = cámara por defecto).
     */
    public CameraCapture(int deviceIndex) {
        this.camera = new VideoCapture(deviceIndex);
    }

    public boolean isOpened() {
        return camera.isOpened();
    }

    public boolean read(Mat frame) {
        return camera.read(frame);
    }

    public void release() {
        camera.release();
    }

    @Override
    public void close() {
        release();
    }
}
