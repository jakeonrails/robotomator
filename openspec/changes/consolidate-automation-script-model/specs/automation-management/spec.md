## MODIFIED Requirements

### Requirement: Automation Model

The system SHALL define Automation as the primary user-facing entity that contains all aspects of a saved automation.

#### Scenario: Automation properties
- **WHEN** an automation is created
- **THEN** it SHALL have the following properties:
  - Unique identifier (UUID)
  - Name (user-provided, required)
  - Description (optional)
  - Script (the executable automation script, embedded)
  - Inputs (form field definitions for runtime collection)
  - Outputs (values captured by the script for display)
  - Icon (optional custom icon, PNG format, max 256x256)
  - Created timestamp
  - Modified timestamp
  - Last run timestamp
  - Run count
  - State (draft, ready, running, error)

#### Scenario: Automation states
- **WHEN** an automation exists
- **THEN** it SHALL have one of these states:
  - **draft**: Script is missing, incomplete, or fails validation
  - **ready**: Script passes validation and can be executed
  - **running**: Currently executing (only one automation can be running)
  - **error**: Last execution failed (returns to ready when edited or re-run)

#### Scenario: State transitions
- **WHEN** an automation's script is modified
- **THEN** the system SHALL re-validate the script
- **AND** set state to ready if validation passes
- **AND** set state to draft if validation fails

### Requirement: Unified Input Definition

The system SHALL use a consistent input field definition across automation management and script execution.

#### Scenario: Input field properties
- **WHEN** an automation defines inputs
- **THEN** each input SHALL have:
  - **name**: Variable name for script reference (required, alphanumeric + underscore)
  - **label**: Display text shown to user (required)
  - **type**: Field type - text, number, select, date, time, boolean (required)
  - **prompt**: Placeholder text or hint (optional)
  - **required**: Whether field must be filled (default true)
  - **default**: Default value (optional)
  - **options**: For select type, array of {value, label} choices (required for select)
  - **validation**: Regex pattern for text fields (optional)

#### Scenario: Input variable binding
- **WHEN** a script references `${varname}`
- **THEN** varname SHALL match an input's name property
- **AND** the runtime value SHALL be substituted during execution

### Requirement: Output Definition

The system SHALL capture and store output values from script execution.

#### Scenario: Output properties
- **WHEN** an automation defines outputs
- **THEN** each output SHALL have:
  - **name**: Variable name matching a script store_as target (required)
  - **label**: Display label for results screen (required)
  - **format**: Display format hint - text, currency, number, date (default text)

#### Scenario: Output capture
- **WHEN** a script completes successfully
- **THEN** all defined output values SHALL be captured from script variables
- **AND** stored in execution history
- **AND** displayed on the results screen

## REMOVED Requirements

### Requirement: Script as Separate Stored Entity

**Reason**: Scripts are now embedded within Automations, not stored separately. The Scripting Engine spec's "Script Storage" requirement is removed to eliminate duplication. Automation Management owns all persistence.

**Migration**: Any existing script storage should be migrated to automation storage with scripts embedded.
