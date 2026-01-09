## ADDED Requirements

### Requirement: Data Classification

The system SHALL classify data into sensitivity levels to determine handling requirements.

#### Scenario: Sensitive field detection
- **WHEN** processing screen content
- **THEN** the system SHALL identify sensitive fields by:
  - Input type flags (TYPE_TEXT_VARIATION_PASSWORD, TYPE_NUMBER_VARIATION_PASSWORD)
  - Resource ID patterns containing: password, pin, ssn, credit, cvv, secret, token
  - Content description patterns indicating sensitive data
- **AND** mark these fields as sensitive in the screen representation

#### Scenario: Sensitive app detection
- **WHEN** reading content from known sensitive app categories
- **THEN** the system SHALL treat all content as potentially sensitive
- **AND** known categories SHALL include: banking, finance, healthcare, authentication apps

### Requirement: PII Masking

The system SHALL mask personally identifiable information before sending to external AI services.

#### Scenario: Automatic PII masking
- **WHEN** preparing screen content for AI transmission
- **THEN** the system SHALL mask detected sensitive field values with placeholder tokens (e.g., `[MASKED_PASSWORD]`)
- **AND** preserve field structure so AI can still reference the element
- **AND** log that masking occurred (without logging the masked content)

#### Scenario: Configurable masking patterns
- **WHEN** the user configures PII detection
- **THEN** the system SHALL allow adding custom patterns (regex) for masking
- **AND** provide sensible defaults for common PII (email, phone, SSN patterns)

### Requirement: Per-App Consent

The system SHALL allow users to control which apps Robotomator can interact with.

#### Scenario: App allowlist
- **WHEN** the user configures app permissions
- **THEN** the system SHALL allow creating an allowlist of approved apps
- **AND** only interact with apps on the allowlist when in strict mode

#### Scenario: App blocklist
- **WHEN** the user configures app permissions
- **THEN** the system SHALL allow creating a blocklist of prohibited apps
- **AND** refuse to read or interact with blocked apps
- **AND** display an error if an automation targets a blocked app

#### Scenario: First-time app access
- **WHEN** an automation attempts to access an app not previously seen
- **THEN** the system SHALL prompt the user to approve access to that app
- **AND** remember the decision for future runs

### Requirement: Audit Logging

The system SHALL maintain an audit log of all automation actions for user review.

#### Scenario: Action logging
- **WHEN** any automation action is performed
- **THEN** the system SHALL log: timestamp, automation ID, action type, target app, selector used, success/failure
- **AND** NOT log sensitive field values

#### Scenario: Log retention
- **WHEN** audit logs accumulate
- **THEN** the system SHALL retain logs for a configurable period (default 30 days)
- **AND** allow the user to export logs
- **AND** allow the user to clear logs

#### Scenario: Log viewing
- **WHEN** the user requests audit log access
- **THEN** the system SHALL display logs grouped by automation and date
- **AND** allow filtering by app, action type, or date range

### Requirement: Sensitive Action Confirmation

The system SHALL require user confirmation for actions that could have significant consequences.

#### Scenario: Payment action detection
- **WHEN** an automation attempts to tap elements containing payment-related text (pay, purchase, buy, confirm order, submit payment)
- **THEN** the system SHALL pause and request user confirmation before proceeding

#### Scenario: Deletion action detection
- **WHEN** an automation attempts to tap elements containing deletion-related text (delete, remove, clear all, erase)
- **THEN** the system SHALL pause and request user confirmation before proceeding

#### Scenario: Confirmation bypass
- **WHEN** the user has previously confirmed the same action in the same automation
- **THEN** the system MAY offer a "don't ask again for this automation" option
- **AND** store this preference per-automation, not globally

### Requirement: Export Sanitization

The system SHALL sanitize exported automations to prevent accidental data leakage.

#### Scenario: Export review
- **WHEN** the user exports an automation
- **THEN** the system SHALL scan the script for potential PII in selectors, stored values, and text literals
- **AND** warn the user if potential PII is detected
- **AND** offer to sanitize before export

#### Scenario: Sanitization options
- **WHEN** sanitizing an export
- **THEN** the system SHALL replace detected PII with placeholders
- **AND** replace specific text selectors with pattern-based alternatives where possible
- **AND** remove execution history

### Requirement: Overlay Attack Mitigation

The system SHALL protect against overlay attacks that could manipulate automation behavior.

#### Scenario: Obscured window detection
- **WHEN** performing an action on a UI element
- **THEN** the system SHALL check if the target window is obscured by an overlay
- **AND** refuse to perform the action if obscured
- **AND** alert the user to the potential security issue

### Requirement: Emergency Stop

The system SHALL provide a reliable way to immediately stop any running automation.

#### Scenario: Notification stop button
- **WHEN** an automation is running
- **THEN** the system SHALL display a persistent notification with a "Stop" action
- **AND** tapping Stop SHALL immediately halt the automation

#### Scenario: Hardware button stop
- **WHEN** the user presses the back button during automation execution
- **THEN** the system SHALL pause the automation
- **AND** prompt to continue or abort

### Requirement: AI Data Transmission Disclosure

The system SHALL clearly inform users about data sent to external AI services.

#### Scenario: Initial consent
- **WHEN** the user first enables AI features
- **THEN** the system SHALL display a clear explanation of what data is sent to AI services
- **AND** require explicit consent before enabling
- **AND** offer on-device-only mode as an alternative (with reduced functionality)

#### Scenario: Transmission indicator
- **WHEN** screen data is about to be sent to external AI
- **THEN** the system SHALL display a brief indicator
- **AND** allow the user to configure verbosity of these indicators

#### Scenario: AI provider disclosure
- **WHEN** configuring AI settings
- **THEN** the system SHALL display which AI provider is being used
- **AND** link to the provider's privacy policy
- **AND** disclose data retention practices
