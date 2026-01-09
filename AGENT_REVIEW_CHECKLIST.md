# Agent Code Review Checklist

Use this checklist after completing a coding task to ensure code quality, robustness, performance, and security before marking work as done.

---

## Quick Pre-Commit Check (30 seconds)

Before committing, verify:
- [ ] Code compiles without errors
- [ ] No `TODO` comments left unaddressed (or they're intentional and tracked)
- [ ] No hardcoded secrets, API keys, or credentials
- [ ] No `println` or debug logging left in production paths
- [ ] File names match class names (Kotlin convention)

---

## 1. Memory Management (Android-Specific)

### AccessibilityNodeInfo Handling
- [ ] **Every `AccessibilityNodeInfo` is recycled** via `recycle()` after use
- [ ] **Use `try-finally`** to ensure recycling even on exceptions:
  ```kotlin
  val node = findNode() ?: return
  try {
      // use node
  } finally {
      node.recycle()
  }
  ```
- [ ] **Document ownership** when returning nodes to callers
- [ ] **Never store nodes** in fields or collections (they become stale)

### General Android Memory
- [ ] **Avoid holding Activity/Context references** in long-lived objects
- [ ] **Use `WeakReference`** for callbacks that might outlive their source
- [ ] **Clear view references** in `onDestroyView()` for Fragments
- [ ] **Cancel coroutines** when lifecycle ends (use `lifecycleScope` or `viewModelScope`)

### Memory Checklist
```
For each method that acquires a resource:
[ ] Is the resource released in ALL code paths?
[ ] What happens if an exception is thrown?
[ ] Is there a finally block or use() call?
[ ] Are there any early returns before cleanup?
```

---

## 2. Thread Safety

### Shared State
- [ ] **Identify all shared mutable state** (companion objects, singletons, static fields)
- [ ] **Use `@Volatile`** for simple flags accessed from multiple threads
- [ ] **Use `AtomicReference`/`AtomicBoolean`** for compare-and-swap operations
- [ ] **Use synchronized blocks** when multiple fields must change together

### Common Race Condition Patterns to Avoid
```kotlin
// BAD: Check-then-act race condition
if (instance != null) {
    instance.doSomething()  // instance could become null between check and use
}

// GOOD: Snapshot the value
val localInstance = instance
if (localInstance != null) {
    localInstance.doSomething()
}

// BAD: Non-atomic compound update
isConnected = true
instance = this  // These are two separate writes!

// GOOD: Atomic state
sealed class State { ... }
@Volatile var state: State = Disconnected
```

### Thread Safety Checklist
```
For each public method:
[ ] Can it be called from multiple threads?
[ ] Does it access any shared mutable state?
[ ] Is the access properly synchronized?
[ ] Are there any check-then-act patterns?
```

---

## 3. Error Handling

### Exception Handling
- [ ] **Catch specific exceptions**, not generic `Exception` or `Throwable`
- [ ] **Never swallow exceptions silently** - at minimum log them
- [ ] **Provide meaningful error messages** to callers
- [ ] **Use sealed classes for result types** instead of nullable returns + exceptions

### Error Handling Patterns
```kotlin
// BAD: Swallowed exception
try {
    riskyOperation()
} catch (e: Exception) {
    // silently ignored
}

// BAD: Generic exception
} catch (e: Exception) {
    return Error("Something went wrong")
}

// GOOD: Specific handling
} catch (e: IllegalStateException) {
    Log.w(TAG, "Service disconnected", e)
    return Result.ServiceDisconnected
} catch (e: SecurityException) {
    Log.e(TAG, "Permission revoked", e)
    return Result.PermissionDenied
}
```

### Error Handling Checklist
```
For each try-catch block:
[ ] Is the caught exception type specific enough?
[ ] Is the exception logged with context?
[ ] Does the caller receive enough information to respond?
[ ] Are there any paths where errors are silently ignored?
```

---

## 4. Performance

### Avoid Common Performance Pitfalls
- [ ] **No blocking operations on main thread** (IO, network, heavy computation)
- [ ] **Use `Dispatchers.IO`** for disk/network operations
- [ ] **Avoid string concatenation in loops** - use StringBuilder
- [ ] **Guard log statements** to avoid string allocation when logging disabled:
  ```kotlin
  if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(TAG, "Expensive string: $complexObject")
  }
  ```
- [ ] **Add depth limits** to recursive functions
- [ ] **Cache expensive computations** when safe to do so

### Android-Specific Performance
- [ ] **Minimize accessibility event processing** - filter early, return fast
- [ ] **Debounce rapid events** (100ms is a good default)
- [ ] **Use indexed lookups** instead of full tree traversal when possible
- [ ] **Recycle objects** instead of allocating new ones in hot paths

### Performance Checklist
```
For each method:
[ ] What thread does this run on?
[ ] How long does it take to execute?
[ ] Is it called frequently? (hot path)
[ ] Are there any allocations that could be avoided?
[ ] Is there any blocking I/O?
```

---

## 5. Security

### Input Validation
- [ ] **Validate all external inputs** (user input, intent extras, IPC)
- [ ] **Set length limits** on string inputs to prevent DoS
- [ ] **Sanitize data** before logging (no passwords, tokens, PII)
- [ ] **Use parameterized queries** for any database operations

### Android Security
- [ ] **No sensitive data in logs** (even debug logs can be extracted)
- [ ] **Use encrypted storage** for credentials (EncryptedSharedPreferences)
- [ ] **Verify caller identity** for exported components
- [ ] **Check for overlay attacks** before sensitive actions:
  ```kotlin
  if (event.flags and FLAG_WINDOW_IS_OBSCURED != 0) {
      // Potential overlay attack - refuse action
  }
  ```

### Security Checklist
```
For each public API or external input:
[ ] Is input validated before use?
[ ] Could malformed input cause crashes?
[ ] Is sensitive data properly protected?
[ ] Are there any injection vulnerabilities?
```

---

## 6. Code Quality

### Readability
- [ ] **Methods are short** (<30 lines preferred, <50 max)
- [ ] **One responsibility per method** - if describing requires "and", split it
- [ ] **Clear naming** - intent obvious from name without reading implementation
- [ ] **No magic numbers** - use named constants
- [ ] **Comments explain WHY, not WHAT** - code should be self-documenting

### Kotlin Idioms
- [ ] **Use `?.let { }` instead of null checks** where appropriate
- [ ] **Use `when` instead of chained `if-else`** for multiple conditions
- [ ] **Use data classes** for simple data holders
- [ ] **Use sealed classes** for restricted hierarchies (especially results)
- [ ] **Prefer immutability** - `val` over `var`, immutable collections

### Code Duplication
- [ ] **DRY principle** - if code appears twice, consider extracting
- [ ] **Extract common patterns** into helper functions
- [ ] **Use higher-order functions** to parameterize behavior differences

### Code Quality Checklist
```
For each new/modified file:
[ ] Are there any methods > 30 lines that could be split?
[ ] Is there duplicated code that could be extracted?
[ ] Are all names clear and intention-revealing?
[ ] Are edge cases handled?
```

---

## 7. Testing

### Test Coverage
- [ ] **Happy path tested** - does it work when everything goes right?
- [ ] **Error paths tested** - does it handle failures correctly?
- [ ] **Edge cases tested** - null, empty, boundary values
- [ ] **Integration points tested** - does it work with real dependencies?

### Test Quality
- [ ] **Tests are independent** - no shared mutable state between tests
- [ ] **Tests are deterministic** - same result every run
- [ ] **Tests are fast** - unit tests < 100ms each
- [ ] **Tests have clear assertions** - obvious what's being verified
- [ ] **No placeholder tests** - every test asserts something meaningful

### Android-Specific Testing
- [ ] **Mock Android framework** - don't depend on device state
- [ ] **Test lifecycle scenarios** - rotation, backgrounding, process death
- [ ] **Test permission states** - granted, denied, revoked
- [ ] **Verify node recycling** - no memory leaks in accessibility code

### Testing Checklist
```
For each public method:
[ ] Is there a test for the happy path?
[ ] Is there a test for each error condition?
[ ] Are edge cases covered?
[ ] Would a bug in this method be caught by existing tests?
```

---

## 8. Spec Compliance

### Before Marking Complete
- [ ] **Read the relevant spec** in `openspec/specs/`
- [ ] **Verify all MUST/SHALL requirements** are implemented
- [ ] **Check scenario coverage** - does implementation handle all specified scenarios?
- [ ] **Note any deviations** - if spec can't be followed exactly, document why

### Spec Compliance Checklist
```
For each requirement in the spec:
[ ] Is it implemented?
[ ] Does implementation match the specified behavior?
[ ] Are all scenarios covered?
[ ] If not implemented, is there a TODO or tracking issue?
```

---

## 9. Documentation

### Code Documentation
- [ ] **Public APIs have KDoc** with description and param/return docs
- [ ] **Complex algorithms explained** - why this approach?
- [ ] **Non-obvious decisions documented** - why not the simpler way?
- [ ] **Thread safety requirements noted** - "must be called from main thread"

### Architecture Documentation
- [ ] **New patterns documented** in CLAUDE.md or README
- [ ] **Breaking changes noted** in commit message
- [ ] **Migration steps provided** if changing data formats

---

## 10. Final Review Questions

Ask yourself before marking done:

1. **Would I be comfortable debugging this at 2 AM?**
   - Is error handling clear enough to diagnose issues?
   - Are log messages helpful?

2. **What happens when this fails?**
   - Does it fail gracefully?
   - Is the user informed?
   - Can it recover?

3. **What if the input is hostile?**
   - Could malformed data crash the app?
   - Could it leak sensitive information?

4. **Will this work in 6 months?**
   - Is it maintainable?
   - Are there hidden assumptions that could break?

5. **Does this match the spec?**
   - Have I actually read the spec?
   - Does my implementation match the specified behavior?

---

## Quick Reference: Common Issues

| Issue | Quick Fix |
|-------|-----------|
| Memory leak | Add `finally { resource.close/recycle() }` |
| Race condition | Use `@Volatile` or `AtomicReference` |
| Main thread blocking | Wrap in `withContext(Dispatchers.IO)` |
| Generic exception catch | Catch specific types: `IllegalStateException`, `SecurityException` |
| Magic number | Extract to `const val NAME = value` |
| Code duplication | Extract function with parameters for differences |
| Missing test | Add test for happy path, error path, edge cases |
| No input validation | Add `require()` checks at function entry |

---

## Automation: Pre-Commit Hook

Consider adding this to `.git/hooks/pre-commit`:

```bash
#!/bin/bash
# Quick checks before commit

# Check for common issues
if grep -r "TODO" --include="*.kt" app/src/main/; then
    echo "Warning: TODOs found in production code"
fi

if grep -r "println" --include="*.kt" app/src/main/; then
    echo "Error: println found in production code"
    exit 1
fi

if grep -r "Exception\)" --include="*.kt" app/src/main/ | grep -v "// allowed"; then
    echo "Warning: Generic Exception catch found"
fi

# Run tests
./gradlew testDebugUnitTest --quiet
```

---

*Use this checklist after every coding task. Quality compounds - each check prevents future debugging.*
