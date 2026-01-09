# Scripting Engine

The system for defining, parsing, and executing automation scripts that control device interactions.

## Requirements

### Requirement: Script Format

The system SHALL support a simple, readable script format for defining automations.

#### Scenario: Basic script structure
- **WHEN** an automation script is created
- **THEN** it SHALL be a JSON or YAML document containing ordered steps
- **AND** each step SHALL have an action type and parameters
- **AND** optionally include wait conditions and recovery hints

#### Scenario: Example script format
- **WHEN** a script is defined
- **THEN** it SHALL follow this structure:
```yaml
name: "Get Uber Price"
description: "Opens Uber and gets price estimate for a destination"
inputs:
  - name: destination
    type: text
    prompt: "Where do you want to go?"
steps:
  - action: launch_app
    package: "com.ubercab"
  - action: wait_for
    selector: "#destination_input"
    timeout: 10000
  - action: tap
    selector: "#destination_input"
  - action: type
    selector: "#destination_input"
    text: "${destination}"
  - action: tap
    selector: "Button:contains('Search')"
  - action: wait_for
    selector: "#price_estimate"
    timeout: 15000
  - action: read
    selector: "#price_estimate"
    store_as: "price"
outputs:
  - name: price
    from: "price"
```

### Requirement: Action Types

The system SHALL support a comprehensive set of action types for device interaction.

#### Scenario: tap action
- **WHEN** a `tap` action is executed
- **THEN** the system SHALL find the element matching the selector
- **AND** perform a click action on it

#### Scenario: long_press action
- **WHEN** a `long_press` action is executed
- **THEN** the system SHALL find the element matching the selector
- **AND** perform a long press action on it

#### Scenario: type action
- **WHEN** a `type` action is executed
- **THEN** the system SHALL find the element matching the selector
- **AND** input the specified text into it
- **AND** support variable substitution with `${varname}` syntax

#### Scenario: clear action
- **WHEN** a `clear` action is executed
- **THEN** the system SHALL find the element matching the selector
- **AND** clear any existing text content

#### Scenario: scroll action
- **WHEN** a `scroll` action is executed
- **THEN** the system SHALL perform a scroll in the specified direction
- **AND** support `up`, `down`, `left`, `right` directions
- **AND** optionally target a specific scrollable container

#### Scenario: launch_app action
- **WHEN** a `launch_app` action is executed
- **THEN** the system SHALL launch the app with the specified package name
- **AND** wait for the app to become visible

#### Scenario: back action
- **WHEN** a `back` action is executed
- **THEN** the system SHALL perform the system back action

#### Scenario: home action
- **WHEN** a `home` action is executed
- **THEN** the system SHALL perform the system home action

#### Scenario: read action
- **WHEN** a `read` action is executed
- **THEN** the system SHALL find the element matching the selector
- **AND** extract its text content
- **AND** store it in the variable specified by `store_as`

#### Scenario: read_screen action
- **WHEN** a `read_screen` action is executed
- **THEN** the system SHALL capture the entire screen representation
- **AND** store it in the variable specified by `store_as`

### Requirement: Wait Conditions

The system SHALL support waiting for conditions before proceeding.

#### Scenario: wait_for element
- **WHEN** a `wait_for` action is executed with a selector
- **THEN** the system SHALL poll for the element to appear
- **AND** continue when found or fail after timeout

#### Scenario: wait_for_text
- **WHEN** a `wait_for_text` action is executed
- **THEN** the system SHALL poll for any element containing the specified text
- **AND** continue when found or fail after timeout

#### Scenario: wait_for_gone
- **WHEN** a `wait_for_gone` action is executed
- **THEN** the system SHALL poll until the specified element is no longer present
- **AND** continue when gone or fail after timeout

#### Scenario: wait duration
- **WHEN** a `wait` action is executed with a duration
- **THEN** the system SHALL pause execution for that duration in milliseconds

#### Scenario: Timeout configuration
- **WHEN** a wait action specifies a timeout
- **THEN** the system SHALL use that timeout
- **AND** default to a configurable global timeout if not specified

### Requirement: Control Flow

The system SHALL support basic control flow in scripts.

#### Scenario: Conditional execution
- **WHEN** a step includes an `if` condition
- **THEN** the system SHALL evaluate the condition
- **AND** only execute the step if the condition is true

#### Scenario: Element existence condition
- **WHEN** a condition `exists("#element_id")` is used
- **THEN** it SHALL evaluate to true if the element is currently on screen

#### Scenario: Variable comparison condition
- **WHEN** a condition `${var} == "value"` is used
- **THEN** it SHALL evaluate to true if the variable equals the value

#### Scenario: Retry on failure
- **WHEN** a step includes `retry: 3`
- **THEN** the system SHALL retry the action up to 3 times if it fails
- **AND** pause briefly between retries

### Requirement: Variable System

The system SHALL support variables for data passing within scripts.

#### Scenario: Input variables
- **WHEN** a script defines inputs
- **THEN** those variables SHALL be available for use in steps via `${varname}`
- **AND** be populated before script execution

#### Scenario: Read capture
- **WHEN** a `read` action uses `store_as`
- **THEN** the captured text SHALL be stored in that variable
- **AND** be available for subsequent steps

#### Scenario: Output variables
- **WHEN** a script defines outputs
- **THEN** the system SHALL collect those variables at the end
- **AND** make them available to the caller

### Requirement: Script Execution Engine

The system SHALL execute scripts reliably with proper error handling.

#### Scenario: Sequential execution
- **WHEN** a script is executed
- **THEN** steps SHALL be executed in order
- **AND** each step SHALL complete before the next begins

#### Scenario: Execution state tracking
- **WHEN** a script is running
- **THEN** the system SHALL track the current step index
- **AND** track execution start time and duration
- **AND** track step-level success/failure status

#### Scenario: Graceful failure
- **WHEN** a step fails and is not recoverable
- **THEN** the system SHALL stop execution
- **AND** return the failure reason and last known state
- **AND** not leave the device in an unexpected state if possible

#### Scenario: Script cancellation
- **WHEN** the user requests script cancellation
- **THEN** the system SHALL stop execution after the current step
- **AND** return partial results and state

### Requirement: Script Validation

The system SHALL validate scripts before execution.

#### Scenario: Syntax validation
- **WHEN** a script is loaded
- **THEN** the system SHALL validate JSON/YAML syntax
- **AND** report parsing errors clearly

#### Scenario: Schema validation
- **WHEN** a script is validated
- **THEN** the system SHALL verify all required fields are present
- **AND** verify action types are valid
- **AND** verify selectors are syntactically valid

#### Scenario: Variable reference validation
- **WHEN** a script is validated
- **THEN** the system SHALL verify all variable references (`${var}`) have corresponding definitions
- **AND** warn about undefined variables

### Requirement: Script Storage

The system SHALL persist scripts for later use.

#### Scenario: Save script
- **WHEN** a script is saved
- **THEN** it SHALL be stored in local database with a unique identifier
- **AND** include metadata (name, description, created date, modified date)

#### Scenario: Load script
- **WHEN** a script is loaded by ID
- **THEN** the system SHALL retrieve it from storage
- **AND** return it in executable form

#### Scenario: List scripts
- **WHEN** available scripts are listed
- **THEN** the system SHALL return all saved scripts with their metadata
- **AND** support filtering and sorting
