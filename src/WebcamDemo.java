import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.videoio.VideoCapture;

public class WebcamDemo {

    public static void main(String[] args) {
        // 1) Cargamos la libreria nativa de OpenCV.
        // Core.NATIVE_LIBRARY_NAME suele resolver a algo como "opencv_java460".
        // Para que funcione, Java debe poder encontrar el archivo .so en java.library.path.
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        // 2) Abrimos la camara por indice. 0 = camara por defecto del equipo.
        VideoCapture camera = new VideoCapture(0);

        // Validamos si la camara se abrio correctamente para evitar errores en tiempo real.
        if (!camera.isOpened()) {
            System.err.println("No se pudo abrir la camara web (indice 0).");
            return;
        }

        // Mat es la estructura de OpenCV para almacenar imagenes (frames de video).
        Mat frame = new Mat();

        System.out.println("Camara iniciada. Presiona ESC o Q para salir.");

        // 3) Bucle principal: leemos frame por frame y lo mostramos en una ventana.
        while (true) {
            // read(frame) devuelve true si pudo capturar correctamente.
            boolean ok = camera.read(frame);
            if (!ok || frame.empty()) {
                System.err.println("No se pudo leer un frame de la camara.");
                break;
            }

            // 4) Mostramos el frame en una ventana usando HighGui.
            HighGui.imshow("Webcam en tiempo real - OpenCV + Java", frame);

            // waitKey(20) espera ~20 ms y captura tecla presionada.
            // 27 = ESC, 'q' = 113, 'Q' = 81.
            int key = HighGui.waitKey(20);
            if (key == 27 || key == 'q' || key == 'Q') {
                break;
            }
        }

        // 5) Liberamos recursos para cerrar limpio.
        camera.release();
        frame.release();
        HighGui.destroyAllWindows();

        System.out.println("Programa finalizado.");
    }
}
