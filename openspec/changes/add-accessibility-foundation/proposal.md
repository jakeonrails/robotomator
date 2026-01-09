# Change: Add Accessibility Service Foundation (Group A)

## Why
Robotomator needs the foundational accessibility service to interact with Android UI. Without this core capability, the app cannot see screen content, tap buttons, type text, or perform any automation tasks. This is the critical first milestone that unlocks all other features.

## What Changes
- Initialize Android project structure with Kotlin, Gradle, and MVVM architecture
- Implement AccessibilityService registration and configuration
- Add permission detection and guided permission request flow
- Implement global actions (back, home, recents, notifications)
- Create element interaction APIs (tap, type, scroll, long press)
- Build screen content reading via accessibility node tree traversal
- Add event monitoring for screen change detection
- Implement app launching by package name
- Establish service lifecycle management (start, stop, bind)

## Impact
- Affected specs: `accessibility-service`
- Affected code: Creates entire Android project foundation
  - `app/src/main/` - Main source tree
  - `app/src/main/AndroidManifest.xml` - Service registration
  - `app/src/main/java/com/robotomator/` - Kotlin source root
  - `app/build.gradle.kts` - Build configuration
  - `settings.gradle.kts` - Project settings
