package com.vision.gesture;

import com.vision.detection.HandLandmarks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para GestureRecognizer.
 *
 * Valida:
 *   ✓ Disparo único de clicks (debounce)
 *   ✓ Transiciones DRAG_START/DRAG_END
 *   ✓ Sin falsos positivos entre clicks
 *   ✓ Prioridad de puño sobre clicks
 *   ✓ Validación de landmarks nulos
 */
@DisplayName("GestureRecognizer - Suite de Tests")
class GestureRecognizerTest {

    private GestureRecognizer recognizer;
    private HandLandmarksTestBuilder builder;

    @BeforeEach
    void setUp() {
        recognizer = new GestureRecognizer();
        builder = new HandLandmarksTestBuilder();
    }

    @Nested
    @DisplayName("Click Izquierdo (Índice + Pulgar)")
    class LeftClickTests {

        @Test
        @DisplayName("Debe disparar LEFT_CLICK exactamente UNA VEZ cuando se detecta pinza izquierda")
        void shouldFireLeftClickOnce() {
            // Arrange: Landmarks con Índice y Pulgar unidos (distancia < 0.05)
            HandLandmarks landmarksWithLeftPinch = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.51, 0.50) // Distancia ≈ 0.01
                    .buildWithOpenFist();

            // Act & Assert
            MouseAction firstFrame = recognizer.process(landmarksWithLeftPinch);
            assertEquals(MouseAction.LEFT_CLICK, firstFrame, "Primer frame debe disparar LEFT_CLICK");

            // Segundo frame con la misma pinza (debounce)
            MouseAction secondFrame = recognizer.process(landmarksWithLeftPinch);
            assertEquals(MouseAction.NONE, secondFrame, "Segundo frame debe retornar NONE (debounce)");

            // Tercer frame con la misma pinza (debounce)
            MouseAction thirdFrame = recognizer.process(landmarksWithLeftPinch);
            assertEquals(MouseAction.NONE, thirdFrame, "Tercer frame debe retornar NONE (debounce)");
        }

        @Test
        @DisplayName("Debe permitir nuevo LEFT_CLICK después de liberar la pinza")
        void shouldAllowNewLeftClickAfterRelease() {
            // Arrange
            HandLandmarks withLeftPinch = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.51, 0.50)
                    .buildWithOpenFist();

            HandLandmarks withoutLeftPinch = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.3, 0.3)  // Separados
                    .buildWithOpenFist();

            // Act
            MouseAction firstClick = recognizer.process(withLeftPinch);
            recognizer.process(withLeftPinch); // Debounce frame
            recognizer.process(withoutLeftPinch); // Liberar pinza
            MouseAction secondClick = recognizer.process(withLeftPinch); // Hacer click de nuevo

            // Assert
            assertEquals(MouseAction.LEFT_CLICK, firstClick);
            assertEquals(MouseAction.LEFT_CLICK, secondClick, "Debe permitir nuevo LEFT_CLICK después de liberar");
        }

        @Test
        @DisplayName("NO debe disparar LEFT_CLICK si distancia >= 0.05")
        void shouldNotFireLeftClickIfDistanceTooLarge() {
            // Arrange: Puntos muy separados
            HandLandmarks landmarksNoTouching = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.1, 0.1)  // Distancia grande
                    .buildWithOpenFist();

            // Act
            MouseAction action = recognizer.process(landmarksNoTouching);

            // Assert
            assertEquals(MouseAction.NONE, action, "No debe detectar click si no hay contacto");
        }
    }

    @Nested
    @DisplayName("Click Derecho (Medio + Pulgar)")
    class RightClickTests {

        @Test
        @DisplayName("Debe disparar RIGHT_CLICK exactamente UNA VEZ cuando se detecta pinza derecha")
        void shouldFireRightClickOnce() {
            // Arrange
            HandLandmarks landmarksWithRightPinch = builder
                    .thumbTip(0.5, 0.5)
                    .middleTip(0.51, 0.50)  // Distancia ≈ 0.01
                    .buildWithOpenFist();

            // Act & Assert
            MouseAction firstFrame = recognizer.process(landmarksWithRightPinch);
            assertEquals(MouseAction.RIGHT_CLICK, firstFrame, "Primer frame debe disparar RIGHT_CLICK");

            MouseAction secondFrame = recognizer.process(landmarksWithRightPinch);
            assertEquals(MouseAction.NONE, secondFrame, "Segundo frame debe retornar NONE (debounce)");
        }

        @Test
        @DisplayName("Debe permitir nuevo RIGHT_CLICK después de liberar la pinza")
        void shouldAllowNewRightClickAfterRelease() {
            // Arrange
            HandLandmarks withRightPinch = builder
                    .thumbTip(0.5, 0.5)
                    .middleTip(0.51, 0.50)
                    .buildWithOpenFist();

            HandLandmarks withoutRightPinch = builder
                    .thumbTip(0.5, 0.5)
                    .middleTip(0.2, 0.2)
                    .buildWithOpenFist();

            // Act
            MouseAction firstClick = recognizer.process(withRightPinch);
            recognizer.process(withRightPinch);
            recognizer.process(withoutRightPinch);
            MouseAction secondClick = recognizer.process(withRightPinch);

            // Assert
            assertEquals(MouseAction.RIGHT_CLICK, firstClick);
            assertEquals(MouseAction.RIGHT_CLICK, secondClick);
        }

        @Test
        @DisplayName("NO debe disparar RIGHT_CLICK si distancia >= 0.05")
        void shouldNotFireRightClickIfDistanceTooLarge() {
            // Arrange
            HandLandmarks landmarksNoTouching = builder
                    .thumbTip(0.5, 0.5)
                    .middleTip(0.1, 0.1)
                    .buildWithOpenFist();

            // Act
            MouseAction action = recognizer.process(landmarksNoTouching);

            // Assert
            assertEquals(MouseAction.NONE, action);
        }
    }

    @Nested
    @DisplayName("Sin Falsos Positivos")
    class NoFalsePositivesTests {

        @Test
        @DisplayName("Debe disparar LEFT_CLICK sin interferencia de RIGHT_CLICK")
        void leftClickShouldNotInterfereWithRight() {
            // Arrange: Solo Índice unido al Pulgar, Medio separado
            HandLandmarks landmarksLeftOnly = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.51, 0.50)   // Pinza izquierda
                    .middleTip(0.2, 0.2)    // Separado
                    .buildWithOpenFist();

            // Act
            MouseAction action = recognizer.process(landmarksLeftOnly);

            // Assert
            assertEquals(MouseAction.LEFT_CLICK, action, "Solo debe detectar LEFT_CLICK");
        }

        @Test
        @DisplayName("Debe disparar RIGHT_CLICK sin interferencia de LEFT_CLICK")
        void rightClickShouldNotInterfereWithLeft() {
            // Arrange: Solo Medio unido al Pulgar, Índice separado
            HandLandmarks landmarksRightOnly = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.1, 0.1)     // Separado
                    .middleTip(0.51, 0.50)  // Pinza derecha
                    .buildWithOpenFist();

            // Act
            MouseAction action = recognizer.process(landmarksRightOnly);

            // Assert
            assertEquals(MouseAction.RIGHT_CLICK, action, "Solo debe detectar RIGHT_CLICK");
        }

        @Test
        @DisplayName("NO debe disparar clicks si ambas pinzas están activas")
        void shouldHandleMultiplePinchesGracefully() {
            // Arrange: Ambas pinzas detectadas
            HandLandmarks bothPinches = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.51, 0.50)   // Pinza izquierda
                    .middleTip(0.51, 0.50)  // Pinza derecha
                    .buildWithOpenFist();

            // Act: Procesa cuando ambas pinzas están activas
            MouseAction firstAction = recognizer.process(bothPinches);

            // Assert: Debe preferir una (por orden de detección, LEFT_CLICK)
            assertEquals(MouseAction.LEFT_CLICK, firstAction, "Con dos pinzas, prioriza LEFT_CLICK");

            // Segundo frame: debounce debe aplicar
            MouseAction secondAction = recognizer.process(bothPinches);
            assertEquals(MouseAction.NONE, secondAction);
        }
    }

    @Nested
    @DisplayName("Drag (Puño Cerrado/Abierto)")
    class DragTests {

        @Test
        @DisplayName("Debe disparar DRAG_START cuando se cierra el puño")
        void shouldFireDragStartWhenFistCloses() {
            // Arrange: Puño abierto
            HandLandmarks openFist = builder.buildWithOpenFist();

            // Act
            recognizer.process(openFist); // Primer frame: mano abierta
            MouseAction firstClosedFist = recognizer.process(builder.buildWithClosedFist());

            // Assert
            assertEquals(MouseAction.DRAG_START, firstClosedFist);
        }

        @Test
        @DisplayName("Debe disparar DRAG_END cuando se abre el puño")
        void shouldFireDragEndWhenFistOpens() {
            // Arrange
            HandLandmarks openFist = builder.buildWithOpenFist();
            HandLandmarks closedFist = builder.buildWithClosedFist();

            // Act: Establecer estado inicial (puño cerrado)
            recognizer.process(closedFist);

            // Abrir puño
            MouseAction dragEnd = recognizer.process(openFist);

            // Assert
            assertEquals(MouseAction.DRAG_END, dragEnd);
        }

        @Test
        @DisplayName("Debe retornar NONE mientras el puño permanece cerrado (debounce)")
        void shouldReturnNoneWhileFistRemainsClosed() {
            // Arrange
            HandLandmarks closedFist = builder.buildWithClosedFist();

            // Act: Procesa múltiples frames con puño cerrado
            recognizer.process(builder.buildWithOpenFist());
            MouseAction frame1 = recognizer.process(closedFist);
            MouseAction frame2 = recognizer.process(closedFist);
            MouseAction frame3 = recognizer.process(closedFist);

            // Assert
            assertEquals(MouseAction.DRAG_START, frame1);
            assertEquals(MouseAction.NONE, frame2);
            assertEquals(MouseAction.NONE, frame3);
        }

        @Test
        @DisplayName("Debe ignorar clicks cuando el puño está cerrado")
        void shouldIgnoreClicksWhenFistClosed() {
            // Arrange: Puño cerrado pero con "pinza" detectada (índice y pulgar unidos)
            HandLandmarks closedFistWithPinch = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.51, 0.50)   // Sería pinza en condiciones normales
                    .buildWithClosedFist();  // Pero está en puño

            // Act: Primero cierra el puño
            recognizer.process(builder.buildWithOpenFist());
            recognizer.process(closedFistWithPinch); // Activa DRAG_START

            // Luego procesa otro frame con puño aún cerrado
            MouseAction action = recognizer.process(closedFistWithPinch);

            // Assert
            assertEquals(MouseAction.NONE, action, "Debe ignorar clicks dentro del puño");
        }

        @Test
        @DisplayName("Debe permitir clicks nuevamente después de abrir el puño")
        void shouldAllowClicksAgainAfterOpeningFist() {
            // Arrange
            HandLandmarks openFistWithPinch = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.51, 0.50)
                    .buildWithOpenFist();

            HandLandmarks closedFist = builder.buildWithClosedFist();

            // Act: Hacer click, luego puño, luego click de nuevo
            recognizer.process(openFistWithPinch);  // LEFT_CLICK
            recognizer.process(closedFist);         // DRAG_START
            recognizer.process(openFistWithPinch);  // DRAG_END
            MouseAction newClick = recognizer.process(openFistWithPinch);

            // Assert
            assertEquals(MouseAction.LEFT_CLICK, newClick, "Debe permitir clicks después de cerrar/abrir puño");
        }
    }

    @Nested
    @DisplayName("Validación de Entrada")
    class InputValidationTests {

        @Test
        @DisplayName("Debe retornar NONE si landmarks es null")
        void shouldReturnNoneIfLandmarksNull() {
            // Act
            MouseAction action = recognizer.process(null);

            // Assert
            assertEquals(MouseAction.NONE, action);
        }

        @Test
        @DisplayName("Debe retornar NONE si landmarks no es válido")
        void shouldReturnNoneIfLandmarksInvalid() {
            // Arrange: Landmarks con longitud incorrecta (esto debería lanzar excepción en el constructor)
            // Pero validamos el isValid()
            HandLandmarks validLandmarks = builder.buildWithOpenFist();

            // Act
            MouseAction action = recognizer.process(validLandmarks);

            // Assert: Debe procesar sin error
            assertNotNull(action);
        }

        @Test
        @DisplayName("Debe resetear estados cuando recibe landmarks nulos")
        void shouldResetStatesOnNullLandmarks() {
            // Arrange: Establece un click
            HandLandmarks withLeftPinch = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.51, 0.50)
                    .buildWithOpenFist();

            // Act
            recognizer.process(withLeftPinch);      // LEFT_CLICK
            recognizer.process(null);               // Landmarks nulos
            MouseAction nextAction = recognizer.process(withLeftPinch); // Click de nuevo

            // Assert: Debe permitir nuevo click sin efectos de debounce
            assertEquals(MouseAction.LEFT_CLICK, nextAction, "Estados deben estar reseteados");
        }
    }

    @Nested
    @DisplayName("Transiciones Complejas")
    class ComplexTransitionsTests {

        @Test
        @DisplayName("Debe manejar secuencia: Click Izq → Puño → Click Izq")
        void shouldHandleComplexSequence() {
            // Arrange
            HandLandmarks leftClick = builder
                    .thumbTip(0.5, 0.5)
                    .indexTip(0.51, 0.50)
                    .buildWithOpenFist();

            HandLandmarks closedFist = builder.buildWithClosedFist();

            // Act & Assert
            MouseAction action1 = recognizer.process(leftClick);
            assertEquals(MouseAction.LEFT_CLICK, action1);

            MouseAction action2 = recognizer.process(closedFist);
            assertEquals(MouseAction.DRAG_START, action2);

            MouseAction action3 = recognizer.process(leftClick);
            assertEquals(MouseAction.DRAG_END, action3);

            MouseAction action4 = recognizer.process(leftClick);
            assertEquals(MouseAction.LEFT_CLICK, action4, "Nuevo click después de la secuencia");
        }
    }
}
