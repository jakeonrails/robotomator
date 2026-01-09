#!/bin/bash

# orchestrator.sh - Autonomous development workflow with Claude Code
# This script orchestrates three Claude Code instances:
# 1. Implementation agent: Claims and implements next roadmap item
# 2. Review agent: Reviews code, fixes issues, writes tests
# 3. Finalization agent: Commits, updates docs/roadmap, pushes to GitHub

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Log functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if claude command is available
if ! command -v claude &> /dev/null; then
    log_error "Claude Code CLI not found. Please install it first."
    exit 1
fi

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

log_info "Starting Robotomator autonomous development workflow..."
log_info "Working directory: $SCRIPT_DIR"

# Phase 1: Implementation Agent
log_info "=== PHASE 1: IMPLEMENTATION AGENT ==="
log_info "This agent will claim the next unclaimed roadmap item and implement it."

IMPLEMENTATION_PROMPT="You are the Implementation Agent. Your job is to implement EXACTLY ONE roadmap item.

STEP-BY-STEP PROCESS:

1. Read ROADMAP.md and find THE FIRST unclaimed item (no 'COMPLETED' or 'IN PROGRESS' status)
2. Mark ONLY that ONE item as 'IN PROGRESS' in ROADMAP.md
3. Implement that ONE feature completely
4. Review your code against AGENT_REVIEW_CHECKLIST.md:
   - Memory Management: Recycle all AccessibilityNodeInfo in try-finally
   - Thread Safety: Use @Volatile for shared state
   - Error Handling: Catch specific exceptions, not generic Exception
   - Performance: No main thread blocking, add depth limits to recursion
   - Code Quality: Follow Kotlin idioms, clear naming
5. Mark ONLY that ONE item as 'COMPLETED' in ROADMAP.md
6. STOP IMMEDIATELY - Do not look at the next item

ABSOLUTE STOPPING RULES:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
❌ DO NOT implement multiple items even if they seem related
❌ DO NOT continue to the next item even if the first was easy
❌ DO NOT bundle features together for efficiency
❌ DO NOT look ahead at what's next
✅ IMPLEMENT ONE ITEM → MARK COMPLETED → STOP
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

ONE ITEM MEANS:
- One row in the roadmap table
- One Priority number
- One feature name

Example: If you implement 'Priority 4: Global Actions', you are DONE.
Do NOT continue to Priority 5, even if you have time/energy.

CODE QUALITY REQUIREMENTS:
Before marking complete, verify against AGENT_REVIEW_CHECKLIST.md:
- All AccessibilityNodeInfo objects are recycled with try-finally
- Shared state uses @Volatile or AtomicReference
- Specific exception types, never generic Exception
- No main thread blocking (use Dispatchers.IO for IPC)
- Recursive functions have depth limits

Do not commit or push - that's for the finalization agent.

OUTPUT FORMAT:
When done, output 'IMPLEMENTATION COMPLETE' followed by a brief summary listing:
- Which ONE item you completed (Priority N: Feature Name)
- Key files created/modified
- Any critical implementation notes

Now implement THE ONE ITEM and STOP."

log_info "Launching implementation agent..."
if echo "$IMPLEMENTATION_PROMPT" | claude --dangerously-skip-permissions --print --model sonnet; then
    log_success "Phase 1 complete: Implementation done"
else
    log_error "Phase 1 failed: Implementation agent encountered an error"
    exit 1
fi

# Phase 2: Review Agent
log_info "=== PHASE 2: REVIEW AGENT ==="
log_info "This agent will review the code, fix issues, and write tests."

REVIEW_PROMPT="You are the Review Agent. Your job is to systematically review code using AGENT_REVIEW_CHECKLIST.md.

SYSTEMATIC REVIEW PROCESS:

1. Read AGENT_REVIEW_CHECKLIST.md to understand all quality criteria

2. Review code changes against EACH checklist category:

   ✓ MEMORY MANAGEMENT (Section 1):
     - Every AccessibilityNodeInfo has try-finally with recycle()
     - No nodes stored in fields/collections
     - Clear ownership documented for returned nodes
     - Coroutines canceled when lifecycle ends

   ✓ THREAD SAFETY (Section 2):
     - Shared mutable state uses @Volatile or AtomicReference
     - No check-then-act race conditions
     - Compound updates are atomic
     - Service singleton uses proper synchronization

   ✓ ERROR HANDLING (Section 3):
     - Specific exception types (IllegalStateException, SecurityException)
     - Never catch generic Exception or Throwable
     - All exceptions logged with context
     - Sealed classes for result types

   ✓ PERFORMANCE (Section 4):
     - No blocking on main thread
     - Dispatchers.IO for disk/network
     - Recursive functions have depth limits
     - Accessibility events filtered early

   ✓ SECURITY (Section 5):
     - Input validation on all external data
     - No sensitive data in logs
     - Length limits on string inputs

   ✓ CODE QUALITY (Section 6):
     - Methods < 30 lines
     - Clear naming
     - No magic numbers
     - Kotlin idioms used properly

   ✓ TESTING (Section 7):
     - Happy path tested
     - Error paths tested
     - Edge cases covered
     - Node recycling verified

3. Fix ALL issues you find - do not leave TODOs

4. Write comprehensive tests if missing

5. Run tests and verify they pass

CRITICAL FIXES REQUIRED:
- Any AccessibilityNodeInfo without try-finally → ADD IT
- Any generic Exception catch → MAKE IT SPECIFIC
- Any shared state without @Volatile → ADD IT
- Any main thread blocking → MOVE TO Dispatchers.IO
- Any recursive function without depth limit → ADD LIMIT

Do not commit or push - that's for the finalization agent.

OUTPUT FORMAT:
When done, output 'REVIEW COMPLETE' followed by summary:
- Number of issues found and fixed (by category)
- Test coverage added
- Build/test status

Now review systematically using the checklist."

log_info "Launching review agent..."
if echo "$REVIEW_PROMPT" | claude --dangerously-skip-permissions --print --model sonnet; then
    log_success "Phase 2 complete: Review and testing done"
else
    log_error "Phase 2 failed: Review agent encountered an error"
    exit 1
fi

# Phase 3: Finalization Agent
log_info "=== PHASE 3: FINALIZATION AGENT ==="
log_info "This agent will commit changes, update documentation, and push to GitHub."

FINALIZATION_PROMPT="You are the Finalization Agent. Your job is to:

1. Review all the changes that were made
2. Verify the ROADMAP.md has the completed item(s) marked properly
3. Update any relevant documentation (README.md, docs/, etc.) if needed
4. Create a descriptive commit message that explains:
   - Which specific roadmap item(s) were implemented (e.g., 'Group A, Priority 4: Global Actions')
   - What was implemented
   - What was fixed/improved in review
   - What tests were added
5. Commit all changes with the Co-Authored-By line
6. Push to GitHub

IMPORTANT:
- Use a clear commit message format like: 'feat(group-a): implement [Feature Name] (Priority N)'
- This is the final step - make sure everything is clean and ready to ship
- Verify the push succeeds before declaring completion

When you're done, respond with 'FINALIZATION COMPLETE'."

log_info "Launching finalization agent..."
if echo "$FINALIZATION_PROMPT" | claude --dangerously-skip-permissions --print --model sonnet; then
    log_success "Phase 3 complete: Changes committed and pushed"
else
    log_error "Phase 3 failed: Finalization agent encountered an error"
    exit 1
fi

log_success "========================================="
log_success "Autonomous development workflow complete!"
log_success "========================================="
