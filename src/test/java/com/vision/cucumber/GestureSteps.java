package com.vision.cucumber;

import com.vision.gesture.GestureRecognizer;
import com.vision.gesture.MouseAction;
import com.vision.detection.HandLandmarks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.Before;
import static org.junit.jupiter.api.Assertions.*;

public class GestureSteps {
    
    private GestureRecognizer recognizer;
    private HandLandmarks landmarks;
    private MouseAction gestureResult;
    
    @Before
    public void setup() {
        recognizer = new GestureRecognizer();
    }
    
    @Given("incomplete hand landmarks")
    public void incomplete_landmarks() {
        landmarks = null;
    }
    
    @Given("complete valid hand landmarks in neutral position")
    public void valid_landmarks() {
        landmarks = new HandLandmarks(TestDataFactory.createValidLandmarks());
    }
    
    @Given("hand landmarks representing {}")
    public void landmarks_for_gesture(String gesture) {
        String normalizedGesture = gesture == null ? "" : gesture.trim().toLowerCase();

        switch (normalizedGesture) {
            case "open palm" -> landmarks = new HandLandmarks(TestDataFactory.createValidLandmarks());
            case "closed fist" -> landmarks = new HandLandmarks(TestDataFactory.createClosedFistLandmarks());
            case "pointing" -> landmarks = new HandLandmarks(TestDataFactory.createPointingLandmarks());
            default -> landmarks = new HandLandmarks(TestDataFactory.createValidLandmarks());
        }
    }
    
    @When("gesture recognition is performed")
    public void recognize_gesture() {
        gestureResult = recognizer.process(landmarks);
    }
    
    @Then("gesture result is {string}")
    public void gesture_result(String expected) {
        String expectedAction = (expected == null) ? "NONE" : expected.trim().toUpperCase();
        
        // Map the old test strings to the new continuous state names
        if (expectedAction.equals("NONE")) expectedAction = "MOVE";
        if (expectedAction.equals("DRAG_START")) expectedAction = "DRAG";
        if (expectedAction.equals("LEFT_CLICK")) expectedAction = "CLICK";

        String actualAction = (gestureResult != null) ? gestureResult.name() : "NONE";
        assertEquals(expectedAction, actualAction);
    }
}
