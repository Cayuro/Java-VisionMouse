package com.vision;

// IMPORTACIONES:
// CanvasFrame: Ventana de GUI optimizada para video (evita parpadeo visual).
import org.bytedeco.javacv.CanvasFrame;
// Frame: Objeto contenedor que transporta los datos de imagen capturados por el sensor.
import org.bytedeco.javacv.Frame;
// OpenCVFrameGrabber: Driver que conecta Java con la cámara física a través de OpenCV.
import org.bytedeco.javacv.OpenCVFrameGrabber;
// WindowConstants: Define reglas de cierre para liberar memoria al cerrar la ventana.
import javax.swing.WindowConstants;

public class InitializingOpenCV {

    public static void main(String[] args) {
        // QUÉ: Mensaje informativo inicial.
        // CÓMO: Imprime en consola estándar de Java.
        // POR QUÉ: Notifica al desarrollador que el proceso de inicio ha comenzado.
        System.out.println("Initializing real-time camera capture...");

        // QUÉ: Bloque Try-with-resources para el Grabber.
        // CÓMO: Instancia el capturador en el índice 0 (cámara por defecto).
        // POR QUÉ: Garantiza que la cámara se "apague" automáticamente al terminar.
        try (OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0)) {
            grabber.start(); // QUÉ: Activa el hardware. POR QUÉ: Inicia el flujo de datos.

            // QUÉ: Cálculo de corrección Gamma.
            // CÓMO: Obtiene el gamma del sistema vs el valor reportado por la cámara.
            // POR QUÉ: Ajusta el brillo para que la imagen no se vea oscura o quemada.
            double gamma = CanvasFrame.getDefaultGamma();
            double cameraGamma = Math.max(grabber.getGamma(), 1.0);

            // QUÉ: Inicialización de la ventana de visualización.
            // CÓMO: Crea un CanvasFrame con título y factor de gamma calculado.
            // POR QUÉ: Proporciona el lienzo donde se renderizará cada cuadro de video.
            CanvasFrame canvas = new CanvasFrame("Real-Time Camera - Java 21", gamma / cameraGamma);
            
            try {
                // QUÉ: Configuración de comportamiento de la ventana.
                // CÓMO: Establece visibilidad, redimensionado y cierre de recursos.
                // POR QUÉ: Permite que el usuario vea e interactúe con la interfaz.
                canvas.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                canvas.setVisible(true);
                canvas.setResizable(true);

                System.out.println("Capturing video... Close the window to exit.");

                // QUÉ: Bucle principal de renderizado (Game Loop).
                // CÓMO: Mientras la ventana sea visible, extrae y muestra frames.
                // POR QUÉ: Crea la secuencia de video continua a partir de fotos fijas.
                while (canvas.isVisible()) {
                    Frame frame = grabber.grab(); // QUÉ: Captura un frame del sensor.
                    
                    if (frame == null) {
                        System.err.println("Failed to capture frame from camera.");
                        break;
                    }

                    // QUÉ: Renderizado de imagen.
                    // CÓMO: Envía el objeto Frame al lienzo del canvas.
                    // POR QUÉ: Actualiza la imagen en pantalla para el usuario.
                    canvas.showImage(frame); 
                    
                    // QUÉ: Control de FPS (Frames per Second).
                    // CÓMO: Pausa el hilo actual por 33 milisegundos.
                    // POR QUÉ: Mantiene la fluidez a 30 FPS y evita sobrecalentar la CPU.
                    Thread.sleep(33); 
                }
            } finally {
                // QUÉ: Liberación de recursos de interfaz.
                // CÓMO: Destruye el objeto de la ventana y libera su memoria de video.
                // POR QUÉ: Evita fugas de memoria (memory leaks) en el sistema.
                canvas.dispose();
            }

            System.out.println("Exiting and releasing camera hardware...");
        } catch (Exception e) {
            // QUÉ: Manejo de errores de hardware y concurrencia.
            // CÓMO: Captura fallos de acceso a cámara o interrupciones de hilo.
            // POR QUÉ: Proporciona diagnóstico si el dispositivo está ocupado o falla.
            System.err.println("Error accessing camera: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Camera hardware released successfully.");
    }
}