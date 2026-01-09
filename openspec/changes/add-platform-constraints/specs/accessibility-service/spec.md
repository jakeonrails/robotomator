## ADDED Requirements

### Requirement: Minimum SDK Version

The system SHALL target Android API 24 (Android 7.0 Nougat) as the minimum supported version.

#### Scenario: API 24 feature usage
- **WHEN** implementing accessibility features
- **THEN** the system MAY use APIs available in API 24+
- **AND** SHALL NOT use APIs requiring higher versions without version checks

#### Scenario: Gesture dispatch availability
- **WHEN** gesture-based interactions are needed
- **THEN** the system SHALL use dispatchGesture() (API 24+)
- **AND** this enables swipe, pinch, and custom gesture automation

### Requirement: Platform Limitations

The system SHALL gracefully handle known Android platform limitations that prevent automation.

#### Scenario: Secure window detection
- **WHEN** attempting to read a window with FLAG_SECURE set
- **THEN** the system SHALL detect that content is unavailable
- **AND** return an error indicating "secure content cannot be read"
- **AND** NOT attempt to capture or process the content
- **AND** inform the user that this app/screen cannot be automated

#### Scenario: Accessibility-blocking apps
- **WHEN** an app actively blocks or interferes with accessibility services
- **THEN** the system SHALL detect reduced functionality (empty or minimal node tree)
- **AND** warn the user that automation may not work for this app
- **AND** suggest checking if the app has accessibility restrictions

#### Scenario: WebView limitations
- **WHEN** automating WebView content
- **THEN** the system SHALL handle potentially limited accessibility node exposure
- **AND** fallback to coordinate-based interaction if nodes are unavailable
- **AND** warn the user about reduced reliability for web content

#### Scenario: Custom view limitations
- **WHEN** encountering custom views with IMPORTANT_FOR_ACCESSIBILITY_NO
- **THEN** the system SHALL skip these elements in screen representation
- **AND** log that hidden elements exist
- **AND** NOT fail the entire screen read

### Requirement: OEM Compatibility

The system SHALL account for OEM-specific Android variations.

#### Scenario: Battery optimization interference
- **WHEN** running on devices with aggressive battery optimization (Xiaomi, Huawei, Samsung)
- **THEN** the system SHALL detect if it's being killed unexpectedly
- **AND** guide users to exclude the app from battery optimization
- **AND** provide OEM-specific instructions where known

#### Scenario: Accessibility service variations
- **WHEN** accessibility behavior differs between OEMs
- **THEN** the system SHALL use standard APIs where possible
- **AND** document known OEM-specific issues
- **AND** recommend testing on major OEM devices (Samsung, Xiaomi, Google Pixel)

#### Scenario: Permission UI variations
- **WHEN** directing users to enable accessibility service
- **THEN** the system SHALL handle that the Settings UI differs by OEM
- **AND** provide general instructions that work across devices
- **AND** offer troubleshooting guidance if user cannot find the setting

### Requirement: Advanced Gesture Support

The system SHALL support gesture-based interactions beyond simple taps.

#### Scenario: Swipe gesture
- **WHEN** a swipe action is requested
- **THEN** the system SHALL use dispatchGesture() with a stroke path
- **AND** support configurable start/end coordinates
- **AND** support configurable duration

#### Scenario: Long press with gesture
- **WHEN** ACTION_LONG_CLICK is unsupported by target element
- **THEN** the system SHALL fallback to dispatchGesture() with a long-duration tap
- **AND** use element center coordinates

#### Scenario: Pinch gesture
- **WHEN** a pinch/zoom gesture is requested
- **THEN** the system SHALL use dispatchGesture() with two-finger stroke paths
- **AND** support pinch-in and pinch-out directions

## MODIFIED Requirements

### Requirement: App Launch

The system SHALL launch applications by package name through coordination with the main app process.

#### Scenario: Launch installed app
- **WHEN** an app launch is requested with a package name
- **THEN** the main app process (not AccessibilityService) SHALL resolve the launch intent
- **AND** start the activity using startActivity()
- **AND** the accessibility service SHALL detect the window change
- **AND** return success when the target app's window is active

#### Scenario: Launch coordination
- **WHEN** the accessibility service needs to launch an app
- **THEN** it SHALL communicate with the main app via IPC (bound service, broadcast, or shared repository)
- **AND** the main app SHALL perform the actual launch
- **AND** the service SHALL wait for the window change event

#### Scenario: App not installed
- **WHEN** an app launch is requested for an uninstalled app
- **THEN** the main app SHALL check package availability before launching
- **AND** return a failure status indicating "app not installed"
- **AND** include the package name in the error
