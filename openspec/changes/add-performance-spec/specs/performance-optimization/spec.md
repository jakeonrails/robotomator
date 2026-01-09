## ADDED Requirements

### Requirement: Screen Representation Caching

The system SHALL cache screen representations to avoid redundant traversals.

#### Scenario: Cache hit
- **WHEN** a screen read is requested and a valid cache entry exists
- **THEN** the system SHALL return the cached representation
- **AND** complete the request in under 10ms

#### Scenario: Cache invalidation on content change
- **WHEN** a TYPE_WINDOW_CONTENT_CHANGED event is received
- **THEN** the system SHALL invalidate the cache for the affected window
- **AND** NOT proactively rebuild (lazy invalidation)

#### Scenario: Cache invalidation on window change
- **WHEN** a TYPE_WINDOW_STATE_CHANGED event is received
- **THEN** the system SHALL invalidate the entire cache
- **AND** clear indexed lookups

#### Scenario: Cache size limit
- **WHEN** cache entries exceed the memory limit (default 5MB)
- **THEN** the system SHALL evict least-recently-used entries
- **AND** log cache eviction events for debugging

### Requirement: Event Debouncing

The system SHALL debounce rapid accessibility events to prevent overwhelm.

#### Scenario: Content change debouncing
- **WHEN** multiple TYPE_WINDOW_CONTENT_CHANGED events occur within 100ms
- **THEN** the system SHALL coalesce them into a single invalidation
- **AND** only process the final state

#### Scenario: Configurable debounce interval
- **WHEN** the app is configured
- **THEN** the debounce interval SHALL be configurable (default 100ms)
- **AND** minimum allowed value SHALL be 50ms

#### Scenario: Event flood protection
- **WHEN** more than 50 events are received within 1 second
- **THEN** the system SHALL drop intermediate events
- **AND** only process the most recent state
- **AND** log a warning about event flooding

### Requirement: Selector Indexing

The system SHALL maintain indexes for fast selector resolution.

#### Scenario: Index building
- **WHEN** a screen representation is built
- **THEN** the system SHALL simultaneously build indexes for:
  - Resource IDs (map from ID to node list)
  - Text content (map from text to node list)
  - Element types (map from class name to node list)

#### Scenario: Indexed lookup
- **WHEN** a selector query uses only indexed properties (ID, text, or type)
- **THEN** the system SHALL use index lookup instead of full tree traversal
- **AND** complete simple lookups in under 5ms

#### Scenario: Index invalidation
- **WHEN** the screen representation cache is invalidated
- **THEN** the corresponding indexes SHALL also be invalidated

### Requirement: Background Threading

The system SHALL perform expensive operations on background threads.

#### Scenario: Screen traversal threading
- **WHEN** a full screen traversal is required
- **THEN** it SHALL be performed on a background thread
- **AND** NOT block the main/UI thread
- **AND** results SHALL be delivered via callback or coroutine

#### Scenario: AI communication threading
- **WHEN** communicating with AI services
- **THEN** all network operations SHALL occur on IO threads
- **AND** responses SHALL be processed on background threads before UI delivery

#### Scenario: Action execution threading
- **WHEN** accessibility actions are performed
- **THEN** they SHALL be dispatched from the accessibility service thread
- **AND** completion SHALL be awaited with configurable timeout

### Requirement: Latency Targets

The system SHALL meet defined latency targets for key operations.

#### Scenario: Screen read latency
- **WHEN** a cached screen read is requested
- **THEN** it SHALL complete in under 10ms (P95)

#### Scenario: Fresh screen read latency
- **WHEN** an uncached screen read is requested on a typical screen (under 200 nodes)
- **THEN** it SHALL complete in under 100ms (P95)

#### Scenario: Selector resolution latency
- **WHEN** resolving a simple selector (single ID, text, or type)
- **THEN** it SHALL complete in under 5ms (P95)

#### Scenario: Action execution latency
- **WHEN** executing a tap or type action
- **THEN** the action dispatch SHALL complete in under 50ms
- **AND** the system SHALL NOT guarantee app response time

### Requirement: Node Lifecycle Management

The system SHALL properly manage AccessibilityNodeInfo object lifecycles to prevent memory leaks.

#### Scenario: Node recycling
- **WHEN** AccessibilityNodeInfo objects are obtained
- **THEN** they SHALL be recycled via recycle() after use
- **AND** never held in long-lived references without explicit ownership

#### Scenario: Snapshot copying
- **WHEN** node data needs to persist beyond immediate use
- **THEN** the system SHALL copy required properties to data objects
- **AND** recycle the original node immediately

#### Scenario: Traversal cleanup
- **WHEN** a screen traversal completes (success or failure)
- **THEN** all obtained nodes SHALL be recycled
- **AND** cleanup SHALL occur even on exceptions

### Requirement: AI Response Caching

The system SHALL cache AI responses for similar contexts to reduce API calls.

#### Scenario: Intent parsing cache
- **WHEN** the same or very similar automation description is submitted
- **THEN** the system MAY return cached intent parsing results
- **AND** cache entries SHALL expire after 24 hours

#### Scenario: Recovery action cache
- **WHEN** AITM encounters a previously seen unexpected state
- **THEN** the system MAY suggest the previously successful recovery action
- **AND** still allow AI override if recovery fails

### Requirement: Incremental Screen Diffing

The system SHALL compute screen differences incrementally when possible.

#### Scenario: Partial content change
- **WHEN** a content change event indicates a specific node changed
- **THEN** the system SHALL update only the affected subtree in the representation
- **AND** NOT rebuild the entire screen representation

#### Scenario: Diff computation
- **WHEN** comparing two screen states
- **THEN** the system SHALL identify added, removed, and modified nodes
- **AND** complete diff computation in under 50ms for typical screens

### Requirement: Battery Impact Mitigation

The system SHALL minimize battery drain from accessibility service operation.

#### Scenario: Idle shutdown
- **WHEN** no automations have run for a configurable period (default 5 minutes)
- **THEN** the accessibility service MAY reduce its event subscriptions
- **AND** re-enable full monitoring when automation starts

#### Scenario: Polling avoidance
- **WHEN** waiting for UI conditions
- **THEN** the system SHALL use event-driven detection where possible
- **AND** fall back to polling only when events are insufficient
- **AND** polling interval SHALL be at least 200ms

#### Scenario: Battery usage disclosure
- **WHEN** the user views app settings
- **THEN** the system SHALL display estimated battery impact
- **AND** offer power-saving mode options
