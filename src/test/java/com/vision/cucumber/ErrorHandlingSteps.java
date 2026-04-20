package com.vision.cucumber;

import com.vision.control.MouseController;
import com.vision.detection.HandLandmarks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import java.awt.Robot;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ErrorHandlingSteps {

    private Robot mockRobot;
    private MouseController mouseController;
    private HandLandmarks landmarks;

    @Given("the mouse controller uses a failing physical robot")
    public void failing_robot() {
        mockRobot = mock(Robot.class);
        // Hacer que mouseMove lance una excepción
        doThrow(new RuntimeException("Permiso denegado")).when(mockRobot).mouseMove(anyInt(), anyInt());
        mouseController = new MouseController(mockRobot);
        mouseController.setDebounceThreshold(1);
    }

    @Given("hand landmarks are available for execution")
    public void hand_landmarks_available() {
        landmarks = new HandLandmarks(TestDataFactory.createValidLandmarks());
    }

    @When("mouse controller executes the action for error test")
    public void execute_action_error() {
        mouseController.processGesture("MOVE", landmarks, 1920, 1080);
        mouseController.execute();
    }

    @Then("an error message should be captured")
    public void error_captured() {
        assertTrue(mouseController.hasError());
        assertEquals("Error de ejecución: Permiso denegado", mouseController.getLastError());
    }

    @Then("the system should remain in a valid state")
    public void system_stable() {
        // Verificar que podemos seguir procesando gestos
        String state = mouseController.processGesture("MOVE", landmarks, 1920, 1080);
        assertEquals("MOVE", state);
    }
}
