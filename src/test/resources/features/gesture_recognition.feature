Feature: Gesture Recognition from Landmarks
  As a gesture recognition system
  I want to identify gestures from hand landmarks
  So that mouse movements and clicks can be mapped to hand poses

  Scenario: Invalid or incomplete landmarks
    Given incomplete hand landmarks
    When gesture recognition is performed
    Then gesture result is "NONE"

  @smoke
  Scenario: Valid hand landmarks for move gesture
    Given complete valid hand landmarks in neutral position
    When gesture recognition is performed
    Then gesture result is "MOVE"

  Scenario Outline: Different hand gestures
    Given hand landmarks representing <gesture>
    When gesture recognition is performed
    Then gesture result is "<expected_gesture>"

    Examples:
      | gesture       | expected_gesture |
      | open palm     | MOVE            |
      | closed fist   | CLICK           |
      | pointing      | DRAG            |
