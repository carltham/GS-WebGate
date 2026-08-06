# GS-WebGate Planning

**Status:** Phase 0 COMPLETE (2026-08-06) | Phase 1 IN PROGRESS
**Purpose:** Implement the GS-WebGate private-search gateway and its REST-based coordination layer with the simplest possible persistence.

## Current Product Direction

GS-WebGate is a two-part system:
- GS-WebGate runs on a private machine and performs outbound search work.
- GS-relay accepts work submissions over REST, stores them with minimal persistence, and returns results asynchronously.

The planning here is intentionally smaller and more implementation-focused than the old Swing-era plan.

## Working Principles

- Start from the contract, not from the implementation details.
- Write tests first for observable behavior.
- Keep the REST boundary simple and explicit.
- Prefer real implementations over mocks at the system boundaries.

## Delivery Phases

### Phase 0 — Contracts and test harness ✅ COMPLETE (2026-08-06)
Focus on the shared submission/result contract and the first failing tests.

Deliverables:
- ✅ submission and result message shapes (WorkItemRequest, WorkItemResponse, SearchResult)
- ✅ test harness for submission and retrieval (5 relay tests + 5 searcher tests)
- ✅ initial acceptance tests for submit/poll/retrieve flow (all 10 tests passing)
- ✅ a clear state lifecycle: submitted → pending → claimed → completed → consumed
- ✅ PolledWork contract for message ID correlation in poll responses
- ✅ Real HTTP integration between relay and searcher modules (not mocked)
- ✅ Message ID generation and tracking (UUID-based correlation)

### Phase 1 — GS-relay core behavior
Implement the persistence service so it can accept work, hold it until a worker claims it, and return results by message ID.

Deliverables:
- pending work storage with minimal persistence
- result storage
- correlation by message ID
- basic polling and retrieval behavior for the next pending work item

### Phase 2 — GS-WebGate worker loop
Implement the private worker so it can poll for work over REST, execute the search step, and publish the result.

Deliverables:
- polling loop over REST
- worker execution flow
- result publishing
- failure handling and retries

### Phase 3 — Integration and resilience
Wire the modules together and harden the path for real use.

Deliverables:
- end-to-end work/result flow
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
