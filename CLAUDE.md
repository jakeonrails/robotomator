# Robotomator

AI-powered Android automation platform. Users describe automations in natural language, and the AI generates executable scripts using Android Accessibility APIs.

See `openspec/project.md` for full project context and conventions.

## Code Quality Requirements

**After completing any coding task, review your changes using `AGENT_REVIEW_CHECKLIST.md`.**

Critical requirements for this codebase:
1. **Always recycle AccessibilityNodeInfo** objects using try-finally
2. **Use atomic state updates** for the singleton service pattern
3. **Never block the main thread** - use `Dispatchers.IO` for IPC
4. **Add depth limits** to recursive tree traversal
5. **Catch specific exceptions**, not generic `Exception`

See `TECH_DEBT_REPORT.md` for known issues and remediation priorities.

<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->