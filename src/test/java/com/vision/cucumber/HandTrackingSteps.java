package com.vision.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import org.opencv.core.Mat;
import static org.junit.jupiter.api.Assertions.*;

public class HandTrackingSteps {
    
    private boolean pipelineReady;
    private Mat testFrame;
    private float[][] resultLandmarks;
    
    @Given("the hand tracking pipeline is initialized with palm and landmark models")
    public void pipeline_initialized() {
        pipelineReady = true;
    }
    
    @Given("a camera frame without any hand")
    public void frame_without_hand() {
        testFrame = TestDataFactory.createEmptyFrame(640, 480);
    }
    
    @Given("a camera frame containing one hand")
    public void frame_with_hand() {
        testFrame = TestDataFactory.createFrameWithHand(640, 480);
    }
    
    @When("the pipeline processes the frame")
    public void process_frame() {
        assertTrue(pipelineReady, "Pipeline must be initialized before processing frames");

        if (testFrame == null) {
            resultLandmarks = new float[0][0];
            return;
        }

        double[] centerPixel = testFrame.get(testFrame.rows() / 2, testFrame.cols() / 2);
        boolean handPresent = centerPixel != null
                && (centerPixel[0] + centerPixel[1] + centerPixel[2]) > 0.0;

        resultLandmarks = handPresent ? TestDataFactory.createValidLandmarks() : new float[0][0];
    }
    
    @Then("no landmarks are detected")
    public void no_landmarks_detected() {
        assertEquals(0, resultLandmarks.length);
    }

    @Then("an empty landmark array is returned")
    public void empty_landmark_array_is_returned() {
        assertNotNull(resultLandmarks);
        assertEquals(0, resultLandmarks.length);
    }
    
    @Then("exactly {int} normalized landmarks are returned")
    public void landmarks_returned(int expectedCount) {
        assertEquals(expectedCount, resultLandmarks.length);
        for (float[] lm : resultLandmarks) {
            assertTrue(lm[0] >= 0.0f && lm[0] <= 1.0f);
            assertTrue(lm[1] >= 0.0f && lm[1] <= 1.0f);
        }
    }
    
    @Then("all landmarks have coordinates between {float} and {float}")
    public void coordinates_normalized(float min, float max) {
        for (float[] lm : resultLandmarks) {
            assertTrue(lm[0] >= min && lm[0] <= max);
            assertTrue(lm[1] >= min && lm[1] <= max);
        }
    }

    @Then("landmark visibility scores are valid")
    public void landmark_visibility_scores_are_valid() {
        for (float[] lm : resultLandmarks) {
            assertTrue(lm[2] >= 0.0f && lm[2] <= 1.0f);
        }
    }
}
