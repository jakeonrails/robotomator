# Accessibility Service

The core Android service that enables Robotomator to read screen content and interact with UI elements across all applications.

## Requirements

### Requirement: Accessibility Service Registration

The system SHALL register as an Android AccessibilityService that can be enabled through system settings.

#### Scenario: Service registration in manifest
- **WHEN** the app is installed
- **THEN** it SHALL declare an AccessibilityService in AndroidManifest.xml
- **AND** provide a service configuration XML specifying required capabilities

#### Scenario: Service configuration
- **WHEN** the service is configured
- **THEN** it SHALL request `flagReportViewIds` to access view identifiers
- **AND** request `flagRequestFilterKeyEvents` if keyboard input monitoring needed
- **AND** specify `feedbackType` as generic or spoken as appropriate

### Requirement: Permission Status Detection

The system SHALL detect whether accessibility permissions are currently granted and provide this status to the application.

#### Scenario: Permission granted check
- **WHEN** the app queries accessibility permission status
- **THEN** it SHALL return true if the Robotomator accessibility service is enabled in system settings
- **AND** return false otherwise

#### Scenario: Permission change monitoring
- **WHEN** the user enables or disables the accessibility service
- **THEN** the app SHALL be notified of the permission state change
- **AND** update its UI accordingly

### Requirement: Permission Request Flow

The system SHALL guide users to enable accessibility permissions when they are not granted.

#### Scenario: First-time permission request
- **WHEN** the user attempts to create or run an automation without accessibility permission
- **THEN** the app SHALL display an explanation of why the permission is needed
- **AND** provide a button to open system accessibility settings
- **AND** deep-link directly to the Robotomator service entry if possible

#### Scenario: Permission denial handling
- **WHEN** the user returns from settings without granting permission
- **THEN** the app SHALL display a message explaining that automations cannot run without this permission
- **AND** offer to try again

### Requirement: Screen Content Reading

The system SHALL read the current screen content by traversing the accessibility node tree.

#### Scenario: Full screen capture
- **WHEN** a screen content read is requested
- **THEN** the service SHALL traverse all accessibility nodes in the current window
- **AND** capture text content, content descriptions, class names, and bounds
- **AND** capture view IDs and resource names where available
- **AND** return a structured representation of the screen

#### Scenario: Window type filtering
- **WHEN** reading screen content
- **THEN** the service SHALL be able to filter by window type (application, system, input method)
- **AND** default to reading the active application window

#### Scenario: Node property extraction
- **WHEN** processing an accessibility node
- **THEN** the service SHALL extract: text, contentDescription, className, viewIdResourceName, bounds, isClickable, isCheckable, isChecked, isEnabled, isScrollable, isEditable, childCount

### Requirement: Element Interaction

The system SHALL perform actions on UI elements identified by selectors.

#### Scenario: Click action
- **WHEN** a click action is requested on a selector
- **THEN** the service SHALL find the matching node
- **AND** perform AccessibilityNodeInfo.ACTION_CLICK on the node
- **AND** return success or failure status

#### Scenario: Text input action
- **WHEN** a text input action is requested on an editable field
- **THEN** the service SHALL find the matching node
- **AND** verify it is editable
- **AND** perform ACTION_SET_TEXT with the provided text
- **AND** return success or failure status

#### Scenario: Scroll action
- **WHEN** a scroll action is requested
- **THEN** the service SHALL find the matching scrollable container
- **AND** perform ACTION_SCROLL_FORWARD or ACTION_SCROLL_BACKWARD as specified
- **AND** return success or failure status

#### Scenario: Long press action
- **WHEN** a long press action is requested
- **THEN** the service SHALL find the matching node
- **AND** perform ACTION_LONG_CLICK on the node
- **AND** return success or failure status

### Requirement: Global Actions

The system SHALL perform global device actions that don't target specific elements.

#### Scenario: Back button
- **WHEN** a back action is requested
- **THEN** the service SHALL perform GLOBAL_ACTION_BACK
- **AND** return success or failure status

#### Scenario: Home button
- **WHEN** a home action is requested
- **THEN** the service SHALL perform GLOBAL_ACTION_HOME
- **AND** return success or failure status

#### Scenario: Recent apps
- **WHEN** a recent apps action is requested
- **THEN** the service SHALL perform GLOBAL_ACTION_RECENTS
- **AND** return success or failure status

#### Scenario: Notifications
- **WHEN** a notification shade action is requested
- **THEN** the service SHALL perform GLOBAL_ACTION_NOTIFICATIONS
- **AND** return success or failure status

### Requirement: Event Monitoring

The system SHALL monitor accessibility events to detect screen changes.

#### Scenario: Window change detection
- **WHEN** the active window changes (app switch, dialog open, etc.)
- **THEN** the service SHALL emit a window change event
- **AND** include the new window's package name and title

#### Scenario: Content change detection
- **WHEN** screen content changes within the current window
- **THEN** the service SHALL emit a content change event
- **AND** include information about what changed if available

#### Scenario: Event filtering
- **WHEN** subscribing to events
- **THEN** the caller SHALL be able to filter by event type
- **AND** filter by package name

### Requirement: App Launch

The system SHALL launch applications by package name.

#### Scenario: Launch installed app
- **WHEN** an app launch is requested with a package name
- **THEN** the service SHALL resolve the launch intent for that package
- **AND** start the activity
- **AND** return success or failure status

#### Scenario: App not installed
- **WHEN** an app launch is requested for an uninstalled app
- **THEN** the service SHALL return a failure status
- **AND** indicate that the app is not installed

### Requirement: Service Lifecycle Management

The system SHALL properly manage the accessibility service lifecycle.

#### Scenario: Service start
- **WHEN** the accessibility service is enabled
- **THEN** it SHALL initialize required resources
- **AND** register for appropriate accessibility events
- **AND** notify the app that it is ready

#### Scenario: Service stop
- **WHEN** the accessibility service is disabled
- **THEN** it SHALL release all resources
- **AND** cancel any pending operations
- **AND** notify the app that it is no longer available

#### Scenario: Service binding
- **WHEN** the app needs to communicate with the service
- **THEN** it SHALL be able to bind to the service
- **AND** send commands and receive responses
