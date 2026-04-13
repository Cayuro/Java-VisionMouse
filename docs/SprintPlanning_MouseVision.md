# 🏃 Sprint Planning — MouseVision MVP

**Proyecto:** MouseVision — Hand-Controlled Mouse
**Fecha:** 2026-03-31
**Equipo:** Juan Esteban, Nicolas, Jainer, Salvador
**Duración total:** 2 Sprints (2 semanas)
**Tecnología:** Java 21 · JavaCV/OpenCV · MediaPipe ONNX · Swing

---

## 📌 Definition of Done (DoD)

Una Historia de Usuario se considera **DONE** cuando cumple TODOS estos criterios:

| # | Criterio | Verificación |
|---|----------|--------------|
| 1 | El código compila sin errores | `mvn clean compile` exitoso |
| 2 | Todos los criterios de aceptación verificados | Pruebas manuales |
| 3 | No hay memory leaks de objetos Mat de OpenCV | Cada `Mat` se libera con `release()` |
| 4 | El código tiene Javadoc en clases y métodos públicos | Revisión visual |
| 5 | El código está en rama `feature/US-XX.X` con PR hacia `develop` | `git push` exitoso |
| 6 | La aplicación corre sin crash por al menos 2 minutos | Prueba de estabilidad |

---

## 👥 Capacidad del Equipo

| Parámetro | Valor |
|-----------|-------|
| Tamaño del equipo | 4 desarrolladores |
| Duración del sprint | 1 semana (5 días hábiles) |
| Horas disponibles por día por persona | 3-4 horas (aprendiendo + trabajando) |
| Horas totales por sprint (equipo) | ~60 horas |
| Factor de enfoque (aprendizaje) | 0.6 (60%) |
| **Capacidad real por sprint** | **~36 horas** |
| Scrum Master / PO | Rota cada sprint |

---

## 🏃 Sprint 1 — Video + Detección (Semana 1)

### Sprint Goal

> **Al final de este sprint, la aplicación captura video de la cámara en tiempo real, detecta una mano con MediaPipe ONNX, y muestra en consola las coordenadas del dedo índice (Point 8).**

### Sprint Backlog

| ID | Historia | SP | Responsable sugerido |
|----|---------|----|----------------------|
| US-01.1 | Inicialización de OpenCV | 3 | Juan Esteban |
| US-01.2 | Captura de Frame Único | 3 | Juan Esteban |
| US-01.3 | Loop de Video (Thread) | 5 | Nicolas |
| US-01.4 | Conversión Mat → Image | 3 | Jainer |
| US-01.5 | Interfaz de Visualización | 5 | Salvador |
| US-02.1 | Integración MediaPipe ONNX | 8 | Nicolas + Salvador |
| US-02.2 | Extracción del Point 8 | 3 | Jainer |
| US-02.3 | Normalización de Coordenadas | 2 | Jainer |
| US-02.4 | Detección de Presencia | 2 | Juan Esteban |
| | **TOTAL SPRINT 1** | **34 SP** | |

---

### Task Breakdown — Sprint 1

#### US-01.1: Inicialización de OpenCV (3 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-01.1.1 | Crear proyecto Maven con pom.xml (Java 21, javacv-platform 1.5.13) | 0.5h |
| T-01.1.2 | Configurar `maven-compiler-plugin` source/target 21 | 0.25h |
| T-01.1.3 | Configurar `exec-maven-plugin` para `mvn exec:java` | 0.25h |
| T-01.1.4 | Crear `MouseVisionApp.java` con main() que verifica carga de OpenCV | 0.5h |
| T-01.1.5 | Ejecutar `mvn clean compile` y verificar dependencias | 0.25h |
| | **Subtotal** | **1.75h** |

#### US-01.2: Captura de Frame Único (3 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-01.2.1 | Crear clase `CameraCapture.java` en paquete `core` | 0.5h |
| T-01.2.2 | Implementar `openCamera(int deviceId)` con VideoCapture | 0.5h |
| T-01.2.3 | Implementar `captureFrame()` que retorna Mat | 0.5h |
| T-01.2.4 | Manejo de errores (cámara no disponible) | 0.5h |
| T-01.2.5 | Configurar resolución 640×480 | 0.25h |
| T-01.2.6 | Implementar `release()` para liberar cámara | 0.25h |
| | **Subtotal** | **2.5h** |

#### US-01.3: Loop de Video (5 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-01.3.1 | Implementar Runnable en CameraCapture con loop `while(running)` | 0.5h |
| T-01.3.2 | Agregar flag `volatile boolean running` | 0.25h |
| T-01.3.3 | Implementar `start()` que lanza el thread | 0.5h |
| T-01.3.4 | Implementar `stop()` que detiene limpiamente | 0.5h |
| T-01.3.5 | Implementar `getLatestFrame()` thread-safe con AtomicReference | 0.75h |
| T-01.3.6 | Liberar Mat anterior en cada frame nuevo | 0.5h |
| T-01.3.7 | Verificar ≥ 25 FPS | 0.5h |
| | **Subtotal** | **3.5h** |

#### US-01.4: Conversión Mat → BufferedImage (3 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-01.4.1 | Crear clase `FrameConverter.java` en paquete `core` | 0.25h |
| T-01.4.2 | Implementar `matToBufferedImage(Mat)` con conversión BGR→RGB | 1h |
| T-01.4.3 | Manejar Mat vacío o null (retornar placeholder) | 0.5h |
| T-01.4.4 | Verificar colores correctos (no invertidos) | 0.25h |
| | **Subtotal** | **2h** |

#### US-01.5: Interfaz de Visualización (5 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-01.5.1 | Crear clase `DisplayWindow.java` en paquete `ui` | 0.5h |
| T-01.5.2 | Crear JFrame con título "MouseVision - Hand Control" | 0.25h |
| T-01.5.3 | Agregar JLabel como canvas para el video | 0.25h |
| T-01.5.4 | Implementar `updateFrame(BufferedImage)` con `SwingUtilities.invokeLater()` | 0.5h |
| T-01.5.5 | Centrar ventana en pantalla al inicio | 0.25h |
| T-01.5.6 | Implementar cierre limpio (ESC + X) con liberación de recursos | 0.5h |
| T-01.5.7 | Integrar con CameraCapture y FrameConverter en el loop principal | 0.75h |
| | **Subtotal** | **3h** |

#### US-02.1: Integración MediaPipe ONNX (8 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-02.1.1 | Descargar `palm_detection_mediapipe.onnx` desde OpenCV Zoo | 0.5h |
| T-02.1.2 | Descargar `handpose_estimation_mediapipe.onnx` | 0.5h |
| T-02.1.3 | Colocar modelos en `src/main/resources/models/` | 0.25h |
| T-02.1.4 | Crear clase `HandDetector.java` en paquete `detection` | 0.5h |
| T-02.1.5 | Implementar carga de modelos con `Dnn.readNetFromONNX()` | 0.75h |
| T-02.1.6 | Implementar preprocesamiento para palm detector | 1.5h |
| T-02.1.7 | Implementar post-procesamiento (decodificar anchors, NMS) | 2h |
| T-02.1.8 | Implementar recorte ROI de mano del frame | 0.75h |
| T-02.1.9 | Implementar preprocesamiento para hand landmark model | 0.75h |
| T-02.1.10 | Implementar extracción de los 21 landmarks | 1h |
| T-02.1.11 | Probar pipeline completo con frame de prueba | 1h |
| | **Subtotal** | **9.5h** |

#### US-02.2: Extracción del Point 8 (3 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-02.2.1 | Crear record `HandLandmarks.java` con los 21 puntos | 0.5h |
| T-02.2.2 | Crear enum `LandmarkIndex` con constantes (INDEX_FINGER_TIP=8, THUMB_TIP=4) | 0.5h |
| T-02.2.3 | Implementar `getIndexFingerTip()` retornando Point2D del Landmark 8 | 0.5h |
| T-02.2.4 | Implementar `getThumbTip()` para el Landmark 4 | 0.25h |
| | **Subtotal** | **1.75h** |

#### US-02.3: Normalización de Coordenadas (2 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-02.3.1 | Implementar `toPixelCoords(double normX, double normY, int w, int h)` | 0.5h |
| T-02.3.2 | Implementar clamping al rango [0.0, 1.0] | 0.25h |
| T-02.3.3 | Verificar con valores conocidos: (0.5, 0.5) → (320, 240) | 0.25h |
| | **Subtotal** | **1h** |

#### US-02.4: Detección de Presencia (2 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-02.4.1 | Implementar `isHandDetected()` basado en confianza del palm detector | 0.5h |
| T-02.4.2 | Flag `volatile` para acceso thread-safe | 0.25h |
| T-02.4.3 | Mostrar "No se detecta mano" en UI cuando false | 0.25h |
| T-02.4.4 | Guard de null safety en todo acceso a landmarks | 0.5h |
| | **Subtotal** | **1.5h** |

### Resumen de Horas — Sprint 1

| Historia | Horas Est. |
|----------|-----------|
| US-01.1 | 1.75h |
| US-01.2 | 2.5h |
| US-01.3 | 3.5h |
| US-01.4 | 2h |
| US-01.5 | 3h |
| US-02.1 | 9.5h |
| US-02.2 | 1.75h |
| US-02.3 | 1h |
| US-02.4 | 1.5h |
| **TOTAL** | **26.5h** |

### Criterios de Éxito — Sprint 1

- [ ] `mvn clean compile` ejecuta sin errores
- [ ] La ventana muestra el feed de la cámara a ≥ 25 FPS
- [ ] Los modelos ONNX se cargan correctamente
- [ ] Se detecta una mano y se extraen las coordenadas del Point 8
- [ ] La UI muestra "Mano detectada" / "No se detecta mano"
- [ ] La aplicación se cierra limpiamente sin errores

### Riesgos — Sprint 1

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| Modelos ONNX incompatibles con OpenCV DNN | Media | Alto | Probar con múltiples versiones; consultar OpenCV Zoo |
| Post-procesamiento de palm detection complejo | Alta | Alto | Consultar implementaciones Python de referencia y portear a Java |
| Memory leaks con objetos Mat | Media | Medio | Usar try-with-resources y `release()` explícito |
| Equipo sin experiencia en JavaCV | Alta | Alto | Dedicar el día 1 a explorar ejemplos de JavaCV antes de codificar |

---

## 🏃 Sprint 2 — Control + Gestos (Semana 2)

### Sprint Goal

> **Al final de este sprint, el cursor del mouse se mueve siguiendo el dedo índice con efecto espejo y suavizado EMA, y se puede hacer click izquierdo juntando pulgar e índice.**

### Sprint Backlog

| ID | Historia | SP | Responsable sugerido |
|----|---------|----|----------------------|
| US-03.1 | Robot Singleton | 2 | Juan Esteban |
| US-03.2 | Mapeo Lineal Simple | 3 | Nicolas |
| US-03.3 | Efecto Espejo | 2 | Nicolas |
| US-03.4 | Movimiento Físico | 3 | Salvador |
| US-04.1 | Filtro EMA | 3 | Jainer |
| US-04.2 | Reducción de Jitter | 3 | Jainer |
| US-04.3 | Gesto de Click (Pinch) | 5 | Salvador + Juan Esteban |
| US-04.4 | Ejecutar Click Izquierdo | 2 | Nicolas |
| | **TOTAL SPRINT 2** | **23 SP** | |

---

### Task Breakdown — Sprint 2

#### US-03.1: Robot Singleton (2 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-03.1.1 | Crear clase `MouseController.java` en paquete `control` | 0.5h |
| T-03.1.2 | Implementar Singleton con enum pattern | 0.5h |
| T-03.1.3 | Instanciar `java.awt.Robot` en constructor privado | 0.25h |
| T-03.1.4 | Capturar AWTException con mensaje descriptivo | 0.25h |
| T-03.1.5 | Agregar `getScreenSize()` con Toolkit | 0.25h |
| | **Subtotal** | **1.75h** |

#### US-03.2: Mapeo Lineal (3 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-03.2.1 | Implementar `mapToScreen(double camX, double camY, int camW, int camH)` | 0.75h |
| T-03.2.2 | Obtener resolución de pantalla con Toolkit dinámicamente | 0.25h |
| T-03.2.3 | Implementar clamping a los bordes de la pantalla | 0.5h |
| T-03.2.4 | Verificar mapeo con valores conocidos: centro cámara → centro pantalla | 0.5h |
| | **Subtotal** | **2h** |

#### US-03.3: Efecto Espejo (2 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-03.3.1 | Implementar inversión eje X: `mirroredX = camWidth - camX` | 0.25h |
| T-03.3.2 | Integrar inversión ANTES del mapeo lineal | 0.25h |
| T-03.3.3 | Verificar: mano derecha → cursor va a la derecha | 0.5h |
| T-03.3.4 | Invertir el frame de video en la UI (flip horizontal) | 0.5h |
| | **Subtotal** | **1.5h** |

#### US-03.4: Movimiento Físico (3 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-03.4.1 | Implementar `moveMouse(int screenX, int screenY)` con `robot.mouseMove()` | 0.5h |
| T-03.4.2 | Limitar frecuencia a máximo 30 llamadas/segundo | 0.5h |
| T-03.4.3 | No mover cursor si `isHandDetected() == false` | 0.25h |
| T-03.4.4 | Integrar en loop principal: frame → detect → map → move | 1h |
| T-03.4.5 | Verificar movimiento con mano real frente a cámara | 0.75h |
| | **Subtotal** | **3h** |

#### US-04.1: Filtro EMA (3 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-04.1.1 | Crear clase `EMAFilter.java` en paquete `util` | 0.5h |
| T-04.1.2 | Implementar fórmula: `St = α · Yt + (1 − α) · St-1` | 0.5h |
| T-04.1.3 | Primer valor = valor crudo (inicialización) | 0.25h |
| T-04.1.4 | Agregar método `reset()` | 0.25h |
| T-04.1.5 | Verificar: α=0.3, St-1=100, Yt=120 → 106.0 | 0.25h |
| | **Subtotal** | **1.75h** |

#### US-04.2: Reducción de Jitter (3 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-04.2.1 | Crear instancias de EMAFilter para X e Y en MouseController | 0.25h |
| T-04.2.2 | Aplicar filtro DESPUÉS del mapeo y ANTES del mouseMove() | 0.5h |
| T-04.2.3 | Reset de filtros cuando la mano desaparece y reaparece | 0.5h |
| T-04.2.4 | Comparar visualmente con/sin filtro | 0.75h |
| T-04.2.5 | Ajustar α default para mejor experiencia (default 0.3) | 0.25h |
| | **Subtotal** | **2.25h** |

#### US-04.3: Gesto de Click — Pinch (5 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-04.3.1 | Crear clase `GestureRecognizer.java` en paquete `gesture` | 0.5h |
| T-04.3.2 | Implementar `calculateDistance(Point2D p1, Point2D p2)` — euclidiana | 0.5h |
| T-04.3.3 | Implementar `isPinching(HandLandmarks)` — distancia L4-L8 vs umbral T | 0.75h |
| T-04.3.4 | Implementar state machine: IDLE → PINCHING → CLICKED → RELEASED | 1h |
| T-04.3.5 | Implementar debounce de 300ms entre clicks | 0.5h |
| T-04.3.6 | Umbral T configurable (default 0.05) | 0.25h |
| T-04.3.7 | Verificar: dedos juntos → evento, separados → no evento | 0.75h |
| | **Subtotal** | **4.25h** |

#### US-04.4: Ejecutar Click Izquierdo (2 SP)

| Tarea | Descripción | Horas Est. |
|-------|-------------|-----------|
| T-04.4.1 | Implementar `leftClick()` con `mousePress` + `mouseRelease` | 0.5h |
| T-04.4.2 | Delay de 50-100ms entre press y release | 0.25h |
| T-04.4.3 | Integrar GestureRecognizer con MouseController en el loop | 0.75h |
| T-04.4.4 | Verificar: pinch → click real en el OS | 0.75h |
| | **Subtotal** | **2.25h** |

### Resumen de Horas — Sprint 2

| Historia | Horas Est. |
|----------|-----------|
| US-03.1 | 1.75h |
| US-03.2 | 2h |
| US-03.3 | 1.5h |
| US-03.4 | 3h |
| US-04.1 | 1.75h |
| US-04.2 | 2.25h |
| US-04.3 | 4.25h |
| US-04.4 | 2.25h |
| **TOTAL** | **18.75h** |

### Criterios de Éxito — Sprint 2

- [ ] El cursor sigue el dedo índice en tiempo real con efecto espejo
- [ ] El movimiento es suave, sin temblor visible (α=0.3)
- [ ] Click izquierdo funciona al juntar pulgar e índice
- [ ] El click tiene debounce (no se repite mientras los dedos están juntos)
- [ ] El cursor no se mueve cuando no hay mano detectada
- [ ] Latencia mano → acción < 150ms

### Riesgos — Sprint 2

| Riesgo | Probabilidad | Impacto | Mitigación |
|--------|-------------|---------|------------|
| Cursor tiembla a pesar del EMA | Media | Medio | Experimentar con diferentes valores de α |
| Robot bloqueado por permisos del OS | Baja | Alto | Probar en día 1 del sprint antes de codificar |
| Falsos positivos en gesto de click | Media | Alto | Ajustar umbral T experimentalmente |
| Latencia alta por pipeline completo | Baja | Medio | Medir y optimizar; reducir resolución si es necesario |

---

## 📊 Resumen Total del Proyecto MVP

| Sprint | Story Points | Horas Est. | Historias | Duración |
|--------|-------------|-----------|-----------|---------|
| Sprint 1 — Video + Detección | 34 SP | 26.5h | 9 historias | Semana 1 |
| Sprint 2 — Control + Gestos | 23 SP | 18.75h | 8 historias | Semana 2 |
| **TOTAL MVP** | **57 SP** | **45.25h** | **17 historias** | **2 semanas** |

---

## 📅 Ceremonias Scrum

| Ceremonia | Duración | Cuándo |
|-----------|----------|--------|
| **Sprint Planning** | 1 hora | Inicio de cada sprint |
| **Daily Standup** | 10 min | Cada día |
| **Sprint Review** | 30 min | Final de sprint — demo del incremento |
| **Sprint Retrospective** | 20 min | Final de sprint — mejoras del proceso |

### Formato Daily Standup

```
📝 Daily Stand-up — [Fecha]
─────────────────────────────────
✅ Ayer completé:
  - [Tarea completada]

🎯 Hoy voy a trabajar en:
  - [Tarea planificada]

🚧 Bloqueos / dudas:
  - [Impedimentos]
```

---

## ✅ Checklist Antes de Empezar el Sprint 1

- [ ] Java 21 JDK instalado en todos los equipos (`java -version`)
- [ ] Maven instalado (`mvn -version`)
- [ ] Cámara web conectada y accesible
- [ ] Repositorio Git inicializado con ramas `main` y `develop`
- [ ] Modelos ONNX descargados desde OpenCV Zoo
- [ ] Roles del Sprint 1 definidos (quién es SM/PO esta semana)
