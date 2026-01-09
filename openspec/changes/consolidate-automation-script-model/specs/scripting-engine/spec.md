## MODIFIED Requirements

### Requirement: Script Format

The system SHALL support a simple, readable script format as the execution representation of an automation.

#### Scenario: Basic script structure
- **WHEN** an automation script is defined
- **THEN** it SHALL be a JSON or YAML document containing:
  - **steps**: Ordered list of actions to execute (required)
  - **recovery_hints**: Optional hints for AI-in-the-middle recovery
- **AND** metadata (name, description, inputs, outputs) SHALL be stored in the parent Automation, not duplicated in the script

#### Scenario: Example script format
- **WHEN** a script is defined
- **THEN** it SHALL follow this structure:
```yaml
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
recovery_hints:
  - trigger: "modal_detected"
    suggestion: "dismiss_modal"
  - trigger: "element_not_found:#destination_input"
    suggestion: "scroll_down"
```

#### Scenario: Input variable substitution
- **WHEN** a script step contains `${varname}` syntax
- **THEN** the system SHALL substitute the value from the parent Automation's collected input
- **AND** fail if the variable is not defined in Automation inputs

### Requirement: Recovery Hints

The system SHALL support recovery hints in scripts to guide AI-in-the-middle recovery.

#### Scenario: Recovery hint structure
- **WHEN** a script includes recovery_hints
- **THEN** each hint SHALL have:
  - **trigger**: Condition that activates the hint (modal_detected, element_not_found:selector, timeout:step_index)
  - **suggestion**: Recommended recovery action (dismiss_modal, scroll_down, scroll_up, wait_longer, retry, ask_user)
  - **context**: Optional additional context for the AI

#### Scenario: Recovery hint usage
- **WHEN** AITM is invoked due to a failure condition
- **THEN** the system SHALL check for matching recovery hints
- **AND** provide matching hints to the AI as suggested actions
- **AND** AI MAY choose different action if hint is inappropriate

## REMOVED Requirements

### Requirement: Script Storage

**Reason**: Storage is handled by Automation Management. Scripts are embedded in Automations and stored as part of the Automation entity. Separate script storage creates model confusion and data duplication.

**Migration**: N/A - this is an architectural clarification, not a removal of functionality.

### Requirement: Script Metadata in Script Format

**Reason**: Script metadata (name, description, inputs, outputs) is stored in the parent Automation entity. The script format is now purely the execution steps and recovery hints.

**Migration**: Move name, description, inputs, outputs from script YAML to Automation model properties.
