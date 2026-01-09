# Change: Consolidate Automation and Script Models

## Why

The current specs have overlapping and contradictory definitions for Automation (in automation-management) and Script (in scripting-engine). Both have name, description, inputs, and storage. The Scripting Engine even has its own "Script Storage" requirement that duplicates Automation Management storage. This creates confusion about which is the source of truth and how they relate.

## What Changes

- **MODIFIED** `automation-management` spec:
  - Clarify that Automation is the user-facing entity
  - Script becomes an embedded property, not a separate stored entity
  - Inputs definition consolidated with unified field set
  - Remove ambiguity about what defines draft vs ready state

- **MODIFIED** `scripting-engine` spec:
  - Remove Script Storage requirement (delegated to Automation Management)
  - Script becomes a pure execution format, not a stored entity
  - Inputs in script reference Automation's input definitions
  - Add explicit outputs definition to script format
  - Clarify script format is the "compiled" form of an automation

## Impact

- Affected specs: `automation-management`, `scripting-engine`
- Affected code: Storage layer, automation CRUD, script execution
- **BREAKING**: Changes to data model and storage approach
