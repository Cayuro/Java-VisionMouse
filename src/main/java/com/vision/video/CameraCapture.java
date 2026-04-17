package com.vision.video;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * CameraCapture - Captura de video en un hilo separado.
 * 
 * ¿POR QUÉ ESTO?: Evita que la aplicación se "congele" si el procesamiento
 * de IA es lento, permitiendo que la cámara siga fluyendo.
 */
public class CameraCapture implements Runnable {
    private VideoCapture camera;
    private final AtomicReference<Mat> latestFrame = new AtomicReference<>(new Mat());
    private final Object frameLock = new Object(); // Para sincronizar el acceso a la memoria nativa
    private volatile boolean running = false;
    private double currentFps = 0;
    private int cameraId;

    public CameraCapture(int cameraId) {
        this.cameraId = cameraId;
        this.camera = new VideoCapture();
    }

    public synchronized void start() {
        if (running) return;
        
        if (!camera.open(cameraId)) {
            camera.release();
            throw new RuntimeException("No se pudo abrir la cámara: " + cameraId);
        }
        
        running = true;
        Thread thread = new Thread(this, "CameraCapture-Thread");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        if (camera.isOpened()) {
            camera.release();
        }
    }

    @Override
    public void run() {
        Mat frame = new Mat();
        long lastTime = System.nanoTime();
        int frameCount = 0;

        while (running) {
            if (camera.read(frame) && !frame.empty()) {
                synchronized (frameLock) {
                    // Actualizamos el frame de forma atómica y segura
                    Mat oldFrame = latestFrame.getAndSet(frame.clone());
                    if (oldFrame != null) oldFrame.release();
                }
                frameCount++;
            }


            // Cálculo de FPS cada segundo
            long currentTime = System.nanoTime();
            if (currentTime - lastTime >= 1_000_000_000L) {
                currentFps = frameCount;
                frameCount = 0;
                lastTime = currentTime;
            }

            // Pequeño descanso para no saturar la CPU (Optimizado)
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public Mat getLatestFrame() {
        synchronized (frameLock) {
            Mat frame = latestFrame.get();
            if (frame == null || frame.empty()) return null;
            // Clonamos DENTRO del bloque sincronizado para evitar que el hilo de captura
            // libere la memoria mientras estamos haciendo la copia.
            return frame.clone();
        }
    }

    public double getFps() {
        return currentFps;
    }

    public boolean isRunning() {
        return running;
    }
}
