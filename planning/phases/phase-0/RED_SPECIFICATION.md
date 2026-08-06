# Phase 0: RED Specification - Contracts and Test Harness

## Goal

Define the first contract for REST-based work submission and result retrieval with minimal persistence.

## Acceptance Criteria

1. A work item can be submitted and assigned a message ID.
2. A pending work item can be retrieved by a worker through a simple polling endpoint that returns the next pending item.
3. A result can be stored and correlated to the original work item.
4. The work item lifecycle is explicit: submitted → pending → claimed → completed → consumed.
5. The behavior is testable without depending on the external search provider.

## Test Cases

- submit_work_returns_message_id
- fetch_pending_work_returns_next_pending_item
- submit_result_is_linked_to_message_id
- fetch_result_returns_correlated_result

## Out of Scope

- real external search execution
- network transport details
- production observability beyond basic logging
