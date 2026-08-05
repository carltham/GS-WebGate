# Phase 1: RED Specification - GS-relay Core Behavior

## Goal

Implement the persistence service so it can store pending work and completed responses for REST-based access.

## Acceptance Criteria

1. Pending requests are stored until claimed.
2. Claiming a request removes it from the pending pool.
3. Responses are stored and fetched by request ID.
4. The system handles missing requests and missing responses gracefully.

## Test Cases

- store_pending_request
- claim_oldest_pending_request
- store_response_for_request_id
- return_empty_result_for_missing_response

## Out of Scope

- worker execution logic
- external provider integration
