# Phase 0: RED Specification - Shared Contracts

**Goal:** Define the first client-visible behavior for the GS-WebGate queue flow.

## Acceptance Tests

1. A client can submit a request and receive a request ID.
2. The queue can return a pending request to a worker.
3. A worker can publish a response that is correlated to the original request.
4. The client can retrieve the completed response by request ID.

## Expected Behaviors

- Enqueue request stores the payload and returns a request ID.
- Dequeue request returns the oldest pending request.
- Enqueue response stores a result keyed by request ID.
- Dequeue response returns the associated result once available.

## Implementation Note

These tests should be written before the queue or worker implementation is added.
