## 1. Model Clarification

- [ ] 1.1 Define clear relationship: Automation contains Script
- [ ] 1.2 Unify inputs definition (merge prompt/label, add all fields)
- [ ] 1.3 Define when automation is draft vs ready (script validation)
- [ ] 1.4 Add outputs to Automation model (from script)

## 2. Scripting Engine Updates

- [ ] 2.1 Remove Script Storage requirement
- [ ] 2.2 Clarify script is execution format only
- [ ] 2.3 Update script format to reference external inputs
- [ ] 2.4 Add recovery hints specification to script format

## 3. Automation Management Updates

- [ ] 3.1 Clarify Automation is the stored/managed entity
- [ ] 3.2 Add unified input field definition
- [ ] 3.3 Add state transition rules (draft ↔ ready)
- [ ] 3.4 Define script validation as readiness gate

## 4. Validation

- [ ] 4.1 Validate spec with `openspec validate consolidate-automation-script-model --strict`
