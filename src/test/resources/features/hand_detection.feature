Feature: Hand Detection Pipeline
  As a hand tracking developer
  I want to detect palms and extract landmarks from camera frames
  So that I can track hand position and gestures for mouse control

  Background:
    Given the hand tracking pipeline is initialized with palm and landmark models

  @smoke
  Scenario: No hand is present in the frame
    Given a camera frame without any hand
    When the pipeline processes the frame
    Then no landmarks are detected
    And an empty landmark array is returned

  @smoke
  Scenario: Single hand is detected and landmarks extracted
    Given a camera frame containing one hand
    When the pipeline processes the frame
    Then exactly 21 normalized landmarks are returned
    And all landmarks have coordinates between 0.0 and 1.0
    And landmark visibility scores are valid
