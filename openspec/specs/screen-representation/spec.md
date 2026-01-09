# Screen Representation

A text-based, structured representation of Android screen content that enables the AI to understand and address specific UI elements.

## Requirements

### Requirement: Hierarchical Text Format

The system SHALL convert the accessibility node tree into a text format that preserves hierarchy and is parseable by an LLM.

#### Scenario: Basic screen serialization
- **WHEN** a screen capture is serialized
- **THEN** it SHALL produce a text representation showing element hierarchy through indentation
- **AND** include element type, text content, and addressable identifier for each node
- **AND** be human-readable while remaining machine-parseable

#### Scenario: Example output format
- **WHEN** a simple screen with a button and text is serialized
- **THEN** it SHALL produce output similar to:
```
[Screen: com.example.app / MainActivity]
  [0] FrameLayout
    [0.0] LinearLayout
      [0.0.0] TextView "Welcome to the app" @text_welcome
      [0.0.1] Button "Get Started" @btn_start {clickable}
```

### Requirement: Element Addressing

The system SHALL assign unique, stable addresses to each element that can be used in selectors.

#### Scenario: Index-based addressing
- **WHEN** an element is serialized
- **THEN** it SHALL include an index path (e.g., "0.1.2") representing its position in the hierarchy
- **AND** this path SHALL be usable to find the element again within the same screen state

#### Scenario: Resource ID addressing
- **WHEN** an element has a viewIdResourceName
- **THEN** it SHALL include this ID prefixed with @ (e.g., "@btn_submit")
- **AND** this ID SHALL be usable as a selector

#### Scenario: Text-based addressing
- **WHEN** an element contains text
- **THEN** it SHALL be addressable by its text content
- **AND** support partial text matching with wildcards

#### Scenario: Combined selectors
- **WHEN** multiple addressing methods are available
- **THEN** selectors SHALL support combining them (e.g., "Button@btn_submit" or "TextView:contains('Welcome')")

### Requirement: Element Properties

The system SHALL include relevant properties for each element in the representation.

#### Scenario: Interactive state flags
- **WHEN** an element is clickable, checkable, or scrollable
- **THEN** the representation SHALL include these as flags (e.g., {clickable, checkable, checked})

#### Scenario: Text content inclusion
- **WHEN** an element has text or contentDescription
- **THEN** the representation SHALL include it in quotes after the element type
- **AND** truncate extremely long text with ellipsis

#### Scenario: Bounds information
- **WHEN** requested in verbose mode
- **THEN** the representation SHALL include element bounds [left,top,right,bottom]

### Requirement: Selector Language

The system SHALL provide a selector language for addressing elements, similar to CSS selectors.

#### Scenario: By resource ID
- **WHEN** a selector `#btn_submit` is used
- **THEN** it SHALL match elements with viewIdResourceName containing "btn_submit"

#### Scenario: By element type
- **WHEN** a selector `Button` is used
- **THEN** it SHALL match elements whose className ends with "Button"

#### Scenario: By text content
- **WHEN** a selector `:text("Submit")` is used
- **THEN** it SHALL match elements whose text exactly equals "Submit"

#### Scenario: By partial text
- **WHEN** a selector `:contains("Submit")` is used
- **THEN** it SHALL match elements whose text contains "Submit"

#### Scenario: By content description
- **WHEN** a selector `:desc("Close button")` is used
- **THEN** it SHALL match elements whose contentDescription equals "Close button"

#### Scenario: Combining selectors
- **WHEN** a selector `Button:text("Submit")` is used
- **THEN** it SHALL match elements that are Buttons AND have text "Submit"

#### Scenario: Descendant selector
- **WHEN** a selector `LinearLayout > Button` is used
- **THEN** it SHALL match Button elements that are direct children of LinearLayout

#### Scenario: Index selector
- **WHEN** a selector `Button[2]` is used
- **THEN** it SHALL match the third Button element (0-indexed)

#### Scenario: Attribute selectors
- **WHEN** a selector `[clickable]` is used
- **THEN** it SHALL match elements where isClickable is true

### Requirement: Selector Resilience

The system SHALL generate and recommend selectors that are resilient to minor UI changes.

#### Scenario: Prefer ID over index
- **WHEN** generating a selector for an element with a resource ID
- **THEN** the system SHALL prefer the ID-based selector over index-based

#### Scenario: Selector validation
- **WHEN** a selector is created or used
- **THEN** the system SHALL validate that it matches exactly one element
- **AND** warn if it matches zero or multiple elements

#### Scenario: Alternative selectors
- **WHEN** an element is being addressed
- **THEN** the system SHALL be able to generate multiple alternative selectors
- **AND** rank them by expected stability

### Requirement: Screen Diffing

The system SHALL compare two screen states to identify changes.

#### Scenario: Detect new elements
- **WHEN** comparing screen states before and after an action
- **THEN** the system SHALL identify elements that appeared

#### Scenario: Detect removed elements
- **WHEN** comparing screen states before and after an action
- **THEN** the system SHALL identify elements that disappeared

#### Scenario: Detect changed text
- **WHEN** comparing screen states before and after an action
- **THEN** the system SHALL identify elements whose text content changed

### Requirement: Screen Summarization

The system SHALL provide concise summaries of screen content for AI context management.

#### Scenario: Full screen summary
- **WHEN** a screen summary is requested
- **THEN** the system SHALL produce a brief description of the screen's purpose and key elements
- **AND** include the app name and activity/screen title

#### Scenario: Actionable elements summary
- **WHEN** an actionable elements summary is requested
- **THEN** the system SHALL list only interactive elements (buttons, links, inputs)
- **AND** include their text and selectors

#### Scenario: Modal detection
- **WHEN** analyzing a screen
- **THEN** the system SHALL identify if a modal dialog or overlay is present
- **AND** indicate this prominently in the representation
