# Phase 3: RED Specification - Integration and Resilience

## Goal

Wire GS-relay and GS-WebGate together over REST and harden the path for real operation.

## Acceptance Criteria

1. A full work item can travel from submission to result publication over REST.
2. Basic retries, timeouts, and logging are present.
3. The flow remains understandable and testable end to end.
4. Startup and failure modes are documented.

## Test Cases

- full_work_item_to_result_round_trip_over_rest
- retry_on_transient_failure
- timeout_handling_for_stalled_work
- startup_failure_is_logged_and_reported

## Out of Scope

- advanced cluster deployment
- high-throughput optimization
