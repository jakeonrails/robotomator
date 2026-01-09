## MODIFIED Requirements

### Requirement: Tool/Function Calling

The system SHALL expose device capabilities to the AI as callable tools/functions.

#### Scenario: Complete tool definition
- **WHEN** the AI session is initialized
- **THEN** the system SHALL provide tool definitions for:
  - `tap(selector: string)` - tap an element
  - `long_press(selector: string)` - long press an element
  - `type(selector: string, text: string)` - type text into element
  - `clear(selector: string)` - clear text from an editable element
  - `scroll(direction: "up"|"down"|"left"|"right", selector?: string)` - scroll the screen or container
  - `scroll_to_find(selector: string, direction: "up"|"down", max_scrolls?: number)` - scroll until element found
  - `read_element(selector: string)` - read element text, returns string
  - `read_screen()` - get full screen representation, returns ScreenRepresentation
  - `wait_for(selector: string, timeout_ms?: number)` - wait for element to appear
  - `wait_for_gone(selector: string, timeout_ms?: number)` - wait for element to disappear
  - `launch_app(package: string)` - launch an app by package name
  - `back()` - press system back button
  - `home()` - press system home button
  - `record_step(step: ScriptStep)` - record a step to the script being built

#### Scenario: Tool execution
- **WHEN** the AI returns a tool call
- **THEN** the system SHALL execute the corresponding device action
- **AND** return the result to the AI
- **AND** include timing information (duration_ms) in result

#### Scenario: Tool error handling
- **WHEN** a tool execution fails
- **THEN** the system SHALL return an error object containing:
  - error_type: "element_not_found" | "action_failed" | "timeout" | "permission_denied"
  - message: Human-readable error description
  - selector: The selector that failed (if applicable)
  - screen_state: Current screen representation for recovery context

### Requirement: Screen Representation Format for AI

The system SHALL provide screen state to AI in a consistent, parseable format.

#### Scenario: read_screen return format
- **WHEN** read_screen() tool is called
- **THEN** it SHALL return a ScreenRepresentation object containing:
  - app_package: Current foreground app package name
  - activity: Current activity name (if available)
  - has_modal: Boolean indicating if a modal/dialog is detected
  - elements: Hierarchical text representation of UI elements
  - actionable_elements: List of interactive elements with selectors
  - summary: Brief description of screen purpose

#### Scenario: Screen representation text format
- **WHEN** elements are serialized
- **THEN** they SHALL follow this format:
```
[Screen: com.example.app / MainActivity]
  [0] FrameLayout
    [0.0] LinearLayout
      [0.0.0] TextView "Welcome" @text_welcome
      [0.0.1] Button "Continue" @btn_continue {clickable}
      [0.0.2] EditText "" @input_email {editable} hint="Enter email"
```

#### Scenario: Actionable elements format
- **WHEN** actionable_elements are listed
- **THEN** each SHALL include:
  - selector: Recommended selector (preferring ID, then text, then index)
  - alternative_selectors: Array of fallback selectors
  - type: Element type (Button, EditText, etc.)
  - text: Element text or content description
  - actions: Available actions (click, long_click, set_text, scroll)

### Requirement: Script Generation Contract

The system SHALL define how AI generates and returns automation scripts.

#### Scenario: Progressive step recording
- **WHEN** AI successfully performs an action during script building
- **THEN** it SHALL call record_step() with a ScriptStep object containing:
  - action: Action type matching scripting engine actions
  - selector: Resilient selector for the element
  - parameters: Action-specific parameters (text, direction, timeout, store_as)
  - wait_condition: Recommended wait_for before this step
  - recovery_hint: Optional hint for AITM if this step fails

#### Scenario: Selector preference for recording
- **WHEN** AI records a step with a selector
- **THEN** it SHALL prefer selectors in this order:
  1. Resource ID (#id) - most stable
  2. Unique text content (:text("exact"))
  3. Content description (:desc("description"))
  4. Type + text combination (Button:text("Submit"))
  5. Index-based ([0.1.2]) - least stable, last resort
- **AND** include alternative_selector if the primary is index-based

#### Scenario: Script finalization
- **WHEN** AI determines the automation goal is achieved
- **THEN** it SHALL call a finalize_script() tool with:
  - success: boolean
  - final_steps: Any cleanup steps needed
  - outputs: List of variables to capture as outputs
  - summary: Brief description of what the script does

### Requirement: AITM Recovery Contract

The system SHALL define how AI-in-the-middle recovery integrates with scripts.

#### Scenario: Recovery context provision
- **WHEN** AITM is invoked due to a step failure
- **THEN** the AI SHALL receive:
  - failed_step: The step that failed
  - error: Error details from tool execution
  - current_screen: Full screen representation
  - recovery_hints: Matching hints from script (if any)
  - goal_context: Original automation goal and current progress
  - history: Recent actions taken (last 5 steps)

#### Scenario: Recovery hint matching
- **WHEN** providing recovery hints to AI
- **THEN** the system SHALL match hints by:
  - Exact trigger match (e.g., "element_not_found:#btn_submit")
  - Pattern trigger match (e.g., "element_not_found:*" matches any not found)
  - Condition trigger match (e.g., "modal_detected" when has_modal is true)

#### Scenario: Recovery action response
- **WHEN** AI determines a recovery action
- **THEN** it SHALL return a RecoveryAction containing:
  - action_type: "execute_action" | "modify_selector" | "skip_step" | "abort" | "ask_user"
  - action: Tool call to execute (for execute_action)
  - new_selector: Modified selector (for modify_selector)
  - reason: Explanation of recovery choice
  - confidence: "high" | "medium" | "low"

#### Scenario: Recovery success handling
- **WHEN** a recovery action succeeds
- **THEN** the system SHALL:
  - Resume script execution from the recovered point
  - Optionally record the recovery for future hint generation
  - Continue with next step

#### Scenario: Recovery failure escalation
- **WHEN** recovery action fails or confidence is low
- **THEN** the system SHALL escalate by:
  - Retrying with AI if attempts < max_recovery_attempts (default 3)
  - Escalating to user if AI cannot recover
  - Aborting if user dismisses escalation
