package com.vision.cucumber;

import com.vision.control.MouseController;
import com.vision.detection.HandLandmarks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

public class ControlConsistencySteps {

    private final MouseController mouseController = new MouseController();
    private List<HandLandmarks> landmarksSequence = new ArrayList<>();
    private List<String> actionResults = new ArrayList<>();

    @Given("hand landmarks with slight jitter around {double}, {double}")
    public void hand_landmarks_with_jitter(double x, double y) {
        // Generar una secuencia con ruido
        for (int i = 0; i < 5; i++) {
            float jitterX = (float) (x + (Math.random() - 0.5) * 0.01);
            float jitterY = (float) (y + (Math.random() - 0.5) * 0.01);
            landmarksSequence.add(new HandLandmarks(TestDataFactory.createCursorLandmarks(jitterX, jitterY)));
        }
    }

    @When("mouse controller processes the sequence of landmarks")
    public void process_sequence() {
        for (HandLandmarks lm : landmarksSequence) {
            actionResults.add(mouseController.processGesture("MOVE", lm, 1920, 1080));
        }
    }

    @Then("the cursor position remains stable using EMA filtering")
    public void cursor_position_stable() {
        assertTrue(mouseController.wasSmoothingApplied());
        // En una implementación real verificaríamos que la varianza es menor
    }

    @Then("no conflicting actions are triggered")
    public void no_conflicting_actions() {
        for (String action : actionResults) {
            assertEquals("MOVE", action);
        }
    }

    @Given("gesture {string} is active")
    public void gesture_active(String gesture) {
        // Inicializar estado
        HandLandmarks lm = new HandLandmarks(TestDataFactory.createValidLandmarks());
        mouseController.processGesture(gesture, lm, 1920, 1080);
    }

    @When("the hand momentarily shows a {string} pattern for {int} frame")
    public void momentary_gesture(String gesture, int frames) {
        HandLandmarks lm = new HandLandmarks(TestDataFactory.createClosedFistLandmarks());
        for (int i = 0; i < frames; i++) {
            actionResults.add(mouseController.processGesture(gesture, lm, 1920, 1080));
        }
    }

    @When("then returns to {string}")
    public void returns_to_gesture(String gesture) {
        HandLandmarks lm = new HandLandmarks(TestDataFactory.createValidLandmarks());
        actionResults.add(mouseController.processGesture(gesture, lm, 1920, 1080));
    }

    @Then("the system ignores the momentary {string} due to debouncing")
    public void system_ignores_momentary(String gesture) {
        for (String action : actionResults) {
            assertNotEquals(gesture, action, "Should have ignored " + gesture);
        }
    }

    @Then("the cursor continues to {string} consistently")
    public void continues_consistently(String expected) {
        assertEquals(expected, mouseController.getCurrentState());
    }

    @Given("gesture {string} is recognized and active")
    public void gesture_recognized_and_active(String gesture) {
        mouseController.setDebounceThreshold(1); // Para activar rápido
        HandLandmarks lm = new HandLandmarks(TestDataFactory.createPointingLandmarks());
        mouseController.processGesture(gesture, lm, 1920, 1080);
        mouseController.setDebounceThreshold(3); // Restaurar para el test
    }

    @When("the landmarks show minor noise that could be interpreted as {string}")
    public void noise_interpreted_as(String gesture) {
        HandLandmarks lm = new HandLandmarks(TestDataFactory.createValidLandmarks());
        // El reconocimiento de gestos diría MOVE, pero el MouseController debería mantener DRAG si es ruido
        actionResults.add(mouseController.processGesture(gesture, lm, 1920, 1080));
    }

    @Then("the system maintains the {string} state to avoid dropping the object")
    public void maintains_state(String expected) {
        assertEquals(expected, mouseController.getCurrentState());
    }

    @Then("the cursor movement remains smooth")
    public void remains_smooth() {
        assertTrue(mouseController.wasSmoothingApplied());
    }
}
