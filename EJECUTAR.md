# Ejecutar InitializingOpenCV

Este proyecto usa Maven y Java 21 para cargar OpenCV a través de JavaCV.

## Estructura clave

- Archivo principal: `src/main/java/com/vision/InitializingOpenCV.java`
- Archivo de configuración: `pom.xml`

## Requisitos

- Java 21 instalado
- Maven instalado
- Conexión a Internet la primera vez para descargar dependencias

## Ejecución

1. Abre una terminal en la carpeta raíz del proyecto.
2. Ejecuta:

```bash
mvn compile exec:java
```

3. Si todo está bien, deberías ver una salida como:

```text
Detected OpenCV version: 4.9.0
OpenCV has loaded successfully!
[INFO] BUILD SUCCESS
```

## Notas

- La primera ejecución puede tardar más porque Maven descarga las dependencias.
- Si VS Code muestra mensajes de error aunque el terminal compile bien, recarga el servidor Java con:

    - `Ctrl+Shift+P`
    - `Java: Clean Java Language Server Workspace`

- Si quieres ejecutar explicitamente otra clase `main`, ajusta `com.vision.InitializingOpenCV` en el `pom.xml` o usa `-Dexec.mainClass=...`.
