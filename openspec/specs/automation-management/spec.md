# Automation Management

The system for creating, storing, organizing, and executing user automations.

## Requirements

### Requirement: Automation Model

The system SHALL define a comprehensive automation model that captures all aspects of a user automation.

#### Scenario: Automation properties
- **WHEN** an automation is created
- **THEN** it SHALL have the following properties:
  - Unique identifier (UUID)
  - Name (user-provided, required)
  - Description (optional)
  - Script (the executable automation script)
  - Inputs (form fields to collect before running)
  - Icon (optional custom icon)
  - Created timestamp
  - Modified timestamp
  - Last run timestamp
  - Run count

#### Scenario: Automation states
- **WHEN** an automation exists
- **THEN** it SHALL have a state of: draft, ready, running, or error
- **AND** draft indicates the script is incomplete
- **AND** ready indicates it can be executed
- **AND** running indicates it is currently executing
- **AND** error indicates the last run failed

### Requirement: Automation Creation

The system SHALL support creating automations through multiple methods.

#### Scenario: Text prompt creation
- **WHEN** the user provides a text description of desired automation
- **THEN** the system SHALL invoke the AI to parse intent
- **AND** initiate progressive script building
- **AND** save the resulting automation

#### Scenario: Voice prompt creation
- **WHEN** the user speaks an automation description
- **THEN** the system SHALL transcribe the speech to text
- **AND** proceed with text prompt creation flow

#### Scenario: Manual script creation
- **WHEN** a power user wants to create a script directly
- **THEN** the system SHALL provide a script editor
- **AND** validate the script before saving
- **AND** save as a ready automation if valid

#### Scenario: Import automation
- **WHEN** the user imports an automation file
- **THEN** the system SHALL validate the import format
- **AND** create a new automation from the imported data
- **AND** handle conflicts with existing automations

### Requirement: Automation Storage

The system SHALL persist automations in local storage.

#### Scenario: Create automation
- **WHEN** a new automation is saved
- **THEN** it SHALL be stored in the local database
- **AND** assigned a unique identifier
- **AND** timestamped with creation time

#### Scenario: Update automation
- **WHEN** an automation is modified
- **THEN** the changes SHALL be persisted
- **AND** the modified timestamp SHALL be updated

#### Scenario: Delete automation
- **WHEN** an automation is deleted
- **THEN** it SHALL be removed from storage
- **AND** any associated shortcuts SHALL be removed

#### Scenario: List automations
- **WHEN** automations are requested
- **THEN** the system SHALL return all stored automations
- **AND** support sorting by name, date created, date modified, or run count
- **AND** support filtering by state or search query

### Requirement: Automation Execution

The system SHALL execute automations with proper lifecycle management.

#### Scenario: Start automation
- **WHEN** an automation is started
- **THEN** the system SHALL verify accessibility permissions
- **AND** collect any required input values
- **AND** set state to running
- **AND** begin script execution

#### Scenario: Execution progress
- **WHEN** an automation is running
- **THEN** the system SHALL emit progress events
- **AND** include current step index and description
- **AND** support UI updates for progress display

#### Scenario: Successful completion
- **WHEN** an automation completes successfully
- **THEN** the system SHALL set state to ready
- **AND** update last run timestamp
- **AND** increment run count
- **AND** display results or outputs to user

#### Scenario: Failed execution
- **WHEN** an automation fails
- **THEN** the system SHALL set state to error
- **AND** store the error details
- **AND** display error information to user
- **AND** offer retry or edit options

#### Scenario: Cancel execution
- **WHEN** the user cancels a running automation
- **THEN** the system SHALL stop execution gracefully
- **AND** set state to ready
- **AND** return to the automation list or home

### Requirement: Input Forms

The system SHALL support collecting user input before automation execution.

#### Scenario: Form definition
- **WHEN** an automation defines inputs
- **THEN** each input SHALL have:
  - Name (variable name for the script)
  - Label (display text)
  - Type (text, number, select, date, etc.)
  - Required flag
  - Default value (optional)
  - Validation rules (optional)

#### Scenario: Form display
- **WHEN** an automation with inputs is started
- **THEN** the system SHALL display a form with the defined fields
- **AND** pre-fill default values
- **AND** validate input before proceeding

#### Scenario: Form validation
- **WHEN** the user submits input form
- **THEN** the system SHALL validate all required fields are filled
- **AND** validate field values against rules
- **AND** display errors for invalid fields
- **AND** only proceed if all validation passes

#### Scenario: Input persistence
- **WHEN** an input form is displayed
- **THEN** the system SHALL remember previous values for that automation
- **AND** offer them as defaults for convenience

### Requirement: Homescreen Shortcuts

The system SHALL support creating homescreen shortcuts for quick automation access.

#### Scenario: Create shortcut
- **WHEN** the user requests a shortcut for an automation
- **THEN** the system SHALL use Android's ShortcutManager API
- **AND** create a pinned shortcut with the automation's name and icon
- **AND** set the shortcut to launch directly into that automation

#### Scenario: Shortcut execution
- **WHEN** a shortcut is tapped
- **THEN** the app SHALL launch directly to that automation
- **AND** show input form if needed, or start execution if no inputs required

#### Scenario: Remove shortcut
- **WHEN** an automation with a shortcut is deleted
- **THEN** the system SHALL remove the associated shortcut
- **AND** handle the shortcut gracefully if the automation no longer exists

#### Scenario: Update shortcut
- **WHEN** an automation's name or icon changes
- **THEN** the system SHALL update the associated shortcut if one exists

### Requirement: Automation Export/Import

The system SHALL support sharing automations between devices or users.

#### Scenario: Export automation
- **WHEN** the user exports an automation
- **THEN** the system SHALL serialize it to a portable format (JSON)
- **AND** include script, inputs, name, description, and icon
- **AND** allow sharing via standard Android share intent

#### Scenario: Import automation
- **WHEN** the user imports an automation file
- **THEN** the system SHALL parse the file
- **AND** validate it contains required fields
- **AND** create a new automation
- **AND** handle naming conflicts by appending a suffix

### Requirement: Automation History

The system SHALL track automation execution history.

#### Scenario: Run history storage
- **WHEN** an automation completes (success or failure)
- **THEN** the system SHALL store a history record with:
  - Automation ID
  - Start timestamp
  - End timestamp
  - Success/failure status
  - Input values used
  - Output values captured
  - Error details if failed

#### Scenario: View history
- **WHEN** the user views an automation's history
- **THEN** the system SHALL display recent runs
- **AND** show success/failure status
- **AND** allow viewing details of each run

#### Scenario: History retention
- **WHEN** history grows beyond a limit
- **THEN** the system SHALL prune oldest records
- **AND** maintain configurable retention period or count
