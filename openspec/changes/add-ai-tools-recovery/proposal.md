# Change: Add Missing AI Tools and Define Recovery Contract

## Why

The AI Integration spec is missing tools that exist in the Scripting Engine (long_press, clear, read_screen format). The recovery hints mentioned in scripting were never defined. The contract between AI-generated scripts and the scripting engine is unclear. Without these, AI cannot generate complete scripts or perform effective recovery.

## What Changes

- **MODIFIED** `ai-integration` spec:
  - Add missing tools: long_press, clear, scroll_to_find
  - Define read_screen return format explicitly
  - Define how AI returns generated scripts
  - Define screen representation format for AI consumption
  - Add selector guidance for resilient script generation
  - Define AITM recovery contract with recovery hints integration

## Impact

- Affected specs: `ai-integration`
- Affected code: AI tool definitions, script generation, AITM recovery
- Enables complete script generation and effective recovery
