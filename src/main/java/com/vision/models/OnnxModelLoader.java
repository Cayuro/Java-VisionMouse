package com.vision.models;

import ai.onnxruntime.*;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.Collections;

/**
 * OnnxModelLoader - Especialista en extraer los 21 puntos (Landmarks) de una mano.
 */
public class OnnxModelLoader implements AutoCloseable {
    private OrtEnvironment env;
    private OrtSession session;

    public OnnxModelLoader(String modelPath) throws OrtException {
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            throw new RuntimeException("Error: No se encontró el modelo en: " + modelFile.getAbsolutePath());
        }
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession(modelPath, new OrtSession.SessionOptions());
        System.out.println("✅ Especialista en Landmarks listo.");
    }

    /**
     * PRE-PROCESAMIENTO: Convierte el recorte de la mano a 224x224 RGB [0,1]
     */
    public float[] preprocess(Mat frame) {
        Mat resized = new Mat();
        Imgproc.resize(frame, resized, new Size(224, 224));
        Imgproc.cvtColor(resized, resized, Imgproc.COLOR_BGR2RGB);
        
        float[] floatValues = new float[1 * 224 * 224 * 3];
        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                double[] rgb = resized.get(y, x);
                // Formato NHWC: Rojo, Verde, Azul entrelazados
                floatValues[(y * 224 + x) * 3 + 0] = (float) (rgb[0] / 255.0);
                floatValues[(y * 224 + x) * 3 + 1] = (float) (rgb[1] / 255.0);
                floatValues[(y * 224 + x) * 3 + 2] = (float) (rgb[2] / 255.0);
            }
        }
        resized.release();
        return floatValues;
    }

    /**
     * Objeto para devolver puntos y confianza.
     */
    public record HandResult(float[][] landmarks, float score) {}

    /**
     * PREDICCIÓN: Retorna los 21 puntos clave y el nivel de confianza.
     */
    public HandResult predict(float[] imageData) throws OrtException {
        long[] shape = { 1, 224, 224, 3 };
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(imageData), shape)) {
            String inputName = session.getInputNames().iterator().next();
            try (OrtSession.Result results = session.run(Collections.singletonMap(inputName, inputTensor))) {
                
                // Salida 0: Landmarks (matriz plana [1][63] que manejamos como float[][])
                float[][] landmarks = (float[][]) results.get(0).getValue();
                
                // Salida 1: Hand Score (Nivel de confianza de que es una mano real)
                float score = 1.0f; // Por defecto si el modelo solo tiene una salida
                if (results.size() > 1) {
                    float[][] scoreFlag = (float[][]) results.get(1).getValue();
                    score = scoreFlag[0][0];
                }
                
                return new HandResult(landmarks, score);
            }
        }
    }

    @Override
    public void close() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}
