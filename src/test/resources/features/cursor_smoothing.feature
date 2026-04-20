Feature: Smooth Cursor Movement
  As a user
  I want the cursor movement to be smooth and fluid
  So that I can control the pointer better without sudden jumps

  Scenario: Smooth movement without jumps
    Given hand landmarks move from 0.5, 0.5 to 0.6, 0.6 with noise
    When the mouse controller filters the movement
    Then the cursor position should change smoothly
    And no sudden jumps greater than 50 pixels should occur between frames

  Scenario: Stable startup without erratic movement
    Given the system just started and no hand was previously detected
    When a hand is detected at 0.7, 0.7 for the first time
    Then the cursor should jump directly to the target position
    And the cursor should NOT slide from the screen origin (0,0)
