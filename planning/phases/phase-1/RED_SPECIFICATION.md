# Phase 1: RED Specification - GS-relay Core Behavior

## Goal

Implement the persistence service so it can store pending work and completed results for REST-based access.

## Acceptance Criteria

1. Pending work is stored until claimed.
2. Claiming the next pending work item removes it from the pending pool.
3. Results are stored and fetched by message ID.
4. The system handles missing work and missing results gracefully.

## Test Cases

- store_pending_work
- claim_oldest_pending_work
- store_result_for_message_id
- return_empty_result_for_missing_result

## Out of Scope

- worker execution logic
- external provider integration
