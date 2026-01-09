# Design: Accessibility Service Foundation

## Context
This is a greenfield Android project requiring fundamental architectural decisions. The accessibility service is the core capability that enables all automation features. We need to establish patterns that will scale through Groups B, C, and D while keeping the codebase maintainable.

Key constraints:
- Accessibility services have performance implications (must be efficient)
- Service runs in separate process context from main app
- Need clean separation between service layer and app layer
- Must support future AI integration and scripting engine

## Goals / Non-Goals

**Goals:**
- Establish Clean Architecture foundation (domain, data, presentation)
- Create performant accessibility service that doesn't drain battery
- Build simple, testable APIs for screen reading and element interaction
- Support future extensibility without major refactoring

**Non-Goals:**
- Advanced UI (Group D concern)
- AI integration (Group C)
- Script execution engine (Group B)
- Production-ready error recovery (will evolve)

## Decisions

### Decision 1: Clean Architecture with Three Layers
- **Domain Layer**: Core business logic, use cases, repository interfaces
- **Data Layer**: Accessibility service implementation, Android APIs
- **Presentation Layer**: UI components (minimal for now)

**Why**: Clear separation of concerns, testable business logic, Android framework isolated in data layer.

**Alternatives considered**:
- Single-layer architecture: Too coupled to Android, hard to test
- Feature-based modules: Premature for MVP, adds complexity

### Decision 2: Kotlin Coroutines for Async Operations
Use Kotlin Flow and suspend functions for all async accessibility operations.

**Why**: Modern Kotlin approach, excellent async/await semantics, integrates with Android lifecycle, better than callbacks.

**Alternatives considered**:
- RxJava: More complexity, Kotlin coroutines now standard
- Callbacks: Difficult to chain, callback hell

### Decision 3: Repository Pattern for Accessibility Operations
Create `AccessibilityRepository` interface in domain, implemented in data layer.

**Why**: Abstracts Android AccessibilityService details, mockable for testing, clear API contract.

**Structure**:
```kotlin
// domain/repository/AccessibilityRepository.kt
interface AccessibilityRepository {
    suspend fun tapElement(selector: ElementSelector): Result<Unit>
    suspend fun typeText(selector: ElementSelector, text: String): Result<Unit>
    suspend fun readScreen(): Result<ScreenSnapshot>
    suspend fun performGlobalAction(action: GlobalAction): Result<Unit>
    suspend fun launchApp(packageName: String): Result<Unit>
    fun observeScreenChanges(): Flow<ScreenChangeEvent>
}
```

### Decision 4: Element Addressing via Simple Selector Model
Initial selector strategy: resource-id, text match, class name, content-description.

**Why**: Simple, predictable, covers 80% of use cases. More sophisticated selectors (XPath-like) can be added in Group B when building the selector language.

**Example**:
```kotlin
data class ElementSelector(
    val resourceId: String? = null,
    val text: String? = null,
    val contentDescription: String? = null,
    val className: String? = null
)
```

### Decision 5: Gradle Kotlin DSL with Version Catalog
Use `libs.versions.toml` for dependency management and Kotlin DSL for build files.

**Why**: Type-safe, better IDE support, version catalog prevents version conflicts, modern Gradle approach.

### Decision 6: Min SDK 26 (Android 8.0)
Target Android 8.0+ for accessibility features and modern API support.

**Why**: Covers 95%+ of devices, enables modern accessibility APIs, reasonable support burden.

## Risks / Trade-offs

### Risk: Accessibility Service Performance
- **Concern**: Traversing node trees on every screen change could impact battery/performance
- **Mitigation**: Debounce events, cache node trees, only traverse when explicitly requested
- **Monitoring**: Add performance logging in initial implementation

### Risk: Permission Denial
- **Concern**: Users may not grant accessibility permission, blocking all functionality
- **Mitigation**: Clear onboarding flow, explain value proposition, graceful degradation where possible
- **Note**: Core functionality requires permission; this is inherent to design

### Trade-off: Simple Selectors vs. Robustness
- **Chosen**: Start with simple selectors (id, text, class)
- **Trade-off**: May not handle all edge cases initially
- **Rationale**: Covers MVP needs, can enhance in Group B without breaking changes

### Risk: Service Lifecycle Complexity
- **Concern**: Accessibility service lifecycle differs from normal components
- **Mitigation**: Clear lifecycle documentation, state machine for service states, extensive logging

## Migration Plan

N/A - Greenfield implementation

## Open Questions

1. **Dependency Injection**: Use Hilt or manual DI?
   - **Recommendation**: Start with manual DI (simple constructor injection), add Hilt if complexity grows

2. **Event Debouncing**: What's the optimal debounce window for screen changes?
   - **Recommendation**: Start with 300ms, tune based on real-world testing

3. **Node Tree Caching**: Should we cache the entire tree or just root node?
   - **Recommendation**: Cache root node only initially, add smart caching if performance issues arise

4. **Testing Strategy**: How to test accessibility service without real Android framework?
   - **Recommendation**: Mock AccessibilityService interface, test business logic in domain layer, manual testing for service integration
