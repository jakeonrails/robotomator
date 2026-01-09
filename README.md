# Robotomator

AI-powered Android automation platform. Describe automations in natural language, and the AI generates executable scripts using Android Accessibility APIs.

## Key Features

- **Natural Language Input**: Create automations by describing them in plain English
- **AI Recovery**: Automatically handles unexpected UI states (modals, A/B tests, layout changes)
- **Progressive Scripting**: AI learns apps by navigating them and building scripts iteratively
- **Accessibility-First**: Uses Android Accessibility Service for reliable interaction

## Tech Stack

- Kotlin / Android (API 26+)
- Claude API for AI capabilities
- Room for local storage

## Status

**Current Progress**: Group A (The Foundation) - Items 1-5/9 Complete

✅ Service Registration - AccessibilityService is registered and ready to connect
✅ Permission Detection - Can detect if accessibility service is enabled
✅ Permission Request Flow - Complete UI for guiding users through permission setup
✅ Global Actions - Back, home, recents, notifications, quick settings, and power dialog
✅ Element Interactions - Tap, type, scroll, long press, and swipe gestures

See `ROADMAP.md` for the complete development plan.

## Building the Project

This is a standard Android project built with Gradle and Kotlin.

### Prerequisites

- JDK 17 or later
- Android SDK with API level 34
- Android Studio (recommended) or command-line tools

### Build Commands

```bash
# Build the app
./gradlew build

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

### Project Structure

```
app/src/main/
├── java/com/robotomator/app/
│   ├── RobotomatorAccessibilityService.kt  # Core AccessibilityService implementation
│   ├── PermissionUtils.kt                  # Permission detection utilities
│   └── MainActivity.kt                     # Main UI with permission flow
├── res/
│   ├── layout/
│   │   └── activity_main.xml                # Main activity layout
│   ├── values/
│   │   ├── strings.xml                      # String resources
│   │   └── colors.xml                       # Color definitions
│   └── xml/
│       └── accessibility_service_config.xml # Service configuration
└── AndroidManifest.xml                      # App manifest with service registration
```

## Implementation Notes

### Group A: Foundation (Items 1-5 Complete)

#### 1. Service Registration (Completed)

The `RobotomatorAccessibilityService` class extends Android's `AccessibilityService` and registers with the system to:

- Receive accessibility events from all apps
- Read screen content via the accessibility node tree
- Perform gestures and global actions
- Monitor screen changes in real-time

Key files:
- `RobotomatorAccessibilityService.kt` at app/src/main/java/com/robotomator/app/RobotomatorAccessibilityService.kt:1
- `accessibility_service_config.xml` at app/src/main/res/xml/accessibility_service_config.xml:1
- Service registration in `AndroidManifest.xml` at app/src/main/AndroidManifest.xml:25

#### 2. Permission Detection (Completed)

The `PermissionUtils` object provides utilities to check accessibility service status:

- `isAccessibilityServiceEnabled()` - Checks if service is enabled in system settings
- `getPermissionStatus()` - Returns detailed status with helpful descriptions
- `isFullyOperational()` - One-line check if service is ready for automation

Key file: `PermissionUtils.kt` at app/src/main/java/com/robotomator/app/PermissionUtils.kt:1

#### 3. Permission Request Flow (Completed)

The `MainActivity` provides a polished user experience for permission setup:

- Real-time permission monitoring
- Clear state-specific messaging
- One-tap access to accessibility settings
- Smooth transitions between permission states

Key files:
- `MainActivity.kt` at app/src/main/java/com/robotomator/app/MainActivity.kt:1
- `activity_main.xml` at app/src/main/res/layout/activity_main.xml:1

#### 4. Global Actions (Completed)

The accessibility service now supports all major global navigation actions:

- `BACK` - Navigate back (back button)
- `HOME` - Go to home screen
- `RECENTS` - Open recent apps/multitasking
- `NOTIFICATIONS` - Open notification shade
- `QUICK_SETTINGS` - Open quick settings panel
- `POWER_DIALOG` - Show power dialog

Each action returns a typed result indicating success or specific failure reasons (service not connected, system denied, etc.).

Key implementation: `RobotomatorAccessibilityService.kt` at app/src/main/java/com/robotomator/app/RobotomatorAccessibilityService.kt:95

#### 5. Element Interactions (Completed)

Full support for interacting with UI elements through the accessibility node tree:

- **Click Actions**: `tap()`, `longPress()` with customizable durations
- **Text Input**: `typeText()` with focus management and clipboard support
- **Scrolling**: `scrollForward()`, `scrollBackward()`, directional scrolling
- **Gestures**: `swipe()` with configurable direction, duration, and distance
- **Element Finding**: Robust selectors by text, ID, content description, or custom predicates
- **Result Tracking**: Detailed success/failure reporting for each action

All interactions include proper error handling, node recycling to prevent memory leaks, and configurable timeouts.

Key implementation: `RobotomatorAccessibilityService.kt` at app/src/main/java/com/robotomator/app/RobotomatorAccessibilityService.kt:189

### Testing

Comprehensive test coverage includes:
- 7 unit tests for permission status logic
- 8 instrumented tests for permission utilities
- 10 instrumented tests for main activity UI
- 3 service lifecycle documentation tests
- 227 lines of global actions tests (6 test cases)
- 526 lines of element interaction tests (comprehensive coverage)

See `TEST_REVIEW.md` for complete test documentation.

## Development

See `openspec/` for detailed specifications and `ROADMAP.md` for the development plan.
