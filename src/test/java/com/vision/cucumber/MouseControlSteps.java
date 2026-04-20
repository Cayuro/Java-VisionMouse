package com.vision.cucumber;

import com.vision.control.MouseController;
import com.vision.detection.HandLandmarks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class MouseControlSteps {

    private final MouseController mouseController = createTestController();
    private HandLandmarks landmarks;
    private String gesture;
    private String actionResult;
    private int firstCursorX;
    private int secondCursorX;

    private MouseController createTestController() {
        MouseController mc = new MouseController();
        mc.setDebounceThreshold(1);
        return mc;
    }

    @Given("hand landmarks at normalized position {double}, {double}")
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

    @Given("the finger starts at normalized x {double} and then moves to x {double}")
    public void the_finger_starts_at_normalized_x_and_then_moves_to_x(double startX, double endX) {
        HandLandmarks first = new HandLandmarks(TestDataFactory.createCursorLandmarks((float) startX, 0.5f));
        mouseController.processGesture(gesture, first, 1920, 1080);
        firstCursorX = mouseController.getLastCursorX();

        HandLandmarks second = new HandLandmarks(TestDataFactory.createCursorLandmarks((float) endX, 0.5f));
        mouseController.processGesture(gesture, second, 1920, 1080);
        secondCursorX = mouseController.getLastCursorX();
    }

    @When("mouse controller processes the rightward movement sequence")
    public void mouse_controller_processes_the_rightward_movement_sequence() {
        // Sequence is executed in the Given step to preserve temporal order between frames.
    }

    @Then("the cursor must move to the right on screen")
    public void the_cursor_must_move_to_the_right_on_screen() {
        assertTrue(secondCursorX > firstCursorX,
                "Expected cursor to move right: first=" + firstCursorX + ", second=" + secondCursorX);
    }
}
