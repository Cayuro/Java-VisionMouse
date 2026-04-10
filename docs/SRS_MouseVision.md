# 📄 Software Requirements Specification (SRS)

**Proyecto:** MouseVision
**Versión:** 1.0.0 — MVP
**Fecha:** 2026-03-31
**Equipo:** Juan Esteban, Nicolas, Jainer, Salvador
**Estándar:** ISO 29148 / IEEE 830

---

## 1. Introducción

### 1.1 Propósito

Este documento define las especificaciones técnicas y funcionales del sistema **MouseVision** en su versión MVP. Sirve como contrato técnico entre el equipo de desarrollo para garantizar calidad, consistencia y trazabilidad del producto final.

### 1.2 Alcance

MouseVision es una aplicación de escritorio que usa visión por computadora para controlar el cursor del mouse mediante movimientos de la mano capturados por una webcam. El sistema detecta landmarks de la mano, los mapea a coordenadas de pantalla y permite mover el cursor y hacer click mediante gestos.

### 1.3 Definiciones

| Término | Definición |
|---------|-----------|
| Landmark | Punto clave de la mano detectado por MediaPipe (21 en total) |
| Point 8 | Punta del dedo índice (INDEX_FINGER_TIP) según convención MediaPipe |
| Point 4 | Punta del pulgar (THUMB_TIP) según convención MediaPipe |
| Pinch | Gesto de juntar pulgar e índice para hacer click |
| EMA | Exponential Moving Average — filtro para suavizar el movimiento |
| ONNX | Open Neural Network Exchange — formato de los modelos de IA |

---

## 2. Descripción General

### 2.1 Perspectiva del Producto

MouseVision es una aplicación standalone construida en Java 21. Usa OpenCV para captura de video y procesamiento de imagen, y modelos MediaPipe ONNX para detección de manos. El sistema interactúa con el OS via `java.awt.Robot` para controlar el mouse.

### 2.2 Funciones del Producto (MVP)

- Captura de video en tiempo real desde webcam
- Detección de mano y extracción de landmarks
- Mapeo de coordenadas de cámara a coordenadas de pantalla
- Control del cursor usando movimiento del dedo índice
- Reconocimiento de gesto pinch para click izquierdo
- Suavizado de movimiento mediante filtro EMA
- Feedback visual via ventana Swing con feed de cámara

### 2.3 Usuarios

Cualquier persona que desee controlar su computadora mediante gestos de la mano sin usar el mouse físico.

---

## 3. Requisitos Específicos

### 3.1 Requisitos Funcionales (RF)

| ID | Nombre | Descripción | Prioridad |
|----|--------|-------------|-----------|
| **RF-01** | Captura de Video | El sistema debe acceder a la webcam y capturar video en tiempo real a mínimo 25 FPS. | Alta |
| **RF-02** | Detección de Mano | El sistema debe detectar una mano y extraer los 21 landmarks usando modelos ONNX de MediaPipe. | Alta |
| **RF-03** | Extracción Point 8 | El sistema debe identificar y extraer las coordenadas del Landmark 8 (punta del índice) en cada frame. | Alta |
| **RF-04** | Mapeo de Coordenadas | El sistema debe mapear coordenadas de cámara (640×480) a resolución de pantalla con efecto espejo horizontal. | Alta |
| **RF-05** | Control del Cursor | El sistema debe mover el cursor del sistema usando `java.awt.Robot` basándose en las coordenadas mapeadas. | Alta |
| **RF-06** | Suavizado EMA | El sistema debe aplicar filtro EMA (α=0.3 por defecto) para eliminar el temblor del cursor. | Alta |
| **RF-07** | Gesto de Click | El sistema debe detectar el gesto pinch (distancia L4-L8 < umbral) y ejecutar click izquierdo. | Alta |
| **RF-08** | Visualización | El sistema debe mostrar el feed de la cámara en una ventana Swing en tiempo real. | Alta |
| **RF-09** | Detección de Presencia | El sistema debe detectar si hay o no una mano en el frame y no actuar cuando no hay mano. | Alta |

### 3.2 Requisitos No Funcionales (RNF)

| ID | Atributo | Descripción |
|----|----------|-------------|
| **RNF-01** | Latencia | El sistema debe procesar frames con latencia < 100ms (mano → movimiento de cursor). |
| **RNF-02** | Rendimiento | El sistema debe mantener ≥ 25 FPS estables durante la captura y procesamiento. |
| **RNF-03** | Estabilidad | La aplicación debe ejecutarse sin crashes por al menos 2 minutos continuos. |
| **RNF-04** | Portabilidad | El sistema debe ejecutarse en Windows y Linux con la misma base de código. |
| **RNF-05** | Seguridad | El acceso a la cámara y al control del mouse deben respetar los permisos del OS. |
| **RNF-06** | Usabilidad | La UI debe ser simple con una única ventana que muestre el video. |
| **RNF-07** | Mantenibilidad | El código debe ser modular, con una clase por responsabilidad y Javadoc en métodos públicos. |
| **RNF-08** | Recursos | No debe haber memory leaks de objetos nativos OpenCV (cada Mat debe liberarse con `release()`). |

---

## 4. Modelado Lógico

### 4.1 Flujo de Detección y Control

```
Inicio
  └→ Capturar Frame
       └→ ¿Mano detectada?
            ├→ NO → Mostrar video sin acción
            └→ SÍ → Extraer Landmark 8 (índice)
                      └→ Mapear coordenadas a pantalla
                           └→ Aplicar filtro EMA
                                └→ Mover cursor
                                     └→ ¿Gesto pinch?
                                          ├→ SÍ → Ejecutar click izquierdo
                                          └→ NO → Continuar loop
```

### 4.2 Pipeline de Procesamiento

```
Frame (Mat)
  └→ HandDetector
       ├→ Palm Detection ONNX → ROI de la mano
       └→ Hand Landmark ONNX → 21 puntos (x, y, z)
            └→ HandLandmarks
                 ├→ Point 8 → coordenadas índice
                 └→ Point 4 → coordenadas pulgar
                      ├→ MouseController
                      │    ├→ Espejo X
                      │    ├→ Mapeo a pantalla
                      │    ├→ EMAFilter (x, y)
                      │    └→ Robot.mouseMove()
                      └→ GestureRecognizer
                           └→ distancia(P4, P8) < T → Robot.mousePress()
```

---

## 5. Restricciones Técnicas

- **Java 21** es obligatorio (uso de Records, switch expressions)
- **JavaCV 1.5.13** como wrapper de OpenCV para Java
- Los modelos ONNX de MediaPipe deben descargarse desde OpenCV Model Zoo
- No se usa MediaPipe SDK directo (no hay versión para Java desktop)
- `java.awt.Robot` requiere permisos de accesibilidad en macOS

---

## 6. Criterios de Aceptación del MVP

El MVP se considera completo cuando:

1. La ventana muestra el feed de cámara a ≥25 FPS sin parpadeo
2. El cursor sigue el dedo índice en tiempo real con efecto espejo
3. El movimiento del cursor es suave (filtro EMA activo)
4. Juntar pulgar e índice ejecuta un click izquierdo real en el OS
5. La aplicación no crashea durante 2 minutos de uso continuo
6. El cursor no se mueve cuando no hay mano visible

---

## 7. Equipo y Roles

| Sprint | Scrum Master / PO | Developers |
|--------|-------------------|-----------|
| Sprint 1 (Semana 1) | Por definir al inicio | Juan Esteban, Nicolas, Jainer, Salvador |
| Sprint 2 (Semana 2) | Por definir al inicio | Juan Esteban, Nicolas, Jainer, Salvador |

> El rol de Scrum Master + Product Owner rota cada semana según decisión del equipo.
