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
        
        float[] floatValues = new float[1 * 3 * 224 * 224];
        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                double[] rgb = resized.get(y, x);
                // Formato NCHW: Rojo continuo, luego Verde, luego Azul
                floatValues[0 * 224 * 224 + y * 224 + x] = (float) (rgb[0] / 255.0);
                floatValues[1 * 224 * 224 + y * 224 + x] = (float) (rgb[1] / 255.0);
                floatValues[2 * 224 * 224 + y * 224 + x] = (float) (rgb[2] / 255.0);
            }
        }
        resized.release();
        return floatValues;
    }

    /**
     * PREDICCIÓN: Retorna los 21 puntos clave (AC 2)
     */
    public float[][] predict(float[] imageData) throws OrtException {
        long[] shape = { 1, 3, 224, 224 };
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(imageData), shape)) {
            String inputName = session.getInputNames().iterator().next();
            try (OrtSession.Result results = session.run(Collections.singletonMap(inputName, inputTensor))) {
                
                // Obtenemos los 63 valores (21 puntos * 3 ejes)
                float[][] output = ((float[][][]) results.get(0).getValue())[0];
                return output;
            }
        }
    }

    @Override
    public void close() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}
