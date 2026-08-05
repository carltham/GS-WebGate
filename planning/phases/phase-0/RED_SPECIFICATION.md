# Phase 0: RED Specification - Contracts and Test Harness

## Goal

Define the first contract for REST-based request and response exchange with minimal persistence.

## Acceptance Criteria

1. A request can be submitted and assigned a request ID.
2. A pending request can be retrieved by a worker through a simple polling endpoint.
3. A response can be stored and correlated to the original request.
4. The behavior is testable without depending on the external search provider.

## Test Cases

- submit_request_returns_request_id
- fetch_pending_request_returns_next_pending_item
- submit_response_is_linked_to_request_id
- fetch_response_returns_correlated_result

## Out of Scope

- real external search execution
- network transport details
- production observability beyond basic logging
