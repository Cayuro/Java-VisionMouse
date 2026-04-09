package com.vision.models;

import ai.onnxruntime.*;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.Collections;

/**
 * OnnxModelLoader - Enfocado en el Criterio de Aceptación 1:
 * Cargar el modelo sin errores y verificar su estado.
 */
public class OnnxModelLoader implements AutoCloseable {
    private OrtEnvironment env;
    private OrtSession session;

    public OnnxModelLoader(String modelPath) throws OrtException {
        // Validación AC1: Verificar si el archivo existe
        File modelFile = new File(modelPath);
        if (!modelFile.exists()) {
            throw new RuntimeException("Error AC1: No se encontró el archivo del modelo en: " + modelFile.getAbsolutePath());
        }

        // 1. Inicializar el entorno de ONNX
        this.env = OrtEnvironment.getEnvironment();
        
        // 2. Cargar el modelo en la sesión
        // Si esta línea falla, lanzará una OrtException, cumpliendo con la detección de errores de carga.
        this.session = env.createSession(modelPath, new OrtSession.SessionOptions());
        
        System.out.println("✅ AC1: Modelo cargado exitosamente.");
        System.out.println("Nombres de entrada: " + session.getInputNames());
        System.out.println("Nombres de salida: " + session.getOutputNames());
    }

    /**
     * Reservado para Criterio de Aceptación 2 y 3.
     */
    public void runInference(float[] imageData) throws OrtException {
        long[] shape = { 1, 3, 224, 224 };
        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(imageData), shape)) {
            // Se usa el nombre de entrada detectado dinámicamente o uno por defecto
            String inputName = session.getInputNames().iterator().next();
            try (OrtSession.Result results = session.run(Collections.singletonMap(inputName, inputTensor))) {
                OnnxValue outputValue = results.get(0);
                float[][] landmarks = (float[][]) outputValue.getValue();
                System.out.println("Inferencia realizada. Puntos: " + landmarks.length);
            }
        }
    }

    @Override
    public void close() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}
