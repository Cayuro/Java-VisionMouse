package com.vision.models;

import ai.onnxruntime.*;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import java.nio.FloatBuffer;
import java.util.*;

/**
 * PalmDetector - Encuentra la ubicación de la palma en la imagen.
 * 
 * LÓGICA DE ANCHORS (ANCLAS):
 * Este modelo usa 2016 anclas predefinidas. Cada ancla es una "caja de referencia".
 * El modelo no predice la posición absoluta, sino un DESPLAZAMIENTO (offset)
 * respecto a estas anclas.
 */
public class PalmDetector implements AutoCloseable {
    private OrtEnvironment env;
    private OrtSession session;

    private static final int INPUT_SIZE = 192;
    private static final float SCORE_THRESHOLD = 0.55f; // Un poco más permisivo para la otra mano

    // Estructura para guardar nuestras "boyas" o anclas
    private static class Anchor {
        float x_center, y_center, w, h;
        Anchor(float x, float y, float w, float h) {
            this.x_center = x; this.y_center = y; this.w = w; this.h = h;
        }
    }
    private List<Anchor> anchors;

    public PalmDetector(String modelPath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(modelPath, new OrtSession.SessionOptions());
        this.anchors = generateAnchors();
        System.out.println("✅ Detector listo con " + anchors.size() + " anclas generadas.");
    }

    /**
     * GENERACIÓN DE ANCHORS:
     * Crea las 2016 cajas de referencia siguiendo el estándar SSD de MediaPipe.
     */
    private List<Anchor> generateAnchors() {
        List<Anchor> anchors = new ArrayList<>();
        // MediaPipe Palm Detection Lite usa escalas y strides específicos
        // para cubrir diferentes tamaños de manos.
        int[] strides = {8, 16, 16, 16}; // Distancia entre anclas en cada nivel
        for (int stride : strides) {
            int grid_size = INPUT_SIZE / stride;
            for (int y = 0; y < grid_size; y++) {
                for (int x = 0; x < grid_size; x++) {
                    // Ponemos 2 anclas por cada celda de la rejilla
                    float x_center = (float) (x + 0.5) / grid_size;
                    float y_center = (float) (y + 0.5) / grid_size;
                    anchors.add(new Anchor(x_center, y_center, 1.0f, 1.0f));
                    anchors.add(new Anchor(x_center, y_center, 1.0f, 1.0f));
                    // Nota: En modelos más complejos se usan anclas de diferentes tamaños.
                }
            }
        }
        // Total para Lite: (24x24*2 + 12x12*2 + 12x12*2 + 12x12*2) = 1152 + 288 + 288 + 288 = 2016
        return anchors;
    }

    public float[] preprocess(Mat frame) {
         Mat resized = new Mat();
         Imgproc.resize(frame, resized, new Size(INPUT_SIZE, INPUT_SIZE));
         Imgproc.cvtColor(resized, resized, Imgproc.COLOR_BGR2RGB);

         float[] data = new float[INPUT_SIZE * INPUT_SIZE * 3];
         for (int i = 0; i < INPUT_SIZE; i++) {
             for (int j = 0; j < INPUT_SIZE; j++) {
                 double[] rgb = resized.get(i, j);
                 data[(i * INPUT_SIZE + j) * 3 + 0] = (float) ((rgb[0] / 127.5) - 1.0);
                 data[(i * INPUT_SIZE + j) * 3 + 1] = (float) ((rgb[1] / 127.5) - 1.0);
                 data[(i * INPUT_SIZE + j) * 3 + 2] = (float) ((rgb[2] / 127.5) - 1.0);
             }
         }
         resized.release();
         return data;
    }

    public Rect2d detect(Mat frame) throws OrtException {
        float[] input = preprocess(frame);
        try (OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(input), new long[]{1, INPUT_SIZE, INPUT_SIZE, 3})) {
            String inputName = session.getInputNames().iterator().next();
            try (OrtSession.Result results = session.run(Collections.singletonMap(inputName, tensor))) {
                float[][][] regressors = (float[][][]) results.get(0).getValue();
                float[][][] scores = (float[][][]) results.get(1).getValue();
                return decode(regressors[0], scores[0], frame.cols(), frame.rows());
            }
        }
    }

    private Rect2d decode(float[][] regressors, float[][] scores, int imgW, int imgH) {
        float maxScore = -1e10f;
        int bestId = -1;

        for (int i = 0; i < scores.length; i++) {
            float score = scores[i][0]; // Algunos modelos ONNX ya tienen el score procesado
            if (score > maxScore) {
                maxScore = score;
                bestId = i;
            }
        }

        if (bestId == -1 || maxScore < SCORE_THRESHOLD) return null;

        // APLICAMOS EL OFFSET AL ANCLA:
        // El modelo nos dice cuánto se movió el centro (dx, dy) y cuánto cambió el tamaño (dw, dh)
        Anchor anchor = anchors.get(bestId);
        float x_center = regressors[bestId][0] / INPUT_SIZE + anchor.x_center;
        float y_center = regressors[bestId][1] / INPUT_SIZE + anchor.y_center;
        float w = regressors[bestId][2] / INPUT_SIZE;
        float h = regressors[bestId][3] / INPUT_SIZE;

        // Convertimos a coordenadas de píxel para OpenCV
        double finalW = w * imgW;
        double finalH = h * imgH;
        double finalX = (x_center * imgW) - (finalW / 2);
        double finalY = (y_center * imgH) - (finalH / 2);

        return new Rect2d(finalX, finalY, finalW, finalH);
    }

    @Override
    public void close() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}
