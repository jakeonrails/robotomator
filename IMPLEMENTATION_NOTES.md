# Implementation Notes

## Service Registration (COMPLETED)

**Date**: January 9, 2026
**Priority**: Group A, Item 1 of 9
**Status**: ✅ Complete

### What Was Implemented

The Android AccessibilityService has been fully registered and configured. This provides the foundation for all automation capabilities.

### Key Components

#### 1. RobotomatorAccessibilityService
**Location**: `app/src/main/java/com/robotomator/app/RobotomatorAccessibilityService.kt`

This class extends `AccessibilityService` and provides:
- **Lifecycle Management**: Proper handling of `onServiceConnected`, `onInterrupt`, `onDestroy`, and `onUnbind`
- **Global State Tracking**: A static `isServiceConnected` flag that other components can query
- **Event Reception**: `onAccessibilityEvent()` method ready to receive all accessibility events
- **Comprehensive Logging**: Debug logging at all lifecycle stages

The service is currently a "skeleton" implementation with extensive TODO comments marking where future features will be added:
- Screen representation building
- Element interaction methods
- Screen change detection
- Script execution integration

#### 2. Service Configuration
**Location**: `app/src/main/res/xml/accessibility_service_config.xml`

Configured with:
- **Event Types**: `typeAllMask` - receives all accessibility event types
- **Flags**:
  - `flagDefault` - standard service behavior
  - `flagRetrieveInteractiveWindows` - access to dialogs, overlays, etc.
  - `flagReportViewIds` - includes resource IDs (critical for stable selectors)
- **Capabilities**:
  - `canRetrieveWindowContent="true"` - read the accessibility node tree
  - `canPerformGestures="true"` - programmatic touch gestures (API 24+)
- **Timing**: `notificationTimeout="100"` - catch rapid UI changes

#### 3. Manifest Registration
**Location**: `app/src/main/AndroidManifest.xml`

The service is registered with:
- Required permission: `android.permission.BIND_ACCESSIBILITY_SERVICE`
- Intent filter for: `android.accessibilityservice.AccessibilityService`
- Meta-data linking to configuration XML
- Exported for system access

#### 4. User-Facing Strings
**Location**: `app/src/main/res/values/strings.xml`

Includes:
- Clear service description explaining what permissions are needed and why
- Privacy-conscious messaging about local processing and AI consent
- Service label for system settings

### Project Structure Created

```
robotomator/
├── app/
│   ├── build.gradle.kts          # App-level build configuration
│   ├── proguard-rules.pro        # ProGuard configuration
│   └── src/main/
│       ├── AndroidManifest.xml   # App manifest with service registration
│       ├── java/com/robotomator/app/
│       │   └── RobotomatorAccessibilityService.kt
│       └── res/
│           ├── drawable/
│           │   └── ic_launcher_foreground.xml
│           ├── mipmap-*/         # Launcher icons (all densities)
│           ├── values/
│           │   ├── colors.xml
│           │   └── strings.xml
│           └── xml/
│               ├── accessibility_service_config.xml
│               ├── backup_rules.xml
│               └── data_extraction_rules.xml
├── gradle/wrapper/               # Gradle wrapper
├── build.gradle.kts              # Root build configuration
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle properties
├── gradlew                       # Gradle wrapper script (Unix)
├── gradlew.bat                   # Gradle wrapper script (Windows)
└── local.properties              # Local SDK configuration (template)
```

### Build Configuration

- **Android Gradle Plugin**: 8.2.0
- **Kotlin**: 1.9.20
- **Gradle**: 8.2
- **Compile SDK**: 34 (Android 14)
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34

### Dependencies Added

Core Android libraries:
- `androidx.core:core-ktx:1.12.0`
- `androidx.appcompat:appcompat:1.6.1`
- `com.google.android.material:material:1.11.0`

Coroutines for async operations:
- `kotlinx-coroutines-android:1.7.3`

Lifecycle components:
- `androidx.lifecycle:lifecycle-runtime-ktx:2.7.0`
- `androidx.lifecycle:lifecycle-service:2.7.0`

Testing frameworks:
- JUnit 4.13.2
- AndroidX Test libraries

### Privacy & Security

Backup exclusions configured in `backup_rules.xml` and `data_extraction_rules.xml`:
- Automation database excluded from cloud backup
- API keys excluded from all backups and transfers
- Automations allowed in device-to-device transfer

### How to Enable the Service

Once the app is installed on a device:

1. Navigate to **Settings > Accessibility**
2. Find **Robotomator Automation** in the list
3. Toggle the service ON
4. Accept the permission dialog

The service will then begin receiving accessibility events from all apps.

### Testing the Implementation

To verify the service is working:

1. Build and install the app: `./gradlew installDebug`
2. Enable the service in accessibility settings
3. Connect to device via ADB: `adb logcat -s RobotomatorA11yService:*`
4. Navigate around the device
5. Watch for log messages:
   - "Robotomator Accessibility Service connected!" when enabled
   - Event logs showing accessibility events being received
   - "Service interrupted" or "disconnected" messages when disabled

### Next Steps

With Service Registration complete, the next items in Group A are:

**Priority 2**: Permission Detection
- Detect whether accessibility service is enabled
- Provide a programmatic way to check service status
- UI integration (when Group D is implemented)

**Priority 3**: Permission Request Flow
- Create a friendly onboarding experience
- Guide users through the accessibility settings flow
- Implement deep-linking to accessibility settings

**Priority 4**: Global Actions
- Implement `performGlobalAction()` for back, home, recents, notifications
- Create a clean API for executing global actions
- Error handling and permission checks

### Technical Notes

#### Accessibility Service Lifecycle

The service follows this lifecycle:
1. **System Bind**: Android binds to the service when enabled in settings
2. **onServiceConnected()**: Service is now active, receives `AccessibilityServiceInfo`
3. **onAccessibilityEvent()**: Receives events as UI changes occur system-wide
4. **onInterrupt()**: System requests temporary pause (rare)
5. **onUnbind()** / **onDestroy()**: Service is disabled or app is killed

#### Performance Considerations

Accessibility services can impact battery and performance if not careful:
- Events arrive on the main thread by default
- Future implementations MUST move heavy processing to background threads
- Screen representation caching will be critical (Track C3)
- Event debouncing will prevent flood scenarios (Track C3)

#### Known Limitations

- Service cannot automate apps that actively block accessibility services
- Limited to visible UI elements only
- Different OEMs may implement accessibility APIs slightly differently
- Service requires explicit user permission (cannot be granted programmatically)

### Code Quality

The implementation follows Kotlin and Android best practices:
- Immutable `val` declarations where possible
- Nullable safety with explicit `?` operators
- Comprehensive logging with appropriate levels (INFO, DEBUG, WARN)
- Clear TODO comments marking future extension points
- Descriptive variable and function names
- Resource strings externalized for localization

### Build System

The project uses:
- **Kotlin DSL** for Gradle build files (modern approach)
- **Version catalogs**: Not yet, but recommended for future dependency management
- **ProGuard**: Configured but not enabled for debug builds
- **Gradle wrapper**: Committed for consistent builds across machines

### Documentation

All key files include:
- XML comments explaining configuration choices
- Kotlin KDoc-style comments on classes
- Inline comments for complex logic
- README updated with build instructions and project structure
- ROADMAP updated with completion status

---

## Build Instructions

### Prerequisites

- **JDK 17+** (JDK 21 recommended)
- **Android SDK** with API level 34
- **Android Studio** Hedgehog (2023.1.1) or later (recommended)
- Or **Android command-line tools** with gradle

### First-Time Setup

1. **Clone the repository** (if not already done)
2. **Configure Android SDK location**:
   - Option A: Set `ANDROID_HOME` environment variable
   - Option B: Edit `local.properties` and set `sdk.dir=/path/to/android/sdk`
3. **Sync Gradle**: `./gradlew --refresh-dependencies`

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (unsigned)
./gradlew assembleRelease

# Install on connected device/emulator
./gradlew installDebug

# Run unit tests
./gradlew test

# Run connected tests (requires device/emulator)
./gradlew connectedAndroidTest

# Clean build artifacts
./gradlew clean
```

### Android Studio Setup

1. Open Android Studio
2. **File > Open** and select the `robotomator` directory
3. Wait for Gradle sync to complete
4. Configure Android SDK if prompted
5. Run configuration will be auto-created

---

## Commit Message (For Reference)

```
Add AccessibilityService registration and Android project foundation

Implement Group A, Priority 1: Service Registration

- Create RobotomatorAccessibilityService extending AccessibilityService
- Configure service to receive all accessibility event types
- Enable window content retrieval and gesture performance
- Register service in AndroidManifest with required permissions
- Add user-facing service description with privacy messaging
- Set up complete Android project structure with Gradle/Kotlin
- Configure build with Kotlin DSL, API 26+, target SDK 34
- Add lifecycle logging for debugging
- Include backup rules to exclude sensitive data
- Create launcher icons and resources

The service is now registered and ready to receive accessibility events
when enabled by the user. Future iterations will add permission detection,
global actions, element interactions, and screen content reading.

Resolves: Group A, Item 1/9
```

---

## Permission Detection (COMPLETED)

**Date**: January 9, 2026
**Priority**: Group A, Item 2 of 9
**Status**: ✅ Complete

### What Was Implemented

A comprehensive permission detection system that provides the "golden ticket" detection - knowing whether Robotomator has been granted accessibility permissions by the user.

### Key Component

#### PermissionUtils
**Location**: `app/src/main/java/com/robotomator/app/PermissionUtils.kt`

This Kotlin object provides three levels of permission checking:

1. **`isAccessibilityServiceEnabled(context: Context): Boolean`**
   - Checks if the service is enabled in Android system settings
   - Queries `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`
   - Parses the colon-separated list of enabled service component names
   - Returns `true` if our service is found in the list

2. **`getPermissionStatus(context: Context): PermissionStatus`**
   - Returns a complete picture of permission state
   - Includes both settings status AND service connection status
   - Useful for diagnostic purposes and detailed UI feedback

3. **`isFullyOperational(context: Context): Boolean`**
   - The primary check most code should use
   - Returns `true` only when both permission is granted AND service is connected
   - Simple, reliable check for "ready to automate"

#### PermissionStatus Data Class

A comprehensive status object with:

**Properties:**
- `isEnabled: Boolean` - Permission granted in Settings
- `isServiceConnected: Boolean` - Service actively bound and running

**Computed Properties:**
- `isFullyOperational: Boolean` - Both enabled and connected
- `isWaitingForConnection: Boolean` - Permission granted but not yet connected
- `needsPermission: Boolean` - User needs to enable in accessibility settings

**Methods:**
- `getStatusDescription(): String` - Human-readable status for debugging/UI

### Status States Explained

The system distinguishes between four possible states:

1. **Fully Operational** (`enabled=true, connected=true`)
   - The golden ticket! Everything works
   - User has granted permission and service is active
   - Ready to automate

2. **Waiting for Connection** (`enabled=true, connected=false`)
   - User granted permission but service not yet started
   - Temporary state during system startup
   - Should resolve automatically within seconds

3. **Needs Permission** (`enabled=false, connected=false`)
   - User hasn't enabled the service yet
   - App should guide user to accessibility settings
   - Normal state for fresh installs

4. **Edge Case** (`enabled=false, connected=true`)
   - Service is being disabled
   - Brief window during settings change
   - Rare and transient

### Technical Implementation

#### How It Works

The permission check:
1. Gets the package name of our app
2. Constructs the expected component name: `com.robotomator.app/com.robotomator.app.RobotomatorAccessibilityService`
3. Queries system settings for `ENABLED_ACCESSIBILITY_SERVICES`
4. Parses the colon-separated string of enabled services
5. Checks if our component name is in the list
6. Combines with service connection state from `RobotomatorAccessibilityService.isServiceConnected`

#### Why Two Checks Are Needed

**Settings Check (`isEnabled`):**
- Reflects what the user configured in Settings
- Persists across reboots
- Can be `true` even if service isn't currently running

**Connection Check (`isServiceConnected`):**
- Reflects whether the service is actively bound
- Set by `onServiceConnected()` and cleared by `onUnbind()`/`onDestroy()`
- Can be temporarily `false` during startup even if settings say `enabled`

**Together:** They provide a complete picture of permission status.

### Usage Examples

```kotlin
// Simple check - is everything working?
if (PermissionUtils.isFullyOperational(context)) {
    // Start automation
    executeScript()
} else {
    // Show permission request flow
    showOnboarding()
}

// Detailed status for UI
val status = PermissionUtils.getPermissionStatus(context)
when {
    status.isFullyOperational -> showAutomationScreen()
    status.isWaitingForConnection -> showLoadingIndicator()
    status.needsPermission -> showPermissionRequest()
}

// Diagnostic logging
Log.d(TAG, status.getStatusDescription())
// Output: "Fully operational - ready to automate!"
```

### Integration with Existing Service

The permission detection integrates seamlessly with the service registration from Priority 1:
- Reads `RobotomatorAccessibilityService.isServiceConnected` for connection state
- Uses the service's class name to construct the component name
- Provides the missing piece: checking if permission is granted
- Service tracks connection, utils check settings - perfect separation of concerns

### Future Integration Points

This permission detection enables:
- **Priority 3 (Permission Request Flow)**: Detect when to show onboarding
- **Group D UI**: Display permission status in the app
- **Script Execution**: Check if ready before running automation
- **Settings Screen**: Show current permission state
- **Debugging**: Diagnose permission issues

### Design Decisions

**Why an Object Instead of Class?**
- No state to maintain
- All methods are utility functions
- Single global truth about permission status
- Kotlin `object` provides clean singleton pattern

**Why Return a Data Class?**
- Immutable status snapshot
- Type-safe with Kotlin data classes
- Provides computed properties for common checks
- Human-readable description method

**Why Both Settings and Connection Checks?**
- Settings check: What user configured (persistent)
- Connection check: What's actually running (transient)
- Together: Complete, accurate picture
- Prevents false positives during startup/shutdown

### Testing Considerations

To test permission detection:

1. **Fresh Install State**:
   - Install app
   - Check `needsPermission` is `true`
   - Check `isFullyOperational` is `false`

2. **After Granting Permission**:
   - Enable service in Settings
   - Check `isEnabled` becomes `true`
   - Wait for `isServiceConnected` to become `true`
   - Check `isFullyOperational` becomes `true`

3. **After Disabling**:
   - Disable service in Settings
   - Check `isEnabled` becomes `false`
   - Check `isServiceConnected` becomes `false`

4. **Startup Scenario**:
   - Reboot device with service enabled
   - Check `isEnabled` is `true` immediately
   - Check `isServiceConnected` becomes `true` within seconds

### Code Quality

The implementation follows best practices:
- Comprehensive KDoc comments explaining each method
- Explains the "why" not just the "what"
- Clear separation of concerns
- Type-safe with Kotlin data classes
- Immutable data structures
- Null-safe with proper `isNullOrEmpty()` checks
- Efficient string parsing with `TextUtils.SimpleStringSplitter`

### Performance

The permission check is lightweight:
- Simple Settings.Secure query (cached by Android)
- String parsing is O(n) where n = number of enabled services (typically <10)
- Static variable read for connection status
- No network or disk I/O
- Safe to call frequently
- Can be called from UI thread without blocking

### Next Steps

With Permission Detection complete, Priority 3 is next:

**Priority 3: Permission Request Flow**
- Create MainActivity with permission check
- Deep-link to accessibility settings
- User-friendly onboarding UI
- Handle back navigation from settings
- Periodic permission checks
- Beautiful empty state design

---

*This document will be updated as implementation progresses through the roadmap.*
