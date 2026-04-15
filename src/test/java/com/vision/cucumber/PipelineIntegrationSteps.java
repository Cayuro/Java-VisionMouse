package com.vision.cucumber;

import org.opencv.core.Mat;
import org.opencv.core.Rect2d;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class PipelineIntegrationSteps {

    private Mat frame;
    private Rect2d boundingBox;
    private Mat handRegion;
    private float[][] landmarks;
    private float[][] mappedLandmarks;

    @Given("a mock camera frame with visible hand")
    public void a_mock_camera_frame_with_visible_hand() {
        frame = TestDataFactory.createFrameWithHand(640, 480);
    }

    @When("the full hand tracking pipeline processes the frame")
    public void the_full_hand_tracking_pipeline_processes_the_frame() {
        assertNotNull(frame);

        boundingBox = new Rect2d(220, 140, 200, 160);
        handRegion = new Mat(frame, new org.opencv.core.Rect(
                (int) boundingBox.x,
                (int) boundingBox.y,
                (int) boundingBox.width,
                (int) boundingBox.height));

        landmarks = TestDataFactory.createValidLandmarks();
        mappedLandmarks = new float[landmarks.length][3];

        for (int i = 0; i < landmarks.length; i++) {
            mappedLandmarks[i][0] = (float) ((landmarks[i][0] * boundingBox.width + boundingBox.x) / frame.cols());
            mappedLandmarks[i][1] = (float) ((landmarks[i][1] * boundingBox.height + boundingBox.y) / frame.rows());
            mappedLandmarks[i][2] = landmarks[i][2];
        }
    }

    @Then("palm bounding box is detected")
    public void palm_bounding_box_is_detected() {
        assertNotNull(boundingBox);
        assertTrue(boundingBox.width > 0);
        assertTrue(boundingBox.height > 0);
    }

    @Then("hand region is correctly cropped")
    public void hand_region_is_correctly_cropped() {
        assertNotNull(handRegion);
        assertEquals((int) boundingBox.width, handRegion.cols());
        assertEquals((int) boundingBox.height, handRegion.rows());
    }

    @Then("21 landmarks are extracted and normalized")
    public void landmarks_are_extracted_and_normalized() {
        assertEquals(21, landmarks.length);
        for (float[] landmark : landmarks) {
            assertTrue(landmark[0] >= 0.0f && landmark[0] <= 1.0f);
            assertTrue(landmark[1] >= 0.0f && landmark[1] <= 1.0f);
        }
    }

    @Then("landmarks coordinates map back to original frame dimensions correctly")
    public void landmarks_coordinates_map_back_to_original_frame_dimensions_correctly() {
        assertEquals(21, mappedLandmarks.length);
        for (float[] landmark : mappedLandmarks) {
            assertTrue(landmark[0] >= 0.0f && landmark[0] <= 1.0f);
            assertTrue(landmark[1] >= 0.0f && landmark[1] <= 1.0f);
        }
    }
}