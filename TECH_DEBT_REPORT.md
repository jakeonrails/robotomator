# Robotomator Tech Debt Report

**Review Date**: 2026-01-09
**Codebase**: 927 lines (3 source files), 1,182 lines (6 test files)
**Overall Health**: Moderate - Solid foundation with critical gaps

---

## Executive Summary

| Category | Status | Risk Level |
|----------|--------|------------|
| Memory Management | Critical gaps | HIGH |
| Thread Safety | Race conditions present | HIGH |
| Build Configuration | ProGuard disabled | HIGH |
| Test Coverage | ~55% estimated | MEDIUM |
| Error Handling | Inconsistent | MEDIUM |
| Performance | No optimization | MEDIUM |
| Security | Missing protections | MEDIUM |

**Immediate Action Required**: 8 critical issues must be fixed before production

---

## Critical Issues (Must Fix)

### 1. Memory Leaks in AccessibilityNodeInfo Handling
**File**: `RobotomatorAccessibilityService.kt`
**Lines**: 241-268, 437-492, 502-564

**Problem**: Missing `try-finally` blocks cause AccessibilityNodeInfo leaks on exception paths.

```kotlin
// CURRENT (BROKEN):
fun findElement(selector: ElementSelector): FindElementResult {
    val rootNode = rootInActiveWindow ?: return FindElementResult.NotFound
    val result = findElementRecursive(rootNode, selector)
    rootNode.recycle()  // Never reached if exception thrown above
    // ...
}

// FIXED:
fun findElement(selector: ElementSelector): FindElementResult {
    val rootNode = rootInActiveWindow ?: return FindElementResult.NotFound
    return try {
        val result = findElementRecursive(rootNode, selector)
        // ... process result
    } finally {
        rootNode.recycle()  // Always called
    }
}
```

**Impact**: Memory leaks accumulate over time, eventually causing OOM crashes.

**Affected Methods**:
- `findElement()` - line 241
- `performType()` - line 437
- `performScroll()` - line 502

---

### 2. Thread Safety Race Conditions
**File**: `RobotomatorAccessibilityService.kt`
**Lines**: 21-42, 47-48, 77-78

**Problem**: Singleton pattern has race condition between `instance` and `isServiceConnected` updates.

```kotlin
// CURRENT (RACE CONDITION):
override fun onServiceConnected() {
    instance = this              // Write 1
    isServiceConnected = true    // Write 2 - not atomic!
}

// Thread A reads instance (non-null) but isServiceConnected is still false
```

**Impact**: Callers may get inconsistent state - instance exists but appears disconnected (or vice versa).

**Fix**: Use atomic state update:
```kotlin
sealed class ServiceState {
    object Disconnected : ServiceState()
    data class Connected(val instance: RobotomatorAccessibilityService) : ServiceState()
}

@Volatile
private var serviceState: ServiceState = ServiceState.Disconnected

val isServiceConnected: Boolean get() = serviceState is ServiceState.Connected
fun getInstance() = (serviceState as? ServiceState.Connected)?.instance
```

---

### 3. ProGuard/R8 Disabled in Release Build
**File**: `app/build.gradle.kts`
**Line**: 22

**Problem**: `isMinifyEnabled = false` means:
- No code obfuscation (fully reverse-engineerable)
- No dead code elimination (larger APK)
- No optimization (slower runtime)

**Impact**:
- Security: Algorithms and logic exposed
- Size: ~3-5x larger APK than necessary
- Performance: Missing compiler optimizations

**Fix**:
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
    )
}
```

---

### 4. Incomplete ProGuard Rules
**File**: `app/proguard-rules.pro`

**Problem**: Missing keep rules for Kotlin features that will crash in release:

**Missing Rules**:
```proguard
# Coroutines (CRITICAL - will crash without these)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Data classes (fields will be stripped)
-keep class com.robotomator.app.PermissionStatus { *; }
-keep class com.robotomator.app.**$Companion { *; }

# Accessibility framework callbacks
-keep class * extends android.accessibilityservice.AccessibilityService {
    public <methods>;
    protected <methods>;
}
```

---

### 5. Instance Leak Risk via Static Reference
**File**: `RobotomatorAccessibilityService.kt`
**Line**: 37

**Problem**: Strong reference to service instance in companion object.

```kotlin
@Volatile
private var instance: RobotomatorAccessibilityService? = null
```

**Impact**: If anything retains this reference after service destruction, the entire service context leaks.

**Fix**: Use WeakReference:
```kotlin
private var instance: WeakReference<RobotomatorAccessibilityService>? = null

fun getInstance(): RobotomatorAccessibilityService? = instance?.get()
```

---

### 6. Main Thread IPC in Polling Loop
**File**: `MainActivity.kt`
**Lines**: 148-159

**Problem**: Permission check performs Binder IPC on main thread every second.

```kotlin
// Settings.Secure.getString() is IPC - can take 1-100ms
while (isActive) {
    delay(1000)
    updatePermissionStatus()  // IPC on main thread!
}
```

**Impact**: Frame drops, potential ANR under system load.

**Fix**:
```kotlin
val status = withContext(Dispatchers.IO) {
    PermissionUtils.getPermissionStatus(this@MainActivity)
}
```

---

### 7. Placeholder Tests Providing False Coverage
**File**: `AccessibilityServiceTest.kt`

**Problem**: All tests are fake:
```kotlin
@Test
fun testInitialServiceState() {
    assertTrue("Test placeholder", true)  // Tests nothing!
}
```

**Impact**: False confidence in test coverage. Service lifecycle is completely untested.

**Fix**: Delete file or implement real tests for:
- `onServiceConnected()` behavior
- `onDestroy()` cleanup
- `getInstance()` singleton access

---

### 8. No Tree Traversal Depth Limit
**File**: `RobotomatorAccessibilityService.kt`
**Lines**: 276-303

**Problem**: Recursive traversal with no depth limit.

```kotlin
private fun findElementRecursive(node, selector): AccessibilityNodeInfo? {
    // No depth check - could stack overflow on deep/cyclic trees
    for (i in 0 until node.childCount) {
        findElementRecursive(child, selector)  // Recursive
    }
}
```

**Impact**: Stack overflow on pathologically deep or malformed accessibility trees.

**Fix**: Add depth parameter:
```kotlin
private fun findElementRecursive(
    node: AccessibilityNodeInfo,
    selector: ElementSelector,
    depth: Int = 0,
    maxDepth: Int = 50
): AccessibilityNodeInfo? {
    if (depth > maxDepth) {
        Log.w(TAG, "Max depth exceeded")
        return null
    }
    // ... recursive calls use depth + 1
}
```

---

## High Priority Issues

### 9. Code Duplication in Interaction Methods
**Location**: `performTap()`, `performLongPress()` - 80% identical code

**Recommendation**: Extract common pattern:
```kotlin
private fun performNodeAction(selector: ElementSelector, action: Int, name: String): InteractionResult
```

### 10. Missing Error Distinction
**Location**: `PermissionUtils.kt:69-73`

**Problem**: Can't distinguish "permission disabled" from "error checking permission".

**Fix**: Return sealed class instead of Boolean.

### 11. Event Type Over-Subscription
**Location**: `accessibility_service_config.xml:4`

**Problem**: `typeAllMask` receives ALL system events (hundreds per second).

**Impact**: Battery drain, CPU overhead.

**Recommendation**: Narrow to needed events:
```xml
android:accessibilityEventTypes="typeWindowStateChanged|typeWindowContentChanged|typeViewClicked"
```

### 12. Backup Security Risk
**Location**: `AndroidManifest.xml:6`

**Problem**: `allowBackup="true"` allows ADB backup extraction.

**Fix**: `android:allowBackup="false"` for security-sensitive app.

---

## Medium Priority Issues

### 13. Outdated Dependencies
| Dependency | Current | Latest |
|------------|---------|--------|
| Android Gradle Plugin | 8.2.0 | 8.7.3 |
| Kotlin | 1.9.20 | 2.1.0 |
| core-ktx | 1.12.0 | 1.15.0 |
| lifecycle | 2.7.0 | 2.8.7 |
| coroutines | 1.7.3 | 1.9.0 |

### 14. Inefficient Polling-Based Monitoring
**Location**: `MainActivity.kt:142-164`

**Problem**: Polls every second even when nothing changes.

**Better**: Use ContentObserver on Settings.Secure.

### 15. No Input Validation on Selectors
**Location**: `RobotomatorAccessibilityService.kt:195-206`

**Problem**: No length limits on selector strings - DoS vector if exposed externally.

### 16. RTL Locale Scroll Direction Bug
**Location**: `RobotomatorAccessibilityService.kt:539-548`

**Problem**: LEFT/RIGHT scroll direction wrong for RTL locales.

### 17. Exception Swallowing in Monitoring Loop
**Location**: `MainActivity.kt:160-162`

**Problem**: Errors logged but UI not updated - user sees frozen state.

---

## Test Quality Issues

### Coverage Gaps
- **0%**: Service lifecycle (`onServiceConnected`, `onDestroy`, `onUnbind`)
- **0%**: `onAccessibilityEvent()` handling
- **0%**: Memory management (node recycling verification)
- **~30%**: MainActivity behavior

### Flaky Test Risks
- `testMultipleCallsReturnConsistentResults` - race condition
- `testUIShowsValidState` - depends on actual device state

### Missing Test Categories
- Integration tests (full flow: find → tap → verify)
- Performance tests (traversal timing)
- Concurrency tests (thread safety)

---

## Spec Compliance Gaps

| Spec Requirement | Status |
|-----------------|--------|
| Screen Content Reading | NOT IMPLEMENTED |
| App Launch | NOT IMPLEMENTED |
| Event Listener API | NOT IMPLEMENTED |
| Permission Change Monitoring | NOT IMPLEMENTED |
| Global Actions | COMPLETE |
| Element Interaction | COMPLETE |
| Service Lifecycle | PARTIAL |

---

## Remediation Roadmap

### Phase 1: Critical Security & Stability (1-2 days)
1. Enable ProGuard with complete rules
2. Fix memory leaks with try-finally
3. Fix thread safety in singleton
4. Add tree traversal depth limit

### Phase 2: Build & Test Quality (2-3 days)
1. Update dependencies
2. Delete/fix placeholder tests
3. Add service lifecycle tests
4. Move permission check to background thread

### Phase 3: Technical Debt (1 week)
1. Refactor duplicated interaction code
2. Add input validation
3. Replace polling with ContentObserver
4. Fix RTL scroll direction

### Phase 4: Spec Compliance (ongoing)
1. Implement screen content reading API
2. Implement app launch API
3. Add event listener system
4. Add permission change monitoring

---

## Metrics to Track

| Metric | Current | Target |
|--------|---------|--------|
| Test Coverage | ~55% | >80% |
| Critical Bugs | 8 | 0 |
| High Bugs | 4 | 0 |
| ProGuard Enabled | No | Yes |
| Memory Leaks | Unknown | 0 |
