# Change: Add Security and Privacy Specification

## Why

The current specs lack comprehensive security and privacy requirements. Screen content is sent to external AI services, the app has extensive device control via accessibility service, and exported automations may contain sensitive data. Without explicit security requirements, implementation could introduce serious privacy vulnerabilities.

## What Changes

- Add new `security-and-privacy` capability spec covering:
  - Data privacy for screen representations sent to AI
  - PII detection and masking strategies
  - Per-app consent model (allow/block specific apps)
  - Audit logging of all automation actions
  - Sensitive action confirmation requirements
  - Export sanitization to prevent PII leakage
  - Overlay attack mitigation
  - Emergency stop mechanism
  - On-device vs cloud AI disclosure

## Impact

- Affected specs: New spec `security-and-privacy`
- Affected code: AI integration module, screen capture, automation execution, export/import
- Cross-cutting concern affecting most components
