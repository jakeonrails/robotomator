# Robotomator Roadmap

**Your Phone, on Autopilot.**

Welcome to the Robotomator roadmap! This document charts our course from "just an idea" to "your phone does your bidding." We have organized the work into parallel groups so multiple builders can work simultaneously without stepping on each other's robot toes.

The journey ahead is organized by what can be built in parallel. Within each group, we tackle the quick wins first (maximum satisfaction, minimum tears) before moving to the meatier challenges.

---

## Group A: The Foundation

**Unlocks:** Everything. Literally everything depends on this.

*Before Robotomator can do anything, it needs to see and touch. This is where we give our robot its eyes and fingers.*

### Accessibility Service

| Priority | Feature | What It Does | Joy Factor | Status |
|----------|---------|--------------|------------|--------|
| 1 | Service Registration | Register as an Android AccessibilityService so the system knows we exist | "It's alive!" moment | ✅ **COMPLETED** |
| 2 | Permission Detection | Know when we have the golden ticket (accessibility enabled) or not | No more guessing | ✅ **COMPLETED** |
| 3 | Permission Request Flow | Kindly guide users through enabling permissions without making them feel like they're defusing a bomb | Smooth onboarding | ✅ **COMPLETED** |
| 4 | Global Actions | Back, home, recents, notifications - the big red buttons of Android | Instant power | ✅ **COMPLETED** |
| 5 | Element Interactions | Tap, type, scroll, long press - teach our robot to poke things properly | The fun begins | ✅ **COMPLETED** |
| 6 | Screen Content Reading | Traverse the accessibility node tree and capture everything we see | Robot vision | ✅ **COMPLETED** |
| 7 | Event Monitoring | Detect when the screen changes so we're never caught off guard | Always aware | ✅ **COMPLETED** |
| 8 | App Launching | Start apps by package name because robots love precision | App summoning | ✅ **COMPLETED** |
| 9 | Service Lifecycle | Gracefully start up, shut down, and bind without drama | Professional robot | ✅ **COMPLETED** |

**Milestone:** When Group A is complete, we can see any screen, tap any button, and type into any field. The robot has awakened!

---

## Group B: The Interpreters

**Requires:** Group A (The Foundation)

**Unlocks:** The ability to read screens intelligently AND execute multi-step automations

*With eyes and fingers in place, we need to teach our robot to understand what it sees and follow instructions. These two tracks can be built in parallel because they both talk to Group A but not to each other.*

### Track B1: Screen Representation

*Teaching the robot to describe what it sees in a way AI can understand.*

| Priority | Feature | What It Does | Joy Factor | Status |
|----------|---------|--------------|------------|--------|
| 1 | Hierarchical Text Format | Convert the messy accessibility tree into clean, indented text that LLMs love | AI-readable vision | ✅ **COMPLETED** |
| 2 | Element Addressing | Give every element a unique address - like GPS coordinates for buttons | Find anything |
| 3 | Element Properties | Track what's clickable, checkable, scrollable - the personality traits of each element | Know your buttons |
| 4 | Selector Language | Build a mini-CSS for Android: `Button:text("Submit")`, `#login_btn`, `[clickable]` | Elegant targeting |
| 5 | Selector Resilience | Prefer stable selectors (IDs, unique text) over brittle ones (indexes) | Future-proof scripts |
| 6 | Modal Detection | Identify when dialogs or overlays pop up unexpectedly | No surprises |
| 7 | Screen Summarization | Generate TL;DR versions for when AI context is precious | Efficient communication |
| 8 | Screen Diffing | Spot what changed between two screen states - new elements, vanished buttons, altered text | Change tracking |

### Track B2: Scripting Engine

*Teaching the robot to follow a recipe. Now simplified to focus purely on execution.*

| Priority | Feature | What It Does | Joy Factor |
|----------|---------|--------------|------------|
| 1 | Script Format | YAML/JSON recipes with steps and recovery hints - metadata lives in Automation | Clean separation |
| 2 | Basic Actions | `tap`, `type`, `scroll`, `back`, `home` - the essential verbs | Robot vocabulary |
| 3 | App Actions | `launch_app`, `read`, `read_screen` - the power moves | Level up |
| 4 | Wait Conditions | `wait_for`, `wait_for_text`, `wait_for_gone` - patience is a virtue | Reliable timing |
| 5 | Variable Substitution | `${varname}` syntax pulls from Automation inputs | Dynamic scripts |
| 6 | Script Validation | Catch errors before they hurt anyone | Safety first |
| 7 | Execution Engine | Run scripts step by step with proper error handling and cancellation | The heart of it all |
| 8 | Control Flow | Conditionals, retry logic - when things get complicated | Smart scripts |
| 9 | Recovery Hints | Guide AI on what to do when things go sideways | Self-healing scripts |

**Milestone:** When Group B is complete, we can describe any screen to an AI and execute any automation script. The robot can now read and follow recipes!

---

## Group C: The Brains

**Requires:** Group B (The Interpreters)

**Unlocks:** AI-powered magic, organized automation management, performance, and security

*Now that we can see screens and run scripts, it's time to add intelligence, organization, speed, and safety. These tracks can be built in parallel - each makes the robot better in different ways.*

### Track C1: AI Integration

*Giving the robot a brain. Enhanced with complete tool contracts and recovery specifications.*

| Priority | Feature | What It Does | Joy Factor |
|----------|---------|--------------|------------|
| 1 | LLM Communication | Talk to Claude (or compatible APIs) with proper rate limiting and error handling | AI connection |
| 2 | Complete Tool Definitions | Expose all actions with typed signatures: `tap(selector)`, `scroll_to_find()`, `record_step()` | AI toolkit |
| 3 | Screen Representation Format | Consistent `ScreenRepresentation` object with elements, actionables, and summary | AI-parseable screens |
| 4 | Intent Parsing | Turn "open Uber and check price to LAX" into structured automation goals | Natural language magic |
| 5 | Progressive Script Building | Watch, act, observe, record - build scripts by actually using apps | Learning by doing |
| 6 | Script Generation Contract | AI calls `record_step()` with resilient selectors, `finalize_script()` when done | Clean API |
| 7 | AITM Recovery Contract | Structured recovery with triggers, hints, confidence levels, and escalation | Smart recovery |
| 8 | Context Management | Keep conversations focused without blowing token budgets | Efficient AI |
| 9 | Privacy Protection | Mask sensitive content, require consent - robot with ethics | Trustworthy AI |

### Track C2: Automation Management

*Giving the robot a filing cabinet. Now the single source of truth for all automation data.*

| Priority | Feature | What It Does | Joy Factor |
|----------|---------|--------------|------------|
| 1 | Automation Model | UUID, name, description, embedded script, inputs, outputs, timestamps, state | Complete package |
| 2 | Automation States | Draft, ready, running, error - know what state everything is in | Clear status |
| 3 | Unified Input Definition | Consistent input fields with name, label, type, validation across the system | No confusion |
| 4 | Output Definition | Capture values from scripts with labels and format hints | Useful results |
| 5 | Storage (CRUD) | Create, read, update, delete - database fundamentals | Persistence |
| 6 | Execution Lifecycle | Start, track progress, complete or fail gracefully | Reliable execution |
| 7 | State Transitions | Auto-revalidate scripts on edit, update states appropriately | Smart state machine |
| 8 | Run History | Track every execution with inputs, outputs, success/failure, timestamps | Accountability |

### Track C3: Performance Optimization

*Making the robot fast and efficient. A cross-cutting concern that touches everything.*

| Priority | Feature | What It Does | Joy Factor |
|----------|---------|--------------|------------|
| 1 | Background Threading | Expensive operations on background threads, never block UI | Smooth UX |
| 2 | Node Lifecycle Management | Properly recycle AccessibilityNodeInfo objects, prevent memory leaks | No crashes |
| 3 | Screen Representation Caching | Cache screen reps, invalidate on content change, under 10ms for cache hits | Instant reads |
| 4 | Event Debouncing | Coalesce rapid accessibility events, protect against event floods | Stay calm |
| 5 | Selector Indexing | Build indexes for ID, text, type - resolve simple selectors in under 5ms | Lightning lookups |
| 6 | Latency Targets | P95 targets: cached reads <10ms, fresh reads <100ms, actions <50ms | Measurable speed |
| 7 | Incremental Screen Diffing | Update only affected subtrees, diff computation under 50ms | Efficient updates |
| 8 | AI Response Caching | Cache intent parsing and recovery actions for similar contexts | Fewer API calls |
| 9 | Battery Impact Mitigation | Idle shutdown, event-driven waits, polling avoidance, usage disclosure | Happy batteries |

### Track C4: Security & Privacy Foundation

*Keeping users safe. Because with great power comes great responsibility.*

| Priority | Feature | What It Does | Joy Factor |
|----------|---------|--------------|------------|
| 1 | Data Classification | Detect sensitive fields by input type, resource ID patterns, content descriptions | Know what's sensitive |
| 2 | PII Masking | Auto-mask sensitive values as `[MASKED_PASSWORD]` before AI transmission | Privacy preserved |
| 3 | Emergency Stop | Persistent notification stop button, back button pauses execution | Always in control |
| 4 | AI Data Transmission Disclosure | Clear consent flow, transmission indicators, provider disclosure | Informed users |
| 5 | Overlay Attack Mitigation | Detect obscured windows, refuse actions, alert user | Security conscious |
| 6 | Sensitive App Detection | Treat banking/finance/healthcare apps as high-sensitivity by default | Extra caution |

**Milestone:** When Group C is complete, users can describe automations in plain English and the AI will build them by actually navigating apps. Everything is organized, fast, and secure!

---

## Group D: The Pretty Face

**Requires:** Groups A, B, and C (everything!)

**Unlocks:** Humans can actually use this thing

*All the power in the world means nothing if nobody can access it. Time to make Robotomator beautiful, intuitive, and delightful.*

### Track D1: User Interface

*The robot learns to smile.*

| Priority | Feature | What It Does | Joy Factor | Status |
|----------|---------|--------------|------------|--------|
| 0 | Robo-Tomator Icon | Create a delightful robotic tomato mascot icon and set it as the app icon | Brand identity | ✅ **COMPLETED** |
| 1 | Permission Flow | Friendly onboarding that guides users through accessibility setup | Warm welcome | ✅ **COMPLETED** |
| 2 | Main Navigation | Bottom nav: Automations, Create, Settings | Easy to find things | |
| 3 | Empty State | Charming "no automations yet" screen with a nudge to create one | Inviting start | |
| 4 | Automations List | Show all automations with name, description, state, last run | Your collection | |
| 5 | List Actions | Tap to run, long-press for context menu (edit, shortcut, export, delete) | Quick actions | |
| 6 | Create Screen | Voice input, text input, or manual script - pick your adventure | Multiple paths | |
| 7 | AI Processing Feedback | Show what the AI is doing as it builds your automation | Transparency | |
| 8 | Clarification UI | When AI needs more info, ask nicely | Helpful dialogue | |
| 9 | Automation Detail | View and edit automation properties, inputs, and outputs | Full control | |
| 10 | Run Button | The big satisfying button that makes magic happen | The moment of joy | |
| 11 | Execution Progress | Current step, overall progress, optional screen preview | Know what's happening | |
| 12 | AITM Notification | Let users know when AI recovery kicks in | No mystery | |
| 13 | User Escalation | When AI is stumped, ask the human for help | Teamwork | |
| 14 | Completion Display | Success message with outputs and "run again" option | Celebration | |
| 15 | Failure Display | Clear error explanation with retry/edit options | Path forward | |
| 16 | Results Display | Show captured outputs with copy and share buttons | Useful data | |
| 17 | Settings Screen | API config, timeouts, privacy controls | Customization | |
| 18 | Search and Filter | Find automations when you have dozens | Scale ready | |
| 19 | Voice Recording | Animated waveforms and real-time transcription | Feels magical | |
| 20 | Loading States | Spinners and skeletons everywhere they're needed | Polish | |
| 21 | Success/Error Feedback | Snackbars and animations for every action | Responsive | |
| 22 | TalkBack Support | Full accessibility so our accessibility app is accessible (meta!) | Inclusive | |

### Track D2: Security & Privacy (Advanced)

*User-facing security controls.*

| Priority | Feature | What It Does | Joy Factor |
|----------|---------|--------------|------------|
| 1 | Per-App Consent | Allowlist/blocklist apps, prompt on first access to new apps | User in control |
| 2 | Sensitive Action Confirmation | Pause and confirm before payment/deletion actions | Safety net |
| 3 | Audit Logging | Track all actions, view by automation/date, filter and export | Full transparency |
| 4 | Audit Log UI | Display logs grouped by automation and date, allow filtering | Easy review |
| 5 | Export Sanitization | Scan exports for PII, warn user, offer to sanitize | Safe sharing |
| 6 | Confirmation Bypass | "Don't ask again for this automation" option | Convenience |
| 7 | Privacy Settings UI | View/revoke AI consent, configure masking sensitivity | Fine control |

### Track D3: Advanced Automation Features

*The finishing touches.*

| Priority | Feature | What It Does | Joy Factor |
|----------|---------|--------------|------------|
| 1 | Input Forms UI | Display forms with all field types, validation, defaults, previous values | Smart forms |
| 2 | Homescreen Shortcuts | One-tap access to your favorite automations | Instant access |
| 3 | Export/Import | Share automations as JSON files | Community ready |
| 4 | Voice Prompt Creation | Speak your automation into existence | Voice magic |
| 5 | History UI | View past runs with details, inputs, outputs, errors | Time travel |

**Milestone:** When Group D is complete, Robotomator is ready for the world. Users can create, manage, and run automations through a polished, secure Android app!

---

## Victory Laps

**The stretch goals. The nice-to-haves. The "if we're feeling fancy" features.**

### Supercharged Screen Reading
- Screenshot capture for visual AI analysis
- OCR fallback for apps with poor accessibility
- Multiple window handling (split-screen, picture-in-picture)

### Smart Scheduling
- Time-based automation triggers
- Event-based triggers (battery level, wifi connection)
- Chained automations (run B after A completes)

### Power User Paradise
- Visual script builder (drag-and-drop steps)
- Script debugging mode with step-through
- Performance analytics (which automations run fastest?)
- Custom PII detection patterns

### Social Robot
- Cloud backup/sync for automations
- Community automation library
- Automation templates for common tasks

### Next-Level AI
- Multi-turn conversations during execution
- Learning from corrections (remember what worked)
- Proactive suggestions ("I noticed you do this often...")
- Automatic recovery hint generation from successful recoveries

### Enterprise Features
- Multi-device orchestration
- Role-based access control
- Centralized automation deployment

---

## The Journey Ahead

```
                    ╔═══════════════════╗
                    ║     Group A       ║
                    ║   The Foundation  ║
                    ║ (Accessibility)   ║
                    ╚═════════╤═════════╝
                              │
              ┌───────────────┴───────────────┐
              │                               │
     ╔════════╧════════╗             ╔════════╧════════╗
     ║    Track B1     ║             ║    Track B2     ║
     ║     Screen      ║             ║    Scripting    ║
     ║ Representation  ║             ║     Engine      ║
     ╚════════╤════════╝             ╚════════╤════════╝
              │                               │
              └───────────────┬───────────────┘
                              │
    ┌────────────┬────────────┼────────────┬────────────┐
    │            │            │            │            │
╔═══╧═══╗  ╔═════╧═════╗  ╔═══╧═══╗  ╔═════╧═════╗
║  C1   ║  ║    C2     ║  ║  C3   ║  ║    C4     ║
║  AI   ║  ║ Automation║  ║ Perf  ║  ║ Security  ║
║ Integ ║  ║   Mgmt    ║  ║ Optim ║  ║ & Privacy ║
╚═══╤═══╝  ╚═════╤═════╝  ╚═══╤═══╝  ╚═════╤═════╝
    │            │            │            │
    └────────────┴────────────┼────────────┘
                              │
              ┌───────────────┼───────────────┐
              │               │               │
     ╔════════╧════════╗ ╔════╧════╗ ╔════════╧════════╗
     ║    Track D1     ║ ║Track D2 ║ ║    Track D3     ║
     ║       UI        ║ ║Security ║ ║    Advanced     ║
     ║                 ║ ║   UI    ║ ║   Automation    ║
     ╚════════╤════════╝ ╚════╤════╝ ╚════════╤════════╝
              │               │               │
              └───────────────┴───────────────┘
                              │
                              ▼
                   ╔═════════════════════╗
                   ║ Robotomator Complete║
                   ║         !           ║
                   ╚═════════════════════╝
```

---

## Quick Reference: Spec to Track Mapping

| Spec | Primary Track | Also Touches |
|------|---------------|--------------|
| Accessibility Service | Group A | C3 (threading, caching) |
| Screen Representation | B1 | C3 (caching, indexing), C4 (PII masking) |
| Scripting Engine | B2 | C1 (recovery hints), C2 (embedded in automation) |
| AI Integration | C1 | C3 (response caching), C4 (data transmission) |
| Automation Management | C2 | D3 (advanced features) |
| Performance Optimization | C3 | Touches all groups |
| Security & Privacy | C4 + D2 | Touches all groups |
| User Interface | D1 | D2, D3 |

---

*Built with love for everyone who has ever wished their phone could just do that thing automatically.*
