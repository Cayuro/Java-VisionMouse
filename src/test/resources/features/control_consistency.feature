Feature: Cursor Control Consistency
  As a user of the Vision Mouse
  I want the cursor movement to be stable and predictable
  So that I can interact with the system without accidental clicks or jitter

  Scenario: Cursor jitter suppression
    Given hand landmarks with slight jitter around 0.5, 0.5
    When mouse controller processes the sequence of landmarks
    Then the cursor position remains stable using EMA filtering
    And no conflicting actions are triggered

  Scenario: Gesture debounce to avoid rapid toggling
    Given gesture "MOVE" is active
    When the hand momentarily shows a "CLICK" pattern for 1 frame
    And then returns to "MOVE"
    Then the system ignores the momentary "CLICK" due to debouncing
    And the cursor continues to "MOVE" consistently

  Scenario: State consistency during DRAG
    Given gesture "DRAG" is recognized and active
    When the landmarks show minor noise that could be interpreted as "MOVE"
    Then the system maintains the "DRAG" state to avoid dropping the object
    And the cursor movement remains smooth
