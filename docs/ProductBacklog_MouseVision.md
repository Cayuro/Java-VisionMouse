# 📋 Product Backlog — MouseVision MVP

**Proyecto:** MouseVision — Hand-Controlled Mouse
**Versión:** MVP 1.0
**Fecha:** 2026-03-31
**Equipo:** Juan Esteban, Nicolas, Jainer, Salvador
**Metodología:** Scrum

---

## Resumen de Epics (MVP)

| Epic ID | Módulo | Descripción | Total SP |
|---------|--------|-------------|----------|
| EP-01 | Video | Captura de cámara, conversión de frames y ventana | **18 SP** |
| EP-02 | Detección | Modelos MediaPipe ONNX, extracción de landmarks | **15 SP** |
| EP-03 | Control | Robot, mapeo de coordenadas, espejo y movimiento | **10 SP** |
| EP-04 | Gestos | Filtro EMA, reducción de jitter, click | **13 SP** |
| | | **TOTAL MVP** | **57 SP** |

---

## Prioridades (MoSCoW)

| Prioridad | Significado |
|-----------|-------------|
| 🔴 Must Have | Imprescindible para el MVP |
| 🟡 Should Have | Fuera del MVP — versión futura |
| 🟢 Could Have | Fuera del MVP — versión futura |

---

## EP-01: Video (Webcam & UI)

> Objetivo: Ventana funcional mostrando feed de cámara en tiempo real.

---

### US-01.1 — Inicialización de OpenCV

| Campo | Detalle |
|-------|---------|
| **ID** | US-01.1 |
| **Epic** | EP-01 — Video |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 3 SP |
| **Dependencias** | Ninguna |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **configurar las dependencias de JavaCV/OpenCV en Maven con Java 21**,
> para que **el sistema pueda usar visión por computadora desde Java**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | El pom.xml está configurado | Se ejecuta `mvn clean compile` | El proyecto compila y descarga `javacv-platform:1.5.13` sin errores |
| 2 | Las dependencias están listas | Se ejecuta la clase principal | OpenCV se carga sin `UnsatisfiedLinkError` |
| 3 | Se usa Java 21 | Se revisa el compilador | `maven-compiler-plugin` tiene source/target en 21 |

---

### US-01.2 — Captura de Frame Único

| Campo | Detalle |
|-------|---------|
| **ID** | US-01.2 |
| **Epic** | EP-01 — Video |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 3 SP |
| **Dependencias** | US-01.1 |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **abrir la cámara y capturar un frame exitosamente**,
> para que **pueda verificar que la cámara funciona antes de implementar el loop**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | La cámara está conectada | Se invoca `captureFrame()` | Se obtiene un `Mat` no nulo con dimensiones > 0 |
| 2 | La cámara NO está conectada | Se invoca `captureFrame()` | Se lanza excepción controlada con mensaje descriptivo, sin crash |
| 3 | Se captura un frame | Se verifica el Mat | El frame tiene resolución 640×480 |

---

### US-01.3 — Loop de Video (Capture Thread)

| Campo | Detalle |
|-------|---------|
| **ID** | US-01.3 |
| **Epic** | EP-01 — Video |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 5 SP |
| **Dependencias** | US-01.2 |

**Historia de Usuario:**
> Como **usuario**,
> quiero **que la captura de video corra en un hilo separado de forma continua**,
> para que **la aplicación no se congele**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | La cámara está abierta | Se inicia el thread | Se capturan frames continuamente sin interrupción |
| 2 | El thread está corriendo | Se solicita detener | El thread se detiene limpiamente con flag `volatile running` |
| 3 | El loop está activo | Se mide el throughput | Se obtienen ≥ 25 FPS estables |

---

### US-01.4 — Conversión Mat → BufferedImage

| Campo | Detalle |
|-------|---------|
| **ID** | US-01.4 |
| **Epic** | EP-01 — Video |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 3 SP |
| **Dependencias** | US-01.2 |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **convertir objetos Mat de OpenCV a BufferedImage de Java**,
> para que **los frames puedan renderizarse en Swing**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Un Mat en formato BGR | Se invoca `matToBufferedImage(mat)` | Se obtiene un BufferedImage con las mismas dimensiones |
| 2 | Un Mat vacío o null | Se invoca el conversor | Se retorna null sin lanzar excepción |
| 3 | Se convierte a BufferedImage | Se verifica visualmente | Los colores son correctos (BGR→RGB) |

---

### US-01.5 — Interfaz de Visualización

| Campo | Detalle |
|-------|---------|
| **ID** | US-01.5 |
| **Epic** | EP-01 — Video |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 5 SP |
| **Dependencias** | US-01.3, US-01.4 |

**Historia de Usuario:**
> Como **usuario**,
> quiero **ver una ventana con el video de la cámara en tiempo real**,
> para que **pueda verificar que mis manos están siendo capturadas**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | La app está corriendo | Se abre la ventana | Se muestra un JFrame con el feed de cámara actualizado |
| 2 | El video está mostrándose | Se observa el stream | El video se actualiza a ≥ 25 FPS sin parpadeo |
| 3 | La ventana está abierta | El usuario presiona X o ESC | La app libera la cámara y cierra limpiamente |

---

## EP-02: Detección (MediaPipe ONNX)

> Objetivo: Detectar la mano y extraer las coordenadas del dedo índice.

---

### US-02.1 — Integración MediaPipe ONNX

| Campo | Detalle |
|-------|---------|
| **ID** | US-02.1 |
| **Epic** | EP-02 — Detección |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 8 SP |
| **Dependencias** | US-01.2 |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **cargar los modelos ONNX de MediaPipe con OpenCV DNN y obtener los 21 landmarks**,
> para que **el sistema pueda detectar manos en Java puro sin dependencias externas**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Los ONNX están en `resources/models/` | Se instancia HandDetector | Los modelos se cargan con `Dnn.readNetFromONNX()` sin errores |
| 2 | Frame con mano visible | Se invoca `detect(frame)` | Se retorna lista de 21 puntos con coordenadas normalizadas (0.0 a 1.0) |
| 3 | Frame sin mano | Se invoca `detect(frame)` | Se retorna lista vacía sin excepción |

---

### US-02.2 — Extracción del Point 8

| Campo | Detalle |
|-------|---------|
| **ID** | US-02.2 |
| **Epic** | EP-02 — Detección |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 3 SP |
| **Dependencias** | US-02.1 |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **obtener las coordenadas (x,y) de la punta del dedo índice (Landmark 8)**,
> para que **el sistema sepa exactamente dónde apunta el usuario**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | 21 landmarks detectados | Se invoca `getIndexFingerTip()` | Se retorna Point2D con coordenadas del Landmark 8 |
| 2 | Sin landmarks | Se invoca `getIndexFingerTip()` | Se retorna null o Optional.empty() sin excepción |

---

### US-02.3 — Normalización de Coordenadas

| Campo | Detalle |
|-------|---------|
| **ID** | US-02.3 |
| **Epic** | EP-02 — Detección |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 2 SP |
| **Dependencias** | US-02.2 |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **convertir coordenadas relativas MediaPipe (0.0 a 1.0) a píxeles absolutos**,
> para que **sean utilizables en el mapeo a pantalla**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Point 8 en (0.5, 0.5) y resolución 640×480 | Se invoca `toPixelCoords(640, 480)` | Se retorna (320, 240) — centro del frame |
| 2 | Coordenadas fuera de rango (< 0 o > 1) | Se invoca la conversión | Se aplica clamping al rango [0, 1] |

---

### US-02.4 — Detección de Presencia

| Campo | Detalle |
|-------|---------|
| **ID** | US-02.4 |
| **Epic** | EP-02 — Detección |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 2 SP |
| **Dependencias** | US-02.1 |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **un flag booleano que indique si hay una mano en el frame**,
> para que **el sistema no actúe ni genere NullPointerException cuando no hay mano**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Mano visible en el frame | Se procesa el frame | `isHandDetected()` retorna true |
| 2 | Sin mano en el frame | Se procesa el frame | `isHandDetected()` retorna false y el cursor no se mueve |
| 3 | isHandDetected() = false | Se intenta obtener landmarks | No se lanza NullPointerException |

---

## EP-03: Control del Sistema (Robot & OS)

> Objetivo: Mover el cursor siguiendo el dedo índice con efecto espejo.

---

### US-03.1 — Robot Singleton

| Campo | Detalle |
|-------|---------|
| **ID** | US-03.1 |
| **Epic** | EP-03 — Control |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 2 SP |
| **Dependencias** | Ninguna |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **una clase Singleton con java.awt.Robot**,
> para que **haya un único punto de control para todas las acciones del mouse**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | La clase está implementada | Se llama `getInstance()` dos veces | Ambas retornan la misma referencia |
| 2 | El OS permite Robot | Se instancia el Singleton | Robot se crea sin errores |
| 3 | El OS bloquea Robot | Se intenta instanciar | Se captura AWTException con mensaje descriptivo |

---

### US-03.2 — Mapeo Lineal Simple

| Campo | Detalle |
|-------|---------|
| **ID** | US-03.2 |
| **Epic** | EP-03 — Control |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 3 SP |
| **Dependencias** | US-02.3, US-03.1 |

**Historia de Usuario:**
> Como **usuario**,
> quiero **que las coordenadas de mi dedo en cámara se traduzcan a coordenadas de pantalla**,
> para que **mi dedo en el centro del frame mueva el cursor al centro de la pantalla**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Coord cámara (320, 240) en frame 640×480, pantalla 1920×1080 | Se invoca `mapToScreen()` | Se retorna (960, 540) — centro de pantalla |
| 2 | Pantalla de cualquier resolución | Se obtiene resolución con Toolkit | El mapeo se adapta automáticamente |

---

### US-03.3 — Efecto Espejo

| Campo | Detalle |
|-------|---------|
| **ID** | US-03.3 |
| **Epic** | EP-03 — Control |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 2 SP |
| **Dependencias** | US-03.2 |

**Historia de Usuario:**
> Como **usuario**,
> quiero **que al mover mi mano a la derecha el cursor vaya a la derecha**,
> para que **la interacción sea intuitiva como verse en un espejo**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Usuario mueve mano a la derecha | La cámara captura X incrementando | El cursor también va a la derecha |
| 2 | Coord original X=100 en frame 640px | Se aplica inversión | Coordenada se transforma a 640-100=540 antes del mapeo |

---

### US-03.4 — Movimiento Físico

| Campo | Detalle |
|-------|---------|
| **ID** | US-03.4 |
| **Epic** | EP-03 — Control |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 3 SP |
| **Dependencias** | US-03.2, US-03.3 |

**Historia de Usuario:**
> Como **usuario**,
> quiero **que el cursor real de mi PC se mueva siguiendo mi dedo índice**,
> para que **pueda navegar por el escritorio usando solo mi mano**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Coordenadas mapeadas (screenX, screenY) | Se invoca `robot.mouseMove(x, y)` | El cursor del sistema se mueve a esa posición |
| 2 | Sin mano detectada | El thread continúa | El cursor permanece en su última posición, no va a (0,0) |

---

## EP-04: Gestos y Suavizado

> Objetivo: Cursor suave sin temblor y click izquierdo con gesto pinch.

---

### US-04.1 — Filtro EMA

| Campo | Detalle |
|-------|---------|
| **ID** | US-04.1 |
| **Epic** | EP-04 — Gestos |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 3 SP |
| **Dependencias** | Ninguna |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **implementar el filtro EMA: `St = α · Yt + (1 − α) · St−1`**,
> para que **las coordenadas del cursor se suavicen eliminando el temblor natural de la mano**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | α=0.3, St-1=100, Yt=120 | Se invoca `apply(120)` | Retorna 0.3×120 + 0.7×100 = 106.0 |
| 2 | Primer valor (no hay St-1) | Se invoca `apply(Yt)` | Retorna Yt como valor inicial |

---

### US-04.2 — Reducción de Jitter

| Campo | Detalle |
|-------|---------|
| **ID** | US-04.2 |
| **Epic** | EP-04 — Gestos |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 3 SP |
| **Dependencias** | US-04.1, US-03.4 |

**Historia de Usuario:**
> Como **usuario**,
> quiero **que el cursor se mueva de forma suave y estable, sin temblar**,
> para que **pueda apuntar con precisión a elementos de la interfaz**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Filtro EMA activo con α=0.3 | Usuario mantiene la mano quieta | Cursor estable con desviación < 3px |
| 2 | Comparando con/sin filtro | Se observa el comportamiento | Con filtro: suave. Sin filtro: tiembla notablemente |

---

### US-04.3 — Gesto de Click (Pinch)

| Campo | Detalle |
|-------|---------|
| **ID** | US-04.3 |
| **Epic** | EP-04 — Gestos |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 5 SP |
| **Dependencias** | US-02.1, US-02.2 |

**Historia de Usuario:**
> Como **desarrollador**,
> quiero **calcular la distancia entre Landmark 4 y 8, y disparar click cuando sea menor al umbral T**,
> para que **el usuario pueda hacer click juntando pulgar e índice**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | L4 en (0.3, 0.5) y L8 en (0.32, 0.52) | Se calcula distancia | d ≈ 0.028 — menor que umbral T, se dispara click |
| 2 | Distancia L4-L8 > umbral T | Se evalúa el gesto | No se dispara click |
| 3 | Dedos juntos y luego separados | Se monitorea | Se dispara UN solo click, no múltiples |

---

### US-04.4 — Ejecutar Click Izquierdo

| Campo | Detalle |
|-------|---------|
| **ID** | US-04.4 |
| **Epic** | EP-04 — Gestos |
| **Prioridad** | 🔴 Must Have |
| **Story Points** | 2 SP |
| **Dependencias** | US-04.3, US-03.1 |

**Historia de Usuario:**
> Como **usuario**,
> quiero **que al juntar pulgar e índice se ejecute un click izquierdo real**,
> para que **pueda interactuar con elementos del escritorio sin tocar el mouse**.

**Criterios de Aceptación:**

| # | Dado | Cuando | Entonces |
|---|------|--------|----------|
| 1 | Gesto pinch detectado | Se dispara el evento | Se ejecuta `mousePress` + `mouseRelease` del botón izquierdo |
| 2 | Pinch sostenido > 1 segundo | Se monitorea | Solo se ejecuta UN click, no repetidos |
| 3 | Click ejecutado | Hay botón bajo el cursor | El botón del OS responde al click |

---

## Resumen del Backlog MVP

| ID | Historia | SP | Prioridad |
|----|----------|----|-----------|
| US-01.1 | Inicialización OpenCV | 3 | 🔴 Must |
| US-01.2 | Captura Frame Único | 3 | 🔴 Must |
| US-01.3 | Loop de Video | 5 | 🔴 Must |
| US-01.4 | Conversión Mat→Image | 3 | 🔴 Must |
| US-01.5 | Interfaz de Visualización | 5 | 🔴 Must |
| US-02.1 | Integración MediaPipe ONNX | 8 | 🔴 Must |
| US-02.2 | Extracción Point 8 | 3 | 🔴 Must |
| US-02.3 | Normalización Coordenadas | 2 | 🔴 Must |
| US-02.4 | Detección de Presencia | 2 | 🔴 Must |
| US-03.1 | Robot Singleton | 2 | 🔴 Must |
| US-03.2 | Mapeo Lineal | 3 | 🔴 Must |
| US-03.3 | Efecto Espejo | 2 | 🔴 Must |
| US-03.4 | Movimiento Físico | 3 | 🔴 Must |
| US-04.1 | Filtro EMA | 3 | 🔴 Must |
| US-04.2 | Reducción de Jitter | 3 | 🔴 Must |
| US-04.3 | Gesto de Click | 5 | 🔴 Must |
| US-04.4 | Click Izquierdo | 2 | 🔴 Must |
| | **TOTAL** | **57 SP** | |

---

## Backlog Futuro (Post-MVP)

| ID | Historia | SP | Prioridad |
|----|----------|----|-----------|
| US-04.5 | Overlay de Landmarks | 5 | 🟡 Should |
| US-05.1 | Slider de Sensibilidad | 3 | 🟡 Should |
| US-05.2 | Ajuste de Suavizado | 3 | 🟡 Should |
| US-05.3 | Dwell Time | 5 | 🟢 Could |
| US-05.4 | Persistencia de Config | 3 | 🟡 Should |
