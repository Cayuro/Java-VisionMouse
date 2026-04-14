package com.vision.cucumber;

import com.vision.gesture.GestureRecognizer;
import com.vision.detection.HandLandmarks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.*;

public class GestureSteps {
    
    private GestureRecognizer recognizer;
    private HandLandmarks landmarks;
    private String gestureResult;
    
@io.cucumber.java.Before
public void setup() {
        recognizer = new GestureRecognizer();
    }
    
    @Given("incomplete hand landmarks")
    public void incomplete_landmarks() {
        landmarks = HandLandmarks.fromPipeline(new float[0][]);
    }
    
    @Given("complete valid hand landmarks in neutral position")
    public void valid_landmarks() {
        landmarks = new HandLandmarks(TestDataFactory.createValidLandmarks());
    }
    
    @Given("hand landmarks representing {string}")
    public void landmarks_for_gesture(String gesture) {
        landmarks = new HandLandmarks(TestDataFactory.createValidLandmarks());
    }
    
    @When("gesture recognition is performed")
    public void recognize_gesture() {
        setup();
        gestureResult = recognizer.detect(landmarks);
    }
    
    @Then("gesture result is {string}")
    public void gesture_result(String expected) {
        assertEquals(expected, gestureResult);
    }
}
