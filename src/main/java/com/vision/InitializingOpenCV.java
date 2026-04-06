package com.vision; // Define el paquete donde reside la clase

// Importa las funciones principales de OpenCV (Matrices, constantes de versión, etc.)
import org.bytedeco.opencv.global.opencv_core;
// Importa el cargador de JavaCPP, encargado de extraer y cargar los binarios (.dll/.so)
import org.bytedeco.javacpp.Loader;

public class InitializingOpenCV {

    public static void main(String[] args) {
        
        System.out.println("Getting Started with OpenCV in Java 21");

        try {
            // LÍNEA CRÍTICA: Intenta vincular el código Java con la librería nativa de C++
            // Si los binarios no están en el PATH o faltan dependencias, aquí saltará el error
            Loader.load(opencv_core.class);

            // Accede a la constante global CV_VERSION dentro de la librería ya cargada
            System.out.println("Detected OpenCV version: " + opencv_core.CV_VERSION);
            
            // Si llegamos aquí, significa que la comunicación Java-C++ es exitosa
            System.out.println("OpenCV has loaded successfully!");

        } catch (Exception err) {
            
            System.out.println("Error loading OpenCV: " + err.getMessage());
            
            // Muestra la ruta del error para saber exactamente qué falló en la carga
            err.printStackTrace();
        }
    }
}
