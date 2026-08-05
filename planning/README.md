# GS-WebGate Planning

**Status:** Reset for the current architecture
**Purpose:** Implement the GS-WebGate private-search gateway and its queue-based coordination layer.

## Current Product Direction

GS-WebGate is a two-part system:
- GS-WebGate runs on a private machine and performs outbound search work.
- GS-mq stores requests, tracks correlation IDs, and returns results asynchronously.

The planning here is intentionally smaller and more implementation-focused than the old Swing-era plan.

## Working Principles

- Start from the contract, not from the implementation details.
- Write tests first for observable behavior.
- Keep the queue boundary simple and explicit.
- Prefer real implementations over mocks at the system boundaries.

## Delivery Phases

### Phase 0 — Contracts and test harness
Focus on the shared request/response contract and the first failing tests.

Deliverables:
- request and response message shapes
- test harness for queue interactions
- initial acceptance tests for enqueue/dequeue flow

### Phase 1 — GS-mq core behavior
Implement the queue service so it can accept work, hold it until a worker claims it, and return results by request ID.

Deliverables:
- request storage
- response storage
- correlation by request ID
- basic polling behavior

### Phase 2 — GS-WebGate worker loop
Implement the private worker so it can poll for work, execute the search step, and publish the result.

Deliverables:
- polling loop
- worker execution flow
- result publishing
- failure handling and retries

### Phase 3 — Integration and resilience
Wire the modules together and harden the path for real use.

Deliverables:
- end-to-end request/result flow
- logging and error handling
- configuration and operational defaults
- documentation updates

## Suggested Working Order

1. Read the architecture overview and module contracts.
2. Start with the Phase 0 test specification.
3. Implement the smallest end-to-end path first.
4. Expand to resilience and operational behavior only after the happy path is stable.

## Key References

- [../architecture/README.md](../architecture/README.md)
- [../DEVELOPER_GUIDE.md](../DEVELOPER_GUIDE.md)
- [TDD_TOP_DOWN_GUIDE.md](TDD_TOP_DOWN_GUIDE.md)
