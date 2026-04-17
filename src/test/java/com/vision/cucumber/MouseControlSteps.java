package com.vision.cucumber;

import com.vision.control.MouseController;
import com.vision.detection.HandLandmarks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class MouseControlSteps {

    private final MouseController mouseController = new MouseController();
    private HandLandmarks landmarks;
    private String gesture;
    private String actionResult;

    @Given("hand landmarks at normalized position ({double}, {double})")
    public void hand_landmarks_at_normalized_position(double x, double y) {
        landmarks = new HandLandmarks(TestDataFactory.createCursorLandmarks((float) x, (float) y));
    }

    @Given("gesture {string} is recognized")
    public void gesture_is_recognized(String gesture) {
        this.gesture = gesture;
    }

    @Given("gesture {string} is recognized from closed fist")
    public void gesture_is_recognized_from_closed_fist(String gesture) {
        this.gesture = gesture;
        landmarks = new HandLandmarks(TestDataFactory.createClosedFistLandmarks());
    }

    @When("mouse controller processes the gesture")
    public void mouse_controller_processes_the_gesture() {
        actionResult = mouseController.processGesture(gesture, landmarks, 1920, 1080);
    }

    @Then("mouse cursor moves to screen center")
    public void mouse_cursor_moves_to_screen_center() {
        assertEquals("MOVE", actionResult);
        assertEquals(960, mouseController.getLastCursorX());
        assertEquals(540, mouseController.getLastCursorY());
    }

    @Then("smooth cursor movement is applied using EMA filter")
    public void smooth_cursor_movement_is_applied_using_ema_filter() {
        assertTrue(mouseController.wasSmoothingApplied());
    }

    @Then("left mouse button is clicked")
    public void left_mouse_button_is_clicked() {
        assertEquals("CLICK", actionResult);
        assertTrue(mouseController.wasLeftClickPerformed());
    }

    @Then("mouse cursor moves to the right side of the screen")
    public void mouse_cursor_moves_to_the_right_side_of_the_screen() {
        assertEquals("MOVE", actionResult);
        assertEquals(1536, mouseController.getLastCursorX());
    }

    @Then("horizontal position is mirrored before screen mapping")
    public void horizontal_position_is_mirrored_before_screen_mapping() {
        assertEquals("MOVE", actionResult);
        assertEquals(1620, mouseController.getLastCursorX());
    }
}
