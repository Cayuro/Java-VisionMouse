Feature: Real Mouse Execution
  As a user with a vision mouse
  I want the system to control the physical cursor
  So that I can interact with the OS using my hands

  Scenario: Physical cursor movement
    Given the system has permissions to control the cursor
    And hand landmarks at normalized position 0.5, 0.5 for execution
    And gesture "MOVE" is recognized for execution
    When mouse controller executes the action
    Then the physical cursor moves to screen center

  Scenario: Physical mouse click
    Given the system has permissions to control the cursor
    And hand landmarks at normalized position 0.5, 0.5 for execution
    And gesture "CLICK" is recognized for execution
    When mouse controller executes the action
    Then the physical left mouse button is pressed and released
