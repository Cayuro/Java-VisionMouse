Feature: Mouse Control from Hand Gestures
  As a mouse controller
  I want to map hand landmarks and gestures to mouse actions
  So that users can control the mouse with hand movements

  Scenario: Mouse cursor movement from hand position
    Given hand landmarks at normalized position (0.5, 0.5)
    And gesture "MOVE" is recognized
    When mouse controller processes the gesture
    Then mouse cursor moves to screen center
    And smooth cursor movement is applied using EMA filter

  Scenario: Mirrored horizontal movement feels natural to the user
    Given hand landmarks at normalized position (0.2, 0.5)
    And gesture "MOVE" is recognized
    When mouse controller processes the gesture
    Then mouse cursor moves to the right side of the screen

  Scenario: Horizontal position is mirrored before screen mapping
    Given hand landmarks at normalized position (0.15625, 0.5)
    And gesture "MOVE" is recognized
    When mouse controller processes the gesture
    Then horizontal position is mirrored before screen mapping

  Scenario: Mouse click from gesture
    Given gesture "CLICK" is recognized from closed fist
    When mouse controller processes the gesture
    Then left mouse button is clicked
