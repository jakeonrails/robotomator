# User Interface

The Android application user interface for managing and interacting with Robotomator.

## Requirements

### Requirement: Main Navigation

The system SHALL provide clear navigation between major app sections.

#### Scenario: Bottom navigation
- **WHEN** the app is open
- **THEN** it SHALL display a bottom navigation bar
- **AND** include: Automations (list), Create (new automation), Settings

#### Scenario: Navigation state
- **WHEN** the user switches tabs
- **THEN** the selected tab SHALL be visually highlighted
- **AND** the corresponding screen SHALL be displayed

### Requirement: Automations List Screen

The system SHALL display all saved automations in a list view.

#### Scenario: List display
- **WHEN** the automations screen is shown
- **THEN** it SHALL display all saved automations
- **AND** show name, description snippet, and last run time for each
- **AND** indicate automation state (draft, ready, error)

#### Scenario: Empty state
- **WHEN** no automations exist
- **THEN** it SHALL display a friendly empty state message
- **AND** include a prompt to create the first automation

#### Scenario: List actions
- **WHEN** the user taps an automation
- **THEN** it SHALL open the automation detail/run screen

#### Scenario: Context menu
- **WHEN** the user long-presses an automation
- **THEN** it SHALL show a context menu with: Run, Edit, Create Shortcut, Export, Delete

#### Scenario: Search and filter
- **WHEN** the user has many automations
- **THEN** they SHALL be able to search by name
- **AND** filter by state (ready, draft, error)
- **AND** sort by different criteria

### Requirement: Create Automation Screen

The system SHALL provide an interface for creating new automations.

#### Scenario: Creation method selection
- **WHEN** the create screen is shown
- **THEN** it SHALL offer options: "Describe with voice", "Type description", "Write script manually"

#### Scenario: Voice input
- **WHEN** the user selects voice input
- **THEN** it SHALL activate the microphone
- **AND** display real-time transcription
- **AND** allow the user to confirm or re-record

#### Scenario: Text input
- **WHEN** the user selects text input
- **THEN** it SHALL display a text field
- **AND** provide placeholder text with examples
- **AND** include a submit button

#### Scenario: AI processing feedback
- **WHEN** the AI is processing the automation description
- **THEN** it SHALL show a progress indicator
- **AND** display status messages about what the AI is doing
- **AND** show the current screen being analyzed during progressive building

#### Scenario: Clarification prompts
- **WHEN** the AI needs clarification
- **THEN** it SHALL display the question
- **AND** provide input options or free text response
- **AND** continue after user responds

### Requirement: Automation Detail Screen

The system SHALL display details and controls for a single automation.

#### Scenario: Detail display
- **WHEN** an automation is selected
- **THEN** it SHALL show: name, description, input form (if any), run button, edit button

#### Scenario: Run automation
- **WHEN** the user taps Run
- **THEN** it SHALL collect inputs if needed
- **AND** start automation execution
- **AND** transition to execution progress view

#### Scenario: Edit automation
- **WHEN** the user taps Edit
- **THEN** it SHALL allow editing name, description, and script
- **AND** allow modifying input form definitions

### Requirement: Execution Progress Screen

The system SHALL show automation execution progress.

#### Scenario: Progress display
- **WHEN** an automation is running
- **THEN** it SHALL show the current step being executed
- **AND** show overall progress (step X of Y)
- **AND** optionally show a minimized preview of current screen

#### Scenario: Cancel option
- **WHEN** an automation is running
- **THEN** it SHALL display a cancel button
- **AND** canceling SHALL stop execution and return to detail screen

#### Scenario: AI recovery notification
- **WHEN** AI-in-the-middle is invoked
- **THEN** it SHALL display a notification that recovery is in progress
- **AND** show what unexpected state was encountered

#### Scenario: User escalation
- **WHEN** the AI escalates to the user for help
- **THEN** it SHALL display the current screen state
- **AND** explain the problem
- **AND** allow user to provide guidance or dismiss manually
- **AND** include an option to abort the automation

#### Scenario: Completion display
- **WHEN** an automation completes successfully
- **THEN** it SHALL display a success message
- **AND** show any output values captured
- **AND** provide options: Done, Run Again

#### Scenario: Failure display
- **WHEN** an automation fails
- **THEN** it SHALL display an error message
- **AND** explain what went wrong
- **AND** provide options: Retry, Edit Automation, Done

### Requirement: Settings Screen

The system SHALL provide configuration options.

#### Scenario: Accessibility permission
- **WHEN** the settings screen is shown
- **THEN** it SHALL display accessibility service status
- **AND** provide a button to open system settings if not enabled

#### Scenario: AI configuration
- **WHEN** configuring AI settings
- **THEN** it SHALL allow setting API key/endpoint
- **AND** allow enabling/disabling AI features
- **AND** show current AI usage status

#### Scenario: Default timeouts
- **WHEN** configuring execution settings
- **THEN** it SHALL allow setting default wait timeout
- **AND** allow setting AITM recovery timeout

#### Scenario: Privacy settings
- **WHEN** configuring privacy
- **THEN** it SHALL allow viewing/revoking AI consent
- **AND** allow configuring sensitive field detection sensitivity

### Requirement: Permission Flow

The system SHALL guide users through required permissions.

#### Scenario: Startup permission check
- **WHEN** the app starts
- **THEN** it SHALL check accessibility service status
- **AND** if not enabled, display a permission prompt screen

#### Scenario: Permission explanation
- **WHEN** showing permission prompt
- **THEN** it SHALL explain why accessibility is needed
- **AND** include a visual or animation showing what it enables
- **AND** provide "Enable" button to open settings

#### Scenario: Post-settings return
- **WHEN** the user returns from settings
- **THEN** it SHALL re-check permission status
- **AND** proceed to main app if granted
- **AND** show reminder if still not granted (with skip option for browsing only)

### Requirement: Voice Dictation

The system SHALL support voice input for automation creation.

#### Scenario: Microphone permission
- **WHEN** voice input is selected
- **THEN** it SHALL request microphone permission if not granted
- **AND** explain why it's needed

#### Scenario: Voice recording
- **WHEN** recording voice input
- **THEN** it SHALL display an animated recording indicator
- **AND** show a waveform or audio level visualization
- **AND** transcribe in real-time if possible

#### Scenario: Recording controls
- **WHEN** in recording mode
- **THEN** it SHALL provide: Stop button, Cancel button
- **AND** automatically stop after silence timeout

#### Scenario: Transcription review
- **WHEN** recording stops
- **THEN** it SHALL display the transcribed text
- **AND** allow user to edit the transcription
- **AND** provide: Confirm, Re-record buttons

### Requirement: Results Display

The system SHALL effectively display automation outputs and captured data.

#### Scenario: Simple output display
- **WHEN** an automation captures outputs (like a price)
- **THEN** it SHALL display each output with its label and value
- **AND** format values appropriately (currency, dates, etc.)

#### Scenario: Copy to clipboard
- **WHEN** viewing output values
- **THEN** each value SHALL have a copy button
- **AND** tapping SHALL copy to clipboard with confirmation

#### Scenario: Share results
- **WHEN** viewing results
- **THEN** it SHALL offer a share button
- **AND** sharing SHALL format outputs as readable text

### Requirement: Visual Feedback

The system SHALL provide clear visual feedback throughout the app.

#### Scenario: Loading states
- **WHEN** any operation is in progress
- **THEN** it SHALL display an appropriate loading indicator
- **AND** disable actions that would conflict

#### Scenario: Success feedback
- **WHEN** an operation succeeds
- **THEN** it SHALL display brief success feedback (snackbar or animation)

#### Scenario: Error feedback
- **WHEN** an operation fails
- **THEN** it SHALL display clear error message
- **AND** suggest resolution if possible
- **AND** provide retry option when appropriate

#### Scenario: Accessibility
- **WHEN** using the app
- **THEN** all UI elements SHALL have content descriptions
- **AND** the app SHALL work with TalkBack
- **AND** colors SHALL have sufficient contrast
