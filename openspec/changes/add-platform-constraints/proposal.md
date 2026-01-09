# Change: Document Platform Constraints and Limitations

## Why

The specs don't document Android platform limitations that will affect implementation. Apps can block accessibility services, secure views are invisible, OEM variations exist, WebViews have poor accessibility, and minimum SDK requirements aren't specified. Without documenting these, developers may attempt impossible features or miss critical compatibility concerns.

## What Changes

- **MODIFIED** `accessibility-service` spec:
  - Add Platform Limitations requirement documenting what cannot be automated
  - Add Minimum SDK requirement (API 24 / Android 7.0)
  - Add OEM Compatibility considerations
  - Add App Launch clarification (requires main app context, not service)
  - Add Gesture support for advanced interactions

- **MODIFIED** `project.md`:
  - Update Tech Stack with specific minimum SDK
  - Document known limitations in Important Constraints

## Impact

- Affected specs: `accessibility-service`
- Affected files: `project.md`
- Documentation only - no functional changes
