package com.vision.cucumber;

import com.vision.control.MouseController;
import com.vision.detection.HandLandmarks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import java.awt.Robot;
import java.awt.event.InputEvent;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RealMouseExecutionSteps {

    private final Robot mockRobot = mock(Robot.class);
    private final MouseController mouseController = createTestController();
    private HandLandmarks landmarks;
    private String gesture;

    private MouseController createTestController() {
        MouseController mc = new MouseController(mockRobot);
        mc.setDebounceThreshold(1);
        return mc;
    }

    @Given("the system has permissions to control the cursor")
    public void permissions_granted() {
        assertNotNull(mockRobot);
    }

    @Given("hand landmarks at normalized position {double}, {double} for execution")
    public void hand_landmarks_at_position(double x, double y) {
        landmarks = new HandLandmarks(TestDataFactory.createCursorLandmarks((float) x, (float) y));
    }

    @Given("gesture {string} is recognized for execution")
    public void gesture_is_recognized_for_exec(String gesture) {
        this.gesture = gesture;
    }

    @When("mouse controller executes the action")
    public void execute_action() {
        mouseController.processGesture(gesture, landmarks, 1920, 1080);
        mouseController.execute();
    }

    @Then("the physical cursor moves to screen center")
    public void physical_cursor_moves() {
        // En 1920x1080, el centro es 960, 540
        verify(mockRobot, atLeastOnce()).mouseMove(960, 540);
    }

    @Then("the physical left mouse button is pressed and released")
    public void physical_mouse_click() {
        verify(mockRobot, times(1)).mousePress(InputEvent.BUTTON1_DOWN_MASK);
        verify(mockRobot, times(1)).mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
}
