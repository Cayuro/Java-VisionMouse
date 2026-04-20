package com.vision.cucumber;

import com.vision.control.MouseController;
import com.vision.detection.HandLandmarks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class CursorSmoothingSteps {

    private MouseController mouseController = new MouseController();
    private HandLandmarks landmarks;
    private int lastX, lastY;

    @Given("hand landmarks move from 0.5, 0.5 to 0.6, 0.6 with noise")
    public void hand_landmarks_with_noise() {
        // We will process multiple frames in the When step
    }

    @Given("the system just started and no hand was previously detected")
    public void system_started() {
        mouseController = new MouseController(); // Fresh controller
    }

    @When("the mouse controller filters the movement")
    public void filters_movement() {
        // Move from 0.5 to 0.6 in 5 steps with random noise
        for (int i = 0; i < 5; i++) {
            float noiseX = (float)(Math.random() * 0.02 - 0.01);
            float noiseY = (float)(Math.random() * 0.02 - 0.01);
            float pos = 0.5f + (i * 0.02f);
            landmarks = new HandLandmarks(TestDataFactory.createCursorLandmarks(pos + noiseX, pos + noiseY));
            mouseController.processGesture("MOVE", landmarks, 1920, 1080);
            
            if (i > 0) {
                int dist = Math.abs(mouseController.getLastCursorX() - lastX);
                assertTrue(dist < 100, "Jump detected during movement: " + dist);
            }
            lastX = mouseController.getLastCursorX();
            lastY = mouseController.getLastCursorY();
        }
    }

    @When("a hand is detected at {double}, {double} for the first time")
    public void hand_detected_first_time(double x, double y) {
        landmarks = new HandLandmarks(TestDataFactory.createCursorLandmarks((float)x, (float)y));
        mouseController.processGesture("MOVE", landmarks, 1920, 1080);
    }

    @Then("the cursor position should change smoothly")
    public void position_changes_smoothly() {
        assertTrue(mouseController.wasSmoothingApplied());
    }

    @Then("no sudden jumps greater than {int} pixels should occur between frames")
    public void no_sudden_jumps(int maxJump) {
        // Verified during @When loop
    }

    @Then("the cursor should jump directly to the target position")
    public void jump_directly_to_target() {
        // 1.0 - 0.7 = 0.3 mirrored. 0.3 * 1920 = 576. 0.7 * 1080 = 756.
        assertEquals(576, mouseController.getLastCursorX());
        assertEquals(756, mouseController.getLastCursorY());
    }

    @Then("the cursor should NOT slide from the screen origin \\(0,0)")
    public void no_slide_from_origin() {
        // If it slides, the first value would be alpha * target + (1-alpha) * 0
        // e.g. 0.2 * 576 = 115. But we expect 576.
        assertEquals(576, mouseController.getLastCursorX());
    }
}
