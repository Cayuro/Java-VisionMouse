Feature: Controlled Error Handling
  As a developer
  I want mouse control errors to be captured and reported
  So that the application remains stable during hardware failures

  Scenario: Capturing Robot execution error
    Given the mouse controller uses a failing physical robot
    And hand landmarks are available for execution
    When mouse controller executes the action for error test
    Then an error message should be captured
    And the system should remain in a valid state
