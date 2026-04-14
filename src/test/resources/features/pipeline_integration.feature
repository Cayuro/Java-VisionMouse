Feature: End-to-End Hand Tracking Pipeline Integration
  As a vision mouse application
  I want the full pipeline to process frames correctly
  So that hand tracking works seamlessly for mouse control

  @smoke
  Scenario: Complete pipeline processes frame with hand
    Given a mock camera frame with visible hand
    When the full hand tracking pipeline processes the frame
    Then palm bounding box is detected
    And hand region is correctly cropped
    And 21 landmarks are extracted and normalized
    And landmarks coordinates map back to original frame dimensions correctly
