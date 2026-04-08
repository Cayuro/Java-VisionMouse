# Ejecutar InitializingOpenCV

Este proyecto usa Maven y Java 21 para capturar y mostrar video en tiempo real desde la cámara del sistema usando JavaCV / OpenCV.

## Estructura clave

- Archivo principal: `src/main/java/com/vision/InitializingOpenCV.java`
- Archivo de configuración: `pom.xml`

## Requisitos

- Java 21 instalado
- Maven instalado
- Conexión a Internet la primera vez para descargar dependencias
- Cámara web disponible y no utilizada por otra aplicación

## Ejecución

1. Abre una terminal en la carpeta raíz del proyecto.
2. Ejecuta:

```bash
mvn compile exec:java
```

3. Se abrirá una ventana llamada `Cámara en Tiempo Real - Java 21`.
4. Verifica que el feed de cámara se muestre sin parpadeo.
5. Cierra la ventana para liberar la cámara correctamente.

## Comportamiento esperado

- La aplicación inicia la cámara por defecto (`ID 0`).
- El video se muestra en una ventana en tiempo real.
- Al cerrar la ventana, la cámara se libera automáticamente.

## Notas

- La primera ejecución puede tardar más porque Maven descarga las dependencias.
- Si la cámara no se abre, comprueba que ningún otro programa la esté usando.
- Si VS Code muestra mensajes de error aunque el terminal compile bien, recarga el servidor Java con:

    - `Ctrl+Shift+P`
    - `Java: Clean Java Language Server Workspace`
