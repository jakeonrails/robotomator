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

IMPLEMENTATION_PROMPT="You are the Implementation Agent. Your job is to:

1. Read the ROADMAP.md file and identify the next unclaimed item (the first item that doesn't have a status like 'in progress' or 'completed')
2. Mark that item as 'in progress' by updating the ROADMAP.md file
3. Implement the feature completely according to the specification
4. Make sure your implementation follows Android best practices and the project structure

IMPORTANT: Only work on ONE item. Once you complete it, your job is done. Do not commit or push anything - that's for the next agent.

When you're done implementing, respond with 'IMPLEMENTATION COMPLETE' so the next agent knows to proceed."

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

REVIEW_PROMPT="You are the Review Agent. Your job is to:

1. Review all the code changes made by the previous agent
2. Check for:
   - Code quality issues
   - Potential bugs
   - Missing error handling
   - Performance issues
   - Security vulnerabilities
3. Fix any issues you find
4. Write comprehensive tests for the new functionality
5. Run the tests to make sure everything works

IMPORTANT: Do not commit or push anything - that's for the next agent. Focus only on review, fixes, and testing.

When you're done, respond with 'REVIEW COMPLETE' so the final agent knows to proceed."

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
2. Update the ROADMAP.md to mark the completed item as 'completed' or '✓'
3. Update any relevant documentation (README.md, docs/, etc.) if needed
4. Create a descriptive commit message that explains:
   - What feature was implemented
   - What was fixed/improved in review
   - What tests were added
5. Commit all changes
6. Push to GitHub

IMPORTANT: This is the final step. Make sure everything is clean and ready to ship.

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
