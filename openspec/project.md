# Project Context

## Purpose

Robotomator is an AI-powered Android automation platform that enables users to create, execute, and manage device automations through natural language. Users describe what they want to automate in plain English (via text or voice), and the AI interprets their intent, explores the target app, and generates executable automation scripts.

### Core Value Propositions

1. **Natural Language Automation**: Users describe automations conversationally rather than coding them
2. **AI-in-the-Middle Recovery**: When automations encounter unexpected UI states (A/B tests, modals, layout changes), the AI dynamically recovers or escalates to the user
3. **Progressive Script Building**: AI learns apps by actually navigating them, building scripts iteratively based on what it discovers
4. **Accessibility-First Approach**: Uses Android Accessibility APIs for reliable, vision-independent screen interaction

## Tech Stack

### Android Application
- **Language**: Kotlin (primary), possibly Lua for user-facing scripting
- **Min SDK**: TBD (likely Android 8.0+ / API 26+ for accessibility features)
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: MVVM with Clean Architecture layers

### AI/Backend
- **LLM Integration**: Claude API (or compatible) for intent parsing, script generation, and recovery
- **Communication**: REST API or direct SDK integration
- **Script Storage**: Local SQLite or Room database for automations

### Key Android APIs
- **Accessibility Service**: Core screen reading and interaction
- **Speech Recognition**: For voice dictation feature
- **App Shortcuts API**: For homescreen automation shortcuts

## Project Conventions

### Code Style
- Follow official Kotlin coding conventions
- Use meaningful, descriptive names for classes and functions
- Prefer immutability (val over var, immutable collections)
- Use Kotlin coroutines for async operations
- Maximum line length: 120 characters

### Architecture Patterns
- **Clean Architecture**: Separate domain, data, and presentation layers
- **Repository Pattern**: Abstract data sources behind repository interfaces
- **Use Cases**: Single-responsibility interactors for business logic
- **State Management**: Unidirectional data flow with sealed classes for UI state

### Testing Strategy
- Unit tests for business logic and use cases
- Integration tests for repository implementations
- UI tests with Espresso for critical user flows
- Accessibility service tests with mocked accessibility events

### Git Workflow
- Branch naming: `feature/description`, `fix/description`, `refactor/description`
- Commit messages: Imperative mood, concise subject line
- PR-based workflow with review required before merge

## Domain Context

### Key Concepts

**Automation**: A saved, replayable sequence of actions that accomplishes a user task. Has a name, trigger method (manual/shortcut), optional input form, and script.

**Script**: The executable representation of an automation. Contains steps with selectors, actions, wait conditions, and control flow.

**Element Selector**: A path or query that uniquely identifies a UI element on screen. Similar concept to CSS selectors but for Android accessibility nodes.

**AI-in-the-Middle (AITM)**: The recovery mechanism where the AI is consulted when the expected UI state doesn't match reality. The AI can:
- Identify and dismiss unexpected modals
- Find alternative paths to the goal
- Adapt selectors when UI layout changes
- Escalate to user when human judgment needed

**Screen Representation**: A text-based, structured representation of visible UI elements that the AI can parse and reason about. Includes element types, text content, hierarchy, and addressable identifiers.

**Progressive Scripting**: The process where AI builds an automation by actually performing it step-by-step, observing results, and codifying successful paths.

### User Personas

1. **Power User**: Wants to automate repetitive tasks across multiple apps, comfortable with some technical concepts
2. **Casual User**: Wants simple automations, prefers voice input, needs guided experience
3. **Developer**: May want to extend or customize automation capabilities

## Important Constraints

### Technical Constraints
- Accessibility Service has significant battery/performance implications - must be efficient
- Cannot automate apps that actively block accessibility services
- Limited to visible UI elements - cannot interact with hidden content
- Voice recognition requires network connectivity (unless using on-device models)

### User Experience Constraints
- Must gracefully handle permission denial scenarios
- Automations should fail safely (never leave user in broken state)
- AI responses should be fast enough to feel interactive
- Privacy-sensitive: no screen content should leave device without explicit consent

### Platform Constraints
- Different Android OEMs may implement accessibility differently
- Need to handle both light and dark themes
- Must work across different screen sizes and densities

## External Dependencies

### APIs and Services
- LLM API (Claude/Anthropic) for natural language processing and script generation
- Android Accessibility APIs (system-level)
- Android Speech Recognition APIs (system-level)

### Potential Libraries (TBD)
- Retrofit/OkHttp for networking
- Room for local persistence
- Kotlin Coroutines + Flow for reactive programming
- Hilt for dependency injection

## Feature Roadmap (Conceptual)

### MVP Features
1. Core accessibility service for screen reading and interaction
2. Text-based screen representation for AI consumption
3. Simple scripting language with basic actions (tap, type, wait, read)
4. Single automation creation via text prompt
5. Manual automation execution
6. Basic AI-in-the-middle recovery for unexpected modals

### Post-MVP Features
1. Voice dictation for automation creation
2. Custom input forms for automations
3. Homescreen shortcuts
4. Multiple automation management
5. Automation sharing/export
6. Advanced control flow in scripts
7. Element selector builder UI
