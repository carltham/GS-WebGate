# Phase 2: RED Specification - GS-WebGate Worker Loop

## Goal

Implement the private worker loop that polls for work over REST, executes a search, and publishes the result.

## Acceptance Criteria

1. The worker can poll for pending requests over REST.
2. The worker can execute the search step using an injected search handler.
3. The worker publishes a response with the original request ID.
4. The worker handles execution errors without crashing the loop.

## Test Cases

- poll_for_pending_request_over_rest
- execute_search_and_publish_result
- publish_error_result_when_search_fails
- continue_loop_after_failed_execution

## Out of Scope

- full production deployment
- external provider-specific configuration details
