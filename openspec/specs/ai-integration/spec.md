# AI Integration

The system for integrating LLM capabilities to interpret user intent, generate automation scripts, and provide intelligent recovery.

## Requirements

### Requirement: LLM Communication

The system SHALL communicate with an LLM API (Claude or compatible) for AI-powered features.

#### Scenario: API configuration
- **WHEN** the app is configured
- **THEN** it SHALL allow specifying the LLM API endpoint and credentials
- **AND** store credentials securely in encrypted local storage

#### Scenario: Request formatting
- **WHEN** sending a request to the LLM
- **THEN** the system SHALL format messages according to the API specification
- **AND** include system prompts, context, and user content as appropriate

#### Scenario: Response parsing
- **WHEN** receiving a response from the LLM
- **THEN** the system SHALL parse the response
- **AND** extract structured data when the AI returns tool calls or structured formats
- **AND** handle errors gracefully

#### Scenario: Rate limiting
- **WHEN** multiple AI requests are made
- **THEN** the system SHALL respect API rate limits
- **AND** queue or throttle requests as needed

### Requirement: Intent Parsing

The system SHALL interpret natural language automation descriptions into structured automation intents.

#### Scenario: Basic automation description
- **WHEN** a user provides an automation description like "Open Uber and check the price to get to LAX"
- **THEN** the AI SHALL identify the target app (Uber)
- **AND** identify the goal (get price estimate)
- **AND** identify required inputs (destination: LAX)
- **AND** return a structured intent object

#### Scenario: Multi-step automation
- **WHEN** a user describes multiple steps like "Open Settings, go to Wi-Fi, and turn it off"
- **THEN** the AI SHALL identify each distinct step
- **AND** the correct order of operations
- **AND** the expected intermediate states

#### Scenario: Ambiguous request clarification
- **WHEN** the user's description is ambiguous or incomplete
- **THEN** the AI SHALL identify what information is missing
- **AND** generate clarifying questions
- **AND** wait for user response before proceeding

#### Scenario: Intent refinement
- **WHEN** the user provides additional information after clarification
- **THEN** the AI SHALL incorporate it into the intent
- **AND** re-validate the complete intent

### Requirement: Progressive Script Building

The system SHALL enable AI to build automation scripts by actually navigating apps and observing results.

#### Scenario: Exploration initiation
- **WHEN** the AI begins building an automation
- **THEN** it SHALL request to launch the target app
- **AND** read the initial screen state
- **AND** determine the first action needed

#### Scenario: Action-observe cycle
- **WHEN** the AI determines an action to take
- **THEN** it SHALL request the action be performed
- **AND** observe the resulting screen state
- **AND** evaluate if progress was made toward the goal
- **AND** record the successful action as a script step

#### Scenario: Element selector generation
- **WHEN** the AI identifies an element to interact with
- **THEN** it SHALL generate a resilient selector for that element
- **AND** prefer stable identifiers (resource IDs, unique text) over positional selectors

#### Scenario: Wait condition inference
- **WHEN** the AI successfully navigates to a new screen
- **THEN** it SHALL infer what condition indicated the screen was ready
- **AND** generate appropriate wait_for conditions for the script

#### Scenario: Script step recording
- **WHEN** an action is successfully performed
- **THEN** the AI SHALL record it as a script step with:
  - The action type
  - The selector used
  - Any wait conditions
  - Any recovery hints for AITM

#### Scenario: Goal completion detection
- **WHEN** observing the screen after an action
- **THEN** the AI SHALL determine if the automation goal has been achieved
- **AND** if so, finalize the script with any necessary cleanup or data extraction steps

### Requirement: AI-in-the-Middle (AITM) Recovery

The system SHALL use AI to recover from unexpected UI states during automation execution.

#### Scenario: Unexpected element detection
- **WHEN** an expected element is not found during execution
- **THEN** the system SHALL capture the current screen state
- **AND** send it to the AI with the expected condition and context
- **AND** request recovery guidance

#### Scenario: Modal dialog handling
- **WHEN** the AI detects an unexpected modal or dialog
- **THEN** it SHALL analyze the modal content
- **AND** determine if it can be safely dismissed
- **AND** recommend an action (dismiss, accept, or escalate to user)

#### Scenario: Alternative path finding
- **WHEN** the expected UI path has changed
- **THEN** the AI SHALL analyze the current screen
- **AND** identify alternative elements or paths to achieve the goal
- **AND** suggest modified actions

#### Scenario: User escalation
- **WHEN** the AI cannot determine a safe recovery action
- **THEN** it SHALL escalate to the user
- **AND** present the current screen state
- **AND** explain what went wrong
- **AND** ask for guidance

#### Scenario: Recovery action execution
- **WHEN** the AI suggests a recovery action
- **THEN** the system SHALL execute it
- **AND** verify the recovery was successful
- **AND** resume normal script execution

#### Scenario: Recovery timeout
- **WHEN** recovery attempts exceed a time or attempt limit
- **THEN** the system SHALL fail the automation gracefully
- **AND** return the user to a known state (home screen)
- **AND** report the failure reason

### Requirement: Context Management

The system SHALL efficiently manage context sent to the AI to stay within token limits.

#### Scenario: Screen context compression
- **WHEN** sending screen state to the AI
- **THEN** the system SHALL compress verbose representations
- **AND** prioritize relevant elements based on the current goal

#### Scenario: Conversation history pruning
- **WHEN** conversation context grows too large
- **THEN** the system SHALL prune older messages
- **AND** retain essential context (goal, key decisions)
- **AND** summarize removed content if needed

#### Scenario: Goal context persistence
- **WHEN** managing context across multiple AI interactions
- **THEN** the system SHALL always include the automation goal
- **AND** current progress status
- **AND** recent action history

### Requirement: Tool/Function Calling

The system SHALL expose device capabilities to the AI as callable tools/functions.

#### Scenario: Tool definition
- **WHEN** the AI session is initialized
- **THEN** the system SHALL provide tool definitions for:
  - `tap(selector)` - tap an element
  - `type(selector, text)` - type text into element
  - `scroll(direction, selector?)` - scroll the screen
  - `read_element(selector)` - read element text
  - `read_screen()` - get screen representation
  - `wait_for(selector, timeout?)` - wait for element
  - `launch_app(package)` - launch an app
  - `back()` - press back
  - `home()` - press home

#### Scenario: Tool execution
- **WHEN** the AI returns a tool call
- **THEN** the system SHALL execute the corresponding device action
- **AND** return the result to the AI

#### Scenario: Tool error handling
- **WHEN** a tool execution fails
- **THEN** the system SHALL return an error message to the AI
- **AND** include relevant context for recovery

### Requirement: Privacy Protection

The system SHALL protect user privacy in AI interactions.

#### Scenario: Data minimization
- **WHEN** sending screen data to the AI
- **THEN** the system SHALL only include necessary information
- **AND** avoid sending sensitive data (passwords, financial info) when possible

#### Scenario: Sensitive content detection
- **WHEN** a screen contains potentially sensitive fields
- **THEN** the system SHALL mask or redact that content before sending
- **AND** indicate that masked content exists

#### Scenario: User consent for AI
- **WHEN** the user first uses AI features
- **THEN** the system SHALL explain that screen content will be sent to the AI
- **AND** require explicit consent
- **AND** allow opting out of AI features while still using manual scripts
